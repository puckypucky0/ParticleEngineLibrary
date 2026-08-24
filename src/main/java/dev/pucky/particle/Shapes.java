package dev.pucky.particle;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bir {@link ParticleShape} üretmenin desteklenen tek yoludur.
 * <p>
 * Partikül motoruna submit edilmek üzere şekil üreten yerler, komutlar gibi, kendi
 * shape'ini üretemez. Bir şekil varsa buraya yeni bir metod olarak eklenir, dolayısıyla
 * her şekil tek bir yerden geçer ve kontrollerin uygulanması kolaylaşır.
 * */
public final class Shapes {

    private Shapes() {}

    /** circle()/filledSquare() gibi düz şekillerin hangi düzleme yatık olacağını seçer. */
    public enum Axis {
        X,
        Y,
        Z
    }

    /**
     * İki boyutlu (a, b) koordinatını, verilen eksene dik bir düzleme yerleştirir.
     * Axis.Y durumunda XZ düzleminde, yere yatık bir çember gibi duracaktır.
     * Axis.X veya Z verilirse dikey bir düzleme döner, duvara yapışık bir çember gibi duracaktır.
     * */
    private static Vector toAxis(double a, double b, Axis axis) {
        return switch (axis) {
            case X -> new Vector(0, a, b);
            case Y -> new Vector(a, 0, b);
            case Z -> new Vector(a, b, 0);
        };
    }

    /** Tek bir noktadan oluşan aşırı basit bir şekil. */
    public static ParticleShape point() {
        return ParticleShape.capped(List.of(new Vector(0, 0, 0)));
    }

    /** İki belirli nokta arasında yaklaşık {@code spacing} boşlukla oluşturulan bir düz çizgi */
    public static ParticleShape line(Vector from, Vector to, double spacing) {
        return ParticleShape.capped(linePoints(new Vector(0, 0, 0), to.clone().subtract(from), spacing));
    }

    /**
     * {@code line} metodunun noktaları belirleyen kısmı. Diğer birçok metod da noktaları belirlemek için bu metodu kullanır.
     * Basitçe bu fonksiyon {@code length} nesnesinde uzunluğu, {@code direction} nesnesinde de bu çizginin
     * ne yöne doğru oluşturulacağını tutar, uzunluğu aralığa bölerek de adım sayısını bulur.
     * for döngüsü ile bu adımların (aralık değeri * adım sırası) sonuç listesine eklenmesi sağlanır.
     * Uzunluğun aralığa tam bölünmemesi ve dolayısıyla en son noktanın atlanması problemini çözmek için de küçük bir kontrol yapar.
     * */
    private static List<Vector> linePoints(Vector from, Vector to, double spacing) {
        List<Vector> result = new ArrayList<>();
        double length = from.distance(to);

        if (length < 1e-6) { // eğer 0 dan küçükse kendini döndür.
            result.add(from.clone());
            return result;
        }

        Vector direction = to.clone().subtract(from).normalize();
        int steps = (int) Math.floor(length / spacing);

        for (int i = 0; i <= steps; i++)
            result.add(from.clone().add(direction.clone().multiply(i * spacing)));

        if (result.get(result.size() - 1).distance(to) > 1e-6)
            result.add(to.clone());

        return result;
    }

    /** Verilen eksene dik düzlemde {@code points} kadar noktadan oluşan bir çember. */
    public static ParticleShape circle(double radius, int points, Axis axis) {
        List<Vector> result = new ArrayList<>();
        int count = Math.max(3, points);

        for (int r = 0; r < count; r++) {
            double angle = (2 * Math.PI * r) / count;
            result.add(toAxis(Math.cos(angle) * radius, Math.sin(angle) * radius, axis));
        }

        return ParticleShape.capped(result);
    }

    /**
     * Yüzeyinde yaklaşık eşit aralıklı {@code points} nokta bulunan boş bir küre.
     * Noktalar (altın açı) spiraliyle dağıtılır. Bu yüzey boyunca düzgün dağılım sağlar.
     */
    public static ParticleShape sphere(double radius, int points) {
        List<Vector> result = new ArrayList<>();
        int count = Math.max(2, points);
        double goldenAngle = Math.PI * (3 - Math.sqrt(5));

        for (int i = 0; i < count; i++) {
            double y = 1 - (i / (double) (count - 1)) * 2;
            double radiusAtY = Math.sqrt(Math.max(0, 1 - y * y));
            double theta = goldenAngle * i;

            result.add(new Vector(
                    Math.cos(theta) * radiusAtY * radius,
                    y * radius,
                    Math.sin(theta) * radiusAtY * radius
            ));
        }

        return ParticleShape.capped(result);
    }

    /** Merkezi origin'de, kenar uzunlukları {@code size} olan bir kutunun 12 kenarının kafesi. */
    public static ParticleShape box(Vector size, double spacing) {
        Vector min = size.clone().multiply(-0.5);
        Vector max = size.clone().multiply(0.5);
        return ParticleShape.capped(boxEdges(min, max, spacing));
    }

