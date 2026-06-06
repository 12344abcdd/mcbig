package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WalkNodeEvaluator extends NodeEvaluator {
    public static final double SPACE_BETWEEN_WALL_POSTS = 0.5;
    private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125;
    private final it.unimi.dsi.fastutil.objects.Object2ObjectMap<BlockPos, PathType> pathTypesByPosCacheByMob = new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>();
    private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();
    private final Node[] reusableNeighbors = new Node[Direction.Plane.HORIZONTAL.length()];

    @Override
    public void prepare(PathNavigationRegion p_77620_, Mob p_77621_) {
        super.prepare(p_77620_, p_77621_);
        p_77621_.onPathfindingStart();
    }

    @Override
    public void done() {
        this.mob.onPathfindingDone();
        this.pathTypesByPosCacheByMob.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int i = this.mob.getBlockY();
        BlockState blockstate = this.currentContext.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), (double)i, this.mob.getZ()));
        if (!this.mob.canStandOnFluid(blockstate.getFluidState())) {
            if (this.canFloat() && this.mob.isInWater()) {
                while (true) {
                    if (!blockstate.is(Blocks.WATER) && blockstate.getFluidState() != Fluids.WATER.getSource(false)) {
                        i--;
                        break;
                    }

                    blockstate = this.currentContext.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
                }
            } else if (this.mob.onGround()) {
                i = Mth.floor(this.mob.getY() + 0.5);
            } else {
                blockpos$mutableblockpos.set(this.mob.getX(), this.mob.getY() + 1.0, this.mob.getZ());

                while (blockpos$mutableblockpos.getY() > this.currentContext.level().getMinY()) {
                    i = blockpos$mutableblockpos.getY();
                    blockpos$mutableblockpos.setY(blockpos$mutableblockpos.getY() - 1);
                    BlockState blockstate1 = this.currentContext.getBlockState(blockpos$mutableblockpos);
                    if (!blockstate1.isAir() && !blockstate1.isPathfindable(PathComputationType.LAND)) {
                        break;
                    }
                }
            }
        } else {
            while (this.mob.canStandOnFluid(blockstate.getFluidState())) {
                blockstate = this.currentContext.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
            }

            i--;
        }

        BlockPos blockpos = this.mob.blockPosition();
        if (!this.canStartAt(blockpos$mutableblockpos.set(blockpos.getX(), i, blockpos.getZ()))) {
            AABB aabb = this.mob.getBoundingBox();
            if (this.canStartAt(blockpos$mutableblockpos.set(aabb.minX, (double)i, aabb.minZ))
                    || this.canStartAt(blockpos$mutableblockpos.set(aabb.minX, (double)i, aabb.maxZ))
                    || this.canStartAt(blockpos$mutableblockpos.set(aabb.maxX, (double)i, aabb.minZ))
                    || this.canStartAt(blockpos$mutableblockpos.set(aabb.maxX, (double)i, aabb.maxZ))) {
                return this.getStartNode(blockpos$mutableblockpos);
            }
        }

        return this.getStartNode(new BlockPos(blockpos.getX(), i, blockpos.getZ()));
    }

    protected Node getStartNode(BlockPos p_230632_) {
        Node node = this.getNode(p_230632_);
        node.type = this.getCachedPathType(node.x, node.y, node.z);
        node.costMalus = this.mob.getPathfindingMalus(node.type);
        return node;
    }

    protected boolean canStartAt(BlockPos p_262596_) {
        PathType pathtype = this.getCachedPathType(p_262596_.getBigX(), p_262596_.getY(), p_262596_.getBigZ());
        return pathtype != PathType.OPEN && this.mob.getPathfindingMalus(pathtype) >= 0.0F;
    }

    @Override
    public Target getTarget(double p_326793_, double p_326919_, double p_326802_) {
        return this.getTargetNodeAt(p_326793_, p_326919_, p_326802_);
    }

    @Override
    public int getNeighbors(Node[] p_77640_, Node p_77641_) {
        int i = 0;
        int j = 0;
        PathType pathtype = this.getCachedPathType(p_77641_.x, p_77641_.y + 1, p_77641_.z);
        PathType pathtype1 = this.getCachedPathType(p_77641_.x, p_77641_.y, p_77641_.z);
        if (this.mob.getPathfindingMalus(pathtype) >= 0.0F && pathtype1 != PathType.STICKY_HONEY) {
            j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
        }

        double d0 = this.getFloorLevel(new BlockPos(p_77641_.x, p_77641_.y, p_77641_.z));

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Node node = this.findAcceptedNode(p_77641_.x.add(direction.getStepX()), p_77641_.y, p_77641_.z.add(direction.getStepZ()), j, d0, direction, pathtype1);
            this.reusableNeighbors[direction.get2DDataValue()] = node;
            if (this.isNeighborValid(node, p_77641_)) {
                p_77640_[i++] = node;
            }
        }

        for (Direction direction1 : Direction.Plane.HORIZONTAL) {
            Direction direction2 = direction1.getClockWise();
            if (this.isDiagonalValid(p_77641_, this.reusableNeighbors[direction1.get2DDataValue()], this.reusableNeighbors[direction2.get2DDataValue()])) {
                Node node1 = this.findAcceptedNode(
                        p_77641_.x.add(direction1.getStepX() + direction2.getStepX()),
                        p_77641_.y,
                        p_77641_.z.add(direction1.getStepZ() + direction2.getStepZ()),
                        j,
                        d0,
                        direction1,
                        pathtype1
                );
                if (this.isDiagonalValid(node1)) {
                    p_77640_[i++] = node1;
                }
            }
        }

        return i;
    }

    protected boolean isNeighborValid(@Nullable Node p_77627_, Node p_77628_) {
        return p_77627_ != null && !p_77627_.closed && (p_77627_.costMalus >= 0.0F || p_77628_.costMalus < 0.0F);
    }

    protected boolean isDiagonalValid(Node p_326907_, @Nullable Node p_326803_, @Nullable Node p_326821_) {
        if (p_326821_ == null || p_326803_ == null || p_326821_.y > p_326907_.y || p_326803_.y > p_326907_.y) {
            return false;
        } else if (p_326803_.type != PathType.WALKABLE_DOOR && p_326821_.type != PathType.WALKABLE_DOOR) {
            boolean flag = p_326821_.type == PathType.FENCE && p_326803_.type == PathType.FENCE && (double)this.mob.getBbWidth() < 0.5;
            return (p_326821_.y < p_326907_.y || p_326821_.costMalus >= 0.0F || flag) && (p_326803_.y < p_326907_.y || p_326803_.costMalus >= 0.0F || flag);
        } else {
            return false;
        }
    }

    protected boolean isDiagonalValid(@Nullable Node p_77630_) {
        if (p_77630_ == null || p_77630_.closed) {
            return false;
        } else {
            return p_77630_.type == PathType.WALKABLE_DOOR ? false : p_77630_.costMalus >= 0.0F;
        }
    }

    private static boolean doesBlockHavePartialCollision(PathType p_326827_) {
        return p_326827_ == PathType.FENCE || p_326827_ == PathType.DOOR_WOOD_CLOSED || p_326827_ == PathType.DOOR_IRON_CLOSED;
    }

    private boolean canReachWithoutCollision(Node p_77625_) {
        AABB aabb = this.mob.getBoundingBox();
        Vec3 vec3 = new Vec3(
                (double)p_77625_.x.doubleValue() - this.mob.getX() + aabb.getXsize() / 2.0,
                (double)p_77625_.y - this.mob.getY() + aabb.getYsize() / 2.0,
                (double)p_77625_.z.doubleValue() - this.mob.getZ() + aabb.getZsize() / 2.0
        );
        int i = Mth.ceil(vec3.length() / aabb.getSize());
        vec3 = vec3.scale((double)(1.0F / (float)i));

        for (int j = 1; j <= i; j++) {
            aabb = aabb.move(vec3);
            if (this.hasCollisions(aabb)) {
                return false;
            }
        }

        return true;
    }

    protected double getFloorLevel(BlockPos p_164733_) {
        BlockGetter blockgetter = this.currentContext.level();
        return (this.canFloat() || this.isAmphibious()) && blockgetter.getFluidState(p_164733_).is(FluidTags.WATER)
                ? (double)p_164733_.getY() + 0.5
                : getFloorLevel(blockgetter, p_164733_);
    }

    public static double getFloorLevel(BlockGetter p_77612_, BlockPos p_77613_) {
        BlockPos blockpos = p_77613_.below();
        VoxelShape voxelshape = p_77612_.getBlockState(blockpos).getCollisionShape(p_77612_, blockpos);
        return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0 : voxelshape.max(Direction.Axis.Y));
    }

    protected boolean isAmphibious() {
        return false;
    }

    @Nullable
    protected Node findAcceptedNode(me.alphamode.mcbig.math.BigInteger p_164726_, int p_164727_, me.alphamode.mcbig.math.BigInteger p_164728_, int p_164729_, double p_164730_, Direction p_164731_, PathType p_326873_) {
        Node node = null;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        double d0 = this.getFloorLevel(blockpos$mutableblockpos.set(p_164726_, p_164727_, p_164728_));
        if (d0 - p_164730_ > this.getMobJumpHeight()) {
            return null;
        } else {
            PathType pathtype = this.getCachedPathType(p_164726_, p_164727_, p_164728_);
            float f = this.mob.getPathfindingMalus(pathtype);
            if (f >= 0.0F) {
                node = this.getNodeAndUpdateCostToMax(p_164726_, p_164727_, p_164728_, pathtype, f);
            }

            if (doesBlockHavePartialCollision(p_326873_) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
                node = null;
            }

            if (pathtype != PathType.WALKABLE && (!this.isAmphibious() || pathtype != PathType.WATER)) {
                if ((node == null || node.costMalus < 0.0F)
                        && p_164729_ > 0
                        && (pathtype != PathType.FENCE || this.canWalkOverFences())
                        && pathtype != PathType.UNPASSABLE_RAIL
                        && pathtype != PathType.TRAPDOOR
                        && pathtype != PathType.POWDER_SNOW) {
                    node = this.tryJumpOn(p_164726_, p_164727_, p_164728_, p_164729_, p_164730_, p_164731_, p_326873_, blockpos$mutableblockpos);
                } else if (!this.isAmphibious() && pathtype == PathType.WATER && !this.canFloat()) {
                    node = this.tryFindFirstNonWaterBelow(p_164726_, p_164727_, p_164728_, node);
                } else if (pathtype == PathType.OPEN) {
                    node = this.tryFindFirstGroundNodeBelow(p_164726_, p_164727_, p_164728_);
                } else if (doesBlockHavePartialCollision(pathtype) && node == null) {
                    node = this.getClosedNode(p_164726_, p_164727_, p_164728_, pathtype);
                }

                return node;
            } else {
                return node;
            }
        }
    }

    private double getMobJumpHeight() {
        return Math.max(1.125, (double)this.mob.maxUpStep());
    }

    private Node getNodeAndUpdateCostToMax(me.alphamode.mcbig.math.BigInteger p_230620_, int p_230621_, me.alphamode.mcbig.math.BigInteger p_230622_, PathType p_326789_, float p_230624_) {
        Node node = this.getNode(p_230620_, p_230621_, p_230622_);
        node.type = p_326789_;
        node.costMalus = Math.max(node.costMalus, p_230624_);
        return node;
    }

    private Node getBlockedNode(me.alphamode.mcbig.math.BigInteger p_230628_, int p_230629_, me.alphamode.mcbig.math.BigInteger p_230630_) {
        Node node = this.getNode(p_230628_, p_230629_, p_230630_);
        node.type = PathType.BLOCKED;
        node.costMalus = -1.0F;
        return node;
    }

    private Node getClosedNode(me.alphamode.mcbig.math.BigInteger p_326935_, int p_326904_, me.alphamode.mcbig.math.BigInteger p_326845_, PathType p_326820_) {
        Node node = this.getNode(p_326935_, p_326904_, p_326845_);
        node.closed = true;
        node.type = p_326820_;
        node.costMalus = p_326820_.getMalus();
        return node;
    }

    @Nullable
    private Node tryJumpOn(
            me.alphamode.mcbig.math.BigInteger p_326914_,
            int p_326900_,
            me.alphamode.mcbig.math.BigInteger p_326886_,
            int p_326902_,
            double p_326800_,
            Direction p_326868_,
            PathType p_326831_,
            BlockPos.MutableBlockPos p_326839_
    ) {
        Node node = this.findAcceptedNode(p_326914_, p_326900_ + 1, p_326886_, p_326902_ - 1, p_326800_, p_326868_, p_326831_);
        if (node == null) {
            return null;
        } else if (this.mob.getBbWidth() >= 1.0F) {
            return node;
        } else if (node.type != PathType.OPEN && node.type != PathType.WALKABLE) {
            return node;
        } else {
            double d0 = (double)(p_326914_.intValue() - p_326868_.getStepX()) + 0.5;
            double d1 = (double)(p_326886_.intValue() - p_326868_.getStepZ()) + 0.5;
            double d2 = (double)this.mob.getBbWidth() / 2.0;
            AABB aabb = new AABB(
                    d0 - d2,
                    this.getFloorLevel(p_326839_.set(d0, (double)(p_326900_ + 1), d1)) + 0.001,
                    d1 - d2,
                    d0 + d2,
                    (double)this.mob.getBbHeight() + this.getFloorLevel(p_326839_.set((double)node.x.doubleValue(), (double)node.y, (double)node.z.doubleValue())) - 0.002,
                    d1 + d2
            );
            return this.hasCollisions(aabb) ? null : node;
        }
    }

    @Nullable
    private Node tryFindFirstNonWaterBelow(me.alphamode.mcbig.math.BigInteger p_326959_, int p_326927_, me.alphamode.mcbig.math.BigInteger p_326932_, @Nullable Node p_326880_) {
        p_326927_--;

        while (p_326927_ > this.mob.level().getMinY()) {
            PathType pathtype = this.getCachedPathType(p_326959_, p_326927_, p_326932_);
            if (pathtype != PathType.WATER) {
                return p_326880_;
            }

            p_326880_ = this.getNodeAndUpdateCostToMax(p_326959_, p_326927_, p_326932_, pathtype, this.mob.getPathfindingMalus(pathtype));
            p_326927_--;
        }

        return p_326880_;
    }

    private Node tryFindFirstGroundNodeBelow(me.alphamode.mcbig.math.BigInteger p_326892_, int p_326901_, me.alphamode.mcbig.math.BigInteger p_326809_) {
        for (int i = p_326901_ - 1; i >= this.mob.level().getMinY(); i--) {
            if (p_326901_ - i > this.mob.getMaxFallDistance()) {
                return this.getBlockedNode(p_326892_, i, p_326809_);
            }

            PathType pathtype = this.getCachedPathType(p_326892_, i, p_326809_);
            float f = this.mob.getPathfindingMalus(pathtype);
            if (pathtype != PathType.OPEN) {
                if (f >= 0.0F) {
                    return this.getNodeAndUpdateCostToMax(p_326892_, i, p_326809_, pathtype, f);
                }

                return this.getBlockedNode(p_326892_, i, p_326809_);
            }
        }

        return this.getBlockedNode(p_326892_, p_326901_, p_326809_);
    }

    private boolean hasCollisions(AABB p_77635_) {
        return this.collisionCache.computeIfAbsent(p_77635_, p_330163_ -> !this.currentContext.level().noCollision(this.mob, p_77635_));
    }

    protected PathType getCachedPathType(me.alphamode.mcbig.math.BigInteger p_326926_, int p_326795_, me.alphamode.mcbig.math.BigInteger p_326824_) {
        return this.pathTypesByPosCacheByMob
                .computeIfAbsent(
                        new BlockPos(p_326926_, p_326795_, p_326824_),
                        p_330161_ -> this.getPathTypeOfMob(this.currentContext, p_326926_, p_326795_, p_326824_, this.mob)
                );
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext p_330551_, me.alphamode.mcbig.math.BigInteger p_326939_, int p_326943_, me.alphamode.mcbig.math.BigInteger p_326876_, Mob p_326786_) {
        Set<PathType> set = this.getPathTypeWithinMobBB(p_330551_, p_326939_, p_326943_, p_326876_);
        if (set.contains(PathType.FENCE)) {
            return PathType.FENCE;
        } else if (set.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        } else {
            PathType pathtype = PathType.BLOCKED;

            for (PathType pathtype1 : set) {
                if (p_326786_.getPathfindingMalus(pathtype1) < 0.0F) {
                    return pathtype1;
                }

                if (p_326786_.getPathfindingMalus(pathtype1) >= p_326786_.getPathfindingMalus(pathtype)) {
                    pathtype = pathtype1;
                }
            }

            return this.entityWidth <= 1
                    && pathtype != PathType.OPEN
                    && p_326786_.getPathfindingMalus(pathtype) == 0.0F
                    && this.getPathType(p_330551_, p_326939_, p_326943_, p_326876_) == PathType.OPEN
                    ? PathType.OPEN
                    : pathtype;
        }
    }

    public Set<PathType> getPathTypeWithinMobBB(PathfindingContext p_331617_, me.alphamode.mcbig.math.BigInteger p_326916_, int p_326906_, me.alphamode.mcbig.math.BigInteger p_326848_) {
        EnumSet<PathType> enumset = EnumSet.noneOf(PathType.class);

        for (int i = 0; i < this.entityWidth; i++) {
            for (int j = 0; j < this.entityHeight; j++) {
                for (int k = 0; k < this.entityDepth; k++) {
                    me.alphamode.mcbig.math.BigInteger l = p_326916_.add(i);
                    int i1 = p_326906_ + j;
                    me.alphamode.mcbig.math.BigInteger j1 = p_326848_.add(k);
                    PathType pathtype = this.getPathType(p_331617_, l, i1, j1);
                    BlockPos blockpos = this.mob.blockPosition();
                    boolean flag = this.canPassDoors();
                    if (pathtype == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && flag) {
                        pathtype = PathType.WALKABLE_DOOR;
                    }

                    if (pathtype == PathType.DOOR_OPEN && !flag) {
                        pathtype = PathType.BLOCKED;
                    }

                    if (pathtype == PathType.RAIL
                            && this.getPathType(p_331617_, blockpos.getBigX(), blockpos.getY(), blockpos.getBigZ()) != PathType.RAIL
                            && this.getPathType(p_331617_, blockpos.getBigX(), blockpos.getY() - 1, blockpos.getBigZ()) != PathType.RAIL) {
                        pathtype = PathType.UNPASSABLE_RAIL;
                    }

                    enumset.add(pathtype);
                }
            }
        }

        return enumset;
    }

    @Override
    public PathType getPathType(PathfindingContext p_330217_, me.alphamode.mcbig.math.BigInteger p_326856_, int p_326857_, me.alphamode.mcbig.math.BigInteger p_326859_) {
        return getPathTypeStatic(p_330217_, new BlockPos.MutableBlockPos(p_326856_, p_326857_, p_326859_));
    }

    public static PathType getPathTypeStatic(Mob p_332010_, BlockPos p_330520_) {
        return getPathTypeStatic(new PathfindingContext(p_332010_.level(), p_332010_), p_330520_.mutable());
    }

    public static PathType getPathTypeStatic(PathfindingContext p_330755_, BlockPos.MutableBlockPos p_331020_) {
        me.alphamode.mcbig.math.BigInteger i = p_331020_.getBigX();
        int j = p_331020_.getY();
        me.alphamode.mcbig.math.BigInteger k = p_331020_.getBigZ();
        PathType pathtype = p_330755_.getPathTypeFromState(i, j, k);
        if (pathtype == PathType.OPEN && j >= p_330755_.level().getMinY() + 1) {
            return switch (p_330755_.getPathTypeFromState(i, j - 1, k)) {
                case OPEN, WATER, LAVA, WALKABLE -> PathType.OPEN;
                case DAMAGE_FIRE -> PathType.DAMAGE_FIRE;
                case DAMAGE_OTHER -> PathType.DAMAGE_OTHER;
                case STICKY_HONEY -> PathType.STICKY_HONEY;
                case POWDER_SNOW -> PathType.DANGER_POWDER_SNOW;
                case DAMAGE_CAUTIOUS -> PathType.DAMAGE_CAUTIOUS;
                case TRAPDOOR -> PathType.DANGER_TRAPDOOR;
                default -> checkNeighbourBlocks(p_330755_, i, j, k, PathType.WALKABLE);
            };
        } else {
            return pathtype;
        }
    }

    public static PathType checkNeighbourBlocks(PathfindingContext p_331893_, me.alphamode.mcbig.math.BigInteger p_332169_, int p_330433_, me.alphamode.mcbig.math.BigInteger p_331506_, PathType p_326944_) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (i != 0 || k != 0) {
                        PathType pathtype = p_331893_.getPathTypeFromState(p_332169_.add(i), p_330433_ + j, p_331506_.add(k));
                        if (pathtype == PathType.DAMAGE_OTHER) {
                            return PathType.DANGER_OTHER;
                        }

                        if (pathtype == PathType.DAMAGE_FIRE || pathtype == PathType.LAVA) {
                            return PathType.DANGER_FIRE;
                        }

                        if (pathtype == PathType.WATER) {
                            return PathType.WATER_BORDER;
                        }

                        if (pathtype == PathType.DAMAGE_CAUTIOUS) {
                            return PathType.DAMAGE_CAUTIOUS;
                        }
                    }
                }
            }
        }

        return p_326944_;
    }

    protected static PathType getPathTypeFromState(BlockGetter p_77644_, BlockPos p_77645_) {
        BlockState blockstate = p_77644_.getBlockState(p_77645_);
        Block block = blockstate.getBlock();
        if (blockstate.isAir()) {
            return PathType.OPEN;
        } else if (blockstate.is(BlockTags.TRAPDOORS) || blockstate.is(Blocks.LILY_PAD) || blockstate.is(Blocks.BIG_DRIPLEAF)) {
            return PathType.TRAPDOOR;
        } else if (blockstate.is(Blocks.POWDER_SNOW)) {
            return PathType.POWDER_SNOW;
        } else if (blockstate.is(Blocks.CACTUS) || blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
            return PathType.DAMAGE_OTHER;
        } else if (blockstate.is(Blocks.HONEY_BLOCK)) {
            return PathType.STICKY_HONEY;
        } else if (blockstate.is(Blocks.COCOA)) {
            return PathType.COCOA;
        } else if (!blockstate.is(Blocks.WITHER_ROSE) && !blockstate.is(Blocks.POINTED_DRIPSTONE)) {
            FluidState fluidstate = blockstate.getFluidState();
            if (fluidstate.is(FluidTags.LAVA)) {
                return PathType.LAVA;
            } else if (isBurningBlock(blockstate)) {
                return PathType.DAMAGE_FIRE;
            } else if (block instanceof DoorBlock doorblock) {
                if (blockstate.getValue(DoorBlock.OPEN)) {
                    return PathType.DOOR_OPEN;
                } else {
                    return doorblock.type().canOpenByHand() ? PathType.DOOR_WOOD_CLOSED : PathType.DOOR_IRON_CLOSED;
                }
            } else if (block instanceof BaseRailBlock) {
                return PathType.RAIL;
            } else if (block instanceof LeavesBlock) {
                return PathType.LEAVES;
            } else if (!blockstate.is(BlockTags.FENCES)
                    && !blockstate.is(BlockTags.WALLS)
                    && (!(block instanceof FenceGateBlock) || blockstate.getValue(FenceGateBlock.OPEN))) {
                if (!blockstate.isPathfindable(PathComputationType.LAND)) {
                    return PathType.BLOCKED;
                } else {
                    return fluidstate.is(FluidTags.WATER) ? PathType.WATER : PathType.OPEN;
                }
            } else {
                return PathType.FENCE;
            }
        } else {
            return PathType.DAMAGE_CAUTIOUS;
        }
    }
}
