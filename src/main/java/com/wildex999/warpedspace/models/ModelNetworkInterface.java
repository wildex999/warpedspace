package com.wildex999.warpedspace.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelNetworkInterface extends ModelBase
{
  //fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Shape8;
    ModelRenderer Shape9;
    ModelRenderer Shape10;
    ModelRenderer Shape11;
    ModelRenderer Shape12;
  
  public ModelNetworkInterface()
  {
    textureWidth = 64;
    textureHeight = 32;
    
      Shape1 = new ModelRenderer(this, 0, 0);
      Shape1.addBox(-8F, -1F, -1F, 12, 2, 2);
      Shape1.setRotationPoint(7F, 23F, -2F);
      Shape1.setTextureSize(64, 32);
      Shape1.mirror = true;
      setRotation(Shape1, 0F, 1.570796F, 0F);
      Shape2 = new ModelRenderer(this, 0, 0);
      Shape2.addBox(-8F, -1F, -1F, 16, 2, 2);
      Shape2.setRotationPoint(0F, 23F, -7F);
      Shape2.setTextureSize(64, 32);
      Shape2.mirror = true;
      setRotation(Shape2, 0F, 0F, 0F);
      Shape3 = new ModelRenderer(this, 0, 0);
      Shape3.addBox(-8F, -1F, -1F, 12, 2, 2);
      Shape3.setRotationPoint(-7F, 23F, -2F);
      Shape3.setTextureSize(64, 32);
      Shape3.mirror = true;
      setRotation(Shape3, 0F, 1.570796F, 0F);
      Shape4 = new ModelRenderer(this, 0, 0);
      Shape4.addBox(-8F, -1F, -1F, 14, 2, 2);
      Shape4.setRotationPoint(7F, 16F, 7F);
      Shape4.setTextureSize(64, 32);
      Shape4.mirror = true;
      setRotation(Shape4, 0F, 0F, 1.570796F);
      Shape5 = new ModelRenderer(this, 0, 0);
      Shape5.addBox(-8F, -1F, -1F, 16, 2, 2);
      Shape5.setRotationPoint(0F, 23F, 7F);
      Shape5.setTextureSize(64, 32);
      Shape5.mirror = true;
      setRotation(Shape5, 0F, 0F, 0F);
      Shape6 = new ModelRenderer(this, 0, 0);
      Shape6.addBox(-8F, -1F, -1F, 14, 2, 2);
      Shape6.setRotationPoint(-7F, 16F, 7F);
      Shape6.setTextureSize(64, 32);
      Shape6.mirror = true;
      setRotation(Shape6, 0F, 0F, 1.570796F);
      Shape7 = new ModelRenderer(this, 0, 0);
      Shape7.addBox(-8F, -1F, -1F, 12, 2, 2);
      Shape7.setRotationPoint(7F, 9F, -2F);
      Shape7.setTextureSize(64, 32);
      Shape7.mirror = true;
      setRotation(Shape7, 0F, 1.570796F, 0F);
      Shape8 = new ModelRenderer(this, 0, 0);
      Shape8.addBox(-8F, -1F, -1F, 14, 2, 2);
      Shape8.setRotationPoint(-7F, 16F, -7F);
      Shape8.setTextureSize(64, 32);
      Shape8.mirror = true;
      setRotation(Shape8, 0F, 0F, 1.570796F);
      Shape9 = new ModelRenderer(this, 0, 0);
      Shape9.addBox(-8F, -1F, -1F, 14, 2, 2);
      Shape9.setRotationPoint(7F, 16F, -7F);
      Shape9.setTextureSize(64, 32);
      Shape9.mirror = true;
      setRotation(Shape9, 0F, 0F, 1.570796F);
      Shape10 = new ModelRenderer(this, 0, 0);
      Shape10.addBox(-8F, -1F, -1F, 12, 2, 2);
      Shape10.setRotationPoint(2F, 9F, 7F);
      Shape10.setTextureSize(64, 32);
      Shape10.mirror = true;
      setRotation(Shape10, 0F, 0F, 0F);
      Shape11 = new ModelRenderer(this, 0, 0);
      Shape11.addBox(-8F, -1F, -1F, 12, 2, 2);
      Shape11.setRotationPoint(-7F, 9F, -2F);
      Shape11.setTextureSize(64, 32);
      Shape11.mirror = true;
      setRotation(Shape11, 0F, 1.570796F, 0F);
      Shape12 = new ModelRenderer(this, 0, 0);
      Shape12.addBox(-8F, -1F, -1F, 12, 2, 2);
      Shape12.setRotationPoint(2F, 9F, -7F);
      Shape12.setTextureSize(64, 32);
      Shape12.mirror = true;
      setRotation(Shape12, 0F, 0F, 0F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    Shape1.render(f5);
    Shape2.render(f5);
    Shape3.render(f5);
    Shape4.render(f5);
    Shape5.render(f5);
    Shape6.render(f5);
    Shape7.render(f5);
    Shape8.render(f5);
    Shape9.render(f5);
    Shape10.render(f5);
    Shape11.render(f5);
    Shape12.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}
