package saber.blocklimiter.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import saber.blocklimiter.BlockLimiter;

public class BlockLimiterCommand implements CommandExecutor {

    private final BlockLimiter plugin;

    public BlockLimiterCommand(BlockLimiter p1){
        plugin = p1;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        //Check command name, probably not needed
        if (!command.getName().equalsIgnoreCase("BlockLimiter")) return true;

        //Check for perms
        if (!sender.hasPermission("blocklimiter.command")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("msgNoPerms")));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {

            //Reload the config values
            plugin.reloadConfig();
            plugin.loadConfigValues();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("msgReloaded")));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("recount")) {

            if (!(sender instanceof Player)){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msgNotPlayer")));
                return true;
            }

            //Recount the chunk for every tracked block
            Player play = (Player) sender;
            for (String x : plugin.getTrackedBlocks()){
                Material mat = Material.getMaterial(x);
                if (mat != null) plugin.countChunk(play.getLocation().getChunk(), mat, play);
            }

            play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msgRecountStarted")));

            return true;
        }

        //Send proper usage
        for (String x : plugin.getConfig().getStringList("msgProperUsage")){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',x));
        }

        return true;
    }
}
