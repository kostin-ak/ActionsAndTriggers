# Known Issues and Gap Analysis

Status: snapshot of `master` at commit `dfc3c93` (2026-09-04). This file records behaviour that is missing, misleading, or risky in the current implementation. It is a map for future work, not a promise that everything listed will be fixed.

Paths are relative to the repository root. Line numbers refer to the same commit and may drift after edits.

Severity legend: **S1** data loss / broken guarantee, **S2** advertised feature does not work, **S3** API or UX inconsistency, **S4** cosmetic or documentation.

---

## S1 — Data loss and broken guarantees

### 1. Items are lost when the server stops or the plugin is disabled
**Where:** `aat-core/.../core/gui/GuiListener.java:87-159`, `ActionsTriggers.java:127-137`.
**What happens:** items are returned only from `InventoryCloseEvent`. `onDisable` cancels tasks and clears the GUI registry but never walks open `AATGuiHolder`s.
**Impact:** a restart or `/reload` with an open machine can destroy items left in input/output slots (Bukkit does not reliably fire `InventoryCloseEvent` for custom holders during shutdown). `README` and tutorials previously promised the opposite.
**Fix direction:** keep a registry of open holders (per player UUID) and return input/output contents in `onDisable` (and on `PlayerQuitEvent` as a safety net). Prefer a `Bukkit.getScheduler().runTask` based drain before shutdown if the server allows it.

### 2. `core:give_item` silently drops items when the inventory is full
**Where:** `core/defaults/actions/DefaultActionParsers.java:214-222`.
**What happens:** `player.getInventory().addItem(item)` returns leftovers that are ignored.
**Impact:** rewards, quest items and unlocks disappear without feedback.
**Fix direction:** handle the returned map, drop leftovers at the player's feet, and return `false` or send a message.

### 3. Machine ingredients are consumed before the result can be guaranteed
**Where:** `DefaultActionParsers.java:567-581` (cryo), `692-714` (magma), `819-834` (distill); output logic at `591-620`, `720-753`, `841-867`.
**What happens:** inputs are removed immediately; the result is written later. If the player disconnects and there is no bound block, the result is dropped only when `holder.getBoundBlock() != null`; otherwise it is silently lost.
**Impact:** players lose ingredients when they close the GUI or leave during the animation.
**Fix direction:** either refund inputs on `InventoryCloseEvent` mid-process, or make the process transactional (reserve inputs, commit on completion), and always persist a fallback drop location.

### 4. `on_close` actions never run
**Where:** parsed in `core/gui/YamlGuiLoader.java:76-78`, stored in `api/gui/GuiDefinition.java:58`, and never read (`getOnCloseActions` has no callers).
**Impact:** every cleanup or refund hook that users put into `on_close` is dead configuration.
**Fix direction:** call `def.getOnCloseActions()` from `GuiListener#onInventoryClose` (with the holder context) and document ordering relative to item return.

### 5. GUI combat flags are not implemented
**Where:** fields `allowCombat`/`closeOnDamage` at `api/gui/GuiDefinition.java:19-20`; loader never sets them (`YamlGuiLoader.java:57-70`); the only check is a hard-coded `astral_atlas` id in `core/combat/CombatListener.java:78-90`.
**Impact:** `allow_combat`/`close_on_damage` in `guis/*.yml` do nothing. Only one specific menu is protected.
**Fix direction:** read both keys in the loader, enforce `allowCombat` in `core:open_gui` (or `GuiRegistry#openGui`), and close any holder with `closeOnDamage` on damage.

### 6. `combat.duration_seconds` and `combat.close_gui_on_damage` are ignored
**Where:** `aat-core/src/main/resources/config.yml:5-10`; `CombatListener` is constructed with defaults (`ActionsTriggers.java:109`).
**Impact:** the documented tuning knobs have no effect; the tag lasts 15 seconds regardless.
**Fix direction:** read the values in `onEnable` and pass them into `CombatListener`; keep a sane default when the keys are absent.

---

## S2 — Advertised YAML features that do not work

### 7. `tab_container` cannot be declared in YAML
**Where:** `YamlGuiLoader.java:147-341` has no branch for it; the `default` branch builds a `ButtonWidget`.
**Impact:** a GUI with `type: tab_container` silently renders as a single stone button; clicks behave like a button. Tabs only exist via `TabContainerWidget` in Java.
**Fix direction:** either add a loader branch (tabs, icons, per-tab widget lists) or reject unknown types with a clear error.

