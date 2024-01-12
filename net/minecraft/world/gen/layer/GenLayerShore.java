package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenJungle;
import net.minecraft.world.biome.BiomeGenMesa;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerShore extends GenLayer {
   public GenLayerShore(long p_i2130_1_, GenLayer p_i2130_3_) {
      super(p_i2130_1_);
      this.parent = p_i2130_3_;
   }

   public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
      int[] aint = this.parent.getInts(areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
      int[] aint1 = IntCache.getIntCache(areaWidth * areaHeight);

      for(int i = 0; i < areaHeight; ++i) {
         for(int j = 0; j < areaWidth; ++j) {
            this.initChunkSeed((long)(j + areaX), (long)(i + areaY));
            int k = aint[j + 1 + (i + 1) * (areaWidth + 2)];
            BiomeGenBase biomegenbase = BiomeGenBase.getBiome(k);
            if(k == BiomeGenBase.mushroomIsland.biomeID) {
               int j2 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
               int i3 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
               int l3 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
               int k4 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
               if(j2 != BiomeGenBase.ocean.biomeID && i3 != BiomeGenBase.ocean.biomeID && l3 != BiomeGenBase.ocean.biomeID && k4 != BiomeGenBase.ocean.biomeID) {
                  aint1[j + i * areaWidth] = k;
               } else {
                  aint1[j + i * areaWidth] = BiomeGenBase.mushroomIslandShore.biomeID;
               }
            } else if(biomegenbase != null && biomegenbase.getBiomeClass() == BiomeGenJungle.class) {
               int i2 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
               int l2 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
               int k3 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
               int j4 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
               if(this.func_151631_c(i2) && this.func_151631_c(l2) && this.func_151631_c(k3) && this.func_151631_c(j4)) {
                  if(!isBiomeOceanic(i2) && !isBiomeOceanic(l2) && !isBiomeOceanic(k3) && !isBiomeOceanic(j4)) {
                     aint1[j + i * areaWidth] = k;
                  } else {
                     aint1[j + i * areaWidth] = BiomeGenBase.beach.biomeID;
                  }
               } else {
                  aint1[j + i * areaWidth] = BiomeGenBase.jungleEdge.biomeID;
               }
            } else if(k != BiomeGenBase.extremeHills.biomeID && k != BiomeGenBase.extremeHillsPlus.biomeID && k != BiomeGenBase.extremeHillsEdge.biomeID) {
               if(biomegenbase != null && biomegenbase.isSnowyBiome()) {
                  this.func_151632_a(aint, aint1, j, i, areaWidth, k, BiomeGenBase.coldBeach.biomeID);
               } else if(k != BiomeGenBase.mesa.biomeID && k != BiomeGenBase.mesaPlateau_F.biomeID) {
                  if(k != BiomeGenBase.ocean.biomeID && k != BiomeGenBase.deepOcean.biomeID && k != BiomeGenBase.river.biomeID && k != BiomeGenBase.swampland.biomeID) {
                     int l1 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                     int k2 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                     int j3 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                     int i4 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                     if(!isBiomeOceanic(l1) && !isBiomeOceanic(k2) && !isBiomeOceanic(j3) && !isBiomeOceanic(i4)) {
                        aint1[j + i * areaWidth] = k;
                     } else {
                        aint1[j + i * areaWidth] = BiomeGenBase.beach.biomeID;
                     }
                  } else {
                     aint1[j + i * areaWidth] = k;
                  }
               } else {
                  int l = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                  int i1 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                  int j1 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                  int k1 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                  if(!isBiomeOceanic(l) && !isBiomeOceanic(i1) && !isBiomeOceanic(j1) && !isBiomeOceanic(k1)) {
                     if(this.func_151633_d(l) && this.func_151633_d(i1) && this.func_151633_d(j1) && this.func_151633_d(k1)) {
                        aint1[j + i * areaWidth] = k;
                     } else {
                        aint1[j + i * areaWidth] = BiomeGenBase.desert.biomeID;
                     }
                  } else {
                     aint1[j + i * areaWidth] = k;
                  }
               }
            } else {
               this.func_151632_a(aint, aint1, j, i, areaWidth, k, BiomeGenBase.stoneBeach.biomeID);
            }
         }
      }

      return aint1;
   }

   private void func_151632_a(int[] p_151632_1_, int[] p_151632_2_, int p_151632_3_, int p_151632_4_, int p_151632_5_, int p_151632_6_, int p_151632_7_) {
      if(isBiomeOceanic(p_151632_6_)) {
         p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_6_;
      } else {
         int i = p_151632_1_[p_151632_3_ + 1 + (p_151632_4_ + 1 - 1) * (p_151632_5_ + 2)];
         int j = p_151632_1_[p_151632_3_ + 1 + 1 + (p_151632_4_ + 1) * (p_151632_5_ + 2)];
         int k = p_151632_1_[p_151632_3_ + 1 - 1 + (p_151632_4_ + 1) * (p_151632_5_ + 2)];
         int l = p_151632_1_[p_151632_3_ + 1 + (p_151632_4_ + 1 + 1) * (p_151632_5_ + 2)];
         if(!isBiomeOceanic(i) && !isBiomeOceanic(j) && !isBiomeOceanic(k) && !isBiomeOceanic(l)) {
            p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_6_;
         } else {
            p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_7_;
         }
      }

   }

   private boolean func_151631_c(int p_151631_1_) {
      return BiomeGenBase.getBiome(p_151631_1_) != null && BiomeGenBase.getBiome(p_151631_1_).getBiomeClass() == BiomeGenJungle.class?true:p_151631_1_ == BiomeGenBase.jungleEdge.biomeID || p_151631_1_ == BiomeGenBase.jungle.biomeID || p_151631_1_ == BiomeGenBase.jungleHills.biomeID || p_151631_1_ == BiomeGenBase.forest.biomeID || p_151631_1_ == BiomeGenBase.taiga.biomeID || isBiomeOceanic(p_151631_1_);
   }

   private boolean func_151633_d(int p_151633_1_) {
      return BiomeGenBase.getBiome(p_151633_1_) instanceof BiomeGenMesa;
   }
}
