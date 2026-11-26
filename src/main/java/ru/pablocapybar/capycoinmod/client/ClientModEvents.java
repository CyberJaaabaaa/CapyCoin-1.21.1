package ru.pablocapybar.capycoinmod.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import ru.pablocapybar.capycoinmod.CapyCoinMod;
import ru.pablocapybar.capycoinmod.item.PochitaEggItem;
import ru.pablocapybar.capycoinmod.registry.ModEntities;
import ru.pablocapybar.capycoinmod.registry.ModItems;

// Клиентские обработчики. Регистрируются вручную из CapyCoinMod только на клиенте.
public class ClientModEvents {
    // Подписка клиентских обработчиков на шину мода. Вызывается только на клиенте.
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientModEvents::onClientSetup);
        modEventBus.addListener(ClientModEvents::registerRenderers);
    }

    // Клиентская инициализация. Регистрирует предикаты для анимации натяжения лука, замаха копья и яйца.
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Предикаты регистрируются в основном потоке после загрузки клиента.
        event.enqueueWork(() -> {
            // Предикат pulling для лука: 1.0, пока лук натянут.
            ItemProperties.register(ModItems.LIGHTNING_BOW.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) ->
                            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            // Предикат pull для лука: доля натяжения от 0 до 1 (20 тиков = полная зарядка). Пороги берутся из JSON-модели.
            ItemProperties.register(ModItems.LIGHTNING_BOW.get(), ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null || entity.getUseItem() != stack) return 0.0F;
                        return (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
                    });
            // Предикат pulling для копья: 1.0, пока копьё в замахе. Нужен для переключения на lightning_spear_throw.
            ItemProperties.register(
                    ModItems.LIGHTNING_SPEAR.get(),
                    ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) ->
                            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

            // Предикат "pochita_dead" для яйца: 1.0, если Почита мёртв и яйцо может воскресить его, иначе 0.0.
            // Имя совпадает с ключом "capycoinmod:pochita_dead" в models/item/pochita_egg.json.
            // Читает флаг из самого стака, а не из данных игрока, потому что стак синхронизируется с клиентом.
            ItemProperties.register(ModItems.POCHITA_EGG.get(),
                    ResourceLocation.fromNamespaceAndPath(CapyCoinMod.MOD_ID, "pochita_dead"),
                    (stack, level, entity, seed) -> PochitaEggItem.isRevivable(stack) ? 1.0F : 0.0F);
        });
    }

    // Регистрация рендеров сущностей: взрывная стрела, летящее копьё и Почита.
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.EXPLOSIVE_ARROW.get(), ExplosiveArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_LIGHTNING_SPEAR.get(), ThrownLightningSpearRenderer::new);
        event.registerEntityRenderer(ModEntities.POCHITA.get(), PochitaRenderer::new);
    }
}
