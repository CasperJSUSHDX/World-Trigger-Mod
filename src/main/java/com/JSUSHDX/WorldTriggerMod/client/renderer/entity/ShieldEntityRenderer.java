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
        state.yRot = net.minecraft.util.Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        state.xRot = net.minecraft.util.Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(ShieldEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        // 稍微往上平移，對齊胸口高度
        poseStack.translate(0, 0.5, 0);

        // 套用旋轉矩陣，讓盾牌的方向與玩家視角垂直
        // Minecraft 中：yRot 是左右轉頭，xRot 是上下抬頭
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-state.yRot));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(state.xRot));

        // 使用 1.21.4 的新 API 來提交自訂幾何圖形
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentCullItemTarget(SHIELD_TEXTURE), (pose, vertexConsumer) -> {
            Matrix4f matrix = pose.pose();

            // 設定神盾的顏色 (World Trigger 綠色系：偏青綠色)，RGBA 範圍是 0~255，A (透明度) 設為 120
            int r = 50, g = 255, b = 150, a = 120;
            
            // 六角形的半徑
            float radius = 1.0f;
            float xOffset = radius * 0.866f; // sqrt(3)/2
            float yOffset = radius * 0.5f;

            // 1. 中央的長方形
            addVertex(vertexConsumer, matrix, -xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, -xOffset, -yOffset, 0, r, g, b, a);

            // 2. 頂部的三角形 (重複一個頂點變成四邊形)
            addVertex(vertexConsumer, matrix, -xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, 0, radius, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, yOffset, 0, r, g, b, a);

            // 3. 底部的三角形
            addVertex(vertexConsumer, matrix, -xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, xOffset, -yOffset, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, 0, -radius, 0, r, g, b, a);
            addVertex(vertexConsumer, matrix, 0, -radius, 0, r, g, b, a);
            
            // 畫另一面 (雙面渲染，為了讓玩家從後面也能看到，將頂點順序反過來)
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
                .setLight(15728880) // 讓盾牌在黑暗中也會發光 (Lightmap full bright)
                .setNormal(0, 0, 1);
    }

    public static class ShieldEntityRenderState extends EntityRenderState {
        public float yRot;
        public float xRot;
    }
}
