<div align="center">

# PARTICLE ENGINE LIBRARY

[![Release](https://img.shields.io/github/v/release/puckypucky0/ParticleEngineLibrary?sort=semver)](https://github.com/puckypucky0/ParticleEngineLibrary/releases)
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-brightgreen)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net)
[![License](https://img.shields.io/github/license/puckypucky0/ParticleEngineLibrary)](LICENSE)

[English](docs/README.en.md) · **Türkçe**


</div>

https://github.com/user-attachments/assets/7e5c0772-b409-4d90-8ad0-6bd98790bcb8

<sub>Bu gösterim videosu shader ve ReplayMod kullanılarak çekilmiştir.</sub>

---

## Ne işe yarar

Beşten fazla şekil seçeneği, gradyanlar ve animasyonlarla Bukkit API'ın partikül
kapasitesini tamamen optimize bir şekilde kullanmaya yönelik bir motor.

> [!CAUTION]
> Bu proje, oyun içi komutlara sahip bir eklenti değildir. Geliştiricilerin projelerinde kullanmaları için geliştirilmiş bir partikül kütüphanesidir.


- **Tek merkezi görev**: Motor, partikül efekti sayısı artsa bile yükün aynı kalması için tek merkezi görevden yönetilir.
- **TPS optimizasyonu**: `config.yml` üzerinden ayarlanabilir TPS optimizasyonu, efekt yoğunluğunun azalması ve bazı efektlerin tamamen atlanması gibi işlemler içerir.
- **Şalterler önünde**: `config.yml` aracılığıyla neredeyse her şeyi kontrol etmek mümkün.

## Galeri

<table>
  <tr>
    <td width="50%" align="center"><b>Tekli blok işaretleme</b><br><img src="https://github.com/user-attachments/assets/77cdaf55-1c11-4922-a1bb-4e1c9c0cc6ac" alt="Blok işaretleme"></td>
    <td width="50%" align="center"><b>Dönme ve renk geçişli sarmal</b><br><img src="https://github.com/user-attachments/assets/b9b2645c-817d-4b05-bb28-7625c0e06b7f" alt="Dönen sarmal"></td>
  </tr>
  <tr>
    <td align="center"><b>Yükselen halka</b><br><img src="https://github.com/user-attachments/assets/88d6f444-94ca-45b3-b35b-6a1b04965220" alt="Yükselen halka"></td>
    <td align="center"><b>Yıldız tozu bulutu</b><br><img src="https://github.com/user-attachments/assets/834a9d8b-5e2a-483e-b39e-24674a5caca5" alt="Bulut"></td>
  </tr>
</table>

## Kurulum

1. Jar dosyasını [Releases](https://github.com/puckypucky0/ParticleEngineLibrary/releases) sayfasından indir.
2. `plugins/` klasörüne at.
3. Sunucuyu yeniden başlat.

İlk açılışta açıklamalı bir `config.yml` oluşur. Başka bir şey gerekmiyor.

| | |
|---|---|
| Sunucu | Paper 1.21 veya üstü tavsiye edilir. |
| Java | 21 veya üstü |
| Spigot | Desteklenmiyor |

## Eklenti geliştiricileri için

Bir efekt tek bir builder çağrısı:

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

Tam API referansı ve şekil kataloğuyla alakalı detaylı bilgi için:
**[Geliştirici dokümantasyonu](docs/DEVELOPERS.tr.md)**'na bakın.

## Lisans

[LICENSE](LICENSE) dosyasına bak. Copyright (c) 2026 Pucky
