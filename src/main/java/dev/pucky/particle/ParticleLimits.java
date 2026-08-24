package dev.pucky.particle;


import dev.pucky.ParticleEngineLibrary;
import org.bukkit.configuration.ConfigurationSection;

/**
 * config.yml particles bölümünden okunan, partikül motorunun tüm ayarlanabilir
 * limitlerinin değiştirilemeyen bir tanımıdır. Sunucu açıldığında tek seferlik bir
 * okuma yapılır. Limitlerin değiştirilmesi için sunucunun tekrar başlatılması gerekir.
 *
 * @param enabled                 Motorun düğmesi gibi düşünebiliriz.
 *                                false ise submit hep null döner ve render taskları gerçekleştirilmez.
 *
 * @param tickIntervalTicks       Merkezi particle task'ının çalışma aralığı (tick cinsinden). Partikül FPS'i gibi.
 *
 * @param viewDistance            Sıraya alınan bir efektin kendi izleyici (viewers) listesini belirtmediğinde
 *                                otomatik olarak izleyici araması yapılacak yarıçap.
 *
 * @param globalMaxPointsPerTick  Merkezi particle task'ı ticki başına render edilebilecek maksimum nokta.
 *
 * @param maxActiveEffects        Merkezi particle task'ında aynı anda render edilebilecek maksimum efekt.
 *
 * @param maxEffectsPerPlayer     Bir oyuncu için aynı anda aktif olabilecek maksimum efekt.
 *
 * @param maxPointsPerShape       Bir ParticleShape'in (efektin) barındırabileceği maksimum nokta.
 *                                (Oluşturma anında uygulanır)
 *
 * @param minSpacing              capped() içinde, birbirine bu mesafeden daha yakın ardışık
 *                                noktaların seyreltilmesi için kullanılan eşik.
 *
 * @param maxDurationTicks        Bir particle efektinin ayakta durabileceği maksimum süre.
 *                                Motorda sıraya giren efektlerin daha fazla süre istemesi önemsenmez.
 *
 * @param tpsThrottleThreshold    Render optimizasyonu (nokta yoğunluğunu yarısı oranında azaltma)
 *                                için TPS'in kaç olması gerektiği. Bu sınırın altına düşülürse
 *                                şekil bozulmadan render optimizasyonu yapılır.
 *
 * @param tpsPauseThreshold       TPS bu değerin altına düşerse render tamamıyla durur. Sadece
 *                                süre/animasyon ilerletilir ve hiç yeni partikül gönderilmez
 */
public record ParticleLimits(
        boolean enabled,
        int tickIntervalTicks,
        double viewDistance,
        int globalMaxPointsPerTick,
        int maxActiveEffects,
        int maxEffectsPerPlayer,
        int maxPointsPerShape,
        double minSpacing,
        int maxDurationTicks,
        double tpsThrottleThreshold,
        double tpsPauseThreshold
) {

    // config.yml'de "particles" bölümü yoksa veya eksikse kullanılabilecek güvenli değerler
    static final ParticleLimits DEFAULTS = new ParticleLimits(
            true,
            2,
            48.0,
            1500,
            64,
            30,
            400,
            0.15,
            20 * 30,
            18.0,
            14.0
    );

    private static ParticleLimits current = DEFAULTS;

    /**
     * Konfigürasyon dosyasından okunan "particle" bölümünün kullanılabilmesi için bir yükleme fonksiyonu.
     * Hiç yükleme yapılmazsa varsayılan olarak partikül limitleri güvenli sınırlara ayarlanır.
     * Ayarların kendi başlarına eksik olma durumunda da ilgili güvenli sınıra çekilirler.
     * Ayrıca bazı değerler bilinçsiz düzenlemeler sonucunda sunucuyu zora sokabileceği için ayrı bir sınıra sokulurlar.
     * tick-interval için minimum 1, max-active-effects için 1 gibi.
     */
    public static void load() {
        ConfigurationSection section = ParticleEngineLibrary.instance.getConfig().getConfigurationSection("particles");

        if (section == null) {
            current = DEFAULTS;
            return;
        }

        current = new ParticleLimits(
                section.getBoolean("enabled", DEFAULTS.enabled()),
                Math.max(1, section.getInt("tick-interval", DEFAULTS.tickIntervalTicks())),
                Math.max(1.0, section.getDouble("view-distance", DEFAULTS.viewDistance())),
                Math.max(1, section.getInt("global-max-points-per-tick", DEFAULTS.globalMaxPointsPerTick())),
                Math.max(1, section.getInt("max-active-effects", DEFAULTS.maxActiveEffects())),
                Math.max(1, section.getInt("max-effects-per-player", DEFAULTS.maxEffectsPerPlayer())),
                Math.max(1, section.getInt("max-points-per-shape", DEFAULTS.maxPointsPerShape())),
                Math.max(0.0, section.getDouble("min-spacing", DEFAULTS.minSpacing())),
                Math.max(20, section.getInt("max-duration-seconds", DEFAULTS.maxDurationTicks() / 20) * 20),
                Math.clamp(section.getDouble("tps-throttle-threshold", DEFAULTS.tpsThrottleThreshold()), 0, 20.0),
                Math.clamp(section.getDouble("tps-pause-threshold", DEFAULTS.tpsPauseThreshold()), 0, 20.0)
        );
    }

    /** Aktif olarak kullanılan partikül limiti değerleri. */
    public static ParticleLimits get() {
        return current;
    }
}