### 8. `paged_list` has no item source
**Where:** `api/gui/widget/impl/PagedListWidget.java:23` (`items` list), loader only reads geometry (`YamlGuiLoader.java:313-323`).
**Impact:** a YAML paged list is always empty; navigation arrows never appear because `totalPages` is 1.
**Fix direction:** add an item source (registry reference, material list, loot table, or a provider id) or document the widget as Java-only.

### 9. `fluid_tank` cannot be driven from YAML
**Where:** `FluidTankWidget.java:43-47` reads a `Number` from session state; the loader exposes no way to write session values; `title` is rendered verbatim at line 67.
**Impact:** the tank always renders empty (0%) and `{level}`/`{max}` appear literally. `cycle_button` writes a `String`, so pairing the two does not help.
**Fix direction:** accept `session_key` with numeric coercion (parse strings), add a `core:set_session` action, and substitute `{level}`/`{max}`/`{percent}` in the title.

### 10. Multi-cell widgets inside a mask render incorrectly
**Where:** `MaskWidget.java:38-67` calls `widget.render(...)` per mask cell and keeps only one item (`subMatrix.get(widget.getX() + widget.getY() * 9)` or the first value).
**Impact:** a 3-cell fluid tank or a paged list placed in a mask shows the same first item in every cell, and its internal layout is ignored.
**Fix direction:** either forbid multi-cell widgets in masks with a validation warning, or map the child's matrix into the surrounding slots instead of collapsing it.

### 11. `target_block` is parsed and never used
**Where:** `YamlGuiLoader.java:67-70`.
**Impact:** users expect a GUI to be bound to a block type; nothing happens.
**Fix direction:** implement the binding or remove the key from the docs and the loader.

### 12. Unknown widget `type` silently becomes a button
**Where:** `YamlGuiLoader.java:325-340` (`case "button": default:`).
**Impact:** a typo (`progres_bar`, `tab_container`) produces a stone button instead of an error. This already hides issue 7.
**Fix direction:** log a warning with the file, widget key and type; optionally fail the file in strict mode.

### 13. Mask rows beyond `rows` and components beyond 9 columns are dropped silently
**Where:** `MaskWidget.java:43-46` (columns limited to 9), `GuiRegistry.java:121-129` (slot bounds check).
**Impact:** a layout mistake produces an invisible, non-working part of the menu with no diagnostic.
**Fix direction:** validate the pattern length against `rows` and length 9 on load.

---

## S3 — API and module inconsistencies

### 14. `:aat-api` is not self-contained
**Where:** `AATParser`, `ParserOptions`, `AATParseException` in `aat-core/.../api/parser/`; Oraxen/ItemsAdder providers in `aat-core/.../api/provider/impl/`.
**Impact:** an addon compiled only against `apiJar` cannot parse AAT YAML, cannot use the parser API, and gets `ClassNotFoundException` at runtime if it tries. The `api` package is split across two jars, which is fragile with the module system and confusing for users.
**Fix direction:** move the parser and providers into `:aat-core` under a non-`api` package, or move the parser into `:aat-api` and keep providers in `:aat-core` with a registry-based SPI.

### 15. `AATGui.Builder` covers only part of the widget set
**Where:** `api/gui/AATGui.java:38-104`.
**Impact:** no builder for paged lists, toggles, input/output slots, progress bars; callers must construct widgets manually. Older docs referenced a `Widgets` factory that does not exist (docs are now fixed, but the ergonomics gap remains).
**Fix direction:** add builder methods (`toggle`, `inputSlot`, `progressBar`, `pagedList`) or a `Widgets` factory, matching `AbstractWidget` configuration.

### 16. No `isInitialized` guard in `ActionTriggerAPI`
**Where:** `api/ActionTriggerAPI.java:51-97`.
**Impact:** addons that call the API before AAT enables get `IllegalStateException` with a Russian message; load-order bugs are hard to diagnose.
**Fix direction:** add `isInitialized()` and a clear English exception message, or queue registrations.

### 17. Registries are not thread-safe
**Where:** `ItemRegistry.java:11`, `BlockRegistry.java:11` use `HashMap`.
**Impact:** registering providers from an async task can corrupt the map. Trigger dispatch may run off the main thread (`AsyncChatTrigger`), so `getFullId` can race with registration.
**Fix direction:** `ConcurrentHashMap`, and document that providers must be registered during `onEnable`.

