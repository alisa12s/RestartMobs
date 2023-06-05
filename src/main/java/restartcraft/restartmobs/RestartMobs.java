package restartcraft.restartmobs;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class RestartMobs extends JavaPlugin {

    private static RestartMobs instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Core core = new Core();
        core.initialization();
        // Plugin startup logic

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static RestartMobs getInstance() { return instance; }
}
