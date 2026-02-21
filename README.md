# TogglePhantoms

A lightweight Paper plugin that allows players to toggle phantom spawning for themselves.

## Features

- Per-player phantom toggle using Paper's `PhantomPreSpawnEvent` for accurate insomnia-based blocking
- Multiple storage backends (SQLite, YAML, MySQL)
- Fully configurable messages using MiniMessage format (any message can be disabled by leaving it empty)
- PlaceholderAPI support
- Admin commands to manage other players
- Async database operations with in-memory caching

## Requirements

- Paper 1.21.10+
- Java 21+
- (Optional) PlaceholderAPI for placeholders

## Installation

1. Drop the JAR into your server's `plugins/` folder
2. Restart the server
3. Edit `plugins/TogglePhantoms/config.yml` to your liking

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/togglephantoms` | Toggle phantom spawning for yourself | `togglephantoms.use` |
| `/togglephantoms status` | Check your current phantom status | `togglephantoms.use` |
| `/togglephantoms admin <player> <on\|off\|toggle\|status>` | Manage phantom settings for other players | `togglephantoms.admin` |
| `/togglephantoms reload` | Reload the configuration | `togglephantoms.reload` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `togglephantoms.use` | Allows toggling phantoms for yourself | Everyone |
| `togglephantoms.admin` | Allows managing other players | OP |
| `togglephantoms.reload` | Allows reloading the config | OP |
| `togglephantoms.*` | All permissions | OP |

## Placeholders

Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) to be installed.

| Placeholder | Description |
|-------------|-------------|
| `%togglephantoms_enabled%` | Returns `true` or `false` |
| `%togglephantoms_status%` | Returns configurable text (default: `Enabled` / `Disabled`) |

## Configuration

The plugin supports three storage backends (`sqlite`, `yaml`, `mysql`) and all player-facing messages are fully customizable using MiniMessage format. See `config.yml` for all options.

## License

[MIT](LICENSE)
