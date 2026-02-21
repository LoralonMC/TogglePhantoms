# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] - 2026-02-21

### Changed

- **BREAKING:** Rewrite config.yml message format from flat keys to nested text/display structure
- **BREAKING:** Move placeholder config keys from `messages.placeholder-enabled` to top-level `placeholder-enabled`/`placeholder-disabled`
- Apply unified color palette with [ᴘʜᴀɴᴛᴏᴍꜱ] prefix to all default messages
- Add proper section separators and documentation to config.yml
- Move permission and player-only checks to Brigadier `.requires()` instead of manual executor checks
- Prepare SQLite and MySQL statements once at init and reuse throughout
- Replace ReentrantLock with synchronized in storage classes
- Make YamlStorage thread-safe with ConcurrentHashMap.newKeySet() and async file writes
- Add connection validation to SQLiteStorage
- Remove deprecated `autoReconnect=true` from MySQL JDBC URL
- Only recreate storage on reload if storage type actually changed
- Preserve stack traces in storage error logging
- Move PhantomsPlaceholderExpansion to placeholder package

### Removed

- Remove no-permission and player-only messages (handled natively by Brigadier)

## [1.1.0] - 2026-02-21

### Added

- MySQL storage backend
- YAML storage backend
- PlaceholderAPI integration with `%togglephantoms_enabled%` and `%togglephantoms_status%` placeholders
- Admin commands to manage other players (`on`, `off`, `toggle`, `status`)
- Admin notification messages when changing another player's setting
- Configurable messages with MiniMessage format

### Changed

- Migrate from plugin.yml commands to Brigadier command registration

## [1.0.0] - 2026-02-21

### Added

- Initial release
- Per-player phantom toggle using Paper's PhantomPreSpawnEvent
- SQLite storage with async operations and in-memory caching
- `/togglephantoms` command to toggle phantom spawning
- `/togglephantoms status` command to check current status
- `/togglephantoms reload` command to reload configuration
- bStats metrics integration
