package com.JSUSHDX.WorldTriggerMod.client.renderer.entity;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.entity.custom.ShieldEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class ShieldEntityRenderer extends EntityRenderer<ShieldEntity, ShieldEntityRenderer.ShieldEntityRenderState> {
    private static final Identifier SHIELD_TEXTURE = Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "textures/misc/white.png");

    public ShieldEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ShieldEntityRenderState createRenderState() {
        return new ShieldEntityRenderState();
    }

    @Override
    public void extractRenderState(ShieldEntity entity, ShieldEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public void submit(ShieldEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0, 0.5, 0);

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentCullItemTarget(SHIELD_TEXTURE), (pose, vertexConsumer) -> {
            Matrix4f matrix = pose.pose();

            // Setup color
            int r = 50, g = 255, b = 150, a = 120;
            
            // Setup values
            float radius = 1.0f;
            float xOffset = radius * 0.866f; // sqrt(3)/2
            float yOffset = radius * 0.5f;

            // Center
            addVertex(vertexConsumer, matrix, -xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, -xOffset, -yOffset, 0, r, g, b, a);

            // Top triangle
            addVertex(vertexConsumer, matrix, -xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, 0, radius, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, yOffset, 0, r, g, b, a);

            // Bottom triangle
            addVertex(vertexConsumer, matrix, -xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, 0, -radius, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, 0, -radius, 0, r, g, b, a);
            
            // Opposite side
            addVertex(vertexConsumer, matrix, -xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, -xOffset, yOffset, 0, r, g, b, a);

            addVertex(vertexConsumer, matrix, xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, 0, radius, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, -xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, -xOffset, yOffset, 0, r, g, b, a);

            addVertex(vertexConsumer, matrix, 0, -radius, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, -xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, -xOffset, -yOffset, 0, r, g, b, a);
        });

        poseStack.popPose();
        
        super.submit(state, poseStack, collector, cameraState);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z, int r, int g, int b, int a) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880) // Lightmap full bright
                .setNormal(0, 0, 1);
    }

    public static class ShieldEntityRenderState extends EntityRenderState {
        // 在這裡可以儲存要傳遞給 submit 方法的狀態
    }
}
