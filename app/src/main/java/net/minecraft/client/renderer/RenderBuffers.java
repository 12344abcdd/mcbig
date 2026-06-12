package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SequencedMap;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelBakery;

public class RenderBuffers {
    private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
    private final SectionBufferBuilderPool sectionBufferPool;
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource crumblingBufferSource;
    private final OutlineBufferSource outlineBufferSource;

    public RenderBuffers(int p_307464_) {
        this.sectionBufferPool = SectionBufferBuilderPool.allocate(p_307464_);
        SequencedMap<RenderType, ByteBufferBuilder> sequencedmap = Util.make(new Object2ObjectLinkedOpenHashMap<>(), p_366352_ -> {
            p_366352_.put(Sheets.solidBlockSheet(), this.fixedBufferPack.buffer(RenderType.solid()));
            p_366352_.put(Sheets.cutoutBlockSheet(), this.fixedBufferPack.buffer(RenderType.cutout()));
            p_366352_.put(Sheets.bannerSheet(), this.fixedBufferPack.buffer(RenderType.cutoutMipped()));
            p_366352_.put(Sheets.translucentItemSheet(), this.fixedBufferPack.buffer(RenderType.translucent()));
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, Sheets.shieldSheet());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, Sheets.bedSheet());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, Sheets.shulkerBoxSheet());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, Sheets.signSheet());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, Sheets.hangingSignSheet());
            p_366352_.put(Sheets.chestSheet(), new ByteBufferBuilder(786432));
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, RenderType.armorEntityGlint());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, RenderType.glint());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, RenderType.glintTranslucent());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, RenderType.entityGlint());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_366352_, RenderType.waterMask());
        });
        this.bufferSource = MultiBufferSource.immediateWithBuffers(sequencedmap, new ByteBufferBuilder(786432));
        this.outlineBufferSource = new OutlineBufferSource(this.bufferSource);
        SequencedMap<RenderType, ByteBufferBuilder> sequencedmap1 = Util.make(
            new Object2ObjectLinkedOpenHashMap<>(),
            p_359206_ -> ModelBakery.DESTROY_TYPES
                    .forEach(p_173062_ -> put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_359206_, p_173062_))
        );
        this.crumblingBufferSource = MultiBufferSource.immediateWithBuffers(sequencedmap1, new ByteBufferBuilder(0));
    }

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> p_110102_, RenderType p_110103_) {
        p_110102_.put(p_110103_, new ByteBufferBuilder(p_110103_.bufferSize()));
    }

    public SectionBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public SectionBufferBuilderPool sectionBufferPool() {
        return this.sectionBufferPool;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public MultiBufferSource.BufferSource crumblingBufferSource() {
        return this.crumblingBufferSource;
    }

    public OutlineBufferSource outlineBufferSource() {
        return this.outlineBufferSource;
    }
}
