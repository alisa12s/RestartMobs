package restartcraft.restartmobs;

import com.willfp.eco.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class HpBar {
    Core core = RestartMobs.getCore();

    private final Map<UUID, BossBar> mapBar = new HashMap<>();
    private final Map<UUID, BukkitTask> taskID = new HashMap<>();
    public void show(Player player, LivingEntity livingEntity) {
        int lvl = livingEntity.getPersistentDataContainer().get(core.lvlKey, PersistentDataType.INTEGER);

        UUID uuid = player.getUniqueId();
        BossBar bar;
        if(mapBar.containsKey(uuid)) {
            bar = mapBar.get(uuid);
            mapBar.remove(uuid);
            taskID.get(uuid).cancel();
            taskID.remove(uuid);
        } else bar = Bukkit.getServer().createBossBar("", BarColor.GREEN, BarStyle.SOLID);

        double maxHealth = Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
        double health = livingEntity.getHealth();

        String titleText = core.pattern;
        titleText = titleText.replace("{lvl}", lvl + "");
        titleText = titleText.replace("{name}", core.mobList.get(livingEntity.getType().toString().toLowerCase()));
        titleText = titleText.replace("{health}", ((int) health) + "");

         double percent = health / maxHealth;

         if(percent > 0.7) bar.setColor(BarColor.GREEN);
         else if(percent > 0.3) bar.setColor(BarColor.YELLOW);
         else bar.setColor(BarColor.RED);

         bar.setTitle(StringUtils.format(titleText));
         bar.setProgress(percent);

         bar.addPlayer(player);
         mapBar.put(uuid, bar);
         taskID.put(uuid, Bukkit.getScheduler().runTaskLater(RestartMobs.getInstance(), () -> {
             bar.removeAll(); mapBar.remove(uuid); taskID.remove(uuid);
         }, 40));
    }

}
