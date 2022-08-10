package me.rominer_11.dry;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;

import static org.bukkit.Bukkit.getPlayer;

public final class Dry extends JavaPlugin implements Listener {

    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    private static Dry plugin;
    public static HashMap<Player, Player> tracking = new HashMap<>();
    BukkitScheduler scheduler = getServer().getScheduler();

    @Override
    public void onEnable() {
        // Plugin startup logic

        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.dispatchCommand(console, "gamerule naturalRegeneration false");

        scheduler.scheduleSyncRepeatingTask(this, Dry::track, 100, 100);


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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
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

    static void ActionBar(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte)2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