### 18. No vanilla block provider; `placeBlock` cannot place vanilla blocks
**Where:** `BlockRegistry.java:20-36`, only Oraxen/ItemsAdder providers are registered.
**Impact:** `getFullId` works through the fallback, but `placeBlock("minecraft:stone")` returns `false`.
**Fix direction:** add a `VanillaBlockProvider` or special-case the `minecraft` namespace in `BlockRegistry`.

### 19. `ActionRegistry#register` logs every action at INFO
**Where:** `api/action/ActionRegistry.java:31-34`.
**Impact:** startup spam for ~29 actions and every custom registration.
**Fix direction:** log at `FINE`, or log a single summary line.

---

## S4 — Behavioural inconsistencies

### 20. `core:player_interact` dispatches for both hands
**Where:** `core/defaults/triggers/PlayerInteractTrigger.java:36-37` (no `EquipmentSlot` check).
**Impact:** an off-hand interaction can run an ability twice, consuming cooldowns or items twice.
**Fix direction:** skip `EquipmentSlot.OFF_HAND` (or expose it as a context key so scripts can filter).

### 21. `core:player_sneak` only fires when entering the sneak state
**Where:** `PlayerToggleSneakTrigger.java:17-19`.
**Impact:** scripts that expect a toggle notification never see the release event; there is no `is_sneaking` key.
**Fix direction:** dispatch on both transitions and declare `IS_SNEAKING`.

### 22. `core:entity_death` puts the killer into `{player}`
**Where:** `EntityDeathTrigger.java:12-17`.
**Impact:** `{player}` is `null` for environmental deaths and means "killer" otherwise; scripts using it as "victim" break. There is no `{entity_type}`.
**Fix direction:** keep `{player}` empty (or victim if it is a player) and rely on `{killer}`; declare `entity_type`.

### 23. Context gaps in several triggers
**Where:** `PlayerDeathTrigger` (no killer), `PlayerLevelChangeTrigger` (new level only), `CraftItemTrigger` (no result amount), `PlayerJoinTrigger`/`PlayerQuitTrigger` (no messages).
**Impact:** common conditions cannot be written without `core:command` workarounds.
**Fix direction:** add the missing keys (`old_level`, `new_level`, `recipe_result_amount`, `killer`, join/quit messages) as new `ContextKey`s.

### 24. Parse-time parameters cannot depend on context
**Where:** `DefaultActionParsers.java:51` (`as_console`), `:76` (`type`), `:190` (`if_absent`), and `:942` (`core:schedule` default id).
**Impact:** `as_console: "{some_flag}"` does not work; more importantly, the default `core:schedule` id is generated once when the script is parsed, so every execution reuses the same id and cancels the previous schedule.
**Fix direction:** read these values inside the returned lambda and generate the id per execution (`AtomicLong` counter or UUID).

### 25. `core:potion_effect` duration is in ticks
**Where:** `DefaultActionParsers.java:157,169`.
**Impact:** `duration: 3` is 3 ticks, not 3 seconds. This is a classic source of "the effect does not work" reports.
**Fix direction:** keep ticks for compatibility, add a `seconds` alias and document both.

### 26. `core:firework` ignores appearance parameters
**Where:** `DefaultActionParsers.java:267-290`.
**Impact:** `type` and `colors` are silently ignored; every firework is red/yellow `BALL_LARGE` with trail.
**Fix direction:** parse `type`, `colors`, `fade`, `trail`, `flicker`.

### 27. Legacy Bukkit sound conversion is effectively dead
**Where:** `DefaultActionParsers.java:114-117`.
**Impact:** the condition only matches all-uppercase names and then produces a dotted lowercase key, so legacy names such as `BLOCK_NOTE_BLOCK_HARP` are not converted to `minecraft:block.note_block.harp` reliably.
**Fix direction:** use `Sound`/`Registry.SOUNDS` lookup with a namespaced fallback.

### 28. `core:gt` / `core:lt` require `Double`
**Where:** `DefaultFilterParsers.java:176-188`, `Filters.gt/lt` at `Filters.java:124-140`.
**Impact:** `{level}`, `{amount}` and other `Integer` keys always fail; `{damage}` works.
**Fix direction:** read `Number` and compare `doubleValue()`.

### 29. `core:chance` has no percent form and uses `<=`
**Where:** `DefaultFilterParsers.java:113-122`, `Filters.java:116-118`.
**Impact:** `percent: 25` is ignored (chance stays 1.0, so the condition always passes). `chance: 0.0` can theoretically pass on an exact 0.0 random value.
**Fix direction:** support `percent`, clamp the range, and use `<` for the zero case.

