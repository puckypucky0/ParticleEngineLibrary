package dev.pucky.particle;

import dev.pucky.ParticleEngineLibrary;
import dev.pucky.util.log.Logging;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bütün particle sistemi, sıraya alınan efektlerin başlatılması ve yönetilmesi
 * bu sınıf üzerinden yürütülür.
 * <p>
 * Sunucu açık olduğu ve {@link ParticleEngine} açık olduğu sürece çalışan tek bir
 * {@code runTaskTimer} vardır. Her bir efekt için ayrı {@code runTaskTimer} vermek,
 * efekt sayısı arttıkça scheduler yükünün şişmesine sebep olacağı için tercih edilmez.
 * Tek bir merkezi {@code runTaskTimer} kullanmak, scheduler yükünü ve optimizasyon kontrolünü
 * tek {@code runTaskTimer}'da toplar.
 * <p>
 * {@link #init()} fonksiyonu {@code onEnable()} da çağırılır ve önce {@code config.yml}'den
 * {@link ParticleLimits} okunur ve merkezi task başlatılır.
 * <p>
 * {@link #shutdown()} fonksiyonu da {@code onDisable()} da çağırılır ve
 * merkezi task'ı durdurduktan sonra bütün aktif efektleri temizler.
 * <p>
 * Her bir merkezi task tick'inde {@link #tick()} çalıştırılır.
 * <p>
 * Bu metodda önce sunucu TPS'i kontrol edilir ve duruma göre optimizasyon amacıyla
 * render aşamaları tamamen atlanabilir veya nokta yoğunluğu yarıya indirilebilir.
 * <p>
 * {@link #rotatedOrder()} ile kaydırılan sırada her aktif efektin süre kontrolü yapılır.
 * Eğer süresi dolmamışsa {@link #render(ActiveEffect, int, int)} ile gösterilir.
 *
 * @see ParticleEffect  Bir efektin değiştirilemeyen tanımı
 * @see ActiveEffect    Bir efektin değişen (dinamik) aktif tanımı
 * @see ParticleLimits  Tüm sayısal limitlerin config.yml'den okunan anlık görüntüsü.
 * @see Shapes          Şekil üretmenin tek onaylanmış yolu.
 */
public final class ParticleEngine {

    // Tüm aktif efektlerin kaynağı
    private static final Map<UUID, ActiveEffect> active = new ConcurrentHashMap<>();

    private static BukkitTask task;

    // Her merkezi particle task'ı tickinde 1 artar.
    // rotatedOrder(), bu değere göre adil bir sıralama üretir.
    private static int tickRotationOffset = 0;

    private ParticleEngine() {}

    /**
     * ParticleEngine'i başlatır: ParticleLimits'i config.yml'den okur, varsa eski merkezi task'ı durdurur,
     * ParticleLimits.enabled() false ise hiçbir şey başlatmadan durur, eğer true ise merkezi task'ı başlatır.
     * onEnable()'da, ParticleEngine'i kullanacak modüllerden önce çağrılmalıdır.
     */
    public static void init() {
        ParticleLimits.load();
        Shapes.clearCaches();
        ParticleLimits limits = ParticleLimits.get();

        if (task != null) {
            task.cancel();
            task = null;
        }

        if (!limits.enabled()) {
            Logging.inform("Partikül sistemi config.yml üzerinden devre dışı bırakıldı.");
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(ParticleEngineLibrary.instance, ParticleEngine::tick, 0L, limits.tickIntervalTicks());
        Logging.success("Partikül motoru başlatıldı. (tick aralığı: " + limits.tickIntervalTicks() + ")");
    }

    /**
     * Merkezi task'ı durdurur ve tüm aktif efektleri şekil veya süre farketmeksizin
     * anında siler. onDisable()'da, eklenti tamamen kapanmadan önce çağrılır.
     */
    public static void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        active.clear();
        Logging.inform("Partikül motoru durduruldu.");
    }

    /**
     * Bir {@link ParticleEffect}'i sunucuda gösterilmek üzere sıraya koyar.
     * Sıraya koyma aşamasında bazı kontroller vardır.
     * <p>
     * Eğer {@link ParticleLimits} {@code enabled} = false ise null döner.
     * <p>
     * Eğer {@link ParticleLimits} {@code maxActiveEffects} sınırı bu efekt ile aşılacaksa reddedilir.
     * <p>
     * Eğer bir owner (kotaya sayılacak oyuncu) verilmişse ve o owner'ın
     * {@link ParticleLimits} {@code maxEffectsPerPlayer} limiti dolmuşsa reddedilir.
     * <p>
     * Eğer süre limiti aşılırsa reddedilmez, yalnızca konsola bildirme yapılır.
     * Çünkü bu kontrol, {@code ActiveEffect.expired()}'ın görevidir.
     * <p>
     * Eğer bütün kontroller başarıyla tamamlanırsa, bu {@link ParticleEffect} sonraki tick'de gösterilmeye başlanmak üzere
     * sıraya koyulur ve efektin sonradan kontrolü için bir {@link Handle} döndürür.
     * @return Efektin sonradan kontrol edilebilmesi için dışarıya kapalı bir {@link Handle}
     * */
    public static Handle submit(ParticleEffect effect) {
        ParticleLimits limits = ParticleLimits.get();

        if (!limits.enabled())
            return null;

        if (active.size() >= limits.maxActiveEffects()) {
            Logging.warn("Sunucu genelinde aktif efekt limiti (" + limits.maxActiveEffects() + ") doldu, yeni efekt reddedildi.");
            return null;
        }

        Player owner = effect.owner();
        if (owner != null) {
            long ownerCount = active.values().stream()
                    .filter(a -> owner.equals(a.effect().owner()))
                    .count();

            if (ownerCount >= limits.maxEffectsPerPlayer()) {
                Logging.warn(owner.getName() + " isimli oyuncunun aktif efekt limiti (" + limits.maxEffectsPerPlayer() + ") doldu.");
                return null;
            }
        }

        if (effect.durationTicks() > limits.maxDurationTicks()) {
            Logging.warn("İstenen efekt süresi (" + effect.durationTicks() + " tick) sunucu limitini (" + limits.maxDurationTicks() + " tick) aşıyor, otomatik sınırlandırılacak.");
        }

        UUID id = UUID.randomUUID();
        active.put(id, new ActiveEffect(effect));
        return new Handle(id);
    }

    /** Belirli bir efekti, tick bazında uzunluğundan bağımsız olarak anında yok eder. */
    public static void cancel(Handle handle) {
        if (handle != null)
            active.remove(handle.id);
    }

    /**
     * Belirtilen oyuncunun owner olarak tanımlandığı bütün efektleri kaldırır.
     */
    public static void cancelAll(Player player) {
        active.values().removeIf(a -> player.equals(a.effect().owner()));
    }

    /**
     * Her {@code tickIntervalTicks} oyun tick'inde bir çalıştırılan olaylar.
     * Hiç bir efekt sırada değilse anında döner, en az bir efekt varsa mevcut TPS'i baz alarak bir karar verir.
     * Eğer TPS, {@code tpsPauseThreshold}'un altındaysa, bütün render olayları atlanır, yalnızca efektlerin süreleri ilerletilir.
     * Eğer TPS, {@code tpsThrottleThreshold}'un altında ama {@code tpsPauseThreshold}'un üstündeyse, efektler renderlanır
     * ancak nokta yoğunluğu yarı yarıya azaltılır.
     * <p>
     * Ardından {@link #rotatedOrder()} ile adil bir sırayla her efekt sıra sıra ilerletilir,
     * süresi dolanlar silinir, süresi dolmayanlar sunucu geneli nokta bütçesi {@code globalMaxPointsPerTick}
     * tükenene kadar {@link #render(ActiveEffect, int, int)} ile gerçek partiküllere çevrilir.
     * */
    private static void tick() {
        if (active.isEmpty())
            return;

        ParticleLimits limits = ParticleLimits.get();
        double tps = Bukkit.getTPS()[0];

        if (tps < limits.tpsPauseThreshold()) {
            advanceAndExpireOnly(limits);
            return;
        }

        int pointStep = tps < limits.tpsThrottleThreshold() ? 2 : 1;
        int remainingBudget = limits.globalMaxPointsPerTick();

        for (UUID id : rotatedOrder()) {
            ActiveEffect activeEffect = active.get(id);
            if (activeEffect == null)
                continue;

            activeEffect.advance(limits.tickIntervalTicks());

            if (activeEffect.expired()) {
                active.remove(id);
                continue;
            }

            if (remainingBudget <= 0)
                continue;

            remainingBudget = render(activeEffect, pointStep, remainingBudget);
        }

        tickRotationOffset++;
    }

    /**
     * TPS pause eşiğinin altındayken ve renderlar atlanmışken bile sıradaki efektlerin
     * ticksAlive değerlerinin ilerletilmesini sağlar. Süresi dolanları da siler.
     * TPS normale dönene kadar donmuş bir şekilde kalmaların önüne geçilmiş olur.
     * */
    private static void advanceAndExpireOnly(ParticleLimits limits) {
        for (Iterator<Map.Entry<UUID, ActiveEffect>> it = active.entrySet().iterator(); it.hasNext(); ) {
            ActiveEffect activeEffect = it.next().getValue();
            activeEffect.advance(limits.tickIntervalTicks());
            if (activeEffect.expired())
                it.remove();
        }
    }
    /**
     * {@code active} map'inde saklanan sıraya koyulmuş efektleri {@code tickRotationOffset}'e göre
     * kaydırılmış bir sırada döndürür. Her tick'de sabit sırayla gidilseydi efektlerin
     * eklendikleri sıraya göre öncelikleri olurdu. Mesela sırayla A, B ve C efektleri kaydedilsin.
     * A efekti sıradaki diğer efektlere o tick'de bütçe kalmayacağı için diğer efektler yüklenmez,
     * bütçeden faydalanamazlardı. Bu yüzden efektler, tick başına
     * kaydırılıyor. [A,B,C] [B,C,A] [C,A,B] gibi bir sırayla ilerler.
     * */
    private static List<UUID> rotatedOrder() {
        List<UUID> keys = new ArrayList<>(active.keySet());
        if (keys.isEmpty())
            return keys;

        int offset = tickRotationOffset % keys.size();
        if (offset == 0)
            return keys;

        List<UUID> rotated = new ArrayList<>(keys.size());
        rotated.addAll(keys.subList(offset, keys.size()));
        rotated.addAll(keys.subList(0, offset));
        return rotated;
    }

    /**
     * Tek bir efektin o tick'deki asıl görsel çıktısını üreten kapsamlı metod.
     * Geniş bir kapsama sahip olması nedeniyle adımlar tek tek açıklanacaktır.
     * <p>
     * Öncelikle bu efektin oluşturulacağı lokasyon, viewer'ları (yani görüntüleyicileri) ve noktaları
     * kontrol edilir. Eğer lokasyon geçersizse, viewerlar boşsa veya hiç nokta yoksa bu efekt
     * o tick için sessizce atlanır.
     * <p>
     * {@code effect.pulse()} özelliğinin açık olup olmadığı kontrol edilir.
     * Eğer açıksa 0.30 oranında bir büyüme / küçülme uygulanır.
     * <p>
     * {@code effect.animateGradient()} özelliğinin açık olup olmadığı kontrol edilir.
     * Eğer açıksa önce {@code gradientShift} adında, 0 ve 0.99 arasında tutulan ve gradyan
     * animasyonunun ilerlemesini kontrol eden bir değer tutulur.
     * <p>
     * Ardından noktaların tek tek oluşturulduğu for döngüsü başlatılır.
     * remainingBudget sunucu genelince tek tick'de oluşturulabilecek nokta sayısı anlamına gelir.
     * Bu nokta limiti aşılmadıkça ve noktalar tükenmedikçe bu döngü devam eder.
     * */
    private static int render(ActiveEffect activeEffect, int pointStep, int remainingBudget) {
        ParticleEffect effect = activeEffect.effect();

        Location origin = effect.origin().get();
        if (origin == null || origin.getWorld() == null)
            return remainingBudget;

        Collection<Player> viewers = resolveViewers(effect, origin);
        if (viewers.isEmpty())
            return remainingBudget;

        List<Vector> points = effect.shape().points();
        if (points.isEmpty())
            return remainingBudget;

        double angle = activeEffect.rotationAngle();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        // pulse açıksa boyut, ticksAlive'a bağlı olarak %30 salınır.
        float size = effect.pulse()
                ? effect.size() * (float) (1 + 0.3 * Math.sin(activeEffect.ticksAlive() * 0.2))
                : effect.size();

        // animateGradient açıksa, gradyanın şekil üzerindeki konumuna (t) zamanla 0 - 1 arası
        // döngüsel bir kayma eklenir; bu, renk geçişinin şekil boyunca hareketliymiş gibi görünmesini sağlar.
        double gradientShift = effect.animateGradient()
                ? (activeEffect.ticksAlive() % 100) / 100.0
                : 0.0;

        for (int i = 0; i < points.size() && remainingBudget > 0; i += pointStep) {
            Vector point = points.get(i);

            // Noktayı origin'in Y ekseni etrafında döndürür.
            double x = point.getX() * cos - point.getZ() * sin;
            double z = point.getX() * sin + point.getZ() * cos;

            Location location = origin.clone().add(x, point.getY(), z);

            double t = points.size() == 1 ? 0.0 : (i / (double) (points.size() - 1) + gradientShift) % 1.0;
            Color color = effect.gradient().at(t);

            spawn(effect, viewers, location, color, size);
            remainingBudget--;
        }

        return remainingBudget;
    }

    /**
     * Bu efekti kimlerin göreceğini çözümler. Bir efektin viewer'larını bulmanın iki yolu vardır.
     * Biri efektin kendi viewerlarını tanımlaması, diğeri ise kendi viewerlarını tanımlamaması halinde
     * o efektin merkezinden sabit bir yarıçapta viewer aranmasıdır. {@code resolveViewers}'da tam olarak hangisinin
     * uygun olacağına karar verip onu döner. Yani viewer listesi verildiyse onu döner, verilmediyse o sabit
     * yarıçapta oyuncu arayıp bulduğu oyuncuları döner.
     * */
    private static Collection<Player> resolveViewers(ParticleEffect effect, Location origin) {
        if (effect.viewers() != null)
            return effect.viewers();

        return origin.getWorld().getNearbyPlayers(origin, ParticleLimits.get().viewDistance());
    }

    /**
     * {@link #render(ActiveEffect, int, int)} metodunun partikülleri ortaya çıkartan asıl yardımcısıdır.
     * Tek bir partikülün hesaplanmış renk ve boyutla verilen bütün izleyicilere gönderilmesini sağlar.
     * DUST tipi, içine renk ve boyut da verilebileceği için özel olarak ele alınır, diğer tipler direkt gösterilir.
     * */
    private static void spawn(ParticleEffect effect, Collection<Player> viewers, Location location, Color color, float size) {
        if (effect.particle() == Particle.DUST) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, size);
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, location, 1, 0, 0, 0, 0, dustOptions);
            }
        } else {
            for (Player viewer : viewers) {
                viewer.spawnParticle(effect.particle(), location, 1);
            }
        }
    }

    /**
     * Bir {@link #submit(ParticleEffect)} çağrısından sonra döndürülen ve gösterilmek üzere
     * sıraya koyulan partikül efektini tanımlayan referanstır. Bu referansın bilinçsiz yapılandırmalar
     * sonucunda sonradan değiştirilmesinin engellenmesi amacıyla id değeri private olarak tutulur.
     * {@link #cancel(Handle)} metodu ile efektin sonradan elle durdurulabilmesini sağlar.
     * */
    public static final class Handle {
        private final UUID id;

        private Handle(UUID id) {
            this.id = id;
        }
    }
}
