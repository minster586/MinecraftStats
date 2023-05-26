package de.pdinklag.mcstats.bukkit;

import java.nio.file.Path;

import org.bukkit.plugin.java.JavaPlugin;

import de.pdinklag.mcstats.util.MinecraftServerUtils;

/**
 * The entry point of the MinecraftStats Bukkit plugin.
 */
public class MinecraftStatsPlugin extends JavaPlugin {
    private static final long TICKS_PER_MINUTE = 60L * MinecraftServerUtils.TICKS_PER_SECOND;

    private BukkitConfig config;
    private BukkitUpdater updater;
    private BukkitUpdateTask updateTask;

    @Override
    public void onEnable() {
        // load config
        saveDefaultConfig();
        config = new BukkitConfig(getServer(), getConfig());

        // detect webserver if necessary
        if(config.getDocumentRoot() == null) {
            final PluginWebserver webserver = PluginWebserver.find(getServer());
            if(webserver != null) {
                final Path documentRoot = webserver.getDocumentRoot().resolve(config.getSubdirName());
                config.setDocumentRoot(documentRoot);
                if(config.isUnpackWebFiles()) {
                    new UnpackWebFilesTask(this, documentRoot).runTaskAsynchronously(this);
                } else {
                    onWebPathInitialized();
                }
            } else {
                getLogger().warning("No document root specified -- please state one explictly in the configuration, or install a supported plugin featuring a webserver!");
            }
        } else {
            onWebPathInitialized();
        }
    }

    void onWebPathInitialized() {
        updater = new BukkitUpdater(getServer(), config, new LoggerLogWriter(getLogger()));
        updateTask = new BukkitUpdateTask(updater);
        updateTask.runTaskTimerAsynchronously(this, 0, TICKS_PER_MINUTE * config.getUpdateInterval());
    }

    @Override
    public void onDisable() {
        if(updateTask != null) {
            updateTask.cancel();
        }
    }
}
