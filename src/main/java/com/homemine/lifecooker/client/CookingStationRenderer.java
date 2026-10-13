package com.homemine.lifecooker.client;

import com.homemine.lifecooker.blockentity.CookingStationBlockEntity;
import com.homemine.lifecooker.cook.CookingStationKind;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class CookingStationRenderer implements BlockEntityRenderer<CookingStationBlockEntity> {
    public CookingStationRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CookingStationBlockEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!entity.isCooking()) {
            return;
        }
        float time = entity.getLevel() == null ? 0.0F : entity.getLevel().getGameTime() + partialTick;
        poseStack.pushPose();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());
        switch (entity.getStationKind()) {
            case GRILL -> renderGrill(consumer, poseStack, time);
            case STOVE -> renderStove(consumer, poseStack, time);
            case INDUCTION -> renderInduction(consumer, poseStack, time);
        }
        poseStack.popPose();
    }

    private static void renderGrill(VertexConsumer c, PoseStack p, float t) {
        float pulse = 0.45F + 0.35F * (0.5F + 0.5F * Mth.sin(t * 0.28F));
        quad(c, p, 0.14F, 0.69F, 0.14F, 0.86F, 0.69F, 0.86F, 1.0F, 0.24F, 0.04F, pulse);
    }

    private static void renderStove(VertexConsumer c, PoseStack p, float t) {
        float pulse = 0.55F + 0.35F * (0.5F + 0.5F * Mth.sin(t * 0.5F));
        quad(c, p, 0.27F, 0.94F, 0.27F, 0.44F, 1.10F, 0.44F, 1.0F, 0.48F, 0.08F, pulse);
        quad(c, p, 0.59F, 0.94F, 0.59F, 0.76F, 1.10F, 0.76F, 1.0F, 0.48F, 0.08F, pulse * 0.9F);
        quad(c, p, 0.28F, 0.04F, 0.28F, 0.73F, 0.48F, 0.73F, 1.0F, 0.30F, 0.06F, pulse * 0.65F);
    }

    private static void renderInduction(VertexConsumer c, PoseStack p, float t) {
        float[][] centers = {{0.22F, 0.22F}, {0.60F, 0.22F}, {0.22F, 0.60F}, {0.60F, 0.60F}};
        for (int i = 0; i < centers.length; i++) {
            float pulse = 0.35F + 0.55F * (0.5F + 0.5F * Mth.sin(t * 0.35F + i * 1.35F));
            float x = centers[i][0], z = centers[i][1];
            quad(c, p, x, 0.6F, z, x + 0.18F, 0.6F, z + 0.18F, 1.0F, 0.06F, 0.03F, pulse);
        }
    }

    private static void quad(VertexConsumer c, PoseStack p, float x1, float y, float z1, float x2, float y2,
            float z2, float r, float g, float b, float a) {
        PoseStack.Pose pose = p.last();
        int alpha = (int) (Mth.clamp(a, 0.0F, 1.0F) * 255.0F);
        vertex(c, pose, x1, y, z1, r, g, b, alpha);
        vertex(c, pose, x2, y, z1, r, g, b, alpha);
        vertex(c, pose, x2, y, z2, r, g, b, alpha);
        vertex(c, pose, x1, y, z2, r, g, b, alpha);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z,
            float r, float g, float b, int a) {
        c.addVertex(pose.pose(), x, y, z).setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), a)
                .setUv2(0xF000F0, 0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
