/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.learnstemlabs.mcturtles;

/**
 *
 * @author techplex
 */
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.util.Optional;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;



@Plugin(id = "com.learnstemlabs.mcturtles", name = "MCTurtles Plugin", description = "An example plugin", version ="'1.0-SNAPSHOT")
public class MCTurtlesPlugin {

    @Inject
    private Logger logger;

    public Logger getLogger() {
        return logger;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("Starting MCTurtlesPlugin");
    }
    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        Optional<Player> optPlayer = event.getCause().first(Player.class);
        if (optPlayer.isPresent()) { // && optPlayer.get()
            // The event was caused by the player we're interested in
            logger.info("Block Broken by player: "+optPlayer.get().getName());
        } else {
            return; // Don't care, this event is not in our interest
        }
    }
    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event) {
        Optional<Player> optPlayer = event.getCause().first(Player.class);
        if (optPlayer.isPresent()) { // && optPlayer.get()
            // The event was caused by the player we're interested in
            logger.info("Block Placed by player: "+optPlayer.get().getName());
            for(Transaction<BlockSnapshot> transaction : event.getTransactions()){
				BlockSnapshot snap = transaction.getFinal();

					if(snap.getLocation().isPresent()) {
						Location l = snap.getLocation().get();
						boolean canBuildHere = MCTurtlesPlugin.canBuildHere(optPlayer.get(), l);
						if (! canBuildHere) {
							event.setCancelled(true);
							optPlayer.get().sendMessage(Text.of("Sorry you cannot place blocks here."));
						}
						Location l2 = l.add(0, -1, 0);
						logger.info("ID Below: "+l2.getBlock().getId());
					} else {
						logger.info("Location Missing");
					}

//                BlockSnapshot from = transaction.getOriginal();
//                BlockSnapshot to = transaction.getFinal();

//                logger.info("Trans: "+from.getState().getType().toString()+"to: "+to.getState().getType().toString());

            }
        } else {
            return; // Don't care, this event is not in our interest
        }
    }
	public final static String BUILD_ALLOW_BLOCK_ID = "minecraft:wool[color=lime]";
	public final static String BUILD_DISALLOW_BLOCK_ID = "minecraft:wool[color=red]";
	public final static String BORDER_BLOCK_ID = "minecraft:brick_block";
	
	public final static String TURTLE_BUILD_ALLOW_ID = "minecraft:stained_hardened_clay[color=lime]";
	public final static String TURTLE_BUILD_DISALOW_ID = "minecraft:stained_hardened_clay[color=red]";
	public final static String TURTLE_BORDER_BLOCK_ID = "minecraft:stained_glass[color=red]"; 
	public final static String TURTLE_ANTIBORDER_BLOCK_ID = "minecraft:stained_glass[color=lime]";
    public static boolean canBuildHere(Player player, Location<World> start) {
        //remember in MC y is the altitude while x and z are the lat and lon
		Extent e = player.getLocation().getExtent();
		int x = start.getBlockX();
		int y = start.getBlockY();
		int z = start.getBlockZ();
//		Location<World> start = new Location(e, x, y, z);
		boolean allowBuild = false; //is student building enabled
		
		
		//are we in a restricted area @todo
		//no
		//allowBuild = true;
		//return allowBuild;
		//yes
		
        //search down for build allow of build deny block
		//start at block y and look downward for special blocks which prevent building, stopping at a depth of -64 or the first build allow or disallow block
        //if we don't find a build disallow block and building is enabled return true
		for(int alt = y-1; alt > alt-64 && alt >= 0; --alt) {
			String bid = new Location(e, x, alt, z).getBlock().getId();
			if (bid.equals(BUILD_ALLOW_BLOCK_ID)) {
				allowBuild = true;
				break;
			}
			if (bid.equals(BUILD_DISALLOW_BLOCK_ID)) {
				allowBuild = false;
				break;
			}
			if (bid.equals(BORDER_BLOCK_ID)) {
				allowBuild = false;
				break;
			}
		}
		//If we have determined that the player can build
		//starting a block y look up for a border block. if found, return false, else true
		if (allowBuild) {
			for(int alt = y+1; alt <= 255; ++alt) {
				String bid = new Location(e, x, alt, z).getBlock().getId();
                if (bid.equals(BORDER_BLOCK_ID)) {
					allowBuild = false;
					break;
				}
            }
		}
        
        return allowBuild;
    }

}
