package dev.pucky.util.log;

import dev.pucky.util.MiniMessageUtil;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;

/**
 * Sunucu konsoluna bilgilendirici metinler göndermeyi kolaylaştıran yardımcı sınıf.
 * */
public class Logging {
    /** Bilgilendirme mesajının önem seviyesini belirtir. */
    private enum Level {
        SUCCESS("<dark_green>", "SUCCESS"),
        FAIL("<red>", "FAIL"),
        FATAL("<dark_red>", "FATAL"),
        WARN("<yellow>", "WARN"),
        INFO("<blue>", "INFO");

        private final String color;
        private final String label;

        Level(String color, String label) {
            this.color = color;
            this.label = label;
        }
    }

    /** Konsola mesaj gönderen asıl metod. */
    public static void log(String log) {
        Bukkit.getConsoleSender().sendMessage(MiniMessageUtil.deserialize(log));
    }

    /** Bilgilendirme mesajı ile ilgili ekler içeren mesajı gönderen metod. */
    private static void write(Level level, String message) {
        String levelPrefix = "<light_gray>[</light_gray>" + level.color + level.label + "<light_gray>]</light_gray> ";
        log(levelPrefix + message);
    }

    /** Başarı mesajı iletmek için kullanılan metod. */
    public static void success(@Nullable String message) {
        write(Level.SUCCESS, message);
    }

    /** Hata mesajı iletmek için kullanılan metod. */
    public static void fail(@Nullable String message) {
        write(Level.FAIL, message);
    }

    /** Ciddi hata mesajı iletmek için kullanılan metod. */
    public static void fatal(@Nullable String message) {
        write(Level.FATAL, message);
    }

    /** Uyarı mesajı iletmek için kullanılan metod. */
    public static void warn(@Nullable String message) {
        write(Level.WARN, message);
    }

    /** Bilgi mesajı iletmek için kullanılan metod. */
    public static void inform(@Nullable String message) {
        write(Level.INFO, message);
    }
}
