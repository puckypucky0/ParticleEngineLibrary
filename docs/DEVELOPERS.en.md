# Developer documentation

**English** · [Türkçe](DEVELOPERS.tr.md) · [Back to README](../README.md)

---

## Contents

- [Setup](#setup)
- [Shapes](#shapes)
- [Effect options](#effect-options)
- [Gradients](#gradients)
- [Configuration](#configuration)
- [Pitfalls](#pitfalls)
- [Examples](#examples)

---

## Setup

### 1. Add the dependency

<details open>
<summary><b>Maven</b></summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.puckypucky0</groupId>
        <artifactId>ParticleEngineLibrary</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

</details>

<details>
<summary><b>Gradle (Kotlin DSL)</b></summary>

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.puckypucky0:ParticleEngineLibrary:1.0.0")
}
```

</details>

> [!IMPORTANT]
> The scope is `provided` / `compileOnly` on purpose. The engine runs as its own plugin;
> shading it into your jar would give you a second, isolated copy with its own state.
> It will lead to corruption.

### 2. Declare it in your plugin.yml

```yaml
depend: [ParticleEngineLibrary]
```

Without this, load order is not guaranteed and you may hit `NoClassDefFoundError` on
startup.

### 3. Submit an effect

```java
import dev.pucky.particle.*;

ParticleEffect effect = ParticleEffect.builder()
        .shape(Shapes.circle(1.5, 48, Shapes.Axis.Y))
        .origin(player::getLocation)          // re-evaluated every tick, so it follows
        .gradient(Gradient.of("#00ffc8", "#0066ff", "#00ffc8"))
        .durationTicks(20 * 10)               // ten seconds
        .rotationSpeed(0.05)
        .animateGradient(true)
        .owner(player)
        .build();

ParticleEngine.Handle handle = ParticleEngine.submit(effect);
```

Stop it early with `ParticleEngine.cancel(handle)`, or clear everything a player owns
with `ParticleEngine.cancelAll(player)` — useful in a quit listener.

| Type | Role |
|---|---|
| `ParticleShape` | Which points to draw, as offsets from the origin. Built only through `Shapes`. |
| `ParticleEffect` | An immutable description of one effect. Submitting it twice gives two independent runs. |
| `ParticleEngine` | The single scheduler task, the budget, the limits, and the packet sending. |

Each tick the engine reads the server TPS, decides whether to skip or thin rendering,
walks the active effects in a rotating order, advances their timers, and spends the
shared point budget until it runs out. The rotation is what stops the same effects from
being starved every tick when the budget is tight.

## Shapes

Every factory returns offsets relative to the effect's origin.

| Factory | Produces |
|---|---|
| `point()` | A single point |
| `line(from, to, spacing)` | A straight line, relative to `from` |
| `circle(radius, points, axis)` | A ring on the plane perpendicular to `axis` |
| `sphere(radius, points)` | A hollow sphere, points spread by the golden-angle spiral |
| `box(size, spacing)` | The twelve edges of a box centred on the origin |
| `blockHighlight()` | A slightly inflated wireframe cube for one block (cached) |
| `chunkHighlight(minY, maxY)` | Four vertical chunk edges plus top and bottom frames |
| `helix(radius, height, turns, points)` | A spiral rising along Y |
| `filledSquare(size, pointsPerSide, axis)` | A filled grid square |
| `pointCloud(radius, count)` | Random points filling a sphere, uniform by volume |

`Shapes.Axis` is `X`, `Y` or `Z` and selects the plane flat shapes lie on.

> [!NOTE]
> `Shapes` is the only supported way to build a `ParticleShape`. That is deliberate: it
> guarantees every shape passes the same thinning and point-count limits.

> [!NOTE]
> Some options are highlighted with [D], because they are only available with DUST particle type.

## Effect options

| Option | Type | Default | Meaning |
|---|---|---|---|
| `shape` | `ParticleShape` | required | What to draw |
| `origin` | `Supplier<Location>` | required | Where to draw it, re-evaluated every render tick |
| `particle` | `Particle` | `DUST` | Vanilla particle type |
| `gradient` | `Gradient` | solid white | [D] Colour across the shape |
| `size` | `float` | `1.0` | [D] Dust size |
| `durationTicks` | `int` | `100` | Requested lifetime, clamped server-side |
| `rotationSpeed` | `double` | `0` | Radians per tick around the vertical axis |
| `pulse` | `boolean` | `false` | [D] Oscillate dust size over time |
| `animateGradient` | `boolean` | `false` | [D] Slide the colours along the shape |
| `viewers` | `Collection<Player>` | `null` | Explicit audience; `null` means distance-based |
| `owner` | `Player` | `null` | Quota holder and `cancelAll` target |

## Gradients

```java
Gradient.solid(Color.RED);
Gradient.of("#F72585", "#7209B7", "#3A0CA3");
Gradient.of(Color.RED, Color.BLUE);
```

Colours are interpolated between stops. Each point on the shape gets a position `t`
between 0 and 1 and asks the gradient for the colour there, so the same gradient works
whether the shape has five points or four hundred.

> [!TIP]
> With `animateGradient` the colour position wraps from 1 back to 0. If your first and
> last stops differ you will see a seam there. Repeat the first colour at the end —
> `"#ff0055", "#ffaa00", "#ff0055"` — for a continuous loop.

## Configuration

`plugins/ParticleEngineLibrary/config.yml`. Every key is optional; missing keys fall back to
built-in defaults.

| Key | Default | Effect |
|---|---|---|
| `enabled` | `true` | Master switch. When false, `submit()` returns null and nothing renders. |
| `tick-interval` | `2` | Period of the central task, in ticks. Lower is smoother and costlier. |
| `view-distance` | `48` | Radius in blocks for automatic viewer lookup. |
| `global-max-points-per-tick` | `1500` | Shared point budget across all effects, per tick. |
| `max-active-effects` | `64` | Server-wide cap on concurrent effects. |
| `max-effects-per-player` | `30` | Per-owner cap on concurrent effects. |
| `max-points-per-shape` | `400` | Hard cap applied when a shape is built. |
| `min-spacing` | `0.15` | Points closer than this to the previous kept point are dropped. |
| `max-duration-seconds` | `30` | Ceiling on effect lifetime, always enforced. |
| `tps-throttle-threshold` | `18.0` | Below this TPS, point density is halved. |
| `tps-pause-threshold` | `14.0` | Below this TPS, rendering stops but timers keep running. |

> [!NOTE]
> Limits are read once when the engine starts. Change them and restart the server for the
> new values to take effect.

## Pitfalls

> [!IMPORTANT]
> **Shape points are offsets, never world coordinates.** The engine adds them to the
> origin each tick. That is what lets one shape be reused at any location.

> [!WARNING]
> **`owner` and `viewers` are unrelated.** `owner` decides whose quota the effect counts
> against and who `cancelAll` reaches. `viewers` decides who receives the packets. Setting
> `viewers` also disables distance culling entirely, so those players keep receiving
> packets however far away they walk.

> [!CAUTION]
> **Limits are enforced quietly.** `submit()` returns `null` when a cap is reached, dense
> shapes are thinned and truncated, and durations are clamped. A console warning is
> logged, but your code sees only a null handle — check it if the effect matters.

> [!TIP]
> **`pulse` needs a moving origin to be visible.** Dust particles keep the size they had
> when they spawned and live about as long as one pulse cycle, so at a fixed point every
> size is on screen at once and the effect averages out. Move the origin and the size
> change becomes a visible ripple along the trail.

> [!NOTE]
> **Rotation is around the vertical axis only.** A flat ring spins like a record; it
> cannot tumble. Rotation is also pointless on rotationally symmetric shapes such as
> `sphere`, and wrong on `blockHighlight` and `chunkHighlight`, which must stay aligned to
> the world grid.

## Examples

[`ParticleExamples`](../src/main/java/dev/pucky/particle/ParticleExamples.java) contains
eight working, documented effects to copy from:

| Method | Shows |
|---|---|
| `highlightBlock` | Highlighting a block's edges |
| `highlightChunk` | Chunk boundary between two heights (will be fixed) |
| `gradientCircle` | Animated gradient on a ring, fixed or following |
| `followingHalo` | Tracks a player by giving locations by tick (supplier) |
| `animatedHelix` | Rotation and gradient animation together |
| `sparkleCloud` | Random particle filling |
| `pulseTrail` | Pulse made visible through a moving origin |
| `gradientLine` | Line between two locations |

## Compatibility

Built and tested against Paper 1.21.4. Plugins compiled against an older Paper
API run on newer servers, so this library targets 1.21 deliberately rather than the latest
release.

The API is pre-1.0 and may still change between minor versions.
