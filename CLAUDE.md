# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SightLock is a Spigot plugin that allows players to lock entities (mobs or players) to their line of sight for easy manipulation. The plugin provides distance-adjustable entity control using a configurable tool.

## Build Commands

- `./gradlew build` - Build the plugin JAR
- `./gradlew runServer` - Run a test server with the plugin (Paper 1.21)
- `./gradlew clean` - Clean build artifacts

## Architecture

The plugin follows a standard Spigot plugin structure:

- **SightLock.java** - Main plugin class that handles initialization and provides static access
- **SightLockTask.java** - Core functionality for positioning locked entities using Bukkit schedulers
- **SightLockToggle.java** - Static utility for managing per-player toggle states
- **SightLockCommand.java** - Command handler for `/sl` commands (toggle, help, reload)
- **SightLockListener.java** - Event handler for player interactions (right-click, key presses)
- **ConfigManager.java** - Handles config.yml and lang.yml file management

## Key Technical Details

- Uses Bukkit's scheduler system (BukkitRunnable) running every tick for smooth entity positioning
- Entity positioning calculated using eye location, direction vectors, and distance multiplication
- Player toggle states maintained in static HashSet for session persistence
- Configuration supports Material-based tool selection via config.yml
- Internationalization support through lang.yml message files

## Development Requirements

- Java 21+
- Spigot API 1.21.4+
- Target Minecraft version: 1.21

## Plugin Configuration

- **config.yml**: Contains tool material configuration (`tool-material`)
- **lang.yml**: Contains all user-facing messages
- Both files are reloadable via `/sl reload` command