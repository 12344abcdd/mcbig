package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ViewArea {
    protected final LevelRenderer levelRenderer;
    protected final Level level;
    protected int sectionGridSizeY;
    protected int sectionGridSizeX;
    protected int sectionGridSizeZ;
    private int viewDistance;
    private SectionPos cameraSectionPos;
    public SectionRenderDispatcher.RenderSection[] sections;

    public ViewArea(SectionRenderDispatcher p_296460_, Level p_110846_, int p_110847_, LevelRenderer p_110848_) {
        this.levelRenderer = p_110848_;
        this.level = p_110846_;
        this.setViewDistance(p_110847_);
        this.createSections(p_296460_);
        this.cameraSectionPos = SectionPos.of(this.viewDistance + 1, 0, this.viewDistance + 1);
    }

    protected void createSections(SectionRenderDispatcher p_294825_) {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("createSections called from wrong thread: " + Thread.currentThread().getName());
        } else {
            int i = this.sectionGridSizeX * this.sectionGridSizeY * this.sectionGridSizeZ;
            this.sections = new SectionRenderDispatcher.RenderSection[i];

            for (int j = 0; j < this.sectionGridSizeX; j++) {
                for (int k = 0; k < this.sectionGridSizeY; k++) {
                    for (int l = 0; l < this.sectionGridSizeZ; l++) {
                        int i1 = this.getSectionIndex(j, k, l);
                        this.sections[i1] = p_294825_.new RenderSection(i1, SectionPos.of(j, k + this.level.getMinSectionY(), l));
                    }
                }
            }
        }
    }

    public void releaseAllBuffers() {
        for (SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection : this.sections) {
            sectionrenderdispatcher$rendersection.releaseBuffers();
        }
    }

    private int getSectionIndex(int p_295999_, int p_294097_, int p_294966_) {
        return (p_294966_ * this.sectionGridSizeY + p_294097_) * this.sectionGridSizeX + p_295999_;
    }

    protected void setViewDistance(int p_110854_) {
        int i = p_110854_ * 2 + 1;
        this.sectionGridSizeX = i;
        this.sectionGridSizeY = this.level.getSectionsCount();
        this.sectionGridSizeZ = i;
        this.viewDistance = p_110854_;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public LevelHeightAccessor getLevelHeightAccessor() {
        return this.level;
    }

    public void repositionCamera(SectionPos p_366723_) {
        for (int i = 0; i < this.sectionGridSizeX; i++) {
            me.alphamode.mcbig.math.BigInteger j = p_366723_.x().subtract(this.viewDistance);
            me.alphamode.mcbig.math.BigInteger k = j.add(me.alphamode.mcbig.math.BigMath.floorMod(me.alphamode.mcbig.math.BigInteger.val(i).subtract(j), this.sectionGridSizeX));

            for (int l = 0; l < this.sectionGridSizeZ; l++) {
                me.alphamode.mcbig.math.BigInteger i1 = p_366723_.z().subtract(this.viewDistance);
                me.alphamode.mcbig.math.BigInteger j1 = i1.add(me.alphamode.mcbig.math.BigMath.floorMod(me.alphamode.mcbig.math.BigInteger.val(l).subtract(i1), this.sectionGridSizeZ));

                for (int k1 = 0; k1 < this.sectionGridSizeY; k1++) {
                    int l1 = this.level.getMinSectionY() + k1;
                    SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = this.sections[this.getSectionIndex(i, k1, l)];
                    SectionPos i2 = sectionrenderdispatcher$rendersection.getSectionNode();
                    if (!i2.equals(SectionPos.of(k, l1, j1))) {
                        sectionrenderdispatcher$rendersection.setSectionNode(SectionPos.of(k, l1, j1));
                    }
                }
            }
        }

        this.cameraSectionPos = p_366723_;
        this.levelRenderer.getSectionOcclusionGraph().invalidate();
    }

    public SectionPos getCameraSectionPos() {
        return this.cameraSectionPos;
    }

    public void setDirty(me.alphamode.mcbig.math.BigInteger p_110860_, int p_110861_, me.alphamode.mcbig.math.BigInteger p_110862_, boolean p_110863_) {
        SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = this.getRenderSection(p_110860_, p_110861_, p_110862_);
        if (sectionrenderdispatcher$rendersection != null) {
            sectionrenderdispatcher$rendersection.setDirty(p_110863_);
        }
    }

    @Nullable
    protected SectionRenderDispatcher.RenderSection getRenderSectionAt(BlockPos p_294239_) {
        return this.getRenderSection(SectionPos.of(p_294239_));
    }

    @Nullable
    protected SectionRenderDispatcher.RenderSection getRenderSection(SectionPos p_366845_) {
//        int i = SectionPos.x(p_366845_);
//        int j = SectionPos.y(p_366845_);
//        int k = SectionPos.z(p_366845_);
        return this.getRenderSection(p_366845_.x(), p_366845_.getY(), p_366845_.z());
    }

    @Nullable
    private SectionRenderDispatcher.RenderSection getRenderSection(me.alphamode.mcbig.math.BigInteger p_366548_, int p_366588_, me.alphamode.mcbig.math.BigInteger p_366508_) {
        if (!this.containsSection(p_366548_, p_366588_, p_366508_)) {
            return null;
        } else {
            int i = p_366588_ - this.level.getMinSectionY();
            me.alphamode.mcbig.math.BigInteger j = me.alphamode.mcbig.math.BigMath.floorMod(p_366548_, this.sectionGridSizeX);
            me.alphamode.mcbig.math.BigInteger k = me.alphamode.mcbig.math.BigMath.floorMod(p_366508_, this.sectionGridSizeZ);
            return this.sections[this.getSectionIndex(j.intValue(), i, k.intValue())];
        }
    }

    private boolean containsSection(me.alphamode.mcbig.math.BigInteger p_366483_, int p_366902_, me.alphamode.mcbig.math.BigInteger p_366901_) {
        if (p_366902_ >= this.level.getMinSectionY() && p_366902_ <= this.level.getMaxSectionY()) {
            return p_366483_.compareTo(this.cameraSectionPos.x().subtract(this.viewDistance)) < 0 || p_366483_.compareTo(this.cameraSectionPos.x().add(this.viewDistance)) > 0
                ? false
                : p_366901_.compareTo(this.cameraSectionPos.z().subtract(this.viewDistance)) >= 0 && p_366901_.compareTo(this.cameraSectionPos.z().add(this.viewDistance)) <= 0;
        } else {
            return false;
        }
    }
}
