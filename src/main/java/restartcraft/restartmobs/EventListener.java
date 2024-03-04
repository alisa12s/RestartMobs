package restartcraft.restartmobs;

import com.willfp.eco.util.StringUtils;
import com.willfp.ecoskills.api.EcoSkillsAPI;
import com.willfp.ecoskills.stats.Stats;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class EventListener implements Listener {
    Core core = RestartMobs.getCore();

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if(!core.isCustomname()) return;

        Bukkit.getScheduler().runTaskAsynchronously(RestartMobs.getInstance(), () -> {
            if(event.getEntity().getPersistentDataContainer().has(core.getKey())) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RestartMobs.getInstance(), () -> {
                    int lvl = event.getEntity().getPersistentDataContainer().get(core.getLevelKey(), PersistentDataType.INTEGER);

                    String titleText = core.getPattern();
                    titleText = titleText.replace("{lvl}", lvl + "");
                    titleText = titleText.replace("{name}", core.getMobList().get(event.getEntity().getType().toString().toLowerCase()));
                    titleText = titleText.replace("{health}", ((int) ((LivingEntity) event.getEntity()).getHealth()) + "");

                    event.getEntity().customName(StringUtils.formatToComponent(titleText));
                }, 1);
            }
        });

    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent event) {
        if(core.isBossbar() && event.getEntity().getPersistentDataContainer().has(core.getLevelKey())
        && event.getDamager() instanceof Player)
            Bukkit.getScheduler().scheduleSyncDelayedTask(RestartMobs.getInstance(), ()
                    -> RestartMobs.getHpBar().show((Player) event.getDamager(), (LivingEntity) event.getEntity()), 1);

        Entity damager = event.getDamager();
        if(damager instanceof Arrow || damager instanceof Fireball || damager instanceof ShulkerBullet) {
            if(damager instanceof Arrow)
                damager = (Entity) ((Arrow) event.getDamager()).getShooter();
            if(damager instanceof Fireball)
                damager = (Entity) ((Fireball) event.getDamager()).getShooter();
            if(damager instanceof ShulkerBullet)
                damager = (Entity) ((ShulkerBullet) event.getDamager()).getShooter();
        }

        if(damager == null) return;
        if(damager.getPersistentDataContainer().has(core.getKey())) {
            int lvl = damager.getPersistentDataContainer().get(core.getLevelKey(), PersistentDataType.INTEGER);
            double newDamage = event.getDamage() + (event.getDamage() * (lvl * core.getDamagePercentMultiplier()));
            if(newDamage != 0)
                event.setDamage(newDamage);
        }
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if(!core.getSpawn_reason().contains(event.getSpawnReason().toString()))
            return;

        boolean spawner = event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER;
        if(!spawner && event.getEntity().getType() == EntityType.ENDERMAN)
            for(Entity entity : event.getEntity().getNearbyEntities(20, 5, 20))
                if(entity.getType() == EntityType.ENDERMITE) {
                    spawner = true;
                    break;
                }

        if(!core.getMobList().containsKey(event.getEntity().getType().toString().toLowerCase())) return;

        LivingEntity entity = event.getEntity();

        double radius = core.getCheck_radius();
        if(spawner) radius = radius * 2;

        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger sumLevel = new AtomicInteger(0);
        AtomicInteger max = new AtomicInteger(0);
        boolean finalSpawner = spawner;
        entity.getNearbyEntities(radius, radius, radius).forEach(it -> {
            if(it instanceof Player) {
                count.set(count.get() + 1);

                int strength = EcoSkillsAPI.getStatLevel((OfflinePlayer) it, Objects.requireNonNull(Stats.INSTANCE.getByID("strength")));
                int defence = EcoSkillsAPI.getStatLevel((OfflinePlayer) it, Objects.requireNonNull(Stats.INSTANCE.getByID("defense")));

                sumLevel.set(sumLevel.get() + strength + defence);

                if(finalSpawner && max.get() < (strength + defence))  max.set(strength + defence);
            }
        });
        int lvl = core.getDefaultLevel();
        if(count.get() != 0)
            lvl = sumLevel.get() / (4 * count.get());
        if(spawner && max.get() != 0)
            lvl = max.get() / 4;

        double hp = entity.getHealth() + (entity.getHealth() * (lvl * core.getHealthPercentMultiplier())); // 10% из конфига

        entity.getPersistentDataContainer().set(core.getKey(), PersistentDataType.STRING, event.getEntity().getType().toString());
        entity.getPersistentDataContainer().set(core.getLevelKey(), PersistentDataType.INTEGER, lvl);
        Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(hp);
        entity.setHealth(hp);
        if(core.isCustomname()) {
            String titleText = core.getPattern();
            titleText = titleText.replace("{lvl}", lvl + "");
            titleText = titleText.replace("{name}", core.getMobList().get(event.getEntity().getType().toString().toLowerCase()));
            titleText = titleText.replace("{health}", ((int) entity.getHealth()) + "");

            entity.customName(StringUtils.formatToComponent(titleText));
            entity.setCustomNameVisible(core.isAlways_visible());
            if(core.getDespawnMob() > 0)
                Bukkit.getScheduler().scheduleSyncDelayedTask(RestartMobs.getInstance(), entity::remove, 20L * core.getDespawnMob());
        }
    }


}