### 30. `core:in` uses `values`, not `list`, and `core:eq` is type-strict
**Where:** `DefaultFilterParsers.java:168-174`, `Filters.java:74-79`.
**Impact:** docs-driven configs silently fail: `list:` is null, `eq` with a numeric YAML scalar never equals a string key.
**Fix direction:** accept `list` as an alias, coerce scalar types in `eq`, or document the exact types loudly.

### 31. Unknown placeholders are erased
**Where:** `ContextPlaceholderParser.java:117-124` appends nothing when a token cannot be resolved.
**Impact:** a typo turns `{block_type}` into an empty string; `core:match` then compares `""` with the expected value and fails without any log. This is the most common cause of "the script does nothing".
**Fix direction:** leave the original token in place (or replace with a visible marker) and add a debug log in `/aat debug` mode.

### 32. PAPI and AAT share the `{...}` syntax
**Where:** `api/hook/PapiHook.java:49-65` rewrites `{key}` to `%key%` when PAPI resolves it.
**Impact:** a context key and a PAPI placeholder with the same name collide; PAPI bracket placeholders cannot be used where a context value exists.
**Fix direction:** require an explicit prefix for PAPI bracket syntax (e.g. `%...%` only) or a `{papi:key}` namespace.

### 33. `PapiHook` caches a negative initialization result
**Where:** `PapiHook.java:16-34`.
**Impact:** if the first call happens while PlaceholderAPI is not yet available, `enabled` stays `false` forever (until restart). The same applies if PAPI is enabled later.
**Fix direction:** retry initialization when `enabled == false`, or hook `PluginEnableEvent`.

### 34. `Widget#handleDrag` is never called
**Where:** `api/gui/widget/Widget.java:33-35`; `GuiListener#onInventoryDrag` cancels every drag over the top inventory.
**Impact:** custom widgets cannot implement drag behaviour; drag cancellation is blanket.
**Fix direction:** delegate to `handleDrag` for the widget under the slots, or remove the dead method.

### 35. Shift-click from the player inventory is always cancelled
**Where:** `GuiListener.java:66-69`.
**Impact:** players cannot shift-insert items into input slots and cannot shift-move items out of the GUI; mass operations are impossible.
**Fix direction:** allow shift-clicks that target input/output slots and let the widget validate them, or implement a custom transfer.

### 36. Widget text rendering is inconsistent
**Where:** `ButtonWidget.java:77-92`, `CycleButtonWidget.java:104-110` run PAPI and session substitution; `ToggleWidget.java:88-101` and `FluidTankWidget.java:65-72` do not.
**Impact:** `{player}` and `%placeholders%` work in buttons but not in toggles or the tank title.
**Fix direction:** centralize text formatting in a helper used by every widget.

### 37. `core:cryo_freeze` picks an arbitrary progress bar
**Where:** `DefaultActionParsers.java:622-635` iterates `holder.getSlotWidgets().values()` (a `HashMap`) and takes the first `ProgressBarWidget`.
**Impact:** with two progress bars the animation may attach to the wrong one; the order is unspecified.
**Fix direction:** address the progress bar by slot or by an explicit widget id.

### 38. `ProgressBarWidget` re-renders every widget on each tick
**Where:** `ProgressBarWidget.java:206-228` and `246-255` iterate all `slotWidgets` values (including duplicates from masks).
**Impact:** O(widgets²) inventory writes per step; unnecessary work for large menus.
**Fix direction:** re-render only the progress bar slot and any dependent indicator slots.

### 39. Progress state is per-session and lost on close
**Where:** `ProgressBarWidget.java:73-83` (`onOpen` puts defaults), `AATGuiHolder.sessionState`.
**Impact:** closing and reopening a machine mid-process resets the bar to idle even though the task keeps running; there is no persistence for machine state.
**Fix direction:** store machine progress in the block PDC (or a service keyed by block location) and restore it in `onOpen`.

### 40. Reload does not re-register actions, filters, providers or the combat listener
**Where:** `core/command/ActionCommand.java:176-193`.
**Impact:** a changed custom action requires a restart; `config.yml` combat keys are not re-applied; `messages` reload correctly.
**Fix direction:** document this explicitly (partially done) or add a full reload path that re-runs the registration block.

---

## S4 — Localization, commands and tests

