package ru.pablocapybar.capycoinmod.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import ru.pablocapybar.capycoinmod.entity.PochitaEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

// Рендерер Почиты. Модель, текстура и анимации берутся из assets/capycoinmod по имени pochita.
public class PochitaRenderer extends GeoEntityRenderer<PochitaEntity> {
    public PochitaRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
                ResourceLocation.fromNamespaceAndPath("capycoinmod", "pochita")));
        // Коэффициент уменьшения модели: 0.5 — вдвое меньше исходного размера.
        this.withScale(0.5F);
    }
}
