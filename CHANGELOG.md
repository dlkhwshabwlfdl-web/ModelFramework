# Changelog

All notable changes to ModelFramework are documented here.

## [1.1.0] - 2026-11-07

### Added
- Smart Dual-Engine Rendering: ModelEngine and BetterModel are shown
  dynamically based on the client protocol (via ViaVersion), falling back
  to ModelEngine for clients below 1.21.4.
- Nexo item provider, alongside the existing Oraxen and ItemsAdder providers.
- `ResourceGenerator`: auto-generates resource files for Nexo, ItemsAdder,
  and Oraxen.

### Changed
- Animation lists are now cached sorted per `FrameworkObject` instead of
  being re-sorted on every interaction.

### Fixed
- Entity despawning on chunk unload: added a `ChunkListener` with a chunk
  index so placed models respawn correctly when their chunk reloads.

## [1.0.0] - 2026-08-08

- Initial public release: YAML-based interactive objects on BetterModel,
  with animations, triggers, item requirements, state machine, category
  GUI, instance persistence, and cooldowns.
