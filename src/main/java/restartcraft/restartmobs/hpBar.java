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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class hpBar {
    Core core = new Core();

    private static final Map<UUID, BossBar> mapBar = new HashMap<>();
    private static final Map<UUID, Integer> taskID = new HashMap<>();
    public void show(Player player, LivingEntity livingEntity) {
        if(!livingEntity.getPersistentDataContainer().has(core.getLevelKey())) return;
        int lvl = livingEntity.getPersistentDataContainer().get(core.getLevelKey(), PersistentDataType.INTEGER);

        BossBar bar;

        if(mapBar.containsKey(player.getUniqueId())) {
            bar = mapBar.get(player.getUniqueId());
            mapBar.remove(player.getUniqueId());
            Bukkit.getScheduler().cancelTask(taskID.get(player.getUniqueId()));
            taskID.remove(player.getUniqueId());
        } else
            bar = Bukkit.getServer().createBossBar("", BarColor.GREEN, BarStyle.SOLID);

        double maxHealth = Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
        double health = livingEntity.getHealth();

        String titleText = core.getPattern();
        titleText = titleText.replace("{lvl}", lvl + "");
        titleText = titleText.replace("{name}", core.getMobList().get(livingEntity.getType().toString().toLowerCase()));
        titleText = titleText.replace("{health}", ((int) health) + "");

        double percent = health / maxHealth;

        if(percent > 0.7)
            bar.setColor(BarColor.GREEN);
        else
        if(percent > 0.3)
            bar.setColor(BarColor.YELLOW);
        else
            bar.setColor(BarColor.RED);

        bar.setTitle(StringUtils.format(titleText));
        bar.setProgress(percent);

        bar.addPlayer(player);
        mapBar.put(player.getUniqueId(), bar);
        taskID.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncDelayedTask(RestartMobs.getInstance(), () -> {
            bar.removeAll();
            mapBar.remove(player.getUniqueId());
            taskID.remove(player.getUniqueId());
        }, 40));

    }

}
