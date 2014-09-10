package com.wildex999.warpedspace.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelController extends ModelBase
{
  //fields
    ModelRenderer ControllerBase;
    ModelRenderer Corner2;
    ModelRenderer Corner1;
    ModelRenderer Corner4;
    ModelRenderer Corner3;
  
  public ModelController()
  {
    textureWidth = 64;
    textureHeight = 32;
    
      ControllerBase = new ModelRenderer(this, 0, 0);
      ControllerBase.addBox(-8F, -8F, -8F, 16, 10, 16);
      ControllerBase.setRotationPoint(0F, 22F, 0F);
      ControllerBase.setTextureSize(64, 32);
      ControllerBase.mirror = true;
      setRotation(ControllerBase, 0F, 0F, 0F);
      Corner2 = new ModelRenderer(this, 0, 0);
      Corner2.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3);
      Corner2.setRotationPoint(-5F, 11F, 0F);
      Corner2.setTextureSize(64, 32);
      Corner2.mirror = true;
      setRotation(Corner2, 0.7853982F, 3.141593F, 0.7853982F);
      Corner1 = new ModelRenderer(this, 0, 0);
      Corner1.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3);
      Corner1.setRotationPoint(0F, 11F, -5F);
      Corner1.setTextureSize(64, 32);
      Corner1.mirror = true;
      setRotation(Corner1, 0.7853982F, 0.7853982F, 0.7853982F);
      Corner4 = new ModelRenderer(this, 0, 0);
      Corner4.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3);
      Corner4.setRotationPoint(5F, 11F, 0F);
      Corner4.setTextureSize(64, 32);
      Corner4.mirror = true;
      setRotation(Corner4, 0.7853982F, 1.570796F, 0.7853982F);
      Corner3 = new ModelRenderer(this, 0, 0);
      Corner3.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3);
      Corner3.setRotationPoint(0F, 11F, 5F);
      Corner3.setTextureSize(64, 32);
      Corner3.mirror = true;
      setRotation(Corner3, 0.7853982F, 2.356194F, 0.7853982F);
  }
  
  @Override
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    ControllerBase.render(f5);
    Corner2.render(f5);
    Corner1.render(f5);
    Corner4.render(f5);
    Corner3.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}
