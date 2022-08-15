package me.rominer_11.dry;

import me.rominer_11.dry.Files.TemperData;
import me.rominer_11.dry.Listeners.Temperature;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;

import static org.bukkit.Bukkit.getPlayer;

public final class Dry extends JavaPlugin implements Listener {


    public static Dry plugin;
    public static HashMap<Player, Player> tracking = new HashMap<>();
    BukkitScheduler scheduler = getServer().getScheduler();
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

    @Override
    public void onEnable() {

        // Config.yml
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        // Temperature logic
        new Temperature();
        TemperData.init();
        TemperData.get().options().copyDefaults(true);
        TemperData.save();

        // Events and runnables
        getServer().getPluginManager().registerEvents(this, this);
        scheduler.scheduleSyncRepeatingTask(this, Dry::track, 100, 100);

        Bukkit.dispatchCommand(console, "gamerule naturalRegeneration false");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.setGameMode(GameMode.SPECTATOR);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("track")) {
                if (args.length == 1) {
                    if (getServer().getPlayer(args[0]) != null) {
                        if (tracking.containsKey(player)) {
                            player.sendMessage(ChatColor.GREEN + "Tracking stopped!");
                            tracking.remove(player);
                        } else {
                            player.sendMessage(ChatColor.GREEN + "Tracking started!");
                            tracking.put(player, getPlayer(args[0]));
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + args[0] + " is either not a player or not online!");
                    }
                }
            }
        }
        return false;
    }

    static boolean track() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (tracking.containsKey(player)) {
                Player target = tracking.get(player);
                if (target.isOnline()) {
                    player.sendMessage(ChatColor.GREEN + target.getName() + " is at " + Math.round(target.getLocation().getX()) + ", " + Math.round(target.getLocation().getY()) + ", " + Math.round(target.getLocation().getZ()) + ", In " + target.getLocation().getWorld().getName());
                    System.out.println(player.getName() + " is tracking " + target.getName());
                } else {
                    player.sendMessage(ChatColor.RED + "Target is not online!");
                    tracking.remove(player);
                }
            }
        }

        return true;
    }

    public static void ActionBar(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte)2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
