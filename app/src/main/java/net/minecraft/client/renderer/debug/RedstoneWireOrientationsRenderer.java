package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.network.protocol.common.custom.RedstoneWireOrientationsDebugPayload;
import net.minecraft.world.level.redstone.Orientation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class RedstoneWireOrientationsRenderer implements DebugRenderer.SimpleDebugRenderer {
    public static final int TIMEOUT = 200;
    private final Minecraft minecraft;
    private final List<RedstoneWireOrientationsDebugPayload> updatedWires = Lists.newArrayList();

    RedstoneWireOrientationsRenderer(Minecraft p_364109_) {
        this.minecraft = p_364109_;
    }

    public void addWireOrientations(RedstoneWireOrientationsDebugPayload p_365234_) {
        this.updatedWires.add(p_365234_);
    }

    @Override
    public void render(PoseStack p_363820_, MultiBufferSource p_363043_, double p_364261_, double p_361975_, double p_365341_) {
        VertexConsumer vertexconsumer = p_363043_.getBuffer(RenderType.lines());
        long i = this.minecraft.level.getGameTime();
        Iterator<RedstoneWireOrientationsDebugPayload> iterator = this.updatedWires.iterator();

        while (iterator.hasNext()) {
            RedstoneWireOrientationsDebugPayload redstonewireorientationsdebugpayload = iterator.next();
            long j = i - redstonewireorientationsdebugpayload.time();
            if (j > 200L) {
                iterator.remove();
            } else {
                for (RedstoneWireOrientationsDebugPayload.Wire redstonewireorientationsdebugpayload$wire : redstonewireorientationsdebugpayload.wires()) {
                    Vector3f vector3f = redstonewireorientationsdebugpayload$wire.pos()
                        .getBottomCenter()
                        .subtract(p_364261_, p_361975_ - 0.1, p_365341_)
                        .toVector3f();
                    Orientation orientation = redstonewireorientationsdebugpayload$wire.orientation();
                    ShapeRenderer.renderVector(p_363820_, vertexconsumer, vector3f, orientation.getFront().getUnitVec3().scale(0.5), -16776961);
                    ShapeRenderer.renderVector(p_363820_, vertexconsumer, vector3f, orientation.getUp().getUnitVec3().scale(0.4), -65536);
                    ShapeRenderer.renderVector(p_363820_, vertexconsumer, vector3f, orientation.getSide().getUnitVec3().scale(0.3), -256);
                }
            }
        }
    }
}
