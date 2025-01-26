package restartcraft.restartmobs;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class RestartMobs extends JavaPlugin {

    private static RestartMobs instance;

    private HpBar bar;
    private Core core;

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

    public static HpBar getHpBar() { return instance.bar; }
    public static Core getCore() { return instance.core; }

}
