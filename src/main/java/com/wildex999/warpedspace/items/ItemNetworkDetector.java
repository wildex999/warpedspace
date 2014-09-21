package com.wildex999.warpedspace.items;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.networkdetector.MessageDetectorRelayList;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.INetworkRelay;
import com.wildex999.warpedspace.warpednetwork.INode;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemNetworkDetector extends ItemBase {
    public static final String itemName = "Network Detector";

    public static int drawRange = 64; //How many blocks away to draw Relay Coverage
    public static int clientRemapTime = 100; //Ticks until update coverage map(Depend on drawRange?)
    public static int serverResendFrequency = 100; //Average number of ticks between each resend
    public static Random rand = new Random();

    @SideOnly(Side.CLIENT)
    public ArrayList<ArrayList<Byte>> clientDrawRelayCoverage;
    @SideOnly(Side.CLIENT)
    public int drawX, drawZ; //The center of the coverage map
    @SideOnly(Side.CLIENT)
    public int remapCounter; //How many ticks until update coverage map
    @SideOnly(Side.CLIENT)
    public List<RelayInfo> clientRelayList;

    public static class RelayInfo {
        public int x;
        public int z;
        public int radius;
        public RelayInfo(int x, int z, int radius) { this.x = x; this.z = z; this.radius = radius; }
    }

    public ItemNetworkDetector() {
        setUnlocalizedName(itemName);
        setCreativeTab(CreativeTabs.tabRedstone);
        this.maxStackSize = 1;

        if(WarpedSpace.isClient) {
            initClientDrawMap();
            remapCounter = clientRemapTime;
            clientRelayList = new ArrayList<RelayInfo>(0);
        }

        ItemLibrary.register(this);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        //We use durability to mark item for sync, so don't show the bar
        return false;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float f1, float f2, float f3) {
        if(world.isRemote)
            return false;

        boolean used = false;
        if(player.isSneaking())
        {
            if(!cooldownFinished(itemStack)) {
                return false;
            }

            //Get Network from node
            TileEntity tile = world.getTileEntity(x, y, z);
            if(tile instanceof INode)
            {
                WarpedNetwork oldNetwork = getNetwork(itemStack, player.worldObj);
                WarpedNetwork newNetwork = ((INode) tile).getNetwork();
                boolean wasActive = getIsActivated(itemStack);
                setNetwork(itemStack, newNetwork);

                player.addChatMessage(new ChatComponentText("" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +
                        "[WarpedSpace] Network Detector: " + EnumChatFormatting.WHITE + newNetwork.name));

                if(oldNetwork != newNetwork && oldNetwork != null)
                    onDeactivate(itemStack, (EntityPlayerMP)player, false);
                if(newNetwork != null && wasActive)
                    onActivate(itemStack, (EntityPlayerMP)player, false);
                used = true;
            }
        }
        else
        {
            int damage = itemStack.getItemDamage();
            onItemRightClick(itemStack, world, player);
            if(itemStack.getItemDamage() != damage)
                used = true;
        }

        if(used)
        {
            //Server will not automatically update client, so we force it
            ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
        }

        return used;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if(world.isRemote)
            return itemStack;

        if(!cooldownFinished(itemStack)) {
            return itemStack;
        }

        //Toggle activated
        boolean activated = getIsActivated(itemStack);
        setIsActivated(itemStack, !activated);
        if(activated)
            onDeactivate(itemStack, (EntityPlayerMP)player, true);
        else
            onActivate(itemStack, (EntityPlayerMP)player, true);

        itemStack.setItemDamage(itemStack.getItemDamage()+1);

        return itemStack;
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity player, int slotIndex, boolean inHand) {

        if(world.isRemote)
            updateClient(itemStack);
        else
            updateServer(itemStack, (EntityPlayerMP)player);
    }

    public void updateServer(ItemStack itemStack, EntityPlayerMP player) {
        NBTTagCompound tag = getTag(itemStack);
        int cooldown = tag.getInteger("clickCooldown");
        if(cooldown > 0)
        {
            cooldown--;
            tag.setInteger("clickCooldown", cooldown);
        }

        boolean isActivated = getIsActivated(itemStack);
        if(!isActivated)
            return;

        //Send updated relay list to player
        if(rand.nextInt(serverResendFrequency) == 0)
        {
            sendRelayList(itemStack,player);
        }
    }

    @SideOnly(Side.CLIENT)
    public void updateClient(ItemStack itemStack) {
        if(getIsActivated(itemStack) == false)
            return;

        if(remapCounter-- <= 0)
        {
            updateDrawMap();
            remapCounter = clientRemapTime;
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List tooltipList, boolean advanced) {
        WarpedNetwork network = getNetwork(itemStack, player.worldObj);
        if(network == null)
            tooltipList.add("Network: " + EnumChatFormatting.RED + "<No Network>");
        else
            tooltipList.add("Network: " + EnumChatFormatting.GREEN + network.name);

        boolean activated = getIsActivated(itemStack);
        if(activated)
            tooltipList.add(EnumChatFormatting.GREEN + "Activated");
        else
            tooltipList.add(EnumChatFormatting.RED + "Deactivated");

        tooltipList.add(EnumChatFormatting.GRAY + "Right click a node with a network while holding SHIFT to");
        tooltipList.add(EnumChatFormatting.GRAY + "make that network the current network for the detector.");
    }


    public void onActivate(ItemStack itemStack, EntityPlayerMP player, boolean announce) {
        //Send list of relays
        sendRelayList(itemStack, player);

        if(announce) {
            player.addChatMessage(new ChatComponentText("" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +
                    "[WarpedSpace] Network Detector: " + EnumChatFormatting.GREEN + "Activated"));
        }
    }

    public void onDeactivate(ItemStack itemStack, EntityPlayerMP player, boolean announce) {
        //Send 'clear list'(empty list) message
        sendRelayList(null, player);

        if(announce) {
            player.addChatMessage(new ChatComponentText("" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +
                    "[WarpedSpace] Network Detector: " + EnumChatFormatting.RED + "Deactivated"));
        }

    }

    public void sendRelayList(ItemStack itemStack, EntityPlayerMP player) {
        WarpedNetwork network = getNetwork(itemStack, player.worldObj);
        MessageBase messageList;

        if(network == null) {
            messageList = new MessageDetectorRelayList(new ArrayList<INetworkRelay>(0));
        }
        else {
            int posX = (int)Math.floor(player.posX);
            int posZ = (int)Math.floor(player.posZ);
            List<INetworkRelay> relays = network.getRelaysOverlappingCircle(player.getEntityWorld(), posX, posZ, drawRange, network);
            messageList = new MessageDetectorRelayList(relays);
        }

        messageList.sendToPlayer(player);
    }

    @SideOnly(Side.CLIENT)
    protected void initClientDrawMap() {
        ModLog.logger.info("initClientDrawMap");
        int finalDrawRange = (drawRange*2)+1; //drawRange on both sides + center
        clientDrawRelayCoverage = new ArrayList<ArrayList<Byte>>(finalDrawRange);
        for(int z = 0; z < finalDrawRange; z++)
        {
            ArrayList<Byte> zList = new ArrayList<Byte>(finalDrawRange);
            clientDrawRelayCoverage.add(zList);
            for(int x = 0; x < finalDrawRange; x++)
            {
                zList.add((byte)0);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected void clearClientDrawMap() {
        int finalDrawRange = (drawRange*2)+1;
        for(int z = 0; z < finalDrawRange; z++)
        {
            ArrayList<Byte> zList = clientDrawRelayCoverage.get(z);
            for(int x = 0; x < finalDrawRange; x++)
            {
                zList.set(x, (byte)0);
            }
        }
    }

    //Update the DrawRelayCoverage map to indicate what coordinates
    //to draw overlay on(What coordinates are covered).
    @SideOnly(Side.CLIENT)
    public void updateDrawMap() {
        clearClientDrawMap();

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        drawX = (int)Math.floor(player.posX);
        drawZ = (int)Math.floor(player.posZ);

        //Now we have to test each coordinate against each relay
        //to see if it's within range.
        //TODO: Spread this over multiple frames
        int drawOffset = -(drawRange+1);
        for(int z = drawOffset; z < drawRange; z++)
        {
            for(int x = drawOffset; x < drawRange; x++)
            {
                int testX = drawX + x;
                int testZ = drawZ + z;

                //Test against every relay
                for(RelayInfo relay : clientRelayList)
                {
                    int diffX = relay.x - testX;
                    int diffZ = relay.z - testZ;

                    //This overflows on larger radius and distances, so use long(Can still possibly overflow on int.MAX_VALUE?)
                    long diffX2 = ((long)diffX*(long)diffX);
                    long diffZ2 = ((long)diffZ*(long)diffZ);
                    long radius2 = ((long)relay.radius*(long)relay.radius);

                    if(diffX2 + diffZ2 <= radius2) {
                        clientDrawRelayCoverage.get(z - drawOffset).set(x - drawOffset, (byte) 1);
                        break; //No need to check further relays for this coordinate
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void setRelayList(List<RelayInfo> relayList) {
        clientRelayList = relayList;
        updateDrawMap();
    }

    public boolean cooldownFinished(ItemStack itemStack) {
        NBTTagCompound tag = getTag(itemStack);
        int cooldown = tag.getInteger("clickCooldown");
        tag.setInteger("clickCooldown", 5); //Reset cooldown even if not finished
        if(cooldown > 0)
            return false;
        return true;
    }

    //ItemStack variables
    public NBTTagCompound getTag(ItemStack itemStack) {
        NBTTagCompound tag = itemStack.getTagCompound();
        if(tag == null)
        {
            tag = new NBTTagCompound();
            itemStack.setTagCompound(tag);
        }
        return tag;
    }

    public void setIsActivated(ItemStack itemStack, boolean activated) {
        getTag(itemStack).setBoolean("isActivated", activated);
    }
    public boolean getIsActivated(ItemStack itemStack) {
        return getTag(itemStack).getBoolean("isActivated");
    }

    public void setNetwork(ItemStack itemStack, WarpedNetwork network) {
        NBTTagCompound tag = getTag(itemStack);
        if(network == null)
            tag.setInteger("networkId", -1);
        else
            tag.setInteger("networkId", network.id);
    }
    public WarpedNetwork getNetwork(ItemStack itemStack, World world) {
        if(itemStack == null)
            return null;

        CoreNetworkManager networkManager = CoreNetworkManager.getInstance(world);
        NBTTagCompound tag = getTag(itemStack);
        int networkId;

        if(tag.hasKey("networkId"))
            networkId = getTag(itemStack).getInteger("networkId");
        else
            return null;
        return networkManager.networks.get(networkId);
    }

}
