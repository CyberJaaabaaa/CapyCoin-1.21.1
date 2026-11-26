package ru.pablocapybar.capycoinmod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.pablocapybar.capycoinmod.CapyCoinMod;
import ru.pablocapybar.capycoinmod.entity.ExplosiveArrowEntity;
import ru.pablocapybar.capycoinmod.entity.PochitaEntity;
import ru.pablocapybar.capycoinmod.entity.ThrownLightningSpear;

// Регистрация типов сущностей мода.
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CapyCoinMod.MOD_ID);

    // Взрывная стрела.
    public static final DeferredHolder<EntityType<?>, EntityType<ExplosiveArrowEntity>> EXPLOSIVE_ARROW =
            ENTITY_TYPES.register("explosive_arrow", () -> EntityType.Builder.<ExplosiveArrowEntity>of(ExplosiveArrowEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("explosive_arrow"));

    // Летящее копьё Резе.
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownLightningSpear>> THROWN_LIGHTNING_SPEAR =
            ENTITY_TYPES.register("thrown_lightning_spear",
                    () -> EntityType.Builder.<ThrownLightningSpear>of(ThrownLightningSpear::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("thrown_lightning_spear"));

    // Почита.
    public static final DeferredHolder<EntityType<?>, EntityType<PochitaEntity>> POCHITA =
            ENTITY_TYPES.register("pochita", () -> EntityType.Builder.of(PochitaEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.5F)
                    .build("pochita"));
}
