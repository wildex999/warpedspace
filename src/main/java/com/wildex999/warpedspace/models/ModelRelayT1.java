package com.wildex999.warpedspace.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelRelayT1 extends ModelBase
{
  //fields
    ModelRenderer Base;
    ModelRenderer Pillar4;
    ModelRenderer Pillar1;
    ModelRenderer Pillar2;
    ModelRenderer Pillar3;
    ModelRenderer Node;
  
  public ModelRelayT1()
  {
    textureWidth = 64;
    textureHeight = 32;
    
      Base = new ModelRenderer(this, 0, 0);
      Base.addBox(-8F, 0F, -8F, 16, 1, 16);
      Base.setRotationPoint(0F, 23F, 0F);
      Base.setTextureSize(64, 32);
      Base.mirror = true;
      setRotation(Base, 0F, 0F, 0F);
      Pillar4 = new ModelRenderer(this, 0, 0);
      Pillar4.addBox(0F, -8F, 0F, 1, 16, 1);
      Pillar4.setRotationPoint(-8F, 15F, 7F);
      Pillar4.setTextureSize(64, 32);
      Pillar4.mirror = true;
      setRotation(Pillar4, 0F, 0F, 0F);
      Pillar1 = new ModelRenderer(this, 0, 0);
      Pillar1.addBox(0F, -8F, 0F, 1, 16, 1);
      Pillar1.setRotationPoint(7F, 15F, 7F);
      Pillar1.setTextureSize(64, 32);
      Pillar1.mirror = true;
      setRotation(Pillar1, 0F, 0F, 0F);
      Pillar2 = new ModelRenderer(this, 0, 0);
      Pillar2.addBox(0F, -8F, 0F, 1, 16, 1);
      Pillar2.setRotationPoint(7F, 15F, -8F);
      Pillar2.setTextureSize(64, 32);
      Pillar2.mirror = true;
      setRotation(Pillar2, 0F, 0F, 0F);
      Pillar3 = new ModelRenderer(this, 0, 0);
      Pillar3.addBox(0F, -8F, 0F, 1, 16, 1);
      Pillar3.setRotationPoint(-8F, 15F, -8F);
      Pillar3.setTextureSize(64, 32);
      Pillar3.mirror = true;
      setRotation(Pillar3, 0F, 0F, 0F);
      Node = new ModelRenderer(this, 0, 0);
      Node.addBox(-2F, -2F, -2F, 4, 4, 4);
      Node.setRotationPoint(0F, 14F, 0F);
      Node.setTextureSize(64, 32);
      Node.mirror = true;
      setRotation(Node, 0.7853982F, 0.7853982F, 0.7853982F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    Base.render(f5);
    Pillar4.render(f5);
    Pillar1.render(f5);
    Pillar2.render(f5);
    Pillar3.render(f5);
    Node.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}
