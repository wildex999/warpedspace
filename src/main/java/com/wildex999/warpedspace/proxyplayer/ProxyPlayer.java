package com.wildex999.warpedspace.proxyplayer;

import com.wildex999.utils.ModLog;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

/*
 * Certain remote actions(Like opening vanilla containers) require the player to
 * be within range. We bypass this by using Fake Proxy Players that we place in range to
 * do the action.
 */

public class ProxyPlayer extends FakePlayer {
	public EntityPlayerMP original;
	
	
	public ProxyPlayer(EntityPlayerMP original, WorldServer world, int x, int y, int z) {
		super(world, original.getGameProfile());
		
		this.original = original;
		this.playerNetServerHandler = original.playerNetServerHandler;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.openContainer = original.openContainer;
		this.inventoryContainer = original.inventoryContainer;
		this.inventory = original.inventory;
	}
	
	@Override
	public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
		ModLog.logger.info("Proxy open gui: " + modGuiId);
		//Behaviour copied from EntityPlayer.
		FMLNetworkHandler.openGui(this, mod, modGuiId, world, x, y, z);
		//original.openContainer = this.openContainer;
		//original.currentWindowId = this.currentWindowId;
	}
	
	@Override
	public void displayGUIChest(IInventory p_71007_1_) {
		super.displayGUIChest(p_71007_1_);
		//original.openContainer = this.openContainer;
		//original.currentWindowId = this.currentWindowId;
	}
	
	@Override
	public void closeScreen() {
		ModLog.logger.info("Proxy close screen");
		super.closeScreen();
	}
	
	@Override
	public String toString() {
		return "PROXY: " + super.toString();
	}
}
