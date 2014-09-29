package com.wildex999.warpedspace.worldrenderers;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.items.ItemLibrary;
import com.wildex999.warpedspace.items.ItemNetworkDetector;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

/*
Renderer for rendering the Relay Coverage on the client.
 */
public class NetworkDetectorWorldRenderer {

    @SubscribeEvent
    public void RenderWorldLastEvent(RenderWorldLastEvent event)
    {
        ItemNetworkDetector detector = ItemLibrary.itemNetworkDetector;
        if(detector.clientRelayList.size() == 0)
            return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        float partialTickTime = event.partialTicks;
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTickTime;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTickTime;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTickTime;

        GL11.glPushMatrix();
        GL11.glTranslated(-x, -y, -z); //go from cartesian x,y,z coordinates to in-world x,y,z coordinates

        double minX = 0 + 0.02;
        double maxX = 1 - 0.02;
        double maxY = 0 + 0.02;
        double minZ = 0 + 0.02;
        double maxZ = 1 - 0.02;

        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 0.25f);

        tessellator.startDrawingQuads();

        int drawOffset = -(detector.drawRange+1);
        for(int drawZ = drawOffset; drawZ < detector.drawRange; drawZ++)
        {
            List<Byte> zList = detector.clientDrawRelayCoverage.get(drawZ-drawOffset);
            for(int drawX = drawOffset; drawX < detector.drawRange; drawX++)
            {
                byte value = zList.get(drawX-drawOffset);
                if(value == 0)
                    continue;
                else if(value == 2)
                    tessellator.setColorRGBA_F(1f, 0f, 0f, 0.25f);

                tessellator.setTranslation(detector.drawX + drawX, ((float)player.posY)-1.5f, detector.drawZ + drawZ);
                tessellator.addVertex(minX, maxY, maxZ);
                tessellator.addVertex(maxX, maxY, maxZ);
                tessellator.addVertex(maxX, maxY, minZ);
                tessellator.addVertex(minX, maxY, minZ);

                if(value == 2) //reset the color
                    tessellator.setColorRGBA_F(1f, 1f, 1f, 0.25f);
            }
        }

        tessellator.draw();

        tessellator.setTranslation(0, 0, 0);

        /*GL11.glBegin(GL11.GL_LINES); //begin drawing lines defined by 2 vertices

        GL11.glColor4f(1f, 1f, 1f, 0.5f); //alpha must be > 0.1
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(minX, maxY, maxZ);

        GL11.glEnd();

        //cleanup
        GL11.glEnable(GL11.GL_TEXTURE_2D);*/


        //cleanup
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();

    }

}
