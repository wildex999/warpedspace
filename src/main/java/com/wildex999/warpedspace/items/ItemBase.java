package com.wildex999.warpedspace.items;

import com.wildex999.warpedspace.WarpedSpace;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class ItemBase extends Item {

	public String getProperName() {
		return getUnlocalizedName().substring(5);
	}

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon(WarpedSpace.MODID + ":" + getProperName());
    }
}