### 41. Localization is partial despite the claim
**Where:** hard-coded Russian/English strings in `ActionCommand` (all replies), `AATTabCompleter` (`STANDARD_CONTEXT_KEYS`), `DefaultActionParsers` (machine messages, default `say Привет`, `<red>Текст не задан</red>`), `ProgressBarWidget` (stage names, status text).
**Impact:** switching `language` to `en` leaves large parts of the interface in Russian; the `messages_*.yml` bundles contain keys that are never used (`gui.not_found`, `gui.drag_blocked`, `gui.placeholder_take_blocked`, `combat.action_blocked`).
**Fix direction:** move the strings into the bundles and use `I18n` in commands and widgets; remove unused keys or wire them up.

### 42. Tab completion omits existing subcommands
**Where:** `AATTabCompleter.java:30-32` lists `run, trigger, open, list, guis, get, providers, provider_items, debug, reload`; `ActionCommand` also registers `search` and `hand`.
**Impact:** `/aat sea<TAB>` and `/aat han<TAB>` suggest nothing.
**Fix direction:** add the missing names, ideally derived from the command handler.

### 43. `/aat providers` prints empty labels
**Where:** `ActionCommand.java:336-352` uses `<color:#B02E0C><b>:</b></color>` with no text before the colon.
**Impact:** cosmetic, but the output looks broken.
**Fix direction:** restore the section titles.

### 44. Command permission is class-wide
**Where:** `ActionCommand.java:29-31`.
**Impact:** every subcommand, including `debug`, `reload` and `provider_items`, requires `actionstriggers.admin`; there is no read-only tier for `list`/`guis`/`search`.
**Fix direction:** add per-subcommand permissions if a read-only role is desired.

### 45. Tests do not cover the risky code
**Where:** `aat-api/src/test/...` (registry, scheduler, context, widgets), `aat-core/src/test/...` (combat, i18n, tab completer, filters, mask safety, "version compatibility").
**Impact:** `VersionCompatibilityTest` only checks that some `Material` constants and MiniMessage exist; it does not test versions. No test covers `AATParser`, `YamlTriggerLoader`, `YamlGuiLoader`, `DefaultActionParsers`, item safety on close, or PAPI/LuckPerms fallbacks.
**Fix direction:** add loader tests with sample YAML, action execution tests with a mocked `Player`, and an integration test for item return on close.

### 46. Build task deploys to a hard-coded local path
**Where:** `build.gradle:63-72` (`deployToPlugins` copies to `f:/Files/mc_test/plugins`) and `build` depends on it (`:74-80`).
**Impact:** `./gradlew build` writes to another machine's path and fails or creates stray directories outside the project.
**Fix direction:** make deployment opt-in (a separate task not wired into `build`) and configure the destination via a Gradle property.

### 47. `gradle.properties` is empty
**Where:** `gradle.properties` (0 bytes), `aat-core/build.gradle:7` (`paperVersion` default).
**Impact:** the Paper API version can only be overridden with `-PpaperVersion=...`; there is no documented, persistent setting.
**Fix direction:** add a documented `paperVersion` property.

### 48. Version claims in metadata
**Where:** `README.md`/`README_RU.md` badges and requirements (updated to `1.21.4 – 26.3`), `plugin.yml` (`api-version: '1.21'`).
**Impact:** earlier revisions claimed a range without an automated check. For `0.1.0-beta` the boundary is backed by compile checks (1.21.4, 1.21.5, 1.21.8, 1.21.11) and runtime smoke tests on Paper 1.21.4 build 232, 1.21.11 build 132 and 26.3 build 253. The build is still pinned to one snapshot per release, and there is no automated runtime matrix yet.
**Fix direction:** add the runtime smoke matrix to CI (see `.github/workflows/`) and re-verify the boundary when Paper publishes a new major line.

---

## Suggested order of work

1. Fix the data-loss trio: item return on disable/quit (1), `give_item` leftovers (2), machine input refund (3).
2. Implement or remove the dead GUI contract: `on_close` (4), combat flags (5, 6), `target_block` (11), unknown types (12).
3. Repair the YAML surface that is advertised but unusable: `tab_container` (7), `paged_list` (8), `fluid_tank` (9), mask multi-cell (10).
4. Fix silent failures: placeholder erasure (31), parse-time parameters and `core:schedule` id (24), `chance` (29), `in`/`eq` (30), tick semantics (25).
5. Tighten the module boundary (14), thread safety (17), and add parser/loader tests (45).
