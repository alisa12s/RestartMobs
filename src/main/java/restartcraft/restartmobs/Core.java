package restartcraft.restartmobs;

import com.willfp.ecoskills.api.EcoSkillsAPI;
import com.willfp.ecoskills.stats.Stats;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Core {

    public double hpMultiplier, damageMultiplier, check_radius;
    public final List<String> spawn_reason = new ArrayList<>();
    public final Map<String, String> mobList = new HashMap<>();
    public int defaultLevel = 5, despawnMob;
    public String pattern;
    public boolean bossbar, customname, always_visible, spawner;

    public Core() {
        FileConfiguration fileConfiguration = RestartMobs.getInstance().getConfig();

        pattern = fileConfiguration.getString("hp-bar.pattern");
        bossbar = fileConfiguration.getBoolean("hp-bar.bossbar.enabled");
        customname = fileConfiguration.getBoolean("hp-bar.customname.enabled");
        always_visible = fileConfiguration.getBoolean("hp-bar.customname.always-visible");
        spawner = fileConfiguration.getBoolean("spawner-detect");
        despawnMob = fileConfiguration.getInt("hp-bar.customname.time-to-mob-despawn");
        hpMultiplier = fileConfiguration.getDouble("multiplier.health") / 100;
        damageMultiplier = fileConfiguration.getDouble("multiplier.damage") / 100;
        check_radius = fileConfiguration.getDouble("check-radius");
        Objects.requireNonNull(fileConfiguration.getList("spawn-reasons")).forEach(s -> spawn_reason.add(s.toString()));
        if(fileConfiguration.getConfigurationSection("mobs") != null)
            Objects.requireNonNull(fileConfiguration.getConfigurationSection("mobs")).getKeys(false).forEach(s
                    -> mobList.put(s, fileConfiguration.getString("mobs." + s)));

        Bukkit.getScheduler().runTaskTimer(RestartMobs.getInstance(), () -> {
            AtomicInteger count = new AtomicInteger(0);
            AtomicInteger lvl = new AtomicInteger(0);

            AtomicInteger shag = new AtomicInteger(0);
            Bukkit.getOnlinePlayers().forEach(player
                    -> Bukkit.getScheduler().runTaskLater(RestartMobs.getInstance(), () -> {
                shag.set(shag.get() + 1);

                if(player != null) {
                    count.set(count.get() + 1);
                    int strength = EcoSkillsAPI.getStatLevel(player, Objects.requireNonNull(Stats.INSTANCE.getByID("strength")));
                    int defence = EcoSkillsAPI.getStatLevel(player, Objects.requireNonNull(Stats.INSTANCE.getByID("defense")));
                    lvl.set(lvl.get() + strength + defence);
                }
            }, 1 + (shag.get() * 4L)));

            if(count.get() != 0)
                Bukkit.getScheduler().runTaskLater(RestartMobs.getInstance(), ()
                        -> defaultLevel = lvl.get() / (4 * count.get()), 20 + (shag.get() * 4L));
        }, 0, 20 * 60 * 10);

        if(despawnMob > 0)
            Bukkit.getScheduler().runTaskLater(RestartMobs.getInstance(), ()
                    -> Bukkit.getScheduler().runTaskTimer(RestartMobs.getInstance(), ()
                    -> Bukkit.getWorlds().forEach(world
                    -> world.getEntities().forEach(entity -> {
                if(entity.getPersistentDataContainer().has(key) && entity.getTicksLived() > 20 * despawnMob)
                    entity.remove();
            })), 0, 20 * 60 * 10), 20 * 60 * 5);
    }

    public final NamespacedKey key = new NamespacedKey(RestartMobs.getInstance(), "custom");
    public final NamespacedKey lvlKey = new NamespacedKey(RestartMobs.getInstance(), "lvl");

}
