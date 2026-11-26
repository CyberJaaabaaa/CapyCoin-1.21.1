package ru.pablocapybar.capycoinmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.pablocapybar.capycoinmod.entity.ThrownLightningSpear;
import ru.pablocapybar.capycoinmod.registry.ModItems;

// Рендер летящего копья: показывает предмет копья, повёрнутый по направлению полёта.
public class ThrownLightningSpearRenderer extends EntityRenderer<ThrownLightningSpear> {
    public ThrownLightningSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownLightningSpear entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6) {
            Quaternionf rotation = new Quaternionf().rotationTo(
                    new Vector3f(0, 1, 0), new Vector3f((float) motion.x, (float) motion.y, (float) motion.z).normalize());
            pose.mulPose(rotation);
        }
        ItemStack stack = new ItemStack(ModItems.LIGHTNING_SPEAR.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownLightningSpear entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
