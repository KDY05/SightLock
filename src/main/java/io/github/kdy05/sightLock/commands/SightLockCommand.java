package io.github.kdy05.sightLock.commands;

import io.github.kdy05.sightLock.SightLock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SightLockCommand implements CommandExecutor, TabCompleter {

    private final SightLock plugin;

    public SightLockCommand(SightLock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission("sightlock.use")) {
            commandSender.sendMessage(plugin.getConfigManager().getMessage("error.no-permission"));
            return false;
        }

        if (strings.length == 0) {
            showHelp(commandSender);
            return false;
        }

        String subCommand = strings[0].toLowerCase();
        switch (subCommand) {
            case "help" -> showHelp(commandSender);
            case "reload" -> handleReload(commandSender);
        }

        return false;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getMessage("command.help"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("advancementpicker.reload")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("error.no-permission"));
            return;
        }

        plugin.getConfigManager().reloadConfigs();
        sender.sendMessage(SightLock.PREFIX +
                plugin.getConfigManager().getMessage("command.reloaded"));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> completions = new ArrayList<>();
        if (strings.length == 1) {
            completions.add("help");
            completions.add("reload");
        }
        return completions;
    }
}
