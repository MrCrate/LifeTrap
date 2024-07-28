package com.mrc.dev;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.leymooo.antirelog.Antirelog;
import com.mrc.dev.command.LifetrapCMD;
import com.mrc.dev.general.AbstractItem;

public final class LifeTrap extends JavaPlugin {
    private static Antirelog antiRelog;
    private static LifeTrap instance;
    private Config cfg;
    private final HashMap<String, AbstractItem> items = new HashMap();

    public void onEnable() {
        instance = this;
        this.initAntiRelog();
        this.saveDefaultConfig();
        this.cfg = new Config(getConfig());
        new LifetrapCMD();
        PluginManager e = Bukkit.getPluginManager();
        e.registerEvents(new BukkitEventHandler(), this);
        this.saveResource("plast-h.schem", false);
        this.saveResource("plast-v0.schem", false);
        this.saveResource("plast-v45.schem", false);
        this.saveResource("trap.schem", false);
    }

    public void reload(CommandSender sender) {
        this.reloadConfig();
        this.cfg = new Config(getConfig());
        sender.sendMessage("§a[LifeTrap] успешно перезагружен");
    }

    private void initAntiRelog() {
        PluginManager pluginManager = getServer().getPluginManager();
        Plugin antiRelogPlugin = pluginManager.getPlugin("AntiRelog");

        if (antiRelogPlugin instanceof Antirelog) {
            antiRelog = (Antirelog) antiRelogPlugin;
        } else {
            getLogger().warning("AntiRelog plugin not found or is incompatible.");
            // Handle the situation where AntiRelog plugin is not found or incompatible
        }
    }

    public Config getCfg() {
        return this.cfg;
    }

    public HashMap<String, AbstractItem> getItems() {
        return this.items;
    }

    public static Antirelog getAntiRelog() {
        return antiRelog;
    }

    public static LifeTrap getInstance() {
        return instance;
    }
}