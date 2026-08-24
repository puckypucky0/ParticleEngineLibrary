package dev.pucky.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * MiniMessage / Adventure {@link Component} yardımcı sınıfı.
 * MiniMessage metinlerini Component'e çevirir, geri serialize eder.
 */
public class MiniMessageUtil {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component deserialize(String mm) {
        return miniMessage.deserialize(mm);
    }

    public static String serialize(Component component) {
        return miniMessage.serialize(component);
    }
}
