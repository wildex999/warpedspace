package com.wildex999.warpedspace.items;

import java.util.List;

import com.wildex999.warpedspace.blocks.relay.BlockRelayBase;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemRelayBlock extends ItemBlock {

	public ItemRelayBlock(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List stringList, boolean extraInfo) {
		super.addInformation(itemStack, player, stringList, extraInfo);
		
		if(!(field_150939_a instanceof BlockRelayBase))
			return;
		
		BlockRelayBase relayBlock = (BlockRelayBase)field_150939_a;
		
		stringList.add("Tier: " + EnumChatFormatting.GOLD + EnumChatFormatting.ITALIC + relayBlock.getTier());
		stringList.add("Radius: " + EnumChatFormatting.GREEN + EnumChatFormatting.ITALIC + relayBlock.getRadius());
	}
	
}
