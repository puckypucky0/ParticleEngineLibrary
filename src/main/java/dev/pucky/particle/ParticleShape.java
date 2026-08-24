package dev.pucky.particle;

import dev.pucky.util.log.Logging;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bir partikül şeklini tanımlayan ve merkez noktaya göre noktalar oluşturan
 * değiştirilemez ve sınırlanmış liste.
 * <p>
 * Bir ParticleShape üretebilmenin tek yolu {@link #capped(List)} metodudur.
 */
public final class ParticleShape {

    private final List<Vector> points;

    private ParticleShape(List<Vector> points) {
        this.points = points;
    }

    /**
     * Henüz sınırlanmamış olan nokta listesini seyreltme ve sert tavan aşamalarından geçirerek
     * güvenli hale getirir. Seyreltme aşamasında ardışık noktalar arasında minSpacing değerinden
     * daha yakın olanlar atlanır, dolayısıyla tek bir yerde bitişik binlerce nokta olmaz.
     * <p>
     * Sert tavan aşamasında ise seyreltmeden kalan nokta sayısının hala {@code maxPointsPerShape}'i
     * aşıp aşmadığı kontrol edilir. Eğer aşıyorsa, o nokta listesi sondan kırpılarak sınıra kadar
     * getirilir.
     * Her iki limit de {@link ParticleLimits}'ten okunur.
     * */
    static ParticleShape capped(List<Vector> rawPoints) {
        ParticleLimits limits = ParticleLimits.get();

        List<Vector> thinned = new ArrayList<>();
        double minSpacing = limits.minSpacing();
        Vector last = null;
        for (Vector point : rawPoints) {
            if (last == null || last.distance(point) >= minSpacing) {
                thinned.add(point);
                last = point;
            }
        }

        int max = limits.maxPointsPerShape();
        if (thinned.size() > max) {
            Logging.warn("Bir şekil " + thinned.size() + " nokta üretti ancak " + max + " ile sınırlandırıldı.");
            thinned = new ArrayList<>(thinned.subList(0, max));
        }

        return new ParticleShape(Collections.unmodifiableList(thinned));
    }

    // Şeklin merkez noktaya göre partikül gösterilecek noktalar.
    // ParticleEngine.render() bunları işler ve gerçek konuma çevirir.
    public List<Vector> points() {
        return points;
    }

    /** Şeklin içerdiği nokta sayısını döndürür. */
    public int size() {
        return points.size();
    }
}
