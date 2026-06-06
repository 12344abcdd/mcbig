package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TeleportCommand {
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(
        Component.translatable("commands.teleport.invalidPosition")
    );

    public static void register(CommandDispatcher<CommandSourceStack> p_139009_) {
        LiteralCommandNode<CommandSourceStack> literalcommandnode = p_139009_.register(
            Commands.literal("teleport")
                .requires(p_139039_ -> p_139039_.hasPermission(2))
                .then(
                    Commands.argument("location", Vec3Argument.vec3())
                        .executes(
                            p_379024_ -> teleportToPos(
                                    p_379024_.getSource(),
                                    Collections.singleton(p_379024_.getSource().getEntityOrException()),
                                    p_379024_.getSource().getLevel(),
                                    Vec3Argument.getCoordinates(p_379024_, "location"),
                                    null,
                                    null
                                )
                        )
                )
                .then(
                    Commands.argument("destination", EntityArgument.entity())
                        .executes(
                            p_139049_ -> teleportToEntity(
                                    p_139049_.getSource(),
                                    Collections.singleton(p_139049_.getSource().getEntityOrException()),
                                    EntityArgument.getEntity(p_139049_, "destination")
                                )
                        )
                )
                .then(
                    Commands.argument("targets", EntityArgument.entities())
                        .then(
                            Commands.argument("location", Vec3Argument.vec3())
                                .executes(
                                    p_379026_ -> teleportToPos(
                                            p_379026_.getSource(),
                                            EntityArgument.getEntities(p_379026_, "targets"),
                                            p_379026_.getSource().getLevel(),
                                            Vec3Argument.getCoordinates(p_379026_, "location"),
                                            null,
                                            null
                                        )
                                )
                                .then(
                                    Commands.argument("rotation", RotationArgument.rotation())
                                        .executes(
                                            p_379025_ -> teleportToPos(
                                                    p_379025_.getSource(),
                                                    EntityArgument.getEntities(p_379025_, "targets"),
                                                    p_379025_.getSource().getLevel(),
                                                    Vec3Argument.getCoordinates(p_379025_, "location"),
                                                    RotationArgument.getRotation(p_379025_, "rotation"),
                                                    null
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("facing")
                                        .then(
                                            Commands.literal("entity")
                                                .then(
                                                    Commands.argument("facingEntity", EntityArgument.entity())
                                                        .executes(
                                                            p_379023_ -> teleportToPos(
                                                                    p_379023_.getSource(),
                                                                    EntityArgument.getEntities(p_379023_, "targets"),
                                                                    p_379023_.getSource().getLevel(),
                                                                    Vec3Argument.getCoordinates(p_379023_, "location"),
                                                                    null,
                                                                    new LookAt.LookAtEntity(
                                                                        EntityArgument.getEntity(p_379023_, "facingEntity"), EntityAnchorArgument.Anchor.FEET
                                                                    )
                                                                )
                                                        )
                                                        .then(
                                                            Commands.argument("facingAnchor", EntityAnchorArgument.anchor())
                                                                .executes(
                                                                    p_379021_ -> teleportToPos(
                                                                            p_379021_.getSource(),
                                                                            EntityArgument.getEntities(p_379021_, "targets"),
                                                                            p_379021_.getSource().getLevel(),
                                                                            Vec3Argument.getCoordinates(p_379021_, "location"),
                                                                            null,
                                                                            new LookAt.LookAtEntity(
                                                                                EntityArgument.getEntity(p_379021_, "facingEntity"),
                                                                                EntityAnchorArgument.getAnchor(p_379021_, "facingAnchor")
                                                                            )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.argument("facingLocation", Vec3Argument.vec3())
                                                .executes(
                                                    p_379022_ -> teleportToPos(
                                                            p_379022_.getSource(),
                                                            EntityArgument.getEntities(p_379022_, "targets"),
                                                            p_379022_.getSource().getLevel(),
                                                            Vec3Argument.getCoordinates(p_379022_, "location"),
                                                            null,
                                                            new LookAt.LookAtPosition(Vec3Argument.getVec3(p_379022_, "facingLocation").toVanilla())
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.argument("destination", EntityArgument.entity())
                                .executes(
                                    p_139011_ -> teleportToEntity(
                                            p_139011_.getSource(),
                                            EntityArgument.getEntities(p_139011_, "targets"),
                                            EntityArgument.getEntity(p_139011_, "destination")
                                        )
                                )
                        )
                )
        );
        p_139009_.register(Commands.literal("tp").requires(p_139013_ -> p_139013_.hasPermission(2)).redirect(literalcommandnode));
    }

    private static int teleportToEntity(CommandSourceStack p_139033_, Collection<? extends Entity> p_139034_, Entity p_139035_) throws CommandSyntaxException {
        for (Entity entity : p_139034_) {
            performTeleport(
                p_139033_,
                entity,
                (ServerLevel)p_139035_.level(),
                p_139035_.getBigX(),
                p_139035_.getY(),
                p_139035_.getBigZ(),
                EnumSet.noneOf(Relative.class),
                p_139035_.getYRot(),
                p_139035_.getXRot(),
                null
            );
        }

        if (p_139034_.size() == 1) {
            p_139033_.sendSuccess(
                () -> Component.translatable(
                        "commands.teleport.success.entity.single", p_139034_.iterator().next().getDisplayName(), p_139035_.getDisplayName()
                    ),
                true
            );
        } else {
            p_139033_.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.multiple", p_139034_.size(), p_139035_.getDisplayName()), true);
        }

        return p_139034_.size();
    }

    private static int teleportToPos(
        CommandSourceStack p_139026_,
        Collection<? extends Entity> p_139027_,
        ServerLevel p_139028_,
        Coordinates p_139029_,
        @Nullable Coordinates p_139030_,
        @Nullable LookAt p_380364_
    ) throws CommandSyntaxException {
        me.alphamode.mcbig.core.BigVec3 vec3 = p_139029_.getPosition(p_139026_);
        Vec2 vec2 = p_139030_ == null ? null : p_139030_.getRotation(p_139026_);

        for (Entity entity : p_139027_) {
            Set<Relative> set = getRelatives(p_139029_, p_139030_, entity.level().dimension() == p_139028_.dimension());
            if (vec2 == null) {
                performTeleport(p_139026_, entity, p_139028_, vec3.x, vec3.y, vec3.z, set, entity.getYRot(), entity.getXRot(), p_380364_);
            } else {
                performTeleport(p_139026_, entity, p_139028_, vec3.x, vec3.y, vec3.z, set, vec2.y, vec2.x, p_380364_);
            }
        }

        if (p_139027_.size() == 1) {
            p_139026_.sendSuccess(
                () -> Component.translatable(
                        "commands.teleport.success.location.single",
                        p_139027_.iterator().next().getDisplayName(),
                        formatDouble(vec3.x.doubleValue()),
                        formatDouble(vec3.y),
                        formatDouble(vec3.z.doubleValue())
                    ),
                true
            );
        } else {
            p_139026_.sendSuccess(
                () -> Component.translatable(
                        "commands.teleport.success.location.multiple", p_139027_.size(), formatDouble(vec3.x.doubleValue()), formatDouble(vec3.y), formatDouble(vec3.z.doubleValue())
                    ),
                true
            );
        }

        return p_139027_.size();
    }

    private static Set<Relative> getRelatives(Coordinates p_374290_, @Nullable Coordinates p_374529_, boolean p_381166_) {
        Set<Relative> set = EnumSet.noneOf(Relative.class);
        if (p_374290_.isXRelative()) {
            set.add(Relative.DELTA_X);
            if (p_381166_) {
                set.add(Relative.X);
            }
        }

        if (p_374290_.isYRelative()) {
            set.add(Relative.DELTA_Y);
            if (p_381166_) {
                set.add(Relative.Y);
            }
        }

        if (p_374290_.isZRelative()) {
            set.add(Relative.DELTA_Z);
            if (p_381166_) {
                set.add(Relative.Z);
            }
        }

        if (p_374529_ == null || p_374529_.isXRelative()) {
            set.add(Relative.X_ROT);
        }

        if (p_374529_ == null || p_374529_.isYRelative()) {
            set.add(Relative.Y_ROT);
        }

        return set;
    }

    private static String formatDouble(double p_142776_) {
        return String.format(Locale.ROOT, "%f", p_142776_);
    }

    private static void performTeleport(
        CommandSourceStack p_139015_,
        Entity p_139016_,
        ServerLevel p_139017_,
        me.alphamode.mcbig.math.BigDecimal p_139018_,
        double p_139019_,
        me.alphamode.mcbig.math.BigDecimal p_139020_,
        Set<Relative> p_139021_,
        float p_139022_,
        float p_139023_,
        @Nullable LookAt p_380216_
    ) throws CommandSyntaxException {
        BlockPos blockpos = BlockPos.containing(p_139018_, p_139019_, p_139020_);
        if (!Level.isInSpawnableBounds(blockpos)) {
            throw INVALID_POSITION.create();
        } else {
            me.alphamode.mcbig.math.BigDecimal d0 = p_139021_.contains(Relative.X) ? p_139018_.subtract(p_139016_.getBigX()) : p_139018_;
            double d1 = p_139021_.contains(Relative.Y) ? p_139019_ - p_139016_.getY() : p_139019_;
            me.alphamode.mcbig.math.BigDecimal d2 = p_139021_.contains(Relative.Z) ? p_139020_.subtract(p_139016_.getBigZ()) : p_139020_;
            float f = p_139021_.contains(Relative.Y_ROT) ? p_139022_ - p_139016_.getYRot() : p_139022_;
            float f1 = p_139021_.contains(Relative.X_ROT) ? p_139023_ - p_139016_.getXRot() : p_139023_;
            float f2 = Mth.wrapDegrees(f);
            float f3 = Mth.wrapDegrees(f1);
            if (p_139016_.teleportTo(p_139017_, d0, d1, d2, p_139021_, f2, f3, true)) {
                if (p_380216_ != null) {
                    p_380216_.perform(p_139015_, p_139016_);
                }

                if (!(p_139016_ instanceof LivingEntity livingentity) || !livingentity.isFallFlying()) {
                    p_139016_.setDeltaMovement(p_139016_.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                    p_139016_.setOnGround(true);
                }

                if (p_139016_ instanceof PathfinderMob pathfindermob) {
                    pathfindermob.getNavigation().stop();
                }
            }
        }
    }
}
