package com.wildex999.warpedspace.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelNetworkAgent extends ModelBase
{
  //fields
    ModelRenderer Side1; //Bottom
    ModelRenderer Side2; //North
    ModelRenderer Side3; //East
    ModelRenderer Side4; //South
    ModelRenderer Side5; //West
    ModelRenderer Side6; //Top
    ModelRenderer Node;
    
    boolean north, south, west, east, top, bottom;
  
  public ModelNetworkAgent()
  {
    textureWidth = 64;
    textureHeight = 32;
    
      Side1 = new ModelRenderer(this, 0, 0);
      Side1.addBox(-4F, -4F, -1F, 8, 8, 2);
      Side1.setRotationPoint(0F, 23F, 0F);
      Side1.setTextureSize(64, 32);
      Side1.mirror = true;
      setRotation(Side1, 1.570796F, 0F, 0F);
      Side1.mirror = false;
      Side2 = new ModelRenderer(this, 0, 0);
      Side2.addBox(-4F, -4F, -1F, 8, 8, 2);
      Side2.setRotationPoint(0F, 16F, 7F);
      Side2.setTextureSize(64, 32);
      Side2.mirror = true;
      setRotation(Side2, 0F, 0F, 0F);
      Side2.mirror = false;
      Side3 = new ModelRenderer(this, 0, 0);
      Side3.addBox(-4F, -4F, -1F, 8, 8, 2);
      Side3.setRotationPoint(7F, 16F, 0F);
      Side3.setTextureSize(64, 32);
      Side3.mirror = true;
      setRotation(Side3, 0F, 1.570796F, 0F);
      Side3.mirror = false;
      Side4 = new ModelRenderer(this, 0, 0);
      Side4.addBox(-4F, -4F, -1F, 8, 8, 2);
      Side4.setRotationPoint(0F, 16F, -7F);
      Side4.setTextureSize(64, 32);
      Side4.mirror = true;
      setRotation(Side4, 0F, 0F, 0F);
      Side4.mirror = false;
      Side5 = new ModelRenderer(this, 0, 0);
      Side5.addBox(-4F, -4F, -1F, 8, 8, 2);
      Side5.setRotationPoint(-7F, 16F, 0F);
      Side5.setTextureSize(64, 32);
      Side5.mirror = true;
      setRotation(Side5, 0F, 1.570796F, 0F);
      Side5.mirror = false;
      Side6 = new ModelRenderer(this, 0, 0);
      Side6.addBox(-4F, -4F, -1F, 8, 8, 2);
      Side6.setRotationPoint(0F, 9F, 0F);
      Side6.setTextureSize(64, 32);
      Side6.mirror = true;
      setRotation(Side6, 1.570796F, 0F, 0F);
      Node = new ModelRenderer(this, 0, 0);
      Node.addBox(-2F, -2F, -2F, 4, 4, 4);
      Node.setRotationPoint(0F, 16F, 0F);
      Node.setTextureSize(64, 32);
      Node.mirror = true;
      setRotation(Node, 0.7853982F, 0.7853982F, 0.7853982F);
  }
  
  @Override
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    
    if(bottom)
    	Side1.render(f5);
    if(north)
    	Side2.render(f5);
    if(east)
    	Side3.render(f5);
    if(south)
    	Side4.render(f5);
    if(west)
    	Side5.render(f5);
    if(top)
    	Side6.render(f5);
    
    Node.render(f5);
  }
  
  public void setRenderSides(boolean north, boolean south, boolean west, boolean east, boolean top, boolean bottom) {
	  this.north = north;
	  this.south = south;
	  this.west = west;
	  this.east = east;
	  this.top = top;
	  this.bottom = bottom;
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}
