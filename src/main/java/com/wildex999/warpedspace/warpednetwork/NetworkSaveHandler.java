package com.wildex999.warpedspace.warpednetwork;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkList;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

public class NetworkSaveHandler {
	public static NetworkSaveHandler instance;
	public static boolean isDirty = false;
	public static boolean hasLoaded = false;

	public NetworkSaveHandler() {
		instance = this;
	}

	//Save list of global networks to file
	@SubscribeEvent
	public boolean saveGlobalNetworks(WorldEvent.Save worldSave) {
		if(!isDirty || worldSave.world.isRemote)
			return true;

		CoreNetworkManager networkManager = CoreNetworkManager.getInstance(worldSave.world);
		Map<Integer, WarpedNetwork> networks = networkManager.networks;

		ModLog.logger.info("Saving global list...");

		try {
			File globalNetworksFile = new File(DimensionManager.getCurrentSaveRootDirectory(), "WarpedSpace/global.networks.new");
			File globalNetworksFileBackup = new File(DimensionManager.getCurrentSaveRootDirectory(), "WarpedSpace/global.networks.bkp");
			File globalNetworksFileOld = new File(DimensionManager.getCurrentSaveRootDirectory(), "WarpedSpace/global.networks");

			if(globalNetworksFileBackup.exists())
				globalNetworksFileBackup.delete();
			if(globalNetworksFileOld.exists())
				globalNetworksFileOld.renameTo(globalNetworksFileBackup);

			//--Write to new file
			File folder = globalNetworksFile.getParentFile();
			if(!folder.exists())
				folder.mkdirs();

			NBTTagCompound tags = new NBTTagCompound();

			tags.setInteger("nextFreeId", networkManager.nextFreeId);
			tags.setString("version", "0.0.1"); //TODO: Write mod version
			tags.setInteger("networkCount", networks.size());

			NBTTagList networkTags = new NBTTagList();

			for(Map.Entry<Integer, WarpedNetwork> network : networks.entrySet())
			{
				NBTTagCompound networkTag = new NBTTagCompound();
				networkTag.setInteger("id", network.getKey());
				networkTag.setString("name", network.getValue().name);
				networkTag.setString("owner", network.getValue().owner);
				networkTags.appendTag(networkTag);
			}

			tags.setTag("networks", networkTags);

			CompressedStreamTools.write(tags, globalNetworksFile);

			//Make new file current
			globalNetworksFile.renameTo(globalNetworksFileOld);

			//if(globalNetworksFile.exists())
			//globalNetworksFile.delete();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		ModLog.logger.info("Save complete!");
		isDirty = false;

		return true;
	}

	//Load list of global networks from file
	@SubscribeEvent
	public boolean loadGlobalNetworks(WorldEvent.Load worldLoad) {
		if(hasLoaded || worldLoad.world.isRemote)
			return true;

		ModLog.logger.info("Loading Global network list...");

		NBTTagCompound tags;
		CoreNetworkManager networkManager = CoreNetworkManager.getInstance(worldLoad.world);
		networkManager.clearNetworks();
		File globalNetworksFile = new File(DimensionManager.getCurrentSaveRootDirectory(), "WarpedSpace/global.networks");

		if(!globalNetworksFile.exists())
		{
			ModLog.logger.info("Nothing to load!");
			hasLoaded = true;
			return true;
		}

		try {
			tags = CompressedStreamTools.read(globalNetworksFile);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if(tags == null)
		{
			ModLog.logger.error("Networks file existed, but was corrupted!");
			return false;
		}

		//Read the basic tags
		networkManager.nextFreeId = tags.getInteger("nextFreeId");
		String version = tags.getString("version");
		//TODO: Version checking
		int networkCount = tags.getInteger("networkCount");

		//Read the networks
		//tags.getId, only way other than hard coding the value 10. getId should have been a static method.
		NBTTagList networkTags = tags.getTagList("networks", tags.getId());

		for(int index = 0; index < networkCount; index++)
		{
			NBTTagCompound networkTag = networkTags.getCompoundTagAt(index);
			int id = networkTag.getInteger("id");
			String name = networkTag.getString("name");
			String owner = networkTag.getString("owner");

			if(!networkManager.setNetwork(id, name, owner))
				ModLog.logger.error("Failed to load network: " + id + " | " + name + " | " + owner);
		}

		//Send list to all players currently connected
		MessageBase listMessage = new MessageSCNetworkList(networkManager);
		listMessage.sendToAll();

		hasLoaded = true;

		ModLog.logger.info("Loading complete!");
		return true;
	}

	//Load all the player networks
	public boolean loadPlayerNetworks() {
		//Iterate the /worlds/WarpedSpace/players/ for all playername.list files.
		return false;
	}
}
