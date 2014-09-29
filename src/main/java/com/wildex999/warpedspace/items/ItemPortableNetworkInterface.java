package com.wildex999.warpedspace.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.PortableNetworkInterfaceGui;
import com.wildex999.warpedspace.gui.WarpedControllerGui;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemPortableNetworkInterface extends ItemBase {
	public static final String itemName = "Portable Network Interface";
	public static HashMap<EntityPlayer, TileNetworkInterface> tileMap = new HashMap<EntityPlayer, TileNetworkInterface>();
	public static HashMap<EntityPlayer, TileNetworkInterface> tileMapClient = new HashMap<EntityPlayer, TileNetworkInterface>(); //For single-player
	private static int relayUpdateTick = 20;
	
	public ItemPortableNetworkInterface() {
		setUnlocalizedName(itemName);
		setCreativeTab(CreativeTabs.tabRedstone);
		this.setMaxStackSize(1);
		ItemLibrary.register(this);
	}
	
	//Get the proxy Interface for a player
	public static TileNetworkInterface getProxyInterface(EntityPlayer player) {
		HashMap<EntityPlayer, TileNetworkInterface> tiles;
		if(player.worldObj.isRemote)
			tiles = tileMapClient;
		else
			tiles = tileMap;
		
		TileNetworkInterface tile = tiles.get(player);
		if(tile == null)
		{
			tile = new TileNetworkInterface();
			tile.xCoord = (int)player.posX;
			tile.yCoord = (int)player.posY;
			tile.zCoord = (int)player.posZ;
			tile.setWorldObj(player.worldObj);
			tile.validate();
			tile.itemPlayer = player;
			tiles.put(player, tile);
		}
		return tile;
	}
	
	//Check if player(And thus item) is within range of a Relay
	public static void onTick() {
		if(relayUpdateTick-- > 0)
			return;
		relayUpdateTick = 20; //Check relay status once per second
		
		for(TileNetworkInterface tile : tileMap.values())
		{
			if(tile.getNetwork() == null)
				continue;
			
			EntityPlayer player = tile.itemPlayer;
			tile.xCoord = (int)player.posX;
			tile.yCoord = (int)player.posY;
			tile.zCoord = (int)player.posZ;
			tile.setWorldObj(player.worldObj);
			
			//Check if within tile range
			//TODO: Check relay world
			if(tile.getRelay() == null)
			{
				tile.relaySearchTicks = tile.ticksRelaySearch;
				tile.updateEntity();
			}
			else
			{
				if(tile.getNetwork().relayInRange(tile, tile.getRelay()))
					continue;
				tile.leaveRelay();
				tile.relaySearchTicks = tile.ticksRelaySearch;
				tile.updateEntity();
			}
		}
		
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List tooltipList, boolean advanced) {
		WarpedNetwork network = getNetwork(itemStack, player.worldObj);
		if(network == null)
		{
			tooltipList.add("Network: " + EnumChatFormatting.RED + "<No Network>");
			//tooltipList.add("" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "Shift-Right click with this item on any Node");
			//tooltipList.add("" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "to set the network to the one used by the node.");
		}
		else
			tooltipList.add("Network: " + EnumChatFormatting.GREEN + network.name);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		if(player.worldObj.isRemote)
			return itemStack;
		
		//Prepare the tile
		TileNetworkInterface tile = getProxyInterface(player);
		WarpedNetwork itemNetwork = getNetwork(itemStack, player.worldObj);
		if(tile.getNetwork() != itemNetwork)
		{
			if(itemNetwork != null)
			{
				tile.stackNetworkCard = new ItemStack(ItemLibrary.itemNetworkCard);
				ItemNetworkCard.setNetworkId(tile.stackNetworkCard, itemNetwork.id);
			}
			else
			{
				tile.stackNetworkCard = null;
			}
			tile.onNetworkCardUpdate();
		}

        if(!player.isSneaking())
		    player.openGui(WarpedSpace.instance, PortableNetworkInterfaceGui.GUI_ID, world, (int)player.posX, (int)player.posY, (int)player.posZ);
		else
        {
            if(tile.isNetworkReachable() && tile.currentEntry != null && tile.currentEntry.canUse())
                tile.currentEntry.activateBlock(player, 0, 0f, 0f, 0f);
        }
        return itemStack;
	}
	
	//Get the current network for the Portable Interface
	public static WarpedNetwork getNetwork(ItemStack itemStack, World world) {
		NBTTagCompound tag = itemStack.getTagCompound();
		if(tag == null)
			return null;
		int networkId = tag.getInteger("networkId");
		CoreNetworkManager networkManager = CoreNetworkManager.getInstance(world);
		return networkManager.networks.get(networkId);
	}
	
	//Save network to ItemStack
	public static void setNetwork(ItemStack itemStack, WarpedNetwork network) {
		NBTTagCompound tag = itemStack.getTagCompound();
		if(tag == null)
		{
			tag = new NBTTagCompound();
			itemStack.setTagCompound(tag);
		}
		int networkId = -1;
		if(network != null)
			networkId = network.id;
		tag.setInteger("networkId", networkId);
	}
}
