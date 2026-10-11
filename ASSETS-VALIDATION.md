# Client resource fixes v5.5.1

Candidate validation in an isolated Pinecone 11.1.1 profile, Minecraft 1.21.1 / NeoForge 21.1.257. The existing 118 production mods remain; only our compatibility JAR is revised. The private test probe is not distributed.

## Root causes and changes

- Three exported GeckoLib clips put easing names in numeric position/rotation vectors. Neutralize only those exact malformed vectors: hidden hands in QBZ-95/Vector edit clips, and Clogger's death head offset. Preserve other bones, clips, valid Molang, and the input JSON. Assets stay in their authors' JARs; no animation files are redistributed. GeckoLib passes the inner animations map to its adapter.
- Steam 'n' Rails registers phantom sprites during resource loading, before the block atlas has uploaded them. Its upload callback then runs against an unready or unbound texture. The client mixin leaves the pending update until the exact registered sprites are in the uploaded atlas, binds that atlas, and restores the previous binding. It does not suppress OpenGL errors or disable phantom tracks.
- Power Loader's two loaded chunk-loader item models contain unqualified axis texture references. Child overrides reference the existing Create axis textures and retain the original geometry.
- NTGL stone/diamond hammer attachments have no item models. They are attachment heads, not complete Geo-rendered weapons. Provide two original small baked head models using vanilla stone/diamond/iron textures. Referencing NTGL's complete hammer model did not render these plain attachment items and was rejected after inspecting the native screenshot.
- Design n' Decor's five large-chain events reference missing sound files from another namespace. The shipped mod has no OGG files. They now resolve to the corresponding vanilla metal-chain event at volume 0.9.

## Evidence

Pure Java fixture: 91 assertions passed, including the actual inner-map adapter input, idempotence, immutable input, unchanged unrelated clips and transforms. All four existing common compatibility classes are byte-identical to version 1.1.0, including drone-save and orphan-spawner fixes.

Baseline native client: 12 PASS / 10 FAIL (three animation clips, two item-face texture checks, five sound events). Its screenshot also showed two missing attachment icons; the initial model-identity predicate was insufficient for these wrapped models. Later assertions inspect real baked faces, and screenshots are reviewed as an independent check.

The first baseline attempt exhausted Windows native memory while the user's main 16 GiB client and the QA client ran together. After the user closed the main client, the unchanged baseline entered the test world and exited normally. This was a resource-exhaustion test failure, not evidence of a repaired shader or JVM bug. Subsequent clients run one at a time. The initially attempted game-directory junction also caused Java directory-creation errors; the final isolated profile is physically on D.

Final isolated native client: **30 PASS, 0 FAIL**, three clips in GeckoLib's baked cache, all four repaired item models with non-missing baked face textures, all five sound events resolving, and four phantom-track GPU/state assertions. Native contact sheet reviewed: both attachment heads and the two chunk loaders render; shader-enabled world screenshot also reviewed. OpenGL invalid-texture messages fell from 34 in the completed baseline to zero; animation parse failures fell from three clips (six log mentions) to zero. No shader program-link errors. The test saved all dimensions and its process exited; an OS exit code was not captured for the GUI child, so none is claimed.

The phantom test toggles the actual held track item, invokes the installed manager and reads sprite pixels back from the GPU. Hidden/visible pixels differ, removing the item restores them, pending updates are consumed and the previous texture binding survives. This is not a claim of testing every rail shape or shader combination.

Dedicated-server regression `runtime-20261011-195142`: **163 PASS, 0 FAIL**, ready=true, exit=0, real_clients=0. The installed 165-quest book, references, migration, native serialization and calendar conditions still pass with the revised compatibility JAR. The new client mixins do not load on a dedicated server.

## Remaining limits

This does not certify the full pack for public release. Other upstream NTGL sample item models, an unused Undead WebP mislabeled as PNG, optional Create compatibility models and shader uniform warnings still need assessment. The prior intermittent Create chain-drive registry issue remains open unless independently resolved. Real two-player LAN, loaded siege/transport performance, a full survival playthrough and the physical Zorro are not tested here. Personal worlds, settings, shader ZIPs and radio calibration remain unmanaged.

## Update canary

Local Packwiz update: 118 JARs, all 1173 managed hashes correct, repeat invocation exit 0. Six personal sentinels (radio, custom shader ZIP and settings, Iris choice, keybinds and a world file) unchanged. No QA probe is managed or published.

## Public distribution and host

GitHub Pages served the exact pack and six changed files. The public updater validated all 1173 installed hashes and 118 JARs; repeat exit 0 and six personal sentinels unchanged. The host updater installed the same six files, retired compatibility 1.1.0 and verified all 118 mod SHA-512 hashes. All 708 tracked personal files stayed byte-identical, and heap remains 16384 MiB. The main client was not relaunched after installation; native graphical validation used the isolated profile with the same candidate bytes.
