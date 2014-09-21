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
import net.minecraft.util.IIcon;

public class ItemNetworkCard extends ItemBase {
	public static final String itemName = "Network Card";
	
	public ItemNetworkCard() {
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
			tooltipList.add("Network: " + EnumChatFormatting.RED + "<No Network>");
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
