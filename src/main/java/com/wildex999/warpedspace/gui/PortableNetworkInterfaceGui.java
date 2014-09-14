package com.wildex999.warpedspace.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import com.wildex999.warpedspace.GuiHandler;
import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.inventory.InterfaceContainer;
import com.wildex999.warpedspace.items.ItemPortableNetworkInterface;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;

import cpw.mods.fml.common.network.IGuiHandler;

public class PortableNetworkInterfaceGui implements IGuiHandler {
	public static final int GUI_ID = GuiHandler.getNextGuiID();
	
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileNetworkInterface inventory = ItemPortableNetworkInterface.getProxyInterface(player);
		//TileNetworkInterface inventory = (TileNetworkInterface)world.getTileEntity(-222, 71, 264);
		return new InterfaceContainer(player.inventory, inventory);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,int x, int y, int z) {
		TileNetworkInterface tile = ItemPortableNetworkInterface.getProxyInterface(player);
		//TileNetworkInterface tile = (TileNetworkInterface)world.getTileEntity(-222, 71, 264);
		NetworkInterfaceGui.GUI gui = new NetworkInterfaceGui.GUI(player, tile);
		gui.thisId = GUI_ID;
		return gui;
	}
	
}