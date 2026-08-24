package dev.pucky;

import dev.pucky.particle.ParticleEngine;
import dev.pucky.util.log.Logging;
import org.bukkit.plugin.java.JavaPlugin;

public final class ParticleEngineLibrary extends JavaPlugin {
    public static ParticleEngineLibrary instance;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        Logging.inform("ParticleEngine başlatılıyor.");

        ParticleEngine.init();
    }

    @Override
    public void onDisable() {
        instance = null;
        ParticleEngine.shutdown();
    }
}
