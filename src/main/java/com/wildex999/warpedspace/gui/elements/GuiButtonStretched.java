package com.wildex999.warpedspace.gui.elements;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

//A button which stretches the texture to fill height

public class GuiButtonStretched extends GuiButton {

	public GuiButtonStretched(int p_i1020_1_, int p_i1020_2_, int p_i1020_3_,
			String p_i1020_4_) {
		super(p_i1020_1_, p_i1020_2_, p_i1020_3_, p_i1020_4_);
	}
	
	public GuiButtonStretched(int p_i1021_1_, int p_i1021_2_, int p_i1021_3_,
			int p_i1021_4_, int p_i1021_5_, String p_i1021_6_) {
		super(p_i1021_1_, p_i1021_2_, p_i1021_3_, p_i1021_4_, p_i1021_5_, p_i1021_6_);
	}
	
	//Much of this is just a copy-paste from GuiButton
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            
            //Check if mouse is over button
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            //Get draw state(Disabled, not hovering, hovering)
            int drawState = this.getHoverState(this.field_146123_n);
            
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            
            int left_u1 = 0;
            int left_u2 = this.width / 2;
            int right_u1 = 200 - this.width / 2;
            int right_u2 = 200;
            
            int v1 = 46 + drawState * 20;
            int v2 = 66 + drawState * 20;
            
            
            drawTexturedModalRectUV(this.xPosition, this.yPosition, left_u1, v1, left_u2, v2, this.width / 2, this.height);
            this.drawTexturedModalRectUV(this.xPosition + this.width / 2, this.yPosition, right_u1, v1, right_u2, v2, this.width / 2, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
            int l = 14737632;

            if (packedFGColour != 0)
            {
                l = packedFGColour;
            }
            else if (!this.enabled)
            {
                l = 10526880;
            }
            else if (this.field_146123_n)
            {
                l = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
        }
	}
	
	//Draw textured rect, specifying the texture size independent of rect size.
	public void drawTexturedModalRectUV(int x, int y, int u1, int v1, int u2, int v2, int width, int height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + height), (double)this.zLevel, (double)((float)(u1) * f), (double)((float)(v2) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), (double)this.zLevel, (double)((float)(u2) * f), (double)((float)(v2) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + 0), (double)this.zLevel, (double)((float)(u2) * f), (double)((float)(v1) * f1));
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)this.zLevel, (double)((float)(u1) * f), (double)((float)(v1) * f1));
        tessellator.draw();
	}


}
