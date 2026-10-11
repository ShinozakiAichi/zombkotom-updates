# LAST BASTION 5.4.0 — FPV validation

2026-10-11. Minecraft 1.21.1, NeoForge 21.1.257, Java 21. FPVCraft 0.2.0; Superb Warfare remains 0.8.9.2. Pack-owned Industrial Compatibility patch upgraded from 1.0.0 to 1.1.0.

## Dedicated runtime probe: PASS 86 / FAIL 0

The isolated server loaded the pack's server mods with Create, Aeronautics and Sable. Tests ran through a temporary QA mod in an empty flat world; that QA mod is not distributed.

- FTB loaded 160 quests in 22 chapters. All task and reward items resolved.
- All 38 FPVCraft component recipes loaded with nonempty ingredient choices (empty crafting-grid positions excluded).
- Create recipe overrides loaded; combat drone output is one and requires a precision mechanism; goggles require an electron tube.
- Default FPV build passed the mod's build validator. Battery charge and broken prop count survived native entity NBT save/load; restored drone was disarmed.
- Native SBW interaction accepted RGO, medical kit, C4 and mortar shell and consumed the supplied item.
- A registered NeoForge fake player exercised the normal SBW fire tick for RGO and medical kit. Ammo decreased by one and the expected dropped entity spawned.
- All four SBW payload items, ammo counts and nonempty display NBT survived entity save/load.
- Laser tower entity constructed, ticked and survived entity NBT save/load.

**Real multiplayer clients in these tests: 0.** A fake player does not validate LAN transport, camera rendering or human interaction.

## Reproduced bug and repair

Unmodified Superb Warfare 0.8.9.2 lost the payload item on all four tested drone save/load cases. Its save method checked whether a newly created empty tag was nonempty, so it never serialized the item. It also saved `DisplayEntityTag` as a string while its load method reads a compound.

Industrial Compatibility 1.1.0 injects after that save method, serializes the actual item (including components), and writes the display compound. The mixin requires exactly SBW 0.8.9.2. Its source is included in the JAR and in `src/industrial-compat`. The existing Lost Cities repair remains present. Do not upgrade SBW independently without retesting this patch.

Regression: four payload reload failures before the repair; all four pass after it. No third-party mod JAR was modified. This preserves future saves; it cannot reconstruct an item already lost in an older save.

## Static checks

153 previous quest IDs retained; seven optional new quests. All 644 quest/task/reward IDs unique, references resolve, dependency graph is acyclic. Russian titles/descriptions present, existing `ru_ru` fallback retained. Seventy FPVCraft translation keys checked against upstream keys and formatting placeholders.

## Test fixture issues

- First test fixture combined a flat world with the Lost Cities city profile and timed out searching for spawn. The QA-only profile was disabled for payload tests; production city settings are unchanged. This run did not revalidate city generation.
- Initial recipe check incorrectly treated empty shaped-recipe spaces as missing ingredients; fixed the test.
- Initial drop probe called a private spawn helper, bypassing ammo decrement. Replaced with native `baseTick` and a fake player registered for entity lookup; both spawned payload and ammo decrement then passed.
- An upstream Atlas update-check JSON warning appeared; it did not prevent startup or the probes.

## Still needs a human playtest

User chose automatic checks without connecting Zorro. USB HID detection, physical stick assignment, FPV image with Complementary/Iris, two-player races, drone range/chunk loading under load, live turret targeting/power supply and performance remain unverified. Entity NBT round trips are not a full survival-world shutdown/restart or a two-client test.

See `DRONES-RU.md` for the short in-game checklist and the distinction between unarmed FPVCraft/Zorro flight and the separate keyboard-controlled SBW payload drone.

## Update canary

Local candidate update and repeat invocation passed: 118 JARs, 1161 installed file hashes verified. Radio calibration JSON, custom shader ZIP/settings, Iris selection, keybinds and a save sentinel were byte-preserved. The initial local HTTP fixture rejected two concurrent connections; increasing its listen backlog and retrying completed the update. Public CDN delivery and the real client are checked separately after publishing.
