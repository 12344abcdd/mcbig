package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class GuiGraphics {
    public static final float MAX_GUI_Z = 10000.0F;
    public static final float MIN_GUI_Z = -10000.0F;
    private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
    private final Minecraft minecraft;
    private final PoseStack pose;
    private final MultiBufferSource.BufferSource bufferSource;
    private final GuiGraphics.ScissorStack scissorStack = new GuiGraphics.ScissorStack();
    private final GuiSpriteManager sprites;
    private final ItemStackRenderState scratchItemStackRenderState = new ItemStackRenderState();

    private GuiGraphics(Minecraft p_282144_, PoseStack p_281551_, MultiBufferSource.BufferSource p_281460_) {
        this.minecraft = p_282144_;
        this.pose = p_281551_;
        this.bufferSource = p_281460_;
        this.sprites = p_282144_.getGuiSprites();
    }

    public GuiGraphics(Minecraft p_283406_, MultiBufferSource.BufferSource p_282238_) {
        this(p_283406_, new PoseStack(), p_282238_);
    }

    public int guiWidth() {
        return this.minecraft.getWindow().getGuiScaledWidth();
    }

    public int guiHeight() {
        return this.minecraft.getWindow().getGuiScaledHeight();
    }

    public PoseStack pose() {
        return this.pose;
    }

    public void flush() {
        this.bufferSource.endBatch();
    }

    public void hLine(int p_283318_, int p_281662_, int p_281346_, int p_281672_) {
        this.hLine(RenderType.gui(), p_283318_, p_281662_, p_281346_, p_281672_);
    }

    public void hLine(RenderType p_286630_, int p_286453_, int p_286247_, int p_286814_, int p_286623_) {
        if (p_286247_ < p_286453_) {
            int i = p_286453_;
            p_286453_ = p_286247_;
            p_286247_ = i;
        }

        this.fill(p_286630_, p_286453_, p_286814_, p_286247_ + 1, p_286814_ + 1, p_286623_);
    }

    public void vLine(int p_282951_, int p_281591_, int p_281568_, int p_282718_) {
        this.vLine(RenderType.gui(), p_282951_, p_281591_, p_281568_, p_282718_);
    }

    public void vLine(RenderType p_286607_, int p_286309_, int p_286480_, int p_286707_, int p_286855_) {
        if (p_286707_ < p_286480_) {
            int i = p_286480_;
            p_286480_ = p_286707_;
            p_286707_ = i;
        }

        this.fill(p_286607_, p_286309_, p_286480_ + 1, p_286309_ + 1, p_286707_, p_286855_);
    }

    public void enableScissor(int p_281479_, int p_282788_, int p_282924_, int p_282826_) {
        ScreenRectangle screenrectangle = new ScreenRectangle(p_281479_, p_282788_, p_282924_ - p_281479_, p_282826_ - p_282788_)
            .transformAxisAligned(this.pose.last().pose());
        this.applyScissor(this.scissorStack.push(screenrectangle));
    }

    public void disableScissor() {
        this.applyScissor(this.scissorStack.pop());
    }

    public boolean containsPointInScissor(int p_332689_, int p_332771_) {
        return this.scissorStack.containsPoint(p_332689_, p_332771_);
    }

    private void applyScissor(@Nullable ScreenRectangle p_281569_) {
        this.flush();
        if (p_281569_ != null) {
            Window window = Minecraft.getInstance().getWindow();
            int i = window.getHeight();
            double d0 = window.getGuiScale();
            double d1 = (double)p_281569_.left() * d0;
            double d2 = (double)i - (double)p_281569_.bottom() * d0;
            double d3 = (double)p_281569_.width() * d0;
            double d4 = (double)p_281569_.height() * d0;
            RenderSystem.enableScissor((int)d1, (int)d2, Math.max(0, (int)d3), Math.max(0, (int)d4));
        } else {
            RenderSystem.disableScissor();
        }
    }

    public void fill(int p_282988_, int p_282861_, int p_281278_, int p_281710_, int p_281470_) {
        this.fill(p_282988_, p_282861_, p_281278_, p_281710_, 0, p_281470_);
    }

    public void fill(int p_281437_, int p_283660_, int p_282606_, int p_283413_, int p_283428_, int p_283253_) {
        this.fill(RenderType.gui(), p_281437_, p_283660_, p_282606_, p_283413_, p_283428_, p_283253_);
    }

    public void fill(RenderType p_286602_, int p_286738_, int p_286614_, int p_286741_, int p_286610_, int p_286560_) {
        this.fill(p_286602_, p_286738_, p_286614_, p_286741_, p_286610_, 0, p_286560_);
    }

    public void fill(RenderType p_286711_, int p_286234_, int p_286444_, int p_286244_, int p_286411_, int p_286671_, int p_286599_) {
        Matrix4f matrix4f = this.pose.last().pose();
        if (p_286234_ < p_286244_) {
            int i = p_286234_;
            p_286234_ = p_286244_;
            p_286244_ = i;
        }

        if (p_286444_ < p_286411_) {
            int j = p_286444_;
            p_286444_ = p_286411_;
            p_286411_ = j;
        }

        VertexConsumer vertexconsumer = this.bufferSource.getBuffer(p_286711_);
        vertexconsumer.addVertex(matrix4f, (float)p_286234_, (float)p_286444_, (float)p_286671_).setColor(p_286599_);
        vertexconsumer.addVertex(matrix4f, (float)p_286234_, (float)p_286411_, (float)p_286671_).setColor(p_286599_);
        vertexconsumer.addVertex(matrix4f, (float)p_286244_, (float)p_286411_, (float)p_286671_).setColor(p_286599_);
        vertexconsumer.addVertex(matrix4f, (float)p_286244_, (float)p_286444_, (float)p_286671_).setColor(p_286599_);
    }

    public void fillGradient(int p_283290_, int p_283278_, int p_282670_, int p_281698_, int p_283374_, int p_283076_) {
        this.fillGradient(p_283290_, p_283278_, p_282670_, p_281698_, 0, p_283374_, p_283076_);
    }

    public void fillGradient(int p_282702_, int p_282331_, int p_281415_, int p_283118_, int p_282419_, int p_281954_, int p_282607_) {
        this.fillGradient(RenderType.gui(), p_282702_, p_282331_, p_281415_, p_283118_, p_281954_, p_282607_, p_282419_);
    }

    public void fillGradient(RenderType p_286522_, int p_286535_, int p_286839_, int p_286242_, int p_286856_, int p_286809_, int p_286833_, int p_286706_) {
        VertexConsumer vertexconsumer = this.bufferSource.getBuffer(p_286522_);
        this.fillGradient(vertexconsumer, p_286535_, p_286839_, p_286242_, p_286856_, p_286706_, p_286809_, p_286833_);
    }

    private void fillGradient(VertexConsumer p_286862_, int p_283414_, int p_281397_, int p_283587_, int p_281521_, int p_283505_, int p_283131_, int p_282949_) {
        Matrix4f matrix4f = this.pose.last().pose();
        p_286862_.addVertex(matrix4f, (float)p_283414_, (float)p_281397_, (float)p_283505_).setColor(p_283131_);
        p_286862_.addVertex(matrix4f, (float)p_283414_, (float)p_281521_, (float)p_283505_).setColor(p_282949_);
        p_286862_.addVertex(matrix4f, (float)p_283587_, (float)p_281521_, (float)p_283505_).setColor(p_282949_);
        p_286862_.addVertex(matrix4f, (float)p_283587_, (float)p_281397_, (float)p_283505_).setColor(p_283131_);
    }

    public void fillRenderType(RenderType p_331805_, int p_330261_, int p_330693_, int p_331143_, int p_331708_, int p_330497_) {
        Matrix4f matrix4f = this.pose.last().pose();
        VertexConsumer vertexconsumer = this.bufferSource.getBuffer(p_331805_);
        vertexconsumer.addVertex(matrix4f, (float)p_330261_, (float)p_330693_, (float)p_330497_);
        vertexconsumer.addVertex(matrix4f, (float)p_330261_, (float)p_331708_, (float)p_330497_);
        vertexconsumer.addVertex(matrix4f, (float)p_331143_, (float)p_331708_, (float)p_330497_);
        vertexconsumer.addVertex(matrix4f, (float)p_331143_, (float)p_330693_, (float)p_330497_);
    }

    public void drawCenteredString(Font p_282122_, String p_282898_, int p_281490_, int p_282853_, int p_281258_) {
        this.drawString(p_282122_, p_282898_, p_281490_ - p_282122_.width(p_282898_) / 2, p_282853_, p_281258_);
    }

    public void drawCenteredString(Font p_282901_, Component p_282456_, int p_283083_, int p_282276_, int p_281457_) {
        FormattedCharSequence formattedcharsequence = p_282456_.getVisualOrderText();
        this.drawString(p_282901_, formattedcharsequence, p_283083_ - p_282901_.width(formattedcharsequence) / 2, p_282276_, p_281457_);
    }

    public void drawCenteredString(Font p_282592_, FormattedCharSequence p_281854_, int p_281573_, int p_283511_, int p_282577_) {
        this.drawString(p_282592_, p_281854_, p_281573_ - p_282592_.width(p_281854_) / 2, p_283511_, p_282577_);
    }

    public int drawString(Font p_282003_, @Nullable String p_281403_, int p_282714_, int p_282041_, int p_281908_) {
        return this.drawString(p_282003_, p_281403_, p_282714_, p_282041_, p_281908_, true);
    }

    public int drawString(Font p_283343_, @Nullable String p_281896_, int p_283569_, int p_283418_, int p_281560_, boolean p_282130_) {
        return p_281896_ == null
            ? 0
            : p_283343_.drawInBatch(
                p_281896_,
                (float)p_283569_,
                (float)p_283418_,
                p_281560_,
                p_282130_,
                this.pose.last().pose(),
                this.bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                15728880
            );
    }

    public int drawString(Font p_283019_, FormattedCharSequence p_283376_, int p_283379_, int p_283346_, int p_282119_) {
        return this.drawString(p_283019_, p_283376_, p_283379_, p_283346_, p_282119_, true);
    }

    public int drawString(Font p_282636_, FormattedCharSequence p_281596_, int p_281586_, int p_282816_, int p_281743_, boolean p_282394_) {
        return p_282636_.drawInBatch(
            p_281596_,
            (float)p_281586_,
            (float)p_282816_,
            p_281743_,
            p_282394_,
            this.pose.last().pose(),
            this.bufferSource,
            Font.DisplayMode.NORMAL,
            0,
            15728880
        );
    }

    public int drawString(Font p_281653_, Component p_283140_, int p_283102_, int p_282347_, int p_281429_) {
        return this.drawString(p_281653_, p_283140_, p_283102_, p_282347_, p_281429_, true);
    }

    public int drawString(Font p_281547_, Component p_282131_, int p_282857_, int p_281250_, int p_282195_, boolean p_282791_) {
        return this.drawString(p_281547_, p_282131_.getVisualOrderText(), p_282857_, p_281250_, p_282195_, p_282791_);
    }

    public void drawWordWrap(Font p_281494_, FormattedText p_283463_, int p_282183_, int p_283250_, int p_282564_, int p_282629_) {
        this.drawWordWrap(p_281494_, p_283463_, p_282183_, p_283250_, p_282564_, p_282629_, true);
    }

    public void drawWordWrap(Font p_382905_, FormattedText p_382794_, int p_383047_, int p_382923_, int p_382857_, int p_382915_, boolean p_383224_) {
        for (FormattedCharSequence formattedcharsequence : p_382905_.split(p_382794_, p_382857_)) {
            this.drawString(p_382905_, formattedcharsequence, p_383047_, p_382923_, p_382915_, p_383224_);
            p_382923_ += 9;
        }
    }

    public int drawStringWithBackdrop(Font p_348650_, Component p_348614_, int p_348465_, int p_348495_, int p_348581_, int p_348666_) {
        int i = this.minecraft.options.getBackgroundColor(0.0F);
        if (i != 0) {
            int j = 2;
            this.fill(p_348465_ - 2, p_348495_ - 2, p_348465_ + p_348581_ + 2, p_348495_ + 9 + 2, ARGB.multiply(i, p_348666_));
        }

        return this.drawString(p_348650_, p_348614_, p_348465_, p_348495_, p_348666_, true);
    }

    public void renderOutline(int p_281496_, int p_282076_, int p_281334_, int p_283576_, int p_283618_) {
        this.fill(p_281496_, p_282076_, p_281496_ + p_281334_, p_282076_ + 1, p_283618_);
        this.fill(p_281496_, p_282076_ + p_283576_ - 1, p_281496_ + p_281334_, p_282076_ + p_283576_, p_283618_);
        this.fill(p_281496_, p_282076_ + 1, p_281496_ + 1, p_282076_ + p_283576_ - 1, p_283618_);
        this.fill(p_281496_ + p_281334_ - 1, p_282076_ + 1, p_281496_ + p_281334_, p_282076_ + p_283576_ - 1, p_283618_);
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_363890_, ResourceLocation p_294915_, int p_295058_, int p_294415_, int p_294535_, int p_295510_
    ) {
        this.blitSprite(p_363890_, p_294915_, p_295058_, p_294415_, p_294535_, p_295510_, -1);
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_365436_, ResourceLocation p_365379_, int p_294695_, int p_296458_, int p_294279_, int p_295235_, int p_295034_
    ) {
        TextureAtlasSprite textureatlassprite = this.sprites.getSprite(p_365379_);
        GuiSpriteScaling guispritescaling = this.sprites.getSpriteScaling(textureatlassprite);
        if (guispritescaling instanceof GuiSpriteScaling.Stretch) {
            this.blitSprite(p_365436_, textureatlassprite, p_294695_, p_296458_, p_294279_, p_295235_, p_295034_);
        } else if (guispritescaling instanceof GuiSpriteScaling.Tile guispritescaling$tile) {
            this.blitTiledSprite(
                p_365436_,
                textureatlassprite,
                p_294695_,
                p_296458_,
                p_294279_,
                p_295235_,
                0,
                0,
                guispritescaling$tile.width(),
                guispritescaling$tile.height(),
                guispritescaling$tile.width(),
                guispritescaling$tile.height(),
                p_295034_
            );
        } else if (guispritescaling instanceof GuiSpriteScaling.NineSlice guispritescaling$nineslice) {
            this.blitNineSlicedSprite(p_365436_, textureatlassprite, guispritescaling$nineslice, p_294695_, p_296458_, p_294279_, p_295235_, p_295034_);
        }
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_364966_,
        ResourceLocation p_294549_,
        int p_294560_,
        int p_295075_,
        int p_294098_,
        int p_295872_,
        int p_294414_,
        int p_362199_,
        int p_363608_,
        int p_365523_
    ) {
        TextureAtlasSprite textureatlassprite = this.sprites.getSprite(p_294549_);
        GuiSpriteScaling guispritescaling = this.sprites.getSpriteScaling(textureatlassprite);
        if (guispritescaling instanceof GuiSpriteScaling.Stretch) {
            this.blitSprite(p_364966_, textureatlassprite, p_294560_, p_295075_, p_294098_, p_295872_, p_294414_, p_362199_, p_363608_, p_365523_, -1);
        } else {
            this.enableScissor(p_294414_, p_362199_, p_294414_ + p_363608_, p_362199_ + p_365523_);
            this.blitSprite(p_364966_, p_294549_, p_294414_ - p_294098_, p_362199_ - p_295872_, p_294560_, p_295075_, -1);
            this.disableScissor();
        }
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_364096_, TextureAtlasSprite p_361089_, int p_294223_, int p_296245_, int p_296255_, int p_295669_
    ) {
        this.blitSprite(p_364096_, p_361089_, p_294223_, p_296245_, p_296255_, p_295669_, -1);
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_363285_,
        TextureAtlasSprite p_364680_,
        int p_295194_,
        int p_295164_,
        int p_294823_,
        int p_295650_,
        int p_295401_
    ) {
        if (p_294823_ != 0 && p_295650_ != 0) {
            this.innerBlit(
                p_363285_,
                p_364680_.atlasLocation(),
                p_295194_,
                p_295194_ + p_294823_,
                p_295164_,
                p_295164_ + p_295650_,
                p_364680_.getU0(),
                p_364680_.getU1(),
                p_364680_.getV0(),
                p_364680_.getV1(),
                p_295401_
            );
        }
    }

    private void blitSprite(
        Function<ResourceLocation, RenderType> p_364269_,
        TextureAtlasSprite p_295122_,
        int p_295850_,
        int p_296348_,
        int p_295804_,
        int p_296465_,
        int p_295717_,
        int p_360779_,
        int p_363595_,
        int p_364585_,
        int p_361093_
    ) {
        if (p_363595_ != 0 && p_364585_ != 0) {
            this.innerBlit(
                p_364269_,
                p_295122_.atlasLocation(),
                p_295717_,
                p_295717_ + p_363595_,
                p_360779_,
                p_360779_ + p_364585_,
                p_295122_.getU((float)p_295804_ / (float)p_295850_),
                p_295122_.getU((float)(p_295804_ + p_363595_) / (float)p_295850_),
                p_295122_.getV((float)p_296465_ / (float)p_296348_),
                p_295122_.getV((float)(p_296465_ + p_364585_) / (float)p_296348_),
                p_361093_
            );
        }
    }

    private void blitNineSlicedSprite(
        Function<ResourceLocation, RenderType> p_362339_,
        TextureAtlasSprite p_294394_,
        GuiSpriteScaling.NineSlice p_295735_,
        int p_294769_,
        int p_294546_,
        int p_294421_,
        int p_295807_,
        int p_295009_
    ) {
        GuiSpriteScaling.NineSlice.Border guispritescaling$nineslice$border = p_295735_.border();
        int i = Math.min(guispritescaling$nineslice$border.left(), p_294421_ / 2);
        int j = Math.min(guispritescaling$nineslice$border.right(), p_294421_ / 2);
        int k = Math.min(guispritescaling$nineslice$border.top(), p_295807_ / 2);
        int l = Math.min(guispritescaling$nineslice$border.bottom(), p_295807_ / 2);
        if (p_294421_ == p_295735_.width() && p_295807_ == p_295735_.height()) {
            this.blitSprite(p_362339_, p_294394_, p_295735_.width(), p_295735_.height(), 0, 0, p_294769_, p_294546_, p_294421_, p_295807_, p_295009_);
        } else if (p_295807_ == p_295735_.height()) {
            this.blitSprite(p_362339_, p_294394_, p_295735_.width(), p_295735_.height(), 0, 0, p_294769_, p_294546_, i, p_295807_, p_295009_);
            this.blitNineSliceInnerSegment(
                p_362339_,
                p_295735_,
                p_294394_,
                p_294769_ + i,
                p_294546_,
                p_294421_ - j - i,
                p_295807_,
                i,
                0,
                p_295735_.width() - j - i,
                p_295735_.height(),
                p_295735_.width(),
                p_295735_.height(),
                p_295009_
            );
            this.blitSprite(
                p_362339_,
                p_294394_,
                p_295735_.width(),
                p_295735_.height(),
                p_295735_.width() - j,
                0,
                p_294769_ + p_294421_ - j,
                p_294546_,
                j,
                p_295807_,
                p_295009_
            );
        } else if (p_294421_ == p_295735_.width()) {
            this.blitSprite(p_362339_, p_294394_, p_295735_.width(), p_295735_.height(), 0, 0, p_294769_, p_294546_, p_294421_, k, p_295009_);
            this.blitNineSliceInnerSegment(
                p_362339_,
                p_295735_,
                p_294394_,
                p_294769_,
                p_294546_ + k,
                p_294421_,
                p_295807_ - l - k,
                0,
                k,
                p_295735_.width(),
                p_295735_.height() - l - k,
                p_295735_.width(),
                p_295735_.height(),
                p_295009_
            );
            this.blitSprite(
                p_362339_,
                p_294394_,
                p_295735_.width(),
                p_295735_.height(),
                0,
                p_295735_.height() - l,
                p_294769_,
                p_294546_ + p_295807_ - l,
                p_294421_,
                l,
                p_295009_
            );
        } else {
            this.blitSprite(p_362339_, p_294394_, p_295735_.width(), p_295735_.height(), 0, 0, p_294769_, p_294546_, i, k, p_295009_);
            this.blitNineSliceInnerSegment(
                p_362339_,
                p_295735_,
                p_294394_,
                p_294769_ + i,
                p_294546_,
                p_294421_ - j - i,
                k,
                i,
                0,
                p_295735_.width() - j - i,
                k,
                p_295735_.width(),
                p_295735_.height(),
                p_295009_
            );
            this.blitSprite(
                p_362339_, p_294394_, p_295735_.width(), p_295735_.height(), p_295735_.width() - j, 0, p_294769_ + p_294421_ - j, p_294546_, j, k, p_295009_
            );
            this.blitSprite(
                p_362339_, p_294394_, p_295735_.width(), p_295735_.height(), 0, p_295735_.height() - l, p_294769_, p_294546_ + p_295807_ - l, i, l, p_295009_
            );
            this.blitNineSliceInnerSegment(
                p_362339_,
                p_295735_,
                p_294394_,
                p_294769_ + i,
                p_294546_ + p_295807_ - l,
                p_294421_ - j - i,
                l,
                i,
                p_295735_.height() - l,
                p_295735_.width() - j - i,
                l,
                p_295735_.width(),
                p_295735_.height(),
                p_295009_
            );
            this.blitSprite(
                p_362339_,
                p_294394_,
                p_295735_.width(),
                p_295735_.height(),
                p_295735_.width() - j,
                p_295735_.height() - l,
                p_294769_ + p_294421_ - j,
                p_294546_ + p_295807_ - l,
                j,
                l,
                p_295009_
            );
            this.blitNineSliceInnerSegment(
                p_362339_,
                p_295735_,
                p_294394_,
                p_294769_,
                p_294546_ + k,
                i,
                p_295807_ - l - k,
                0,
                k,
                i,
                p_295735_.height() - l - k,
                p_295735_.width(),
                p_295735_.height(),
                p_295009_
            );
            this.blitNineSliceInnerSegment(
                p_362339_,
                p_295735_,
                p_294394_,
                p_294769_ + i,
                p_294546_ + k,
                p_294421_ - j - i,
                p_295807_ - l - k,
                i,
                k,
                p_295735_.width() - j - i,
                p_295735_.height() - l - k,
                p_295735_.width(),
                p_295735_.height(),
                p_295009_
            );
            this.blitNineSliceInnerSegment(
                p_362339_,
                p_295735_,
                p_294394_,
                p_294769_ + p_294421_ - j,
                p_294546_ + k,
                j,
                p_295807_ - l - k,
                p_295735_.width() - j,
                k,
                j,
                p_295735_.height() - l - k,
                p_295735_.width(),
                p_295735_.height(),
                p_295009_
            );
        }
    }

    private void blitNineSliceInnerSegment(
        Function<ResourceLocation, RenderType> p_371823_,
        GuiSpriteScaling.NineSlice p_371657_,
        TextureAtlasSprite p_371812_,
        int p_371894_,
        int p_371565_,
        int p_371606_,
        int p_371781_,
        int p_371379_,
        int p_371448_,
        int p_371442_,
        int p_371801_,
        int p_371588_,
        int p_371206_,
        int p_371311_
    ) {
        if (p_371606_ > 0 && p_371781_ > 0) {
            if (p_371657_.stretchInner()) {
                this.innerBlit(
                    p_371823_,
                    p_371812_.atlasLocation(),
                    p_371894_,
                    p_371894_ + p_371606_,
                    p_371565_,
                    p_371565_ + p_371781_,
                    p_371812_.getU((float)p_371379_ / (float)p_371588_),
                    p_371812_.getU((float)(p_371379_ + p_371442_) / (float)p_371588_),
                    p_371812_.getV((float)p_371448_ / (float)p_371206_),
                    p_371812_.getV((float)(p_371448_ + p_371801_) / (float)p_371206_),
                    p_371311_
                );
            } else {
                this.blitTiledSprite(
                    p_371823_,
                    p_371812_,
                    p_371894_,
                    p_371565_,
                    p_371606_,
                    p_371781_,
                    p_371379_,
                    p_371448_,
                    p_371442_,
                    p_371801_,
                    p_371588_,
                    p_371206_,
                    p_371311_
                );
            }
        }
    }

    private void blitTiledSprite(
        Function<ResourceLocation, RenderType> p_360478_,
        TextureAtlasSprite p_294349_,
        int p_295093_,
        int p_296434_,
        int p_295268_,
        int p_295203_,
        int p_296398_,
        int p_295542_,
        int p_296165_,
        int p_296256_,
        int p_294814_,
        int p_296352_,
        int p_296203_
    ) {
        if (p_295268_ > 0 && p_295203_ > 0) {
            if (p_296165_ > 0 && p_296256_ > 0) {
                for (int i = 0; i < p_295268_; i += p_296165_) {
                    int j = Math.min(p_296165_, p_295268_ - i);

                    for (int k = 0; k < p_295203_; k += p_296256_) {
                        int l = Math.min(p_296256_, p_295203_ - k);
                        this.blitSprite(p_360478_, p_294349_, p_294814_, p_296352_, p_296398_, p_295542_, p_295093_ + i, p_296434_ + k, j, l, p_296203_);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + p_296165_ + "x" + p_296256_);
            }
        }
    }

    public void blit(
        Function<ResourceLocation, RenderType> p_363559_,
        ResourceLocation p_282034_,
        int p_283671_,
        int p_282377_,
        float p_282285_,
        float p_283199_,
        int p_282058_,
        int p_281939_,
        int p_282186_,
        int p_282322_,
        int p_282481_
    ) {
        this.blit(p_363559_, p_282034_, p_283671_, p_282377_, p_282285_, p_283199_, p_282058_, p_281939_, p_282058_, p_281939_, p_282186_, p_282322_, p_282481_);
    }

    public void blit(
        Function<ResourceLocation, RenderType> p_361481_,
        ResourceLocation p_283573_,
        int p_283574_,
        int p_283670_,
        float p_283029_,
        float p_283061_,
        int p_283545_,
        int p_282845_,
        int p_282558_,
        int p_282832_
    ) {
        this.blit(p_361481_, p_283573_, p_283574_, p_283670_, p_283029_, p_283061_, p_283545_, p_282845_, p_283545_, p_282845_, p_282558_, p_282832_);
    }

    public void blit(
        Function<ResourceLocation, RenderType> p_361404_,
        ResourceLocation p_282639_,
        int p_282732_,
        int p_283541_,
        float p_282660_,
        float p_281522_,
        int p_281760_,
        int p_283298_,
        int p_283429_,
        int p_282193_,
        int p_281980_,
        int p_282315_
    ) {
        this.blit(p_361404_, p_282639_, p_282732_, p_283541_, p_282660_, p_281522_, p_281760_, p_283298_, p_283429_, p_282193_, p_281980_, p_282315_, -1);
    }

    public void blit(
        Function<ResourceLocation, RenderType> p_363000_,
        ResourceLocation p_363701_,
        int p_282225_,
        int p_281487_,
        float p_363958_,
        float p_363869_,
        int p_281985_,
        int p_281329_,
        int p_283035_,
        int p_363829_,
        int p_365041_,
        int p_361356_,
        int p_363808_
    ) {
        this.innerBlit(
            p_363000_,
            p_363701_,
            p_282225_,
            p_282225_ + p_281985_,
            p_281487_,
            p_281487_ + p_281329_,
            (p_363958_ + 0.0F) / (float)p_365041_,
            (p_363958_ + (float)p_283035_) / (float)p_365041_,
            (p_363869_ + 0.0F) / (float)p_361356_,
            (p_363869_ + (float)p_363829_) / (float)p_361356_,
            p_363808_
        );
    }

    private void innerBlit(
        Function<ResourceLocation, RenderType> p_362282_,
        ResourceLocation p_283254_,
        int p_283092_,
        int p_281930_,
        int p_282113_,
        int p_281388_,
        float p_281327_,
        float p_281676_,
        float p_283166_,
        float p_282630_,
        int p_283583_
    ) {
        RenderType rendertype = p_362282_.apply(p_283254_);
        Matrix4f matrix4f = this.pose.last().pose();
        VertexConsumer vertexconsumer = this.bufferSource.getBuffer(rendertype);
        vertexconsumer.addVertex(matrix4f, (float)p_283092_, (float)p_282113_, 0.0F).setUv(p_281327_, p_283166_).setColor(p_283583_);
        vertexconsumer.addVertex(matrix4f, (float)p_283092_, (float)p_281388_, 0.0F).setUv(p_281327_, p_282630_).setColor(p_283583_);
        vertexconsumer.addVertex(matrix4f, (float)p_281930_, (float)p_281388_, 0.0F).setUv(p_281676_, p_282630_).setColor(p_283583_);
        vertexconsumer.addVertex(matrix4f, (float)p_281930_, (float)p_282113_, 0.0F).setUv(p_281676_, p_283166_).setColor(p_283583_);
    }

    public void renderItem(ItemStack p_281978_, int p_282647_, int p_281944_) {
        this.renderItem(this.minecraft.player, this.minecraft.level, p_281978_, p_282647_, p_281944_, 0);
    }

    public void renderItem(ItemStack p_282262_, int p_283221_, int p_283496_, int p_283435_) {
        this.renderItem(this.minecraft.player, this.minecraft.level, p_282262_, p_283221_, p_283496_, p_283435_);
    }

    public void renderItem(ItemStack p_282786_, int p_282502_, int p_282976_, int p_281592_, int p_282314_) {
        this.renderItem(this.minecraft.player, this.minecraft.level, p_282786_, p_282502_, p_282976_, p_281592_, p_282314_);
    }

    public void renderFakeItem(ItemStack p_281946_, int p_283299_, int p_283674_) {
        this.renderFakeItem(p_281946_, p_283299_, p_283674_, 0);
    }

    public void renderFakeItem(ItemStack p_312904_, int p_312257_, int p_312674_, int p_312138_) {
        this.renderItem(null, this.minecraft.level, p_312904_, p_312257_, p_312674_, p_312138_);
    }

    public void renderItem(LivingEntity p_282154_, ItemStack p_282777_, int p_282110_, int p_281371_, int p_283572_) {
        this.renderItem(p_282154_, p_282154_.level(), p_282777_, p_282110_, p_281371_, p_283572_);
    }

    private void renderItem(@Nullable LivingEntity p_283524_, @Nullable Level p_282461_, ItemStack p_283653_, int p_283141_, int p_282560_, int p_282425_) {
        this.renderItem(p_283524_, p_282461_, p_283653_, p_283141_, p_282560_, p_282425_, 0);
    }

    private void renderItem(
        @Nullable LivingEntity p_282619_, @Nullable Level p_281754_, ItemStack p_281675_, int p_281271_, int p_282210_, int p_283260_, int p_281995_
    ) {
        if (!p_281675_.isEmpty()) {
            this.minecraft
                .getItemModelResolver()
                .updateForTopItem(this.scratchItemStackRenderState, p_281675_, ItemDisplayContext.GUI, false, p_281754_, p_282619_, p_283260_);
            this.pose.pushPose();
            this.pose.translate((float)(p_281271_ + 8), (float)(p_282210_ + 8), (float)(150 + (this.scratchItemStackRenderState.isGui3d() ? p_281995_ : 0)));

            try {
                this.pose.scale(16.0F, -16.0F, 16.0F);
                boolean flag = !this.scratchItemStackRenderState.usesBlockLight();
                if (flag) {
                    this.flush();
                    Lighting.setupForFlatItems();
                }

                this.scratchItemStackRenderState.render(this.pose, this.bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
                this.flush();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(p_281675_.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(p_281675_.getComponents()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(p_281675_.hasFoil()));
                throw new ReportedException(crashreport);
            }

            this.pose.popPose();
        }
    }

    public void renderItemDecorations(Font p_281721_, ItemStack p_281514_, int p_282056_, int p_282683_) {
        this.renderItemDecorations(p_281721_, p_281514_, p_282056_, p_282683_, null);
    }

    public void renderItemDecorations(Font p_282005_, ItemStack p_283349_, int p_282641_, int p_282146_, @Nullable String p_282803_) {
        if (!p_283349_.isEmpty()) {
            this.pose.pushPose();
            this.renderItemBar(p_283349_, p_282641_, p_282146_);
            this.renderItemCount(p_282005_, p_283349_, p_282641_, p_282146_, p_282803_);
            this.renderItemCooldown(p_283349_, p_282641_, p_282146_);
            this.pose.popPose();
        }
    }

    public void renderTooltip(Font p_282308_, ItemStack p_282781_, int p_282687_, int p_282292_) {
        this.renderTooltip(
            p_282308_,
            Screen.getTooltipFromItem(this.minecraft, p_282781_),
            p_282781_.getTooltipImage(),
            p_282687_,
            p_282292_,
            p_282781_.get(DataComponents.TOOLTIP_STYLE)
        );
    }

    public void renderTooltip(Font p_283128_, List<Component> p_282716_, Optional<TooltipComponent> p_281682_, int p_283678_, int p_281696_) {
        this.renderTooltip(p_283128_, p_282716_, p_281682_, p_283678_, p_281696_, null);
    }

    public void renderTooltip(
        Font p_371715_, List<Component> p_371741_, Optional<TooltipComponent> p_371604_, int p_371500_, int p_371755_, @Nullable ResourceLocation p_371766_
    ) {
        List<ClientTooltipComponent> list = p_371741_.stream()
            .map(Component::getVisualOrderText)
            .map(ClientTooltipComponent::create)
            .collect(Util.toMutableList());
        p_371604_.ifPresent(p_337410_ -> list.add(list.isEmpty() ? 0 : 1, ClientTooltipComponent.create(p_337410_)));
        this.renderTooltipInternal(p_371715_, list, p_371500_, p_371755_, DefaultTooltipPositioner.INSTANCE, p_371766_);
    }

    public void renderTooltip(Font p_282269_, Component p_282572_, int p_282044_, int p_282545_) {
        this.renderTooltip(p_282269_, p_282572_, p_282044_, p_282545_, null);
    }

    public void renderTooltip(Font p_373080_, Component p_372937_, int p_372898_, int p_372815_, @Nullable ResourceLocation p_373023_) {
        this.renderTooltip(p_373080_, List.of(p_372937_.getVisualOrderText()), p_372898_, p_372815_, p_373023_);
    }

    public void renderComponentTooltip(Font p_282739_, List<Component> p_281832_, int p_282191_, int p_282446_) {
        this.renderComponentTooltip(p_282739_, p_281832_, p_282191_, p_282446_, null);
    }

    public void renderComponentTooltip(Font p_371677_, List<Component> p_371519_, int p_371314_, int p_371389_, @Nullable ResourceLocation p_371458_) {
        this.renderTooltipInternal(
            p_371677_,
            p_371519_.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
            p_371314_,
            p_371389_,
            DefaultTooltipPositioner.INSTANCE,
            p_371458_
        );
    }

    public void renderTooltip(Font p_282192_, List<? extends FormattedCharSequence> p_282297_, int p_281680_, int p_283325_) {
        this.renderTooltip(p_282192_, p_282297_, p_281680_, p_283325_, null);
    }

    public void renderTooltip(
        Font p_373106_, List<? extends FormattedCharSequence> p_373020_, int p_372927_, int p_372819_, @Nullable ResourceLocation p_372954_
    ) {
        this.renderTooltipInternal(
            p_373106_,
            p_373020_.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()),
            p_372927_,
            p_372819_,
            DefaultTooltipPositioner.INSTANCE,
            p_372954_
        );
    }

    public void renderTooltip(Font p_281627_, List<FormattedCharSequence> p_283313_, ClientTooltipPositioner p_283571_, int p_282367_, int p_282806_) {
        this.renderTooltipInternal(
            p_281627_, p_283313_.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), p_282367_, p_282806_, p_283571_, null
        );
    }

    private void renderTooltipInternal(
        Font p_282675_,
        List<ClientTooltipComponent> p_282615_,
        int p_283230_,
        int p_283417_,
        ClientTooltipPositioner p_282442_,
        @Nullable ResourceLocation p_371327_
    ) {
        if (!p_282615_.isEmpty()) {
            int i = 0;
            int j = p_282615_.size() == 1 ? -2 : 0;

            for (ClientTooltipComponent clienttooltipcomponent : p_282615_) {
                int k = clienttooltipcomponent.getWidth(p_282675_);
                if (k > i) {
                    i = k;
                }

                j += clienttooltipcomponent.getHeight(p_282675_);
            }

            int i2 = i;
            int j2 = j;
            Vector2ic vector2ic = p_282442_.positionTooltip(this.guiWidth(), this.guiHeight(), p_283230_, p_283417_, i, j);
            int l = vector2ic.x();
            int i1 = vector2ic.y();
            this.pose.pushPose();
            int j1 = 400;
            TooltipRenderUtil.renderTooltipBackground(this, l, i1, i, j, 400, p_371327_);
            this.pose.translate(0.0F, 0.0F, 400.0F);
            int k1 = i1;

            for (int l1 = 0; l1 < p_282615_.size(); l1++) {
                ClientTooltipComponent clienttooltipcomponent1 = p_282615_.get(l1);
                clienttooltipcomponent1.renderText(p_282675_, l, k1, this.pose.last().pose(), this.bufferSource);
                k1 += clienttooltipcomponent1.getHeight(p_282675_) + (l1 == 0 ? 2 : 0);
            }

            k1 = i1;

            for (int k2 = 0; k2 < p_282615_.size(); k2++) {
                ClientTooltipComponent clienttooltipcomponent2 = p_282615_.get(k2);
                clienttooltipcomponent2.renderImage(p_282675_, l, k1, i2, j2, this);
                k1 += clienttooltipcomponent2.getHeight(p_282675_) + (k2 == 0 ? 2 : 0);
            }

            this.pose.popPose();
        }
    }

    private void renderItemBar(ItemStack p_380278_, int p_379972_, int p_379916_) {
        if (p_380278_.isBarVisible()) {
            int i = p_379972_ + 2;
            int j = p_379916_ + 13;
            this.fill(RenderType.gui(), i, j, i + 13, j + 2, 200, -16777216);
            this.fill(RenderType.gui(), i, j, i + p_380278_.getBarWidth(), j + 1, 200, ARGB.opaque(p_380278_.getBarColor()));
        }
    }

    private void renderItemCount(Font p_380115_, ItemStack p_379291_, int p_379544_, int p_380291_, @Nullable String p_380189_) {
        if (p_379291_.getCount() != 1 || p_380189_ != null) {
            String s = p_380189_ == null ? String.valueOf(p_379291_.getCount()) : p_380189_;
            this.pose.pushPose();
            this.pose.translate(0.0F, 0.0F, 200.0F);
            this.drawString(p_380115_, s, p_379544_ + 19 - 2 - p_380115_.width(s), p_380291_ + 6 + 3, -1, true);
            this.pose.popPose();
        }
    }

    private void renderItemCooldown(ItemStack p_380199_, int p_380397_, int p_379741_) {
        LocalPlayer localplayer = this.minecraft.player;
        float f = localplayer == null
            ? 0.0F
            : localplayer.getCooldowns().getCooldownPercent(p_380199_, this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        if (f > 0.0F) {
            int i = p_379741_ + Mth.floor(16.0F * (1.0F - f));
            int j = i + Mth.ceil(16.0F * f);
            this.fill(RenderType.gui(), p_380397_, i, p_380397_ + 16, j, 200, Integer.MAX_VALUE);
        }
    }

    public void renderComponentHoverEffect(Font p_282584_, @Nullable Style p_282156_, int p_283623_, int p_282114_) {
        if (p_282156_ != null && p_282156_.getHoverEvent() != null) {
            HoverEvent hoverevent = p_282156_.getHoverEvent();
            HoverEvent.ItemStackInfo hoverevent$itemstackinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);
            if (hoverevent$itemstackinfo != null) {
                this.renderTooltip(p_282584_, hoverevent$itemstackinfo.getItemStack(), p_283623_, p_282114_);
            } else {
                HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);
                if (hoverevent$entitytooltipinfo != null) {
                    if (this.minecraft.options.advancedItemTooltips) {
                        this.renderComponentTooltip(p_282584_, hoverevent$entitytooltipinfo.getTooltipLines(), p_283623_, p_282114_);
                    }
                } else {
                    Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
                    if (component != null) {
                        this.renderTooltip(p_282584_, p_282584_.split(component, Math.max(this.guiWidth() / 2, 200)), p_283623_, p_282114_);
                    }
                }
            }
        }
    }

    public void drawSpecial(Consumer<MultiBufferSource> p_371453_) {
        p_371453_.accept(this.bufferSource);
        this.bufferSource.endBatch();
    }

    @OnlyIn(Dist.CLIENT)
    static class ScissorStack {
        private final Deque<ScreenRectangle> stack = new ArrayDeque<>();

        public ScreenRectangle push(ScreenRectangle p_281812_) {
            ScreenRectangle screenrectangle = this.stack.peekLast();
            if (screenrectangle != null) {
                ScreenRectangle screenrectangle1 = Objects.requireNonNullElse(p_281812_.intersection(screenrectangle), ScreenRectangle.empty());
                this.stack.addLast(screenrectangle1);
                return screenrectangle1;
            } else {
                this.stack.addLast(p_281812_);
                return p_281812_;
            }
        }

        @Nullable
        public ScreenRectangle pop() {
            if (this.stack.isEmpty()) {
                throw new IllegalStateException("Scissor stack underflow");
            } else {
                this.stack.removeLast();
                return this.stack.peekLast();
            }
        }

        public boolean containsPoint(int p_332682_, int p_332655_) {
            return this.stack.isEmpty() ? true : this.stack.peek().containsPoint(p_332682_, p_332655_);
        }
    }
}
