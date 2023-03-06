package saber.blocklimiter.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import saber.blocklimiter.BlockLimiter;


public class BlockListener implements Listener {


    private final BlockLimiter plugin;

    public BlockListener(BlockLimiter p1){
        plugin = p1;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){

        //Check if it is a tracked block
        Material type = e.getBlock().getType();
        if (!plugin.getTrackedBlocks().contains(type.toString())) return;

        //Initialize the namespacekey and get the container
        NamespacedKey key = new NamespacedKey(plugin, e.getBlock().getType().toString());
        PersistentDataContainer container = e.getBlock().getChunk().getPersistentDataContainer();

        //Check if the chunk has been counted before
        if (!container.has(key, PersistentDataType.INTEGER)){

            //Count the chunk
            plugin.countChunk(e.getBlock().getChunk(), type, null);
            return;
        }

        //Check if the chunk limit is reached
        Integer blockCount = container.get(key, PersistentDataType.INTEGER);
        if (blockCount >= plugin.getLimits().get(type.toString()) && !e.getPlayer().hasPermission("blocklimiter.bypass")){

            //Stop the player from placing and let them know the limit is reached
            e.setCancelled(true);

            String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msgChunkLimitReached"));
            message = message.replace("{0}", type.toString().toLowerCase());
            message = message.replace("{1}", String.valueOf(plugin.getLimits().get(type.toString())));
            message = message.replace("{2}", String.valueOf(blockCount));

            e.getPlayer().sendMessage(message);

            return;
        }

        //Increment the counter
        container.set(key, PersistentDataType.INTEGER, blockCount+1);


    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        //Check if the broken block is a tracked block
        Material mat = e.getBlock().getType();
        if (!plugin.getTrackedBlocks().contains(mat.toString())) return;

        //Initialize the namespacekey and get the container
        NamespacedKey key = new NamespacedKey(plugin, mat.toString());
        PersistentDataContainer containter = e.getBlock().getChunk().getPersistentDataContainer();

        //If the chunk hasn't been counted, do nothing
        if (!containter.has(key, PersistentDataType.INTEGER)) return;

        //If the counter exists decrement it
        Integer count = containter.get(key,PersistentDataType.INTEGER);
        if (count > 0) containter.set(key, PersistentDataType.INTEGER, count-1);

        //If the counter is not above 0 when a block is broken then the count is wrong, recount it
        else plugin.countChunk(e.getBlock().getChunk(), mat, null);
    }
}
