package com.wildex999.utils;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;

//When rendering the Interfaces, we want to scale the blocks, but the default Tessellator
//has no built in method for that, so we replace it with our mirror/proxy when rendering the hosted
//blocks.
public class TessellatorMirror extends Tessellator {
    public Tessellator original;
    public float scaleX = 1;
    public float scaleY = 1;
    public float scaleZ = 1;
    public float offsetX, offsetY, offsetZ;

    public TessellatorMirror(Tessellator original) {
        this.original = original;
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public void setOffset(float offsetX, float offsetY, float offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    @Override
    public int draw() {
        return original.draw();
    }

    @Override
    public TesselatorVertexState getVertexState(float p_147564_1_, float p_147564_2_, float p_147564_3_) {
        return original.getVertexState(p_147564_1_, p_147564_2_, p_147564_3_);
    }

    @Override
    public void setVertexState(TesselatorVertexState p_147565_1_) {
        original.setVertexState(p_147565_1_);
    }

    @Override
    public void startDrawingQuads() {
        original.startDrawingQuads();
    }

    @Override
    public void startDrawing(int p_78371_1_) {
        original.startDrawing(p_78371_1_);
    }

    @Override
    public void setTextureUV(double p_78385_1_, double p_78385_3_) {
        original.setTextureUV(p_78385_1_, p_78385_3_);
    }

    @Override
    public void setBrightness(int p_78380_1_) {
        original.setBrightness(p_78380_1_);
    }

    @Override
    public void setColorOpaque_F(float p_78386_1_, float p_78386_2_, float p_78386_3_) {
        original.setColorOpaque_F(p_78386_1_, p_78386_2_, p_78386_3_);
    }

    @Override
    public void setColorRGBA_F(float p_78369_1_, float p_78369_2_, float p_78369_3_, float p_78369_4_) {
        original.setColorRGBA_F(p_78369_1_, p_78369_2_, p_78369_3_, p_78369_4_);
    }

    @Override
    public void setColorOpaque(int p_78376_1_, int p_78376_2_, int p_78376_3_) {
        original.setColorOpaque(p_78376_1_, p_78376_2_, p_78376_3_);
    }

    @Override
    public void setColorRGBA(int p_78370_1_, int p_78370_2_, int p_78370_3_, int p_78370_4_) {
        original.setColorRGBA(p_78370_1_, p_78370_2_, p_78370_3_, p_78370_4_);
    }

    @Override
    public void func_154352_a(byte p_154352_1_, byte p_154352_2_, byte p_154352_3_) {
        original.func_154352_a(p_154352_1_, p_154352_2_, p_154352_3_);
    }

    //Do scaling and offset on added vertexes
    @Override
    public void addVertexWithUV(double x, double y, double z, double u, double v) {
        original.addVertexWithUV((x*scaleX)+offsetX, (y*scaleY)+offsetY, (z*scaleZ)+offsetZ, u, v);
        //original.addVertexWithUV((x+offsetX)*scaleX, (y+offsetY)*scaleY, (z+offsetZ)*scaleZ, u, v);
    }

    //Do scaling and offset on added vertexes
    @Override
    public void addVertex(double x, double y, double z) {
        original.addVertex((x*scaleX)+offsetX, (y*scaleY)+offsetY, (z*scaleZ)+offsetZ);
        //original.addVertex((x+offsetX)*scaleX, (y+offsetY)*scaleY, (z+offsetZ)*scaleZ);
    }

    @Override
    public void setColorOpaque_I(int p_78378_1_) {
        original.setColorOpaque_I(p_78378_1_);
    }

    @Override
    public void setColorRGBA_I(int p_78384_1_, int p_78384_2_) {
        original.setColorRGBA_I(p_78384_1_, p_78384_2_);
    }

    @Override
    public void disableColor() {
        original.disableColor();
    }

    @Override
    public void setNormal(float p_78375_1_, float p_78375_2_, float p_78375_3_) {
        original.setNormal(p_78375_1_, p_78375_2_, p_78375_3_);
    }

    @Override
    public void setTranslation(double p_78373_1_, double p_78373_3_, double p_78373_5_) {
        original.setTranslation(p_78373_1_, p_78373_3_, p_78373_5_);
    }

    @Override
    public void addTranslation(float p_78372_1_, float p_78372_2_, float p_78372_3_) {
        original.addTranslation(p_78372_1_, p_78372_2_, p_78372_3_);
    }
}
