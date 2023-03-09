# BlockLimiter
Spigot plugin to put a limit on number of a block in a chunk\
\
If the plugin does not find a running total for a chunk\
it will count what is in the chunk currently so it can be added to already existing servers\
\
Config can be used to limit the amount of any block in each chunk\
the pre-set limits are below but can be changed in config
  - "CHEST:1000"
  - "REDSTONE_WIRE:1000"
  - "HOPPER:1000"
 
commands:
  BlockLimiter:
    description: allows the user to reload the config or recount the chunk\
    This command can be used in the following 2 ways:\
    /blocklimiter reload - reloads the config\
    /blocklimiter recount - recounts all the limited blocks in the chunk
    

permissions:\
  blocklimiter.command:\
    description: Allows the player to use the command\
    default: op\
    \
  blocklimiter.bypass:\
    description: Allows the player to place blocks in excess of the limit\
    default: op
