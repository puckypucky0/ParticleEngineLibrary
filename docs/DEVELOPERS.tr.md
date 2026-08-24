# Geliştirici dokümantasyonu

[English](DEVELOPERS.en.md) · **Türkçe** · [README'ye dön](../README.md)

---

## İçindekiler

- [Kurulum](#kurulum)
- [Şekiller](#şekiller)
- [Efekt seçenekleri](#efekt-seçenekleri)
- [Renk geçişleri](#renk-geçişleri)
- [Yapılandırma](#yapılandırma)
- [Tuzaklar](#tuzaklar)
- [Örnekler](#örnekler)

---

## Kurulum

### 1. Bağımlılığı ekle

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
> Scope'un `provided` / `compileOnly` olması bilinçli olarak yapılmıştır.
> Motor kendi eklentisi olarak çalışır. Bu motoru kullanacağınız bir projede bu jar'ı gömmek,
> birbirinden izole iki farklı kopya oluşturur.

### 2. plugin.yml'de bildir

```yaml
depend: [ParticleEngineLibrary]
```

Bunu yazmazsanız yükleme sırası garanti değildir ve açılışta `NoClassDefFoundError`
alabilirsiniz.

### 3. Bir efekt göndermek

```java
import dev.pucky.particle.*;

ParticleEffect effect = ParticleEffect.builder()
        .shape(Shapes.circle(1.5, 48, Shapes.Axis.Y))
        .origin(player::getLocation)          // her tick lokasyon yeniden alınır, bu yüzden takip eder
        .gradient(Gradient.of("#00ffc8", "#0066ff", "#00ffc8")) // turkuaz mavi turkuaz gradyan
        .durationTicks(20 * 10)               // on saniye
        .rotationSpeed(0.05)
        .animateGradient(true)
        .owner(player)
        .build();

ParticleEngine.Handle handle = ParticleEngine.submit(effect);
```

Erken durdurmak için `ParticleEngine.cancel(handle)`, bir oyuncunun tüm efektlerini
temizlemek için `ParticleEngine.cancelAll(player)` — çıkış event'inde işine yarar.

| Tip | Görevi |
|---|---|
| `ParticleShape` | Hangi noktaların çizileceğini belirtir (origin'e göre offset olarak). Yalnızca `Shapes` üzerinden üretilir. |
| `ParticleEffect` | Tek bir efektin değişmez tanımı. İki kez submit edersen iki bağımsız çalıştırma olur. |
| `ParticleEngine` | Tek merkezi task, bütçe, limitler ve asıl efekt gösterimini üstlenen kısım. |

Motor her tick'te sunucu TPS'ini okuyor, render'ı atlayıp atlamayacağına ya da
seyreltip seyreltmeyeceğine karar veriyor, aktif efektleri kaydırılan bir sırayla
geziyor, sürelerini ilerletiyor ve ortak nokta bütçesi bitene kadar harcıyor.
Kaydırma, bütçe darken hep aynı efektlerin aç kalmasını engelleyen şey.

> [!NOTE]
> Kaydırılan sırayla gösterme, efektlerin motora gönderilme sırasının önemsiz kılınmasını,
> dolayısıyla ilk eklenen efektin sunucu geneli tüm nokta bütçesi'ni tüketip sıradaki
> efektlerin optimizasyon sınırlarına takılmasını önler.

## Şekiller

Tek bir dünya lokasyonu vardır, bu da şeklin merkezi olur.
Şekil seçenekleri de o tek noktaya göre offset döndürür.

| Fabrika | Ürettiği |
|---|---|
| `point()` | Tek bir noktada partikül |
| `line(from, to, spacing)` | Düz çizgi, `from`'a göre |
| `circle(radius, points, axis)` | `axis`'e dik düzlemde bir halka |
| `sphere(radius, points)` | İçi boş küre, noktalar altın açı spiraliyle dağıtılır |
| `box(size, spacing)` | Origin'de merkezlenmiş bir kutunun on iki kenarı |
| `blockHighlight()` | Tek bir blok için hafif şişirilmiş kafes (önbelleklenir) |
| `chunkHighlight(minY, maxY)` | Dört dikey chunk kenarı artı üst ve alt çerçeve |
| `helix(radius, height, turns, points)` | Y ekseni boyunca yükselen sarmal |
| `filledSquare(size, pointsPerSide, axis)` | Izgara ile doldurulmuş kare |
| `pointCloud(radius, count)` | Küreyi dolduran rastgele noktalar, hacme göre düzgün |

`Shapes.Axis` değeri `X`, `Y` veya `Z`; düz şekillerin hangi düzlemde duracağını seçer.

> [!NOTE]
> `ParticleShape` üretmenin tek desteklenen yolu `Shapes`. Bu bilinçli: her şeklin aynı
> seyreltme ve nokta sayısı limitlerinden geçmesini garanti ediyor. Optimizasyon aşılamıyor.

## Efekt seçenekleri

> [!NOTE]
> Bazı seçenekler yalnızca DUST partikül tipine özeldir. [D] ile işaretlenmiştir.

| Seçenek | Tip | Varsayılan | Anlamı |
|---|---|---|---|
| `shape` | `ParticleShape` | zorunlu | Ne çizilecek |
| `origin` | `Supplier<Location>` | zorunlu | Nerede çizilecek, her render tick'inde yeniden alınır. |
| `particle` | `Particle` | `DUST` | Vanilla partikül tipi |
| `gradient` | `Gradient` | düz beyaz | [D] Şekil boyunca renk |
| `size` | `float` | `1.0` | [D] Dust boyutu |
| `durationTicks` | `int` | `100` | İstenen süre, gerekirse sunucu tarafında optimize edilir. |
| `rotationSpeed` | `double` | `0` | Dikey eksen etrafında tick başına radyan |
| `pulse` | `boolean` | `false` | [D] Dust boyutunu zamanla salındırır |
| `animateGradient` | `boolean` | `false` | [D] Renkleri şekil boyunca kaydırır |
| `viewers` | `Collection<Player>` | `null` | Açık izleyici listesi; `null` ise mesafeye göre bulunur |
| `owner` | `Player` | `null` | Kota sahibi ve `cancelAll` hedefi |

## Renk geçişleri

```java
Gradient.solid(Color.RED); // Tek renkli gradyan. Aslında sadece kırmızı renk.
Gradient.of("#F72585", "#7209B7", "#3A0CA3"); // Pembe - mor - mavimsi gradyan.
Gradient.of(Color.RED, Color.BLUE); // Kırmızıdan maviye gradyan. Daha basit.
```

Renkler duraklar arasında `lineer interpolasyon` ile hesaplanıyor. Şekildeki her nokta 0 ile 1
arasında bir `t` konumu alıp gradyandan oradaki rengi soruyor; bu sayede aynı gradyan
beş noktalı da dört yüz noktalı da olsa çalışıyor.

> [!TIP]
> Lineer interpolasyon, iki değer arasında düz bir çizgi üzerinde ara değer bulma yöntemidir.
> A + (B - A) × t

> [!TIP]
> `animateGradient` açıkken renk konumu 1'den 0'a sarar.
> Sürekli bir döngü için ilk rengi sona tekrarlayın.
> `"#ff0055", "#ffaa00", "#ff0055"`.

## Yapılandırma

`plugins/ParticleEngineLibrary/config.yml`. Bütün anahtarlar opsiyoneldir
ancak eksik olanlar jar içine gömülmüş ve güvenli varsayılanlardan gelir.

| Anahtar | Varsayılan | Etkisi |
|---|---|---|
| `enabled` | `true` | False ise `submit()` null döner ve hiçbir şey çizilmez. |
| `tick-interval` | `2` | Merkezi task'ın periyodu (tick). Düşürmek akıcılığı ve maliyeti artırır. |
| `view-distance` | `48` | Otomatik izleyici araması için yarıçap (blok). |
| `global-max-points-per-tick` | `1500` | Sunucu genelindeki tüm efektlerin o tick'de gösterebileceği toplam nokta |
| `max-active-effects` | `64` | Sunucu genelinde eşzamanlı efekt sınırı. |
| `max-effects-per-player` | `30` | Oyuncu başına eşzamanlı efekt sınırı. |
| `max-points-per-shape` | `400` | Şekil üretilirken uygulanan sert tavan. |
| `min-spacing` | `0.15` | Son tutulan noktaya bundan yakın noktalar elenir. |
| `max-duration-seconds` | `30` | Efekt ömrü tavanı, her zaman zorlanır. |
| `tps-throttle-threshold` | `18.0` | TPS bunun altına düşünce nokta yoğunluğu yarıya iner. |
| `tps-pause-threshold` | `14.0` | TPS bunun altına düşünce render durur, fakat efektlerin süreleri hala işler (advance). |

> [!NOTE]
> Limitler, eklenti başlatılırken bir kez okunur. Limitleri değiştirmek için sunucuyu
> yeniden başlatmanız gerekir.

## Tuzaklar

> [!IMPORTANT]
> **Şekil noktaları offset'tir, dünya koordinatı değil.** Motor her tick'te bunları
> origin'e ekliyor. Aynı şeklin herhangi bir konumda yeniden kullanılabilmesini
> sağlayan kısım budur.

> [!WARNING]
> **`owner` ile `viewers` alakalı değildir.** `owner`, efektin kimin kotasına sayılacağını ve
> `cancelAll`'ın kime ulaşacağını belirler, `viewers` ise efektleri kimin alacağını
> belirler. `viewers` verdiğinde mesafe süzmesi tamamen devre dışı kalır, o oyuncular
> ne kadar uzaklaşırsa uzaklaşsın efekti almaya devam eder.

> [!CAUTION]
> **Limitler sessizce uygulanıyor.** Sınıra ulaşıldığında yeni efekt ekleyen 
> `submit()` metodu `null` döner, yoğun şekiller seyreltilip kırpılır, süreler kısaltılır.
> Konsola uyarı düşer ama efekt yalnızca `null` bir `handle` alır.

> [!TIP]
> **`pulse` efektinin görünürlüğü.** Dust partikülleri doğdukları boyutta kaldıkları 
> ve pulse efektinin bir evresi kadar görünür oldukları için sabit bir noktadaki
> pulse efektinin görünür olması zorlaşır. Ortalamadan büyük gösterilen partiküller
> küçük gösterilen partikülleri ezerek efekti geçersiz kılabilir. Hareketli origin 
> tavsiye edilir.

> [!NOTE]
> **Dönme yalnızca dikey eksende.** Düz bir halka plak gibi döner, takla atamaz. Dönme
> ayrıca `sphere` gibi simetrik şekillerde anlamsız, `blockHighlight` ve
> `chunkHighlight`'ta ise yanlıştır. Onların dünya ızgarasıyla hizalı kalması gerekir.

## Örnekler

[`ParticleExamples`](../src/main/java/dev/pucky/particle/ParticleExamples.java) içinde
kopyalayabileceğiniz sekiz çalışan ve belgelenmiş efektler bulunuyor:

| Metot | Gösterdiği |
|---|---|
| `highlightBlock` | Blok üzerinde sabit kafes |
| `highlightChunk` | İki yükseklik arasında chunk sınırı (problemli) |
| `gradientCircle` | Halka üzerinde akan gradyan, sabit ya da takip eden |
| `followingHalo` | Oyuncuyu izleyen supplier tabanlı origin |
| `animatedHelix` | Dönme ve gradyan animasyonu bir arada |
| `sparkleCloud` | Rastgele hacim doldurma |
| `pulseTrail` | Hareketli origin ile görünür kılınmış pulse efekti |
| `gradientLine` | İki dünya konumu arasında çizgi |

## Uyumluluk

Paper 1.21.4 üzerinde derlendi ve test edildi. Eski Paper API'sine derlenmiş
eklentiler yeni sunucularda çalıştığı için bu kütüphane bilinçli olarak en son sürümü
değil 1.21'i hedefliyor.

PuckyPucky
