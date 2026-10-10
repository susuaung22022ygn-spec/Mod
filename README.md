# Crimson Reaper — Fabric 1.21.11 source project

**This is source code, not a compiled or tested JAR.** This environment has Java 21 but no Gradle installation and cannot reach external dependency servers, so a successful build cannot be verified here.

## Features implemented in source
- Command-only item ID: `crimson_reaper:crimson_reaper` (no recipe is included).
- Item display name: **Crimson Reaper**.
- Custom 32×32 crimson-red, black, and purple sword texture.
- Bloodlust: each hit adds a damage stack, up to 20 stacks.
- Execution: right-click to activate for 5 seconds, with a 15-second cooldown.
- Particle bursts on activation and on hits, with extra portal particles during Execution.

## Give command
With the mod installed and cheats enabled:

```mcfunction
/give @s crimson_reaper:crimson_reaper
```

## Build
Requires Java 21, Gradle, and internet access to resolve Fabric/Minecraft dependencies:

```sh
gradle build
```

The output should appear under `build/libs/`. An online GitHub Actions workflow is included in `.github/workflows/build.yml`.

## Important
The source has not been compiled or tested, and Minecraft/Fabric API signatures may require fixes on the first build. Use a backup singleplayer world for testing. Particle colors are limited to Minecraft's built-in particle types here; these are magical red-hit / purple-like visual effects rather than a custom-colored particle system.
