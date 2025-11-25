# TogglePhantoms

A lightweight Paper plugin that allows players to toggle phantom spawning for themselves.

## Features

- Per-player phantom toggle
- Multiple storage backends (SQLite, YAML, MySQL)
- Fully configurable messages using MiniMessage format
- PlaceholderAPI support for menu plugins
- Admin commands to manage other players
- Async database operations with in-memory caching

## Requirements

- Paper 1.21+
- Java 21+
- (Optional) PlaceholderAPI for placeholders

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

## PlaceholderAPI

Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) to be installed.

| Placeholder | Description |
|-------------|-------------|
| `%togglephantoms_enabled%` | Returns `true` or `false` |
| `%togglephantoms_status%` | Returns configurable text (default: `Enabled` / `Disabled`) |

## Configuration

```yaml
# Storage type: yaml, sqlite, or mysql
storage:
  type: sqlite

  # MySQL settings (only used if type is mysql)
  mysql:
    host: localhost
    port: 3306
    database: minecraft
    username: root
    password: ""

# Messages (MiniMessage format)
# Leave a message empty ("") to disable it
messages:
  phantoms-enabled: "<green>Phantom spawning has been <green>enabled</green> for you."
  phantoms-disabled: "<green>Phantom spawning has been <red>disabled</red> for you."
  # ... see config.yml for all options
```

## Building

```bash
./gradlew build
```

The built jar will be in `build/libs/`.

## License

[MIT](LICENSE)
