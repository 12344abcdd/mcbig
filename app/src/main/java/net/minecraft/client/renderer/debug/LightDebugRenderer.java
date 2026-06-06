package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LightDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int MAX_RENDER_DIST = 10;

    public LightDebugRenderer(Minecraft p_113585_) {
        this.minecraft = p_113585_;
    }

    @Override
    public void render(PoseStack p_113587_, MultiBufferSource p_113588_, double p_113589_, double p_113590_, double p_113591_) {
        Level level = this.minecraft.level;
        BlockPos blockpos = BlockPos.containing(p_113589_, p_113590_, p_113591_);
        it.unimi.dsi.fastutil.objects.ObjectSet<SectionPos> longset = new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>();

        for (BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-10, -10, -10), blockpos.offset(10, 10, 10))) {
            int i = level.getBrightness(LightLayer.SKY, blockpos1);
            float f = (float)(15 - i) / 15.0F * 0.5F + 0.16F;
            int j = Mth.hsvToRgb(f, 0.9F, 0.9F);
            SectionPos k = SectionPos.of(blockpos1);
            if (longset.add(k)) {
                DebugRenderer.renderFloatingText(
                    p_113587_,
                    p_113588_,
                    level.getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, k),
                    (double)SectionPos.sectionToBlockCoord(k.x(), 8).doubleValue(),
                    (double)SectionPos.sectionToBlockCoord(k.y(), 8).doubleValue(),
                    (double)SectionPos.sectionToBlockCoord(k.z(), 8).doubleValue(),
                    16711680,
                    0.3F
                );
            }

            if (i != 15) {
                DebugRenderer.renderFloatingText(
                    p_113587_, p_113588_, String.valueOf(i), (double)blockpos1.getX() + 0.5, (double)blockpos1.getY() + 0.25, (double)blockpos1.getZ() + 0.5, j
                );
            }
        }
    }
}
