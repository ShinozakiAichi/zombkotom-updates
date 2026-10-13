# Historical validation: v5.5 and v5.5.1

For the current scope, evidence and remaining public-release gates, see [QUALITY-STATUS-RU.md](QUALITY-STATUS-RU.md). The version and counts below describe their original runs, not the current pack.

See [ASSETS-VALIDATION.md](ASSETS-VALIDATION.md) for the subsequent client repairs and direct runtime evidence. The v5.5 journal validation below remains historical evidence; its client asset issue list is superseded where explicitly fixed in v5.5.1.

# Quality work: journal v5.5

This is an incremental quest release, not certification of the entire modpack as a finished public release. Minecraft 1.21.1, NeoForge 21.1.257, 118 production mod JARs unchanged.

## Verified on 2026-10-11

Final isolated server run `runtime-20261011-190359`: **163 PASS, 0 FAIL**, ready=true, exit=0, real_clients=0. The production book/data overlay was loaded by the installed FTB Quests and NeoForge runtime. QA-only probe JARs are never distributed.

- 165 quests, 23 chapters, eight named groups; Russian text and English-interface fallback loaded.
- All 146 item conditions and 150 item rewards resolve against the actual registry; all 23 advancement conditions resolve.
- Ten selected lessons: inventory quantities detected, another player's inventory does not count, no item consumption, practical conditions remain required. This is API simulation with synthetic players, not two real clients.
- All 160 old completed quest timestamps and 270 old reward claims survive serialization into the revised definitions; the five new quests remain incomplete.
- All advancement tasks reject unearned and accept recorded advancements through the native FTB eligibility method. Create actions themselves were not all physically performed in a survival world; their meaning comes from the installed mod's advancement definitions.
- The actual clock/milestone functions reject the calendar day before each 10/25/40/70/100 threshold, grant at the threshold, and exclude spectators. Calendar age is explicitly not proof of surviving an attack.
- Native FTB book write/read round trip preserves 165 quests and 234 tasks.
- Static audit: 770 unique chapter/quest/link/task/reward IDs, valid dependencies, no cycles, all old quest/reward IDs retained, all staged JSON parsed, no personal paths included.

The final task mix is 146 item, 63 practical checkmark, 23 advancement, one kill and one dimension condition. Checkmarks describe free engineering/defense exercises; they are not presented as automatic structural or combat checks.

## Test-fixture failures and correction

Development runs did not all pass. The public player list is unmodifiable, and standard NeoForge FakePlayer uses no-op advancement storage. The fixture now uses a synthetic player with the ordinary advancement implementation and a controlled registration in the isolated server. An attempted multi-tick fixture then reached network time synchronization without a real channel and crashed the QA world; it was removed. The final calendar probe registers and removes its synthetic player entirely within one server event. None of these QA players or code is installed in the user's game.

One run used the preceding compiled probe after a preparation syntax error; that failed run was discarded. Release preparation requires a later completed zero-failure result and checks the exact staged hashes. A successful API test is not a claim of a human playthrough.

## Open issues before a broad public release

1. **Intermittent Create registry problem.** In development run `runtime-20261011-185241`, `create:encased_chain_drive` was missing during data loading. Four recipes and its block loot table failed to parse. The preceding and final runs did not reproduce it; no registry or mod change was made to claim a repair. More diagnosis is required. Do not describe a one-run absence as a fix.
2. **Client rendering/assets.** Prior real-client log contained NTGL missing item models, SBW/GeckoLib easing parse errors, Undead asset warnings, missing Design n' Decor sound references, OpenGL texture-format errors and Complementary uniform warnings. They are not fixed by this quest-only release.
3. **Real gameplay.** Two-client LAN physics, full loaded-base sieges, turret targeting, drone camera rendering with shaders, FPS/MSPT under load, full-world save/restart and a full survival progression remain separate validation work. Physical Zorro testing was deferred at the user's request.

The updater's local/public transition checks and host startup are recorded below when executed. Personal shader ZIPs, Iris options, controls, worlds and radio calibration remain outside the managed index.

## Local update canary

PASS: 118 JARs, all 1168 managed installed hashes, repeat invocation exit 0. Six personal sentinels (radio JSON, custom shader ZIP and its options, Iris selection, keybinds and a world file) stayed byte-identical.

## Public update and host startup

GitHub Pages served the exact pack.toml and all 19 changed/added data files. The public updater and repeated invocation passed: 118 JARs, 1168 installed hashes, six personal sentinels preserved. The real host has all 19 exact overlay hashes and all 118 exact mod SHA-512 hashes. Personal files matched their pre-update hashes before launch; heap limit remains 16384 MiB.

The real Pinecone pre-launch reported the pack up to date. Client log on 2026-10-11 reported startup completed in 67.537 seconds at 19:10:32; javaw remained responsive. This confirms client startup, not visual book layout, in-world rendering, a full quest playthrough, or two real players. OpenGL texture-format and other prior asset warnings remain.
