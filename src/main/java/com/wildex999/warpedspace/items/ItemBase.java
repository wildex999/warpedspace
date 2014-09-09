package com.wildex999.warpedspace.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class ItemBase extends Item {

	public String getProperName() {
		return getUnlocalizedName().substring(5);
	}
}
