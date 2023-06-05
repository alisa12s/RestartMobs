package restartcraft.restartmobs;

import com.willfp.ecoskills.api.EcoSkillsAPI;
import com.willfp.ecoskills.stats.Stats;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Core {

    private static double hpMultiplier, damageMultiplier, check_radius;
    private static List<String> spawn_reason;
    private static final Map<String, String> mobList = new HashMap<>();
    private static int defaultLevel = 5, despawnMob;
    private static String pattern;
    private static boolean bossbar, customname, always_visible;

    public void initialization() {
        FileConfiguration fileConfiguration = RestartMobs.getInstance().getConfig();

        pattern = fileConfiguration.getString("hp-bar.pattern");
        bossbar = fileConfiguration.getBoolean("hp-bar.bossbar.enabled");
        customname = fileConfiguration.getBoolean("hp-bar.customname.enabled");
        always_visible = fileConfiguration.getBoolean("hp-bar.customname.always-visible");
        despawnMob = fileConfiguration.getInt("hp-bar.customname.time-to-mob-despawn");
        hpMultiplier = fileConfiguration.getDouble("multiplier.health") / 100;
        damageMultiplier = fileConfiguration.getDouble("multiplier.damage") / 100;
        check_radius = fileConfiguration.getDouble("check-radius");
        spawn_reason = (List<String>) fileConfiguration.getList("spawn-reasons");
        if(fileConfiguration.getConfigurationSection("mobs") != null)
            Objects.requireNonNull(fileConfiguration.getConfigurationSection("mobs")).getKeys(false).forEach(s
                    -> mobList.put(s, fileConfiguration.getString("mobs." + s)));

        Bukkit.getScheduler().scheduleSyncRepeatingTask(RestartMobs.getInstance(), () -> {
            AtomicInteger count = new AtomicInteger(0);
            AtomicInteger lvl = new AtomicInteger(0);

            AtomicInteger shag = new AtomicInteger(0);
            Bukkit.getOnlinePlayers().forEach(player
            -> Bukkit.getScheduler().scheduleSyncDelayedTask(RestartMobs.getInstance(), () -> {
                shag.set(shag.get() + 1);

                if(player != null) {
                    count.set(count.get() + 1);
                    int strength = EcoSkillsAPI.getStatLevel(player, Objects.requireNonNull(Stats.INSTANCE.getByID("strength")));
                    int defence = EcoSkillsAPI.getStatLevel(player, Objects.requireNonNull(Stats.INSTANCE.getByID("defense")));
                    lvl.set(lvl.get() + strength + defence);
                }
            }, 1 + (shag.get() * 4L)));

            if(count.get() != 0)
                Bukkit.getScheduler().scheduleSyncDelayedTask(RestartMobs.getInstance(), ()
                    -> defaultLevel = lvl.get() / (4 * count.get()), 20 + (shag.get() * 4L));
        }, 0, 20 * 60 * 10);

        Bukkit.getScheduler().scheduleSyncDelayedTask(RestartMobs.getInstance(), ()
                -> Bukkit.getScheduler().scheduleSyncRepeatingTask(RestartMobs.getInstance(), ()
                -> Bukkit.getWorlds().forEach(world
                -> world.getEntities().forEach(entity -> {
                    if(entity.getPersistentDataContainer().has(getKey()) && entity.getTicksLived() > 20 * despawnMob)
                        entity.remove();
                })), 0, 20 * 60 * 10), 20 * 60 * 5);

    }

    public double getHealthPercentMultiplier() { return hpMultiplier; }
    public double getCheck_radius() { return check_radius; }
    public double getDamagePercentMultiplier() { return damageMultiplier; }
    public List<String> getSpawn_reason() { return spawn_reason; }
    public Map<String, String> getMobList() { return mobList; }
    public int getDefaultLevel() { return defaultLevel; }
    public String getPattern() { return pattern; }
    public int getDespawnMob() { return despawnMob; }
    public boolean isBossbar() { return bossbar; }
    public boolean isCustomname() { return customname; }
    public boolean isAlways_visible() { return always_visible; }

    public NamespacedKey getKey() { return new NamespacedKey(RestartMobs.getInstance(), "custom"); }
    public NamespacedKey getLevelKey() { return new NamespacedKey(RestartMobs.getInstance(), "lvl"); }

}
