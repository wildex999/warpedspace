package com.wildex999.warpedspace;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.blocks.BlockLibrary;
import com.wildex999.warpedspace.gui.BasicNetworkRelayGui;
import com.wildex999.warpedspace.gui.NetworkAgentGui;
import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.gui.NetworkManagerGui;
import com.wildex999.warpedspace.gui.PortableNetworkInterfaceGui;
import com.wildex999.warpedspace.gui.WarpedControllerGui;
import com.wildex999.warpedspace.items.ItemLibrary;
import com.wildex999.warpedspace.networking.Networking;
import com.wildex999.warpedspace.proxyplayer.ProxyContainer;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.NetworkSaveHandler;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = WarpedSpace.MODID, version = WarpedSpace.VERSION)
public class WarpedSpace {
    public static final String MODID = "warpedspace";
    public static final String VERSION = "0.1.001.001";
    
    public static final String RESOURCEPREFIX = MODID.toLowerCase() + ":";
    
    public static boolean isClient = false;
    
    public static WarpedSpace instance;
    public GuiHandler guiHandler;
    public NetworkSaveHandler networkSaveHandler;
    
    @SidedProxy(clientSide="com.wildex999.warpedspace.ClientProxy", serverSide="com.wildex999.warpedspace.CommonProxy")
    public static CommonProxy proxy; 
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ModLog.init(event.getModLog());
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	this.instance = this;
    	
    	BlockLibrary.init();
    	ItemLibrary.init();
    	
    	Networking.init();
    	
    	if(event.getSide() == Side.CLIENT)
    		isClient = true;
    	
    	guiHandler = new GuiHandler();
    	NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);
    	
    	guiHandler.setGuiHandler(NetworkManagerGui.GUI_ID, new NetworkManagerGui());
    	guiHandler.setGuiHandler(WarpedControllerGui.GUI_ID, new WarpedControllerGui());
    	guiHandler.setGuiHandler(NetworkAgentGui.GUI_ID, new NetworkAgentGui());
    	guiHandler.setGuiHandler(NetworkInterfaceGui.GUI_ID, new NetworkInterfaceGui());
    	guiHandler.setGuiHandler(BasicNetworkRelayGui.GUI_ID, new BasicNetworkRelayGui());
    	guiHandler.setGuiHandler(PortableNetworkInterfaceGui.GUI_ID, new PortableNetworkInterfaceGui());
    	
    	networkSaveHandler = new NetworkSaveHandler();
    	
    	//Register event handlers
    	MinecraftForge.EVENT_BUS.register(networkSaveHandler);
    	MinecraftForge.EVENT_BUS.register(new ProxyContainer());
    	
    	FMLCommonHandler.instance().bus().register(this);
    	FMLCommonHandler.instance().bus().register(CoreNetworkManager.serverNetworkManager);
    	FMLCommonHandler.instance().bus().register(new TickHandler());
    	
    	proxy.registerRenderers();
    	
    	ModLog.logger.info("Warped Space initialized!");
    }
    
    //Cleanup any remaining data from previous server
    @SubscribeEvent
    public void onConnect(ClientConnectedToServerEvent event) {
    	CoreNetworkManager.clientNetworkManager.clearNetworks();
	}
    
	//For single-player worlds we need to make sure to clear loaded networks
	@EventHandler
	public boolean onServerStopped(FMLServerStoppedEvent event) {
		networkSaveHandler.hasLoaded = false;
		return true;
	}
    
}


