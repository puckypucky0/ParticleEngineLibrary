<div align="center">

# PARTICLE ENGINE LIBRARY

[![Release](https://img.shields.io/github/v/release/puckypucky0/ParticleEngineLibrary?sort=semver)](https://github.com/puckypucky0/ParticleEngineLibrary/releases)
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-brightgreen)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net)
[![License](https://img.shields.io/github/license/puckypucky0/ParticleEngineLibrary)](LICENSE)

**English** · [Türkçe](../README.md)


</div>

https://github.com/user-attachments/assets/7e5c0772-b409-4d90-8ad0-6bd98790bcb8

<sub>This showcase was recorded with shaders and ReplayMod.</sub>

---

## What it does

An engine that puts the Bukkit particle API to work properly: more than five shape
families, colour gradients and animation, all running under limits the server owner
controls.

> [!CAUTION]
> This is not a plugin with in-game commands. It is a particle library built for developers to use in their own projects.


- **One central task**: the engine is driven by a single scheduled task, so the load stays flat however many effects are running.
- **TPS awareness**: thresholds in `config.yml` thin out effect density and skip rendering entirely when the server starts to struggle.
- **Every dial exposed**: almost everything can be controlled through `config.yml`.

## Gallery

<table>
  <tr>
    <td width="50%" align="center"><b>Single block highlight</b><br><img src="https://github.com/user-attachments/assets/77cdaf55-1c11-4922-a1bb-4e1c9c0cc6ac" alt="Block highlight"></td>
    <td width="50%" align="center"><b>Helix with rotation and gradient</b><br><img src="https://github.com/user-attachments/assets/b9b2645c-817d-4b05-bb28-7625c0e06b7f" alt="Rotating helix"></td>
  </tr>
  <tr>
    <td align="center"><b>Rising ring</b><br><img src="https://github.com/user-attachments/assets/88d6f444-94ca-45b3-b35b-6a1b04965220" alt="Rising ring"></td>
    <td align="center"><b>Sparkle cloud</b><br><img src="https://github.com/user-attachments/assets/834a9d8b-5e2a-483e-b39e-24674a5caca5" alt="Cloud"></td>
  </tr>
</table>

## Installation

1. Download the jar from the [Releases](https://github.com/puckypucky0/ParticleEngineLibrary/releases) page.
2. Drop it into `plugins/`.
3. Restart the server.

A documented `config.yml` is generated on first start. Nothing else is required.

| | |
|---|---|
| Server | Paper 1.21 or newer recommended. |
| Java | 21 or newer |
| Spigot | Not supported |

## For plugin developers

An effect is one builder call:

```java
ParticleEngine.submit(ParticleEffect.builder()
        .shape(Shapes.circle(1.5, 48, Shapes.Axis.Y))
        .origin(player::getLocation)
        .gradient(Gradient.of("#00ffc8", "#0066ff", "#00ffc8"))
        .durationTicks(20 * 10)
        .animateGradient(true)
        .owner(player)
        .build());
```

For the full API reference and the shape catalogue, see the
**[developer documentation](DEVELOPERS.en.md)**.

## License

[LICENSE](../LICENSE). Copyright (c) 2026 Pucky
