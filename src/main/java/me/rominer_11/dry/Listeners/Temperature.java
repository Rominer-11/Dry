package me.rominer_11.dry.Listeners;

import me.rominer_11.dry.Files.TemperData;
import net.minecraft.server.v1_8_R3.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;

import static me.rominer_11.dry.Dry.ActionBar;

public class Temperature implements Listener {

    public Temperature() {


        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Dry");

        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {


            for (Player player : Bukkit.getOnlinePlayers()) {
                TemperData.reload();

                // Minecraft to degrees calculations from https://empireminecraft.com/threads/the-real-life-temperature-of-all-minecraft-biomes-revealed.76448/
                // Temperature limits derived from https://www.universityofcalifornia.edu/news/its-not-just-hot-its-dangerous, https://doi.org/10.1016/j.mpaic.2018.06.003, and https://ars.els-cdn.com/content/image/1-s2.0-S1472029918301474-gr1.jpg

                double rDeg = getEnvTemp(player);
                BigDecimal insulation = new BigDecimal("0");

                // If you wear leather armor, it increases your insulation and makes your temperature drop slower.
                if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType().equals(Material.LEATHER_HELMET)) {
                    insulation = insulation.add(new BigDecimal("0.0005"));
                }
                if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType().equals(Material.LEATHER_CHESTPLATE)) {
                    insulation = insulation.add(new BigDecimal("0.0005"));
                }
                if (player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType().equals(Material.LEATHER_LEGGINGS)) {
                    insulation = insulation.add(new BigDecimal("0.0005"));
                }
                if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getType().equals(Material.LEATHER_BOOTS)) {
                    insulation = insulation.add(new BigDecimal("0.0005"));
                }

                if (rDeg < 104 && rDeg > 32) {
                    // If temperature is within body regulation limits, slowly set temperature to 98.6.
                    if (TemperData.get().getDouble(player.getDisplayName()) < 98.6) {
                        TemperData.get().set(player.getDisplayName(), TemperData.get().getDouble(player.getDisplayName()) + 0.004);
                    } else if (TemperData.get().getDouble(player.getDisplayName()) > 98.6) {
                        TemperData.get().set(player.getDisplayName(), TemperData.get().getDouble(player.getDisplayName()) - (0.004 - insulation.doubleValue()));
                    }
                } else {
                    // If too hot or too cold, regulation is lost at temperature goes towards that of the environment.
                    if (TemperData.get().getDouble(player.getDisplayName()) < rDeg) {
                        TemperData.get().set(player.getDisplayName(), TemperData.get().getDouble(player.getDisplayName()) + 0.004);
                    } else if (TemperData.get().getDouble(player.getDisplayName()) > rDeg) {
                        TemperData.get().set(player.getDisplayName(), TemperData.get().getDouble(player.getDisplayName()) - (0.004 - insulation.doubleValue()));
                    }
                }

                if (TemperData.get().getDouble(player.getDisplayName()) > 100) {
                    // You get nausea and take damage from heat stroke if your body temperature exceeds 100 degrees.
                    // Damage can be negated with fire resistance
                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
                    ((CraftPlayer)player).getHandle().damageEntity(DamageSource.FIRE, 0.2f);
                }

                if (TemperData.get().getDouble(player.getDisplayName()) < 90) {
                    // You get slowness, blindness, and take damage from hypothermia if your body temperature decreases below 90 degrees.
                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
                    ((CraftPlayer)player).getHandle().damageEntity(DamageSource.WITHER, 0.2f);
                }

                TemperData.save();
                ActionBar(player, getTempStatus(player));

            }

        }, 0, 5);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!TemperData.get().contains(event.getPlayer().getDisplayName())) {
            TemperData.get().set(event.getPlayer().getDisplayName(), 98.60d);
            System.out.println("Created temperature data for " + event.getPlayer().getDisplayName());
            TemperData.save();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        TemperData.get().set(event.getEntity().getDisplayName(), 98.60d);
        TemperData.save();
    }

    public static Double getEnvTemp(Player player) {
        double degrees = (((player.getLocation().getBlock().getTemperature() - 0.15) / 0.1) * 5) + 32;
        double rDeg = (Math.round(degrees * 100.0) / 100.0) + (double) (player.getLocation().getBlock().getLightLevel() - player.getLocation().getBlock().getLightLevel() / 2);
        return rDeg;
    }

    public static Double getBodyTemp(Player player) {
        double temp = TemperData.get().getDouble(player.getDisplayName());
        return temp;
    }
    
    public static String getTempStatus(Player player) {
        String envColor = null;
        String bdyColor = null;
        
        if (getEnvTemp(player) < 104 && getEnvTemp(player) > 32) {
            envColor = "§a";
        } else if (getEnvTemp(player) >= 104) {
            envColor = "§c";
        } else if (getEnvTemp(player) <= 32) {
            envColor = "§b";
        }

        if (getBodyTemp(player) < 100 && getBodyTemp(player) > 90) {
            bdyColor = "§a";
        } else if (getBodyTemp(player) >= 100) {
            bdyColor = "§c";
        } else if (getBodyTemp(player) <= 90) {
            bdyColor = "§b";
        }

        String msg = envColor + "ET " + getEnvTemp(player) + bdyColor + " BT " + Math.round(getBodyTemp(player) * 100.0) / 100.0;
        return msg;
    }
}
