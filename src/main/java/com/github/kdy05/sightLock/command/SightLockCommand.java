package com.github.kdy05.sightLock.command;

import com.github.kdy05.sightLock.config.ConfigManager;
import com.github.kdy05.sightLock.core.PlayerToggleService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SightLockCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION_USE = "sightlock.use";
    private static final String PERMISSION_RELOAD = "sightlock.reload";
    private static final List<String> SUB_COMMANDS = Arrays.asList("help", "reload", "status");
    
    private final PlayerToggleService toggleService;
    private final ConfigManager configManager;
    
    public SightLockCommand(@NotNull PlayerToggleService toggleService,
                            @NotNull ConfigManager configManager) {
        this.toggleService = toggleService;
        this.configManager = configManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission(PERMISSION_USE)) {
            sender.sendMessage(configManager.getMessage("error.no-permission"));
            return false;
        }
        
        if (args.length == 0) {
            return handleToggleCommand(sender);
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help" -> handleHelpCommand(sender);
            case "reload" -> handleReloadCommand(sender);
            case "status" -> handleStatusCommand(sender);
            default -> sender.sendMessage(configManager.getMessage("error.unknown-command"));
        }
        
        return true;
    }
    
    private boolean handleToggleCommand(@NotNull CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("error.player-only"));
            return false;
        }
        
        toggleService.toggle(player.getUniqueId());
        
        boolean isEnabled = toggleService.isEnabled(player.getUniqueId());
        String messageKey = isEnabled ? "command.toggle-on" : "command.toggle-off";
        
        player.sendMessage(configManager.getMessage(messageKey));
        return true;
    }
    
    private void handleHelpCommand(@NotNull CommandSender sender) {
        sender.sendMessage(configManager.getMessage("command.help"));
        sender.sendMessage(configManager.getMessage("command.tool-spec"));
    }
    
    private void handleReloadCommand(@NotNull CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_RELOAD)) {
            sender.sendMessage(configManager.getMessage("error.no-permission"));
            return;
        }
        try {
            configManager.reloadConfigurations();
            sender.sendMessage(configManager.getMessage("command.reloaded"));
        } catch (Exception e) {
            sender.sendMessage(configManager.getMessage("command.reload-failed"));
        }
    }
    
    private void handleStatusCommand(@NotNull CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("error.player-only"));
            return;
        }

        boolean isEnabled = toggleService.isEnabled(player.getUniqueId());
        String status = isEnabled ? "<green>ENABLED" : "<yellow>DISABLED";

        player.sendMessage(Component.text("----------------", NamedTextColor.DARK_GRAY));
        player.sendMessage(MiniMessage.miniMessage().deserialize("Status: " + status));
        player.sendMessage(MiniMessage.miniMessage().deserialize("Tool Material: <gray>"
                + configManager.getTriggerItem().name()));
    }
    
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String alias, @NotNull String[] args) {
        
        if (!sender.hasPermission(PERMISSION_USE)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return filterCompletions(args[0]);
        }
        
        return new ArrayList<>();
    }
    
    @NotNull
    private List<String> filterCompletions(@NotNull String input) {
        return SightLockCommand.SUB_COMMANDS.stream()
                .filter(completion -> completion.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }

}