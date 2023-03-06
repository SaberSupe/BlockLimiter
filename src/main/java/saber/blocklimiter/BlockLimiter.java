package saber.blocklimiter;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import saber.blocklimiter.commands.BlockLimiterCommand;
import saber.blocklimiter.listeners.BlockListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public final class BlockLimiter extends JavaPlugin {

    private final List<String> TrackedBlocks = new ArrayList<>();
    private final HashMap<String,Integer> Limits = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic

        //Load Config
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        loadConfigValues();

        //Register listener
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);

        //Register Command
        getCommand("BlockLimiter").setExecutor(new BlockLimiterCommand(this));

        //Log successful launch
        this.getLogger().log(Level.INFO, "BlockLimiter loaded Successfully");
    }

    public void loadConfigValues(){

        TrackedBlocks.clear();
        Limits.clear();

        //Get values from config
        List<String> configList = getConfig().getStringList("LimitedBlocks");

        for (String x : configList){
            String[] split = x.split(":");
            TrackedBlocks.add(split[0]);
            Limits.put(split[0],Integer.parseInt(split[1]));
        }
    }

    public void countChunk(Chunk liveChunk, Material type, Player play){

        //Set up the persistent data keys
        PersistentDataContainer container = liveChunk.getPersistentDataContainer();
        NamespacedKey counting = new NamespacedKey(this,"counting" + type.toString());
        NamespacedKey key = new NamespacedKey(this,type.toString());

        //Check if it is being counted and do nothing if so
        //Note: any blocks placed or broken while the chunk is being counted won't count towards total
        //Counting is fast enough that this should not be an issue
        if (container.has(counting, PersistentDataType.INTEGER)) return;

        //Set it as being counted
        container.set(counting, PersistentDataType.INTEGER, 1);

        //Get variables that may be changed or made null when main thread continues
        ChunkSnapshot chunk = liveChunk.getChunkSnapshot();
        int minY = liveChunk.getWorld().getMinHeight();
        int maxY = liveChunk.getWorld().getMaxHeight();

        //Get config message if needed
        String message;
        if (play != null) {
            message = ChatColor.translateAlternateColorCodes('&', getConfig().getString("msgRecountComplete")).replace("{0}", type.toString());
        }
        else message = "";
        //Start and async task to count all blocks of the type in the chunk
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {

                //Loop through every block in the chunk checking for the counted type
                int count = 0;
                for (int x = 0; x < 16; x++){
                    for (int z = 0; z<16;z++){
                        for (int y = minY; y < maxY; y++){
                            if (chunk.getBlockType(x,y,z) == type){count++;}
                        }
                    }
                }

                //Store the result in the persistent data container
                container.set(key, PersistentDataType.INTEGER, count);
                container.remove(counting);

                if (play!=null) play.sendMessage(message);
            }
        });
    }

    public List<String> getTrackedBlocks(){
        return TrackedBlocks;
    }

    public HashMap<String,Integer> getLimits(){
        return Limits;
    }

}
