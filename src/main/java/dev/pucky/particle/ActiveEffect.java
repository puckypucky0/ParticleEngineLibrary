package dev.pucky.particle;

/**
 * {@link ParticleEffect} objesinin motor içindeki değişen halidir.
 * <p>
 * ParticleEffect'in şuan sadece render edilecek şeyi tanımlıyor. ActiveEffect, gerçekten aktif bir halini tutar.
 * Yani nerede ne durumda'yı tutar.
 */
public final class ActiveEffect {

    private final ParticleEffect effect;

    // rotationAngle: effect.rotationSpeed() ile her advance()'ta birikir; render() bunu
    // şeklin tüm noktalarına aynı anda uygulayarak dönme hissi verir.
    private double rotationAngle = 0;

    // ticksAlive: bu efekt motora submit edildiğinden beri geçen toplam tick sayısı.
    // Hem süre dolumunu (expired()) hem de pulse / gradient animasyon aşamasını belirler.
    private int ticksAlive = 0;

    ActiveEffect(ParticleEffect effect) {
        this.effect = effect;
    }

    ParticleEffect effect() {
        return effect;
    }

    double rotationAngle() {
        return rotationAngle;
    }

    int ticksAlive() {
        return ticksAlive;
    }

    boolean expired() {
        // Süre limiti çağıranın isteğine bakılmaksızın her zaman sunucu tarafında zorlanır:
        // bir çağıran durationTicks(100000) gibi yüksek bir değer verilse bile gerçek ömür
        // burada ParticleLimits.maxDurationTicks() ile kırpılır.
        return ticksAlive >= Math.min(effect.durationTicks(), ParticleLimits.get().maxDurationTicks());
    }

    void advance(int tickDelta) {
        // tickDelta = ParticleLimits.tickIntervalTicks(): motor her oyun tick'inde değil,
        // her motor task'ı tickinde (örn. 2 oyun tick'i) bir kez çağrılır; bu yüzden zaman burada
        // gerçek oyun tick'i biriminde tutulur, task tick'i sayısında değil.
        ticksAlive += tickDelta;
        rotationAngle += effect.rotationSpeed() * tickDelta;
    }
}
