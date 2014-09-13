package com.wildex999.warpedspace.items;

import java.util.List;

import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

public class ItemPortableNetworkInterface extends ItemBase {
	public static final String itemName = "Portable Network Interface";
	
	public ItemPortableNetworkInterface() {
		setUnlocalizedName(itemName);
		setCreativeTab(CreativeTabs.tabRedstone);
		ItemLibrary.register(this);
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		this.itemIcon = iconRegister.registerIcon(WarpedSpace.MODID + ":" + itemName);
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List tooltipList, boolean advanced) {
		int networkId = getNetworkId(itemStack);
		WarpedNetwork network = CoreNetworkManager.getInstance(player.worldObj).networks.get(networkId);
		if(network == null)
		{
			tooltipList.add("Network: " + EnumChatFormatting.RED + "<No Network>");
			tooltipList.add("" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "Shift-Right click with this item on any Node");
			tooltipList.add("" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "to set the network to the one used by the node.");
		}
		else
			tooltipList.add("Network: " + EnumChatFormatting.GREEN + network.name);
	}
	
	public static int getNetworkId(ItemStack itemStack) {
		NBTTagCompound tag = itemStack.getTagCompound();
		if(tag == null)
			return -1;
		return tag.getInteger("networkId");
	}
	
	public static void setNetworkId(ItemStack itemStack, int networkId) {
		NBTTagCompound tag = itemStack.getTagCompound();
		if(tag == null)
		{
			tag = new NBTTagCompound();
			itemStack.setTagCompound(tag);
		}
		
		tag.setInteger("networkId", networkId);
		
	}
}
