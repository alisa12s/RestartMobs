package restartcraft.restartmobs;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class RestartMobs extends JavaPlugin {

    private static RestartMobs instance;

    public static HpBar bar;
    public static Core core;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        core = new Core();
        bar = new HpBar();

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static RestartMobs getInstance() { return instance; }

}
