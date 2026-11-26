package ru.pablocapybar.capycoinmod;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import ru.pablocapybar.capycoinmod.client.ClientModEvents;
import ru.pablocapybar.capycoinmod.client.LaunchBootsClientEvents;
import ru.pablocapybar.capycoinmod.entity.PochitaEntity;
import ru.pablocapybar.capycoinmod.item.ExplosiveArrowItem;
import ru.pablocapybar.capycoinmod.network.ModNetwork;
import ru.pablocapybar.capycoinmod.registry.ModEntities;
import ru.pablocapybar.capycoinmod.registry.ModItems;
import ru.pablocapybar.capycoinmod.registry.ModSounds;

// Главный класс мода. Только подключает регистрации, сеть, клиент и креативные вкладки.
@Mod(CapyCoinMod.MOD_ID)
public class CapyCoinMod {
    // Идентификатор мода. Совпадает с namespace в ресурсах (assets/capycoinmod/).
    public static final String MOD_ID = "capycoinmod";

    public CapyCoinMod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ModNetwork::register);
        // Атрибуты (здоровье, скорость, урон) для Почиты. Без них сущность не создаётся.
        modEventBus.addListener(EntityAttributeCreationEvent.class, event ->
                event.put(ModEntities.POCHITA.get(), PochitaEntity.createAttributes().build()));

        ModItems.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        // Клиентские обработчики подключаются только на клиенте, иначе сервер загружает клиентские классы и падает.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientModEvents.register(modEventBus);
            LaunchBootsClientEvents.register();
        }
    }

    // Общая настройка для клиента и сервера. Регистрирует поведение раздатчика для взрывной стрелы.
    private void commonSetup(final FMLCommonSetupEvent event) {
        // enqueueWork выполняет код в основном потоке после загрузки всех реестров.
        event.enqueueWork(ExplosiveArrowItem::registerDispenseBehavior);
    }

    // Добавление предметов в креативные вкладки. Вызывается для каждой вкладки отдельно.
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Вкладка «Инструменты и утилиты».
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CAPYCOIN);
        }
        // Вкладка «Бой».
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.KNOCKBACK_SWORD);
            event.accept(ModItems.LAUNCH_SWORD);
            event.accept(ModItems.EXPLOSIVE_ARROW);
            event.accept(ModItems.LIGHTNING_BOW);
            event.accept(ModItems.LAUNCH_BOOTS);
            event.accept(ModItems.LIGHTNING_SPEAR);
            event.accept(ModItems.BOMB_HELMET);
            event.accept(ModItems.POCHITA_EGG);
        }
    }
}