    /**
     * Küp şeklinde partikül efekti oluşturmak için sırayla 8 köşeyi,
     * ardından 12 kenarı hesaplayan ve küpün her bir ayrıtını
     * bir çizgi gibi hesaplayıp {@code linePoints} ile çizen fonksiyon.
     * */
    private static List<Vector> boxEdges(Vector min, Vector max, double spacing) {
        Vector c000 = new Vector(min.getX(), min.getY(), min.getZ());
        Vector c001 = new Vector(min.getX(), min.getY(), max.getZ());
        Vector c010 = new Vector(min.getX(), max.getY(), min.getZ());
        Vector c011 = new Vector(min.getX(), max.getY(), max.getZ());
        Vector c100 = new Vector(max.getX(), min.getY(), min.getZ());
        Vector c101 = new Vector(max.getX(), min.getY(), max.getZ());
        Vector c110 = new Vector(max.getX(), max.getY(), min.getZ());
        Vector c111 = new Vector(max.getX(), max.getY(), max.getZ());

        List<Vector> result = new ArrayList<>();
        result.addAll(linePoints(c000, c100, spacing));
        result.addAll(linePoints(c100, c101, spacing));
        result.addAll(linePoints(c101, c001, spacing));
        result.addAll(linePoints(c001, c000, spacing));

        result.addAll(linePoints(c010, c110, spacing));
        result.addAll(linePoints(c110, c111, spacing));
        result.addAll(linePoints(c111, c011, spacing));
        result.addAll(linePoints(c011, c010, spacing));

        result.addAll(linePoints(c000, c010, spacing));
        result.addAll(linePoints(c100, c110, spacing));
        result.addAll(linePoints(c101, c111, spacing));
        result.addAll(linePoints(c001, c011, spacing));

        return result;
    }

    // blockHighlight() her zaman aynı offset'leri üretir,
    // bu yüzden tek seferlik hesaplanıp burada saklanır; chunkHighlight() ise
    // minY ve maxY parametrelerine bağlı olduğundan aynı şekilde önbelleğe alınmaz.
    private static ParticleShape blockHighlightCache;

    // init() sırasında çağrılır. Önbellek eski limitlerle üretilmiş olabilir.
    static void clearCaches() {
        blockHighlightCache = null;
    }

    /** Tek bir bloğun (min köşesine göre) 12 kenarlı, hafif şişirilmiş (pad) kafesi. Önbelleklenir. */
    public static ParticleShape blockHighlight() {
        if (blockHighlightCache == null) {
            double pad = 0.03;
            Vector min = new Vector(-pad, -pad, -pad);
            Vector max = new Vector(1 + pad, 1 + pad, 1 + pad);
            blockHighlightCache = ParticleShape.capped(boxEdges(min, max, 0.12));
        }
        return blockHighlightCache;
    }

    /**
     * Bir chunk'ın (0,0)-(16,16) sınırlarını, minY ve maxY arasında 4 dikey kenar +
     * üst/alt kare çerçeve olarak çizer.
     */
    public static ParticleShape chunkHighlight(int minY, int maxY) {
        double spacing = 0.5;
        double centerY = (minY + maxY) / 2.0;
        double lowY = minY - centerY;
        double highY = maxY - centerY;
        List<Vector> raw = new ArrayList<>();

        int[][] corners = {{0, 0}, {16, 0}, {16, 16}, {0, 16}};
        for (int[] corner : corners) {
            raw.addAll(linePoints(
                    new Vector(corner[0], lowY, corner[1]),
                    new Vector(corner[0], highY, corner[1]),
                    spacing
            ));
        }

        raw.addAll(squareOutline(lowY, spacing));
        raw.addAll(squareOutline(highY, spacing));

        return ParticleShape.capped(raw);
    }

    // chunkHighlight()'ın üst ve alt çerçevesi için tek bir Y seviyesinde 16x16'lık kare çizer.
    private static List<Vector> squareOutline(double y, double spacing) {
        List<Vector> result = new ArrayList<>();
        result.addAll(linePoints(new Vector(0, y, 0), new Vector(16, y, 0), spacing));
        result.addAll(linePoints(new Vector(16, y, 0), new Vector(16, y, 16), spacing));
        result.addAll(linePoints(new Vector(16, y, 16), new Vector(0, y, 16), spacing));
        result.addAll(linePoints(new Vector(0, y, 16), new Vector(0, y, 0), spacing));
        return result;
    }

    /** Y ekseni boyunca yükselen, {@code turns} tur dönen bir sarmal. */
    public static ParticleShape helix(double radius, double height, double turns, int points) {
        List<Vector> result = new ArrayList<>();
        int count = Math.max(2, points);

        for (int i = 0; i < count; i++) {
            double t = i / (double) (count - 1);
            double angle = t * turns * 2 * Math.PI;

            result.add(new Vector(
                    Math.cos(angle) * radius,
                    t * height,
                    Math.sin(angle) * radius
            ));
        }

        return ParticleShape.capped(result);
    }

    /** Verilen eksene dik düzlemde {@code pointsPerSide} x {@code pointsPerSide} ızgaralı dolu bir kare. */
    public static ParticleShape filledSquare(double size, int pointsPerSide, Axis axis) {
        List<Vector> result = new ArrayList<>();
        int perSide = Math.max(2, pointsPerSide);
        double half = size / 2.0;
        double step = size / (perSide - 1);

        for (int i = 0; i < perSide; i++) {
            double a = -half + i * step;
            for (int j = 0; j < perSide; j++) {
                double b = -half + j * step;
                result.add(toAxis(a, b, axis));
            }
        }

        return ParticleShape.capped(result);
    }

    /**
     * Bir kürenin içini dolduran {@code count} kadar rastgele nokta.
     */
    public static ParticleShape pointCloud(double radius, int count) {
        List<Vector> result = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < Math.max(1, count); i++) {
            double r = radius * Math.cbrt(random.nextDouble());
            double theta = random.nextDouble() * 2 * Math.PI;
            double phi = Math.acos(2 * random.nextDouble() - 1);

            result.add(new Vector(
                    r * Math.sin(phi) * Math.cos(theta),
                    r * Math.sin(phi) * Math.sin(theta),
                    r * Math.cos(phi)
            ));
        }

        return ParticleShape.capped(result);
    }
}
