package dev.pucky.particle;


import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Bir partikül efektinin dışarıdan değiştirilemeyen tanımı. Render edilecek efekti tanımlar.
 * <p>
 * Lombok @Builder ile inşa edilir ve {@code ParticleEngine.submit(ParticleEffect)} ile motorda
 * sıraya konur. Bu tanım yalnızca render edilecek efekti belirler. Efektin aktif olarak ne durumda
 * olduğunu {@link ActiveEffect} tutar.
 * */
@Builder @Getter @Accessors(fluent = true)
public class ParticleEffect {

    /** Render edilecek nokta listesi (origin'e göre offset). Sadece {@link Shapes} ile üretilir. */
    private ParticleShape shape;

    /**
     * Şeklin merkezinin dünya üzerindeki konumunu temsil eder. Sabit bir Location değeri değildir.
     * Bazı efektlerin konumunun hareketli olması gerekebilir, dolayısıyla sabit bir Location yerine
     * {@code Supplier} kullanılır. Her render tick'inde Supplier ile location verilir.
     * */
    private Supplier<Location> origin;

    /**
     * Hangi vanilla partikül tipinin spawn edileceği. Varsayılan DUST'tır çünkü DUST,
     * renk + boyutu destekleyen tek tiptir.
     */
    @Builder.Default
    private Particle particle = Particle.DUST;

    /** Şekil boyunca uygulanacak renk geçişi. Varsayılan beyaz tek renk. */
    @Builder.Default
    private Gradient gradient = Gradient.solid(Color.WHITE);

    /** DUST partikülünün boyutu; {@code pulse} açıksa render() bunu zamanla ölçekler. */
    @Builder.Default
    private float size = 1.0f;

    /**
     * İstenen yaşam süresi (tick). Bağlayıcı değildir. Gerçek ömür her zaman
     * ParticleLimits.maxDurationTicks() ile sunucu tarafında ek olarak sınırlanır
     * (ActiveEffect.expired()), bu yüzden çok büyük bir değer versen bile motor
     * onu otomatik kırpar.
     */
    @Builder.Default
    private int durationTicks = 20 * 5;

    /** Tick başına radyan cinsinden dönüş hızı; 0 ise şekil hiç dönmez.
     *  ActiveEffect.rotationAngle() bu değeri kullanır. */
    @Builder.Default
    private double rotationSpeed = 0;

    /** Açıksa render() boyutu zamanla büyütüp küçültür. */
    @Builder.Default
    private boolean pulse = false;

    /** Açıksa gradyanın şekil üzerindeki konumuna zamanla kayan bir offset eklenir (hareketli renk geçişi). */
    @Builder.Default
    private boolean animateGradient = false;

    /**
     * Bu efektin oluşturduğu partikülleri kimin göreceğini belirler. null bırakılırsa
     * ParticleEngine, her render tick'inde merkez nokta etrafında ParticleLimits.viewDistance()
     * yarıçapında bir bölgede oyuncuları otomatik bulur. Belirli bir oyuncu listesi verilirse
     * yalnızca o oyuncular görür.
     * */
    @Builder.Default
    private Collection<Player> viewers = null;

    /**
     * Bu efektin oluşturulmasından sorumlu oyuncu. Bir oyuncu verilirse
     * {@code ParticleLimits.maxEffectsPerPlayer()} limitine sayılır ve
     * {@code ParticleEngine.cancelAll(Player)} ile tek seferde temizlenebilir.
     * */
    @Builder.Default
    private Player owner = null;
}