//Ideas:
/*
 * Space Disturbance Detector
 * --------------------------
 * Save the location of blocks and/or tile entities in a given space.
 * Later on(Liek after a creeper explosion) can get a list of missing blocks/tiles.
 * Render ghost blocks of missing to allow reconstruction.
 * 
 * Automaticly add new construction to map.
 * Automaticly detect changes done by trusted players as valid change and record it.
 * 
 * Player Trade
 * -------------
 * - Cheap item to create, allowing players to trade between their inventories.
 * - Use item to right click another player to send a trade request.
 * - Other player will get a window somewhere, with accept, reject, ignore player, and ignore all trades.
 * - Will show both players inventories, with a offer and request spot per player.
 * - Can select from inventory of other player and put it into their offer slot to suggest trade.
 * - Allow chat inside window(Private chat)
 * - Accept button will lock that players trade slot.
 * - Look: Inventory on top and beneath, trade in middle.
 * - Look: Top inventory goes down into middle on one side(Like a tab), opposite for other.
 * - Look: Trade square in middle between the two tabs.
 * - 'Open Inventory' button to allow other player to freely pick from inventory.
 * 
 * Inventory History/Control
 * -------------------------
 * -Remember the state of the inventory at different times.
 * -Allow to see if picked up everything after death.
 * -Store inventory state(Or a selection of it) to then later use it to automatically
 * fill inventory with those items from network/accessible containers.
 * 
 * - Ability to set items to NOT pick up
 * 
 * Inventory Manager
 * -----------------
 * - See Above.
 * - Automatically ensure inventory has x number of different items.
 * - Will pick up from Warped Network or from nearby chests.
 * - Ability to reserve space for items.
 * 
 * GUI Repeater/ Auto Configurer / Configuration Copier
 * -------------
 * - Record player clicks on a GUI
 * - Play back when using item to open machine GUI.
 * 
 * Respaw Despawned
 * ---------------
 * - Get access to items which have despawned on the server
 * - Sort by who owned item before it was dropped
 * - Also items taken by explosion, lava fire etc.
 * - Have to pay a price to get items(All your lost items... For a price!)
 * - Visual: Enderman trade window
 * 
 * Item Tracker
 * ------------
 * Track item
 * - Get which player/Inventory which as it
 * 
 * Network Quick Access
 * --------------------
 * Create item which is bound to specific tile on network.
 * - Using item is the same as right clicking bound tile entity.
 * - Shift Using will open up the menu for that item(Control network, input, name etc.)
 * - Recipe: Include a Network Access item(?), or be cheaper?
 * 
 * Remote Player Hologram
 * -----------------------
 * Show a 3*3 space around a player when they are far away.
 * Show player animations etc.
 * Show other entities coming close(Including dropped items)
 * (?)Show blocks(And render tile entities without laoding them?) around player(Would cause a lot of network traffic?)
 * 
 * Glasses of removal
 * -------------------
 * - Make certain blocks invisible for a short range(3 debth?).
 * - Ores are always invisible and config with blocks that are to be invisible.
 * - Primary use to see wires beneach blocks.
 * 
 * Glasses of entities
 * -------------------
 * - Show only choosen entities.
 * - Great when looking for certain monsters.
 * - Does see through walls, but cost more energy and require upgrades.
 * 
 * Creative Template
 * ------------------
 * Give user the ability to place a ghost version of
 * any existing blocks and save them to a template.
 * Then a machine can place those blocks, or show a overlay to place by.
 * 
 * Warped Node/BLock/Network
 * --------------------------
 * Any machine connected to a node will be included in the warped space network.
 * Each Warped Node has 6 sides, which by default no name(Or random name? tilentityname_uniueid).
 * You can name each side, and add it to groups.
 * 
 * You can remotely control each named side, and group:
 * - Set redstone power redstone.(I.e remote lever/button)
 * - Allow/Block/toggle remote redstone(Redstone coming into Warped block)
 * - Allow/Block/toggle power
 * - Allow/Block/toggle items
 * - Allow/Block/toggle fluids
 * 
 * Transfer light?
 * 
 * 
 * Warped Network Access Item
 * ---------------------------
 * While inside range of Relay, provide power to anything in inventory.
 * Allow to access whole network while in range.
 * 
 * When outside range, allow emergency access(Double energy use on item) and
 * no energy transfer.
 * Allow single use energy infusion to inventory. Uses energy on item to give energy to other items
 * in inventory. Does not fill access item(Even if multiple in inventory).
 * 
 * 
 * Death Watch Drone
 * -------------
 * Once the player dies, the Drone will pick up the items
 * and wait for the player to come pick them up.
 * - Recall function?
 * - Teleport to drone function?
 * 
 * - Drone acting like magnet
 * - Drone allowing remote player helping(Player control drone, will have radar to owning player,
 *  loose power when too far away from player, and charges when close to owner).
 * - 
 * 
 * Hovering Light Source
 * ----------------------
 * Place a light source in the air where you stand at head level.
 * Cost slightly more than torches.
 * (Make some kind of hover module require by everything that stay in the air?)
 * 
 * Resource Dungeons
 * ------------------
 * - Dungeons for finding certain resources
 * -- Lava, Diamonds, Emeralds, Redstone, Trees etc.
 * - Randomly generated with puzzles and traps
 * - Enemies who auto adjust to players(If it's hit for 9000 hearts, next one 
 *     Spawns with 9000 * 100 hearts)
 * -- Increase damage output to bypass armour(Learn from attacks)
 * -- Give different abilities, but also counters
 * -- Only be defeated by using traps?
 * -- Get building into it somehow.
 * - The further into the dungeon, the more reosurces.
 * -- But also harder enemies
 * - Give players a reason to go explore and fight, other than just mining.
 * - Limited time. Dungeuons despawn after a set time, so can not be used for automated farming.
 * 
 * Machines, Upgrading and progression
 * --------------------------
 * - Network Controller
 * -- Starts out basic structure, includes a very small 16x16 relay.
 * -- Only able to handle network of certain size(X number of nodes).
 * -- Internal energy storage for network
 * -- Upgradeable to expand relay size, energy storage etc.
 * -- Have Tier 1, 2, 3(Multublock, Think Total Annihilation style building)
 * -- Each Tier have different building costs, initial capabilities, and upgrade capacity.
 * 
 * - Energy Storage
 * -- Can power multiple networks(Up to x networks? Upgradeable?)
 * -- Multiple Tiers(1,2,3,4,5)
 * --- Tier 1 and 2 are single block machines.
 * --- Tier 2-5 are multiblock structures(See Network Controller).
 * --- Exponential energy storage increase on tiers
 * -- Can act as failover to certain networks(I.e, only provide energy if network needs it)
 * -- Drain high-tier first(Avoid visiting multiple energy providers)
 * 
 * - Node energy use on actions
 * -- Begins by using a lot of energy per action(FLuid, item, energy, open etc.)
 * -- Upgradeable to be more efficient
 * -- No tiers, only upgrades(But a LOT of space for upgrades)
 * 
 * - Relay stations
 * -- Starts out as low range, high energy usage(use energy by being active, higher range = more energy)
 * -- Multiple tiers(Tier 1 to 10)
 * --- Tier 1-2 is single block, with spaces for upgrades.
 * --- Tier 10 has a base range of 10000, but extremely high energy usage
 * --- Choose between range upgrades or energy upgrades(efficient)
 * 
 * - Inter Dimensional Link
 * -- Link Relay stations on two dimensions
 * 
 * - Warped Node
 * -- Adds Tiles to the network
 * -- Joins Relays
 * -- Has Cost per operation on tiles(Energy, tile, fluid, redstone)
 * -- Cost can be reduced with upgrades
 * -- Upgrades for throughput
 * - Basic can make tileentity available to networt, allowing other machines to pull from it, and push to it.
 * -- However, the tile entity can neither push or pull through the basic Warped Node.
 * -- Can upgrade to Advanced Warped Node(Tier 2?) giving it the capability to push and pull.
 * 
 * - Access Node
 * -- Links to a specific Tile on the network
 * -- Acts as remote access to the linked Tile Entity.
 * -- No energy cost by itself
 * -- Upgrades for throughput
 * 
 * - Generators
 * -- Highly upgradeable RF generators.
 * -- Multiblock structure, acting like a lot of small generators(But much more efficient in fuel and server usage)
 * -- Example: Tier 10 Solid Fuel Generator: Can burn 1000 Fuel sources a tick.
 * -- Same for Lava generator etc.
 * 
 * - Interface blocks:
 * -- Energy interface(Output redstone depending on energy % on network)
 * -- Computercraft interface(Peripheral to control network)
 * 
 * - ChunkLoader upgrade
 * -- Can be placed in Relays to chunk load the radius they reach.
 * -- Energy load exponential to radius.(Max radius set in config)(Max per Relay, max per player, max per network)
 * 
 * - Container upgrade
 * -- Allow node to act as a buffer chest(Contain items).
 * -- Multiple tiers(Higher density)(Cost more energy?)
 * 
 * - Blast Resistance upgrade
 * -- Can survive blasts depending on upgrade(Up to survive nuclear using octuple cobble)
 * 
 * - Relay Loader upgrade
 * -- Allow relay to do it's work even when chunk is unloaded.
 * */
