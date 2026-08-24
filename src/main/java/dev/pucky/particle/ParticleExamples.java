package dev.pucky.particle;

import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.function.Supplier;

/**
 * Partikül sisteminin örnek kullanım dosyası.
 * <p>
 * Bu sınıf hiçbir yerden otomatik çağrılmaz, herhangi bir komuta bağlı değildir.
 * <p>
 * Yalnızca referans amaçlıdır.
 * <p>
 * Genel kurallar DEVELOPERS.(tr/en).md dosyasında belirtilmiştir.
 */
public final class ParticleExamples {

    private ParticleExamples() {}

    /**
     * Bir bloğun etrafına sabit, tek renkli bir kafes çizer.
     */
    public static ParticleEngine.Handle highlightBlock(Block block, Player owner) {
        Location origin = block.getLocation();

        ParticleEffect effect = ParticleEffect.builder()
                .shape(Shapes.blockHighlight())
                .origin(() -> origin)
                .gradient(Gradient.solid(Color.fromRGB(255, 215, 0)))
                .durationTicks(20 * 10)
                .owner(owner)
                .build();

        return ParticleEngine.submit(effect);
    }

    /**
     * Bir chunk'ın dikey kenarlarını + üst/alt çerçevesini belirtilen Y aralığında çizer.
     */
    public static ParticleEngine.Handle highlightChunk(Chunk chunk, int minY, int maxY, Player owner) {
        // chunkHighlight() Y offsetlerini minY ile maxY'nin ortasına göre üretir.
        // Origin'in Y'si de bu yüzden o orta değer olur.
        Location origin = new Location(chunk.getWorld(), chunk.getX() * 16, (minY + maxY) / 2.0, chunk.getZ() * 16);

        ParticleEffect effect = ParticleEffect.builder()
                .shape(Shapes.chunkHighlight(minY, maxY))
                .origin(() -> origin)
                .gradient(Gradient.of("#43E5F7", "#1769FF"))
                .durationTicks(20 * 15)
                .owner(owner)
                .build();

        return ParticleEngine.submit(effect);
    }

    /**
     * Bir noktanın etrafında duran, gradyanı zamanla ilerleyen bir çember.
     * Supplier aracılığıyla hareketli bir hedef için kullanılabilir.
     */
    public static ParticleEngine.Handle gradientCircle(Supplier<Location> origin, double radius, Player owner, String... colors) {
        ParticleEffect effect = ParticleEffect.builder()
                .shape(Shapes.circle(radius, 48, Shapes.Axis.Y))
                .origin(() -> {
                    Location center = origin.get();
                    // Location#add nesneyi kendi üzerinde değiştirir. clone() olmazsa
                    // çağıranın verdiği Location her tick biraz daha yükselir.
                    return center == null ? null : center.clone().add(0, 0.1, 0);
                })
                .gradient(Gradient.of(colors))
                .animateGradient(true)
                .durationTicks(20 * 4)
                .owner(owner)
                .build();

        return ParticleEngine.submit(effect);
    }

    /**
     * Oyuncuyu takip eden ve pulse efektine sahip bir halka.
     */
    public static ParticleEngine.Handle followingHalo(Player target) {
        ParticleEffect effect = ParticleEffect.builder()
                .shape(Shapes.circle(1.2, 24, Shapes.Axis.Y))
                .origin(() -> target.getLocation().add(0, 1.0, 0))
                .gradient(Gradient.of("#FF6B6B", "#FFD93D", "#6BCB77"))
                .rotationSpeed(0.1)
                .pulse(true)
                .durationTicks(20 * 30)
                .owner(target)
                .build();

        return ParticleEngine.submit(effect);
    }

    /**
     * Yukarı doğru yükselen, dönen ve gradyanı akan bir helix.
     */
    public static ParticleEngine.Handle animatedHelix(Location base, Player owner) {
        ParticleEffect effect = ParticleEffect.builder()
                .shape(Shapes.helix(1.5, 4.0, 3, 120))
                .origin(() -> base)
                .gradient(Gradient.of("#00C9FF", "#92FE9D"))
                .animateGradient(true)
                .rotationSpeed(0.15)
                .durationTicks(20 * 20)
                .owner(owner)
                .build();

        return ParticleEngine.submit(effect);
    }

    /**
     * Bir noktanın etrafında dağınık duran, yıldız tozu gibi parıldayan rastgele bir bulut.
     */
    public static ParticleEngine.Handle sparkleCloud(Location center, Player owner) {
        ParticleEffect effect = ParticleEffect.builder()
                .shape(Shapes.pointCloud(1.5, 60))
                .origin(() -> center)
                .gradient(Gradient.of("#F9F871", "#FF6F91", "#845EC2"))
                .animateGradient(true)
                .pulse(true)
                .durationTicks(20 * 8)
                .owner(owner)
                .build();

        return ParticleEngine.submit(effect);
    }

    /**
     * Pulse'ı görünür kılan kurulum, hareketli merkez nokta.
     */
    public static ParticleEngine.Handle pulseTrail(Player owner, double radius) {
        Location base = owner.getLocation().add(0, 1, 0);

        return ParticleEngine.submit(ParticleEffect.builder()
                .shape(Shapes.point())
                .origin(() -> {
                    // 6 saniyede bir tur. Sistem saatinden türetmek, motorun origin'i
                    // tick başına kaç kez çağırdığına bağlı kalmamayı sağlar.
                    double angle = (System.currentTimeMillis() % 6000) / 6000.0 * 2 * Math.PI;
                    return base.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                })
                .gradient(Gradient.solid(Color.fromRGB(255, 255, 255)))
                .size(2.0f)
                .pulse(true)
                .durationTicks(20 * 15)
                .owner(owner)
                .build());
    }

    /**
     * İki nokta arasında gradyan renkli düz bir çizgi.
     * Shapes.line() noktaları from'a göre offset üretir, bu yüzden origin from olur.
     */
    public static ParticleEngine.Handle gradientLine(Location from, Location to, Player owner) {
        Vector fromVector = from.toVector();
        Vector toVector = to.toVector();
        Location worldOrigin = from.clone();

        ParticleEffect effect = ParticleEffect.builder()
                .shape(Shapes.line(fromVector, toVector, 0.3))
                .origin(() -> worldOrigin)
                .gradient(Gradient.of("#F72585", "#7209B7", "#3A0CA3"))
                .durationTicks(20 * 12)
                .owner(owner)
                .build();

        return ParticleEngine.submit(effect);
    }

    /*
     * Bir efekti elle durdurmak istersen:
     *   ParticleEngine.Handle handle = ParticleExamples.followingHalo(player);
     *   ...
     *   ParticleEngine.cancel(handle);
     *
     * Bir oyuncuya ait bütün efektleri durdurmak istersen (örn. oyuncu çıkarken):
     *   ParticleEngine.cancelAll(player);
     */
}
