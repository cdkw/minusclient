package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class ScaledResolution {
   private final double scaledWidthD;
   private final double scaledHeightD;
   private int scaledWidth;
   private int scaledHeight;
   private int scaleFactor;

   public ScaledResolution(Minecraft p_i46445_1_) {
      this(p_i46445_1_, p_i46445_1_.displayWidth, p_i46445_1_.displayHeight);
   }

   public ScaledResolution(Minecraft p_i7_1_, int p_i7_2_, int p_i7_3_) {
      this.scaledWidth = p_i7_2_;
      this.scaledHeight = p_i7_3_;
      this.scaleFactor = 1;
      boolean flag = p_i7_1_.isUnicode();
      int i = p_i7_1_.gameSettings.particleSetting;
      if(i == 0) {
         i = 1000;
      }

      while(this.scaleFactor < i && this.scaledWidth / (this.scaleFactor + 1) >= 320 && this.scaledHeight / (this.scaleFactor + 1) >= 240) {
         ++this.scaleFactor;
      }

      if(flag && this.scaleFactor % 2 != 0 && this.scaleFactor != 1) {
         --this.scaleFactor;
      }

      this.scaledWidthD = (double)this.scaledWidth / (double)this.scaleFactor;
      this.scaledHeightD = (double)this.scaledHeight / (double)this.scaleFactor;
      this.scaledWidth = MathHelper.ceiling_double_int(this.scaledWidthD);
      this.scaledHeight = MathHelper.ceiling_double_int(this.scaledHeightD);
   }

   public int getScaledWidth() {
      return this.scaledWidth;
   }

   public int getScaledHeight() {
      return this.scaledHeight;
   }

   public double getScaledWidth_double() {
      return this.scaledWidthD;
   }

   public double getScaledHeight_double() {
      return this.scaledHeightD;
   }

   public int getScaleFactor() {
      return this.scaleFactor;
   }
}
