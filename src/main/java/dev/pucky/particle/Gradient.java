package dev.pucky.particle;

import org.bukkit.Color;

import java.util.List;


/**
 * Herhangi bir şeklin bütün noktaları boyunca renk geçişlerini tanımlayan gradyan sınıfı
 * <p>
 * stops listesi de o gradyanın renk duraklarıdır. Örneğin:
 * *** Kırmızı - Mavi gradyan ***
 * Kırmızı - Pembe - Mor - Pembe - Mavi (ara renkler önemsizdir)
 * Kırmızı ve Mavi ana renkler olarak stops listesinde tutulur.
 * t = 0 ilk renk, t = 1 son renktir.
 * t, animateGradient özelliği açıksa tick başına arttırılarak akış hissi verir.
 */
public final class Gradient {

    private final List<Color> stops;

    private Gradient(List<Color> stops) {
        this.stops = stops;
    }

    /** Tek bir renge sahip olan geçişsiz gradyan. Her t değeri için aynı rengi döner. */
    public static Gradient solid(Color color) {
        return new Gradient(List.of(color));
    }

    /** Verilen renkleri sırayla ana renk olarak kullanarak çok renkli bir gradyan oluşturur. */
    public static Gradient of(Color... colors) {
        if (colors.length == 0)
            throw new IllegalArgumentException("Gradient en az bir renk gerektirir.");

        return new Gradient(List.of(colors));
    }

    /** {@link #of(Color...)} ile aynı işlev ama renkleri hex string olarak alır. */
    public static Gradient of(String... hexColors) {
        Color[] colors = new Color[hexColors.length];

        for (int i = 0; i < hexColors.length; i++)
            colors[i] = fromHex(hexColors[i]);

        return of(colors);
    }

    /** Hex formatında bir rengi Bukkit {@link Color}'a çevirir. */
    public static Color fromHex(String hex) {
        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
        int rgb = Integer.parseInt(cleaned, 16);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return Color.fromRGB(r, g, b);
    }

    /**
     * 0 ve 1 arasındaki t değerine karşılık gelen rengi döndürür.
     * Bu işlem için öncelikle t değerinin 0 ve 1 arasına getirilir.
     * Renklerin konumuyla ilgili bilgiyi önemseyen değer {@code scaled} değeridir.
     * t değeri, yalnızca o gradyanın neresindeki rengin istendiğini söyler.
     * */
    public Color at(double t) {
        if (stops.size() == 1)
            return stops.get(0);

        double clamped = Math.max(0.0, Math.min(1.0, t));
        double scaled = clamped * (stops.size() - 1);
        int index = (int) Math.floor(scaled);

        if (index >= stops.size() - 1)
            return stops.get(stops.size() - 1);

        double localT = scaled - index;
        Color from = stops.get(index);
        Color to = stops.get(index + 1);

        return Color.fromRGB(
                lerp(from.getRed(), to.getRed(), localT),
                lerp(from.getGreen(), to.getGreen(), localT),
                lerp(from.getBlue(), to.getBlue(), localT)
        );
    }

    // Lineer interpolation
    private static int lerp(int from, int to, double t) {
        return (int) Math.round(from + (to - from) * t);
    }
}
