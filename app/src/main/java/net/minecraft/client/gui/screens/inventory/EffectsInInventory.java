package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

public class EffectsInInventory {
    private static final ResourceLocation EFFECT_BACKGROUND_LARGE_SPRITE = ResourceLocation.withDefaultNamespace("container/inventory/effect_background_large");
    private static final ResourceLocation EFFECT_BACKGROUND_SMALL_SPRITE = ResourceLocation.withDefaultNamespace("container/inventory/effect_background_small");
    private final AbstractContainerScreen<?> screen;
    private final Minecraft minecraft;

    public EffectsInInventory(AbstractContainerScreen<?> p_376708_) {
        this.screen = p_376708_;
        this.minecraft = Minecraft.getInstance();
    }

    public void render(GuiGraphics p_376446_, int p_376167_, int p_376572_, float p_376427_) {
        this.renderEffects(p_376446_, p_376167_, p_376572_);
    }

    public boolean canSeeEffects() {
        int i = this.screen.leftPos + this.screen.imageWidth + 2;
        int j = this.screen.width - i;
        return j >= 32;
    }

    private void renderEffects(GuiGraphics p_376884_, int p_376869_, int p_376740_) {
        int i = this.screen.leftPos + this.screen.imageWidth + 2;
        int j = this.screen.width - i;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty() && j >= 32) {
            boolean flag = j >= 120;
            int k = 33;
            if (collection.size() > 5) {
                k = 132 / (collection.size() - 1);
            }

            Iterable<MobEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
            this.renderBackgrounds(p_376884_, i, k, iterable, flag);
            this.renderIcons(p_376884_, i, k, iterable, flag);
            if (flag) {
                this.renderLabels(p_376884_, i, k, iterable);
            } else if (p_376869_ >= i && p_376869_ <= i + 33) {
                int l = this.screen.topPos;
                MobEffectInstance mobeffectinstance = null;

                for (MobEffectInstance mobeffectinstance1 : iterable) {
                    if (p_376740_ >= l && p_376740_ <= l + k) {
                        mobeffectinstance = mobeffectinstance1;
                    }

                    l += k;
                }

                if (mobeffectinstance != null) {
                    List<Component> list = List.of(
                        this.getEffectName(mobeffectinstance),
                        MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, this.minecraft.level.tickRateManager().tickrate())
                    );
                    p_376884_.renderTooltip(this.screen.getFont(), list, Optional.empty(), p_376869_, p_376740_);
                }
            }
        }
    }

    private void renderBackgrounds(GuiGraphics p_376873_, int p_376485_, int p_376217_, Iterable<MobEffectInstance> p_376444_, boolean p_376129_) {
        int i = this.screen.topPos;

        for (MobEffectInstance mobeffectinstance : p_376444_) {
            if (p_376129_) {
                p_376873_.blitSprite(RenderType::guiTextured, EFFECT_BACKGROUND_LARGE_SPRITE, p_376485_, i, 120, 32);
            } else {
                p_376873_.blitSprite(RenderType::guiTextured, EFFECT_BACKGROUND_SMALL_SPRITE, p_376485_, i, 32, 32);
            }

            i += p_376217_;
        }
    }

    private void renderIcons(GuiGraphics p_376734_, int p_376350_, int p_376678_, Iterable<MobEffectInstance> p_376215_, boolean p_376312_) {
        MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
        int i = this.screen.topPos;

        for (MobEffectInstance mobeffectinstance : p_376215_) {
            Holder<MobEffect> holder = mobeffectinstance.getEffect();
            TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(holder);
            p_376734_.blitSprite(RenderType::guiTextured, textureatlassprite, p_376350_ + (p_376312_ ? 6 : 7), i + 7, 18, 18);
            i += p_376678_;
        }
    }

    private void renderLabels(GuiGraphics p_376227_, int p_376372_, int p_376395_, Iterable<MobEffectInstance> p_376910_) {
        int i = this.screen.topPos;

        for (MobEffectInstance mobeffectinstance : p_376910_) {
            Component component = this.getEffectName(mobeffectinstance);
            p_376227_.drawString(this.screen.getFont(), component, p_376372_ + 10 + 18, i + 6, 16777215);
            Component component1 = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, this.minecraft.level.tickRateManager().tickrate());
            p_376227_.drawString(this.screen.getFont(), component1, p_376372_ + 10 + 18, i + 6 + 10, 8355711);
            i += p_376395_;
        }
    }

    private Component getEffectName(MobEffectInstance p_376252_) {
        MutableComponent mutablecomponent = p_376252_.getEffect().value().getDisplayName().copy();
        if (p_376252_.getAmplifier() >= 1 && p_376252_.getAmplifier() <= 9) {
            mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (p_376252_.getAmplifier() + 1)));
        }

        return mutablecomponent;
    }
}
