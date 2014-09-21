package com.wildex999.warpedspace.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class ItemLibrary {
	public static ItemNetworkCard itemNetworkCard;
	public static ItemPortableNetworkInterface itemPortableNetworkInterface;
    public static ItemNetworkDetector itemNetworkDetector;
	
	public static void init() {
		itemNetworkCard = new ItemNetworkCard();
		itemPortableNetworkInterface = new ItemPortableNetworkInterface();
        itemNetworkDetector = new ItemNetworkDetector();
	}
	
	public static void register(ItemBase item) {
		GameRegistry.registerItem(item, item.getProperName());
	}
}
