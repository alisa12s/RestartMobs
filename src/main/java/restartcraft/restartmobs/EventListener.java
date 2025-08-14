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
    Core core = RestartMobs.core;

    @EventHandler
    public void custom_name_change_event(EntityDamageEvent event) {
        if(!core.customname) return;
        if(event.getEntity().getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) return;

        Bukkit.getScheduler().runTaskAsynchronously(RestartMobs.getInstance(), () -> {
            if(event.getEntity().getPersistentDataContainer().has(core.key)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RestartMobs.getInstance(), () -> {
                    int lvl = event.getEntity().getPersistentDataContainer().get(core.lvlKey, PersistentDataType.INTEGER);

                    String titleText = core.pattern;
                    titleText = titleText.replace("{lvl}", lvl + "");
                    titleText = titleText.replace("{name}", core.mobList.get(event.getEntity().getType().toString().toLowerCase()));
                    titleText = titleText.replace("{health}", ((int) ((LivingEntity) event.getEntity()).getHealth()) + "");

                    event.getEntity().customName(StringUtils.formatToComponent(titleText));
                }, 1);
            }
        });
    }

    @EventHandler
    public void damage_event(EntityDamageByEntityEvent event) {
        if(core.bossbar && event.getEntity().getPersistentDataContainer().has(core.lvlKey)
        && event.getDamager() instanceof Player)
            Bukkit.getScheduler().runTaskLaterAsynchronously(RestartMobs.getInstance(), ()
                    -> RestartMobs.bar.show((Player) event.getDamager(), (LivingEntity) event.getEntity()), 1);

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
        if(damager.getPersistentDataContainer().has(core.key)) {
            int lvl = damager.getPersistentDataContainer().get(core.lvlKey, PersistentDataType.INTEGER);
            double newDamage = event.getDamage() + (event.getDamage() * (lvl * core.damageMultiplier));
            if(newDamage != 0) event.setDamage(newDamage);
        }
    }

    @EventHandler
    public void entity_spawn_event(CreatureSpawnEvent event) {
        if(!core.spawn_reason.contains(event.getSpawnReason().toString()))
            return;

        boolean spawner = false;
        if(core.spawner) {
            spawner = event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER;
            if(!spawner && event.getEntity().getType() == EntityType.ENDERMAN)
                for(Entity entity : event.getEntity().getNearbyEntities(20, 5, 20))
                    if(entity.getType() == EntityType.ENDERMITE) {
                        spawner = true;
                        break;
                    }
        }

        if(!core.mobList.containsKey(event.getEntity().getType().toString().toLowerCase())) return;

        LivingEntity entity = event.getEntity();
        double radius = core.check_radius;
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
        int lvl = core.defaultLevel;
        if(count.get() != 0) lvl = sumLevel.get() / (4 * count.get());
        if(spawner && max.get() != 0) lvl = max.get() / 4;

        double hp = entity.getHealth() + (entity.getHealth() * (lvl * core.hpMultiplier)); // 10% из конфига

        entity.getPersistentDataContainer().set(core.key, PersistentDataType.STRING, event.getEntity().getType().toString());
        entity.getPersistentDataContainer().set(core.lvlKey, PersistentDataType.INTEGER, lvl);
        Objects.requireNonNull(entity.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(hp);
        entity.setHealth(hp);
        if(core.customname && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            String titleText = core.pattern;
            titleText = titleText.replace("{lvl}", lvl + "");
            titleText = titleText.replace("{name}", core.mobList.get(event.getEntity().getType().toString().toLowerCase()));
            titleText = titleText.replace("{health}", ((int) entity.getHealth()) + "");

            entity.customName(StringUtils.formatToComponent(titleText));
            entity.setCustomNameVisible(core.always_visible);
            if(core.despawnMob > 0)
                Bukkit.getScheduler().scheduleSyncDelayedTask(RestartMobs.getInstance(), entity::remove, 20L * core.despawnMob);
        }
    }


}
