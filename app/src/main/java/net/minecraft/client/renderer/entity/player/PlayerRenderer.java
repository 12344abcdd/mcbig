package net.minecraft.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

public class PlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
    public PlayerRenderer(EntityRendererProvider.Context p_174557_, boolean p_174558_) {
        super(p_174557_, new PlayerModel(p_174557_.bakeLayer(p_174558_ ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), p_174558_), 0.5F);
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel(p_174557_.bakeLayer(p_174558_ ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel(p_174557_.bakeLayer(p_174558_ ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
                p_174557_.getEquipmentRenderer()
            )
        );
        this.addLayer(new PlayerItemInHandLayer<>(this));
        this.addLayer(new ArrowLayer<>(this, p_174557_));
        this.addLayer(new Deadmau5EarsLayer(this, p_174557_.getModelSet()));
        this.addLayer(new CapeLayer(this, p_174557_.getModelSet(), p_174557_.getEquipmentAssets()));
        this.addLayer(new CustomHeadLayer<>(this, p_174557_.getModelSet()));
        this.addLayer(new WingsLayer<>(this, p_174557_.getModelSet(), p_174557_.getEquipmentRenderer()));
        this.addLayer(new ParrotOnShoulderLayer(this, p_174557_.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer(this, p_174557_.getModelSet()));
        this.addLayer(new BeeStingerLayer<>(this, p_174557_));
    }

    protected boolean shouldRenderLayers(PlayerRenderState p_362188_) {
        return !p_362188_.isSpectator;
    }

    public Vec3 getRenderOffset(PlayerRenderState p_360756_) {
        Vec3 vec3 = super.getRenderOffset(p_360756_);
        return p_360756_.isCrouching ? vec3.add(0.0, (double)(p_360756_.scale * -2.0F) / 16.0, 0.0) : vec3;
    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer p_386861_, HumanoidArm p_373044_) {
        ItemStack itemstack = p_386861_.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack itemstack1 = p_386861_.getItemInHand(InteractionHand.OFF_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(p_386861_, itemstack, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(p_386861_, itemstack1, InteractionHand.OFF_HAND);
        if (humanoidmodel$armpose.isTwoHanded()) {
            humanoidmodel$armpose1 = itemstack1.isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        return p_386861_.getMainArm() == p_373044_ ? humanoidmodel$armpose : humanoidmodel$armpose1;
    }

    private static HumanoidModel.ArmPose getArmPose(Player p_386775_, ItemStack p_388403_, InteractionHand p_117796_) {
        if (p_388403_.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (p_386775_.getUsedItemHand() == p_117796_ && p_386775_.getUseItemRemainingTicks() > 0) {
                ItemUseAnimation itemuseanimation = p_388403_.getUseAnimation();
                if (itemuseanimation == ItemUseAnimation.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (itemuseanimation == ItemUseAnimation.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (itemuseanimation == ItemUseAnimation.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (itemuseanimation == ItemUseAnimation.CROSSBOW) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (itemuseanimation == ItemUseAnimation.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }

                if (itemuseanimation == ItemUseAnimation.TOOT_HORN) {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }

                if (itemuseanimation == ItemUseAnimation.BRUSH) {
                    return HumanoidModel.ArmPose.BRUSH;
                }
            } else if (!p_386775_.swinging && p_388403_.is(Items.CROSSBOW) && CrossbowItem.isCharged(p_388403_)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }

    public ResourceLocation getTextureLocation(PlayerRenderState p_364673_) {
        return p_364673_.skin.texture();
    }

    protected void scale(PlayerRenderState p_364943_, PoseStack p_117799_) {
        float f = 0.9375F;
        p_117799_.scale(0.9375F, 0.9375F, 0.9375F);
    }

    protected void renderNameTag(PlayerRenderState p_363185_, Component p_117809_, PoseStack p_117810_, MultiBufferSource p_117811_, int p_117812_) {
        p_117810_.pushPose();
        if (p_363185_.scoreText != null) {
            super.renderNameTag(p_363185_, p_363185_.scoreText, p_117810_, p_117811_, p_117812_);
            p_117810_.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
        }

        super.renderNameTag(p_363185_, p_117809_, p_117810_, p_117811_, p_117812_);
        p_117810_.popPose();
    }

    public PlayerRenderState createRenderState() {
        return new PlayerRenderState();
    }

    public void extractRenderState(AbstractClientPlayer p_361478_, PlayerRenderState p_360583_, float p_364121_) {
        super.extractRenderState(p_361478_, p_360583_, p_364121_);
        HumanoidMobRenderer.extractHumanoidRenderState(p_361478_, p_360583_, p_364121_, this.itemModelResolver);
        p_360583_.leftArmPose = getArmPose(p_361478_, HumanoidArm.LEFT);
        p_360583_.rightArmPose = getArmPose(p_361478_, HumanoidArm.RIGHT);
        p_360583_.skin = p_361478_.getSkin();
        p_360583_.arrowCount = p_361478_.getArrowCount();
        p_360583_.stingerCount = p_361478_.getStingerCount();
        p_360583_.useItemRemainingTicks = p_361478_.getUseItemRemainingTicks();
        p_360583_.swinging = p_361478_.swinging;
        p_360583_.isSpectator = p_361478_.isSpectator();
        p_360583_.showHat = p_361478_.isModelPartShown(PlayerModelPart.HAT);
        p_360583_.showJacket = p_361478_.isModelPartShown(PlayerModelPart.JACKET);
        p_360583_.showLeftPants = p_361478_.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        p_360583_.showRightPants = p_361478_.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        p_360583_.showLeftSleeve = p_361478_.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        p_360583_.showRightSleeve = p_361478_.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        p_360583_.showCape = p_361478_.isModelPartShown(PlayerModelPart.CAPE);
        extractFlightData(p_361478_, p_360583_, p_364121_);
        extractCapeState(p_361478_, p_360583_, p_364121_);
        if (p_360583_.distanceToCameraSq < 100.0) {
            Scoreboard scoreboard = p_361478_.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
            if (objective != null) {
                ReadOnlyScoreInfo readonlyscoreinfo = scoreboard.getPlayerScoreInfo(p_361478_, objective);
                Component component = ReadOnlyScoreInfo.safeFormatValue(readonlyscoreinfo, objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
                p_360583_.scoreText = Component.empty().append(component).append(CommonComponents.SPACE).append(objective.getDisplayName());
            } else {
                p_360583_.scoreText = null;
            }
        } else {
            p_360583_.scoreText = null;
        }

        p_360583_.parrotOnLeftShoulder = getParrotOnShoulder(p_361478_, true);
        p_360583_.parrotOnRightShoulder = getParrotOnShoulder(p_361478_, false);
        p_360583_.id = p_361478_.getId();
        p_360583_.name = p_361478_.getGameProfile().getName();
        p_360583_.heldOnHead.clear();
        if (p_360583_.isUsingItem) {
            ItemStack itemstack = p_361478_.getItemInHand(p_360583_.useItemHand);
            if (itemstack.is(Items.SPYGLASS)) {
                this.itemModelResolver.updateForLiving(p_360583_.heldOnHead, itemstack, ItemDisplayContext.HEAD, false, p_361478_);
            }
        }
    }

    private static void extractFlightData(AbstractClientPlayer p_361452_, PlayerRenderState p_363432_, float p_364796_) {
        p_363432_.fallFlyingTimeInTicks = (float)p_361452_.getFallFlyingTicks() + p_364796_;
        Vec3 vec3 = p_361452_.getViewVector(p_364796_);
        Vec3 vec31 = p_361452_.getDeltaMovementLerped(p_364796_);
        double d0 = vec31.horizontalDistanceSqr();
        double d1 = vec3.horizontalDistanceSqr();
        if (d0 > 0.0 && d1 > 0.0) {
            p_363432_.shouldApplyFlyingYRot = true;
            double d2 = Math.min(1.0, (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1));
            double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
            p_363432_.flyingYRot = (float)(Math.signum(d3) * Math.acos(d2));
        } else {
            p_363432_.shouldApplyFlyingYRot = false;
            p_363432_.flyingYRot = 0.0F;
        }
    }

    private static void extractCapeState(AbstractClientPlayer p_364691_, PlayerRenderState p_360814_, float p_364460_) {
        double d0 = Mth.lerp((double)p_364460_, p_364691_.xCloakO, p_364691_.xCloak) - Mth.lerp((double)p_364460_, p_364691_.xo, p_364691_.getX());
        double d1 = Mth.lerp((double)p_364460_, p_364691_.yCloakO, p_364691_.yCloak) - Mth.lerp((double)p_364460_, p_364691_.yo, p_364691_.getY());
        double d2 = Mth.lerp((double)p_364460_, p_364691_.zCloakO, p_364691_.zCloak) - Mth.lerp((double)p_364460_, p_364691_.zo, p_364691_.getZ());
        float f = Mth.rotLerp(p_364460_, p_364691_.yBodyRotO, p_364691_.yBodyRot);
        double d3 = (double)Mth.sin(f * (float) (Math.PI / 180.0));
        double d4 = (double)(-Mth.cos(f * (float) (Math.PI / 180.0)));
        p_360814_.capeFlap = (float)d1 * 10.0F;
        p_360814_.capeFlap = Mth.clamp(p_360814_.capeFlap, -6.0F, 32.0F);
        p_360814_.capeLean = (float)(d0 * d3 + d2 * d4) * 100.0F;
        p_360814_.capeLean = p_360814_.capeLean * (1.0F - p_360814_.fallFlyingScale());
        p_360814_.capeLean = Mth.clamp(p_360814_.capeLean, 0.0F, 150.0F);
        p_360814_.capeLean2 = (float)(d0 * d4 - d2 * d3) * 100.0F;
        p_360814_.capeLean2 = Mth.clamp(p_360814_.capeLean2, -20.0F, 20.0F);
        float f1 = Mth.lerp(p_364460_, p_364691_.oBob, p_364691_.bob);
        float f2 = Mth.lerp(p_364460_, p_364691_.walkDistO, p_364691_.walkDist);
        p_360814_.capeFlap = p_360814_.capeFlap + Mth.sin(f2 * 6.0F) * 32.0F * f1;
    }

    @Nullable
    private static Parrot.Variant getParrotOnShoulder(AbstractClientPlayer p_363115_, boolean p_360551_) {
        CompoundTag compoundtag = p_360551_ ? p_363115_.getShoulderEntityLeft() : p_363115_.getShoulderEntityRight();
        return EntityType.byString(compoundtag.getString("id")).filter(p_362544_ -> p_362544_ == EntityType.PARROT).isPresent()
            ? Parrot.Variant.byId(compoundtag.getInt("Variant"))
            : null;
    }

    public void renderRightHand(PoseStack p_117771_, MultiBufferSource p_117772_, int p_117773_, ResourceLocation p_363694_, boolean p_366898_) {
        this.renderHand(p_117771_, p_117772_, p_117773_, p_363694_, this.model.rightArm, p_366898_);
    }

    public void renderLeftHand(PoseStack p_117814_, MultiBufferSource p_117815_, int p_117816_, ResourceLocation p_361745_, boolean p_366730_) {
        this.renderHand(p_117814_, p_117815_, p_117816_, p_361745_, this.model.leftArm, p_366730_);
    }

    private void renderHand(PoseStack p_117776_, MultiBufferSource p_117777_, int p_117778_, ResourceLocation p_360319_, ModelPart p_117780_, boolean p_366655_) {
        PlayerModel playermodel = this.getModel();
        p_117780_.resetPose();
        p_117780_.visible = true;
        playermodel.leftSleeve.visible = p_366655_;
        playermodel.rightSleeve.visible = p_366655_;
        playermodel.leftArm.zRot = -0.1F;
        playermodel.rightArm.zRot = 0.1F;
        p_117780_.render(p_117776_, p_117777_.getBuffer(RenderType.entityTranslucent(p_360319_)), p_117778_, OverlayTexture.NO_OVERLAY);
    }

    protected void setupRotations(PlayerRenderState p_363355_, PoseStack p_117803_, float p_117804_, float p_117805_) {
        float f = p_363355_.swimAmount;
        float f1 = p_363355_.xRot;
        if (p_363355_.isFallFlying) {
            super.setupRotations(p_363355_, p_117803_, p_117804_, p_117805_);
            float f2 = p_363355_.fallFlyingScale();
            if (!p_363355_.isAutoSpinAttack) {
                p_117803_.mulPose(Axis.XP.rotationDegrees(f2 * (-90.0F - f1)));
            }

            if (p_363355_.shouldApplyFlyingYRot) {
                p_117803_.mulPose(Axis.YP.rotation(p_363355_.flyingYRot));
            }
        } else if (f > 0.0F) {
            super.setupRotations(p_363355_, p_117803_, p_117804_, p_117805_);
            float f4 = p_363355_.isInWater ? -90.0F - f1 : -90.0F;
            float f3 = Mth.lerp(f, 0.0F, f4);
            p_117803_.mulPose(Axis.XP.rotationDegrees(f3));
            if (p_363355_.isVisuallySwimming) {
                p_117803_.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            super.setupRotations(p_363355_, p_117803_, p_117804_, p_117805_);
        }
    }
}
