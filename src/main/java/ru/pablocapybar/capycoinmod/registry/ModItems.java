package ru.pablocapybar.capycoinmod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.pablocapybar.capycoinmod.CapyCoinMod;
import ru.pablocapybar.capycoinmod.item.BombHelmetItem;
import ru.pablocapybar.capycoinmod.item.ExplosiveArrowItem;
import ru.pablocapybar.capycoinmod.item.KnockbackSwordItem;
import ru.pablocapybar.capycoinmod.item.LaunchSwordItem;
import ru.pablocapybar.capycoinmod.item.LightningBowItem;
import ru.pablocapybar.capycoinmod.item.LightningSpearItem;
import ru.pablocapybar.capycoinmod.item.PochitaEggItem;

import java.util.List;
import java.util.Map;

import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID;
import static net.minecraft.world.item.Item.BASE_ATTACK_SPEED_ID;

// Регистрация всех предметов и материалов брони мода в игровых реестрах.
public class ModItems {
    // Реестр предметов, привязанный к MOD_ID.
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CapyCoinMod.MOD_ID);

    // Реестр материалов брони. Нужен, чтобы свой материал попал в игру.
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, CapyCoinMod.MOD_ID);



    // Простой предмет-монета без особого поведения.
    public static final DeferredItem<Item> CAPYCOIN = ITEMS.register("capycoin", () -> new Item(new Item.Properties()));

    // Алмазный меч с отбрасыванием цели.
    public static final DeferredItem<Item> KNOCKBACK_SWORD = ITEMS.register("knockback_sword",
            () -> new KnockbackSwordItem(Tiers.DIAMOND,
                    new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4f))));

    // Алмазный меч, подбрасывающий цель.
    public static final DeferredItem<Item> LAUNCH_SWORD = ITEMS.register("launch_sword",
            () -> new LaunchSwordItem(Tiers.DIAMOND,
                    new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4f))));

    // Стрела, которая взрывается при попадании.
    public static final DeferredItem<Item> EXPLOSIVE_ARROW = ITEMS.register("explosive_arrow",
            () -> new ExplosiveArrowItem(new Item.Properties()));

    // Яйцо Почиты: призывает питомца и воскрешает его после смерти.
    public static final DeferredItem<Item> POCHITA_EGG = ITEMS.registerItem("pochita_egg",
            PochitaEggItem::new);

    // Лук, который с шансом 30% призывает молнии после попадания. Прочность 384, как у обычного лука.
    public static final DeferredItem<Item> LIGHTNING_BOW = ITEMS.register("lightning_bow",
            () -> new LightningBowItem(new Item.Properties().durability(384)));

    // Копьё Резе
    public static final DeferredItem<Item> LIGHTNING_SPEAR = ITEMS.register("lightning_spear",
            () -> new LightningSpearItem(new Item.Properties().stacksTo(1)
                    .attributes(ItemAttributeModifiers.builder()
                            // Урон: базовые 1 игрока + 9 = 10 итогового урона
                            .add(Attributes.ATTACK_DAMAGE,
                                    new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 9.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            // Скорость атаки: базовые 4 игрока - 2.3 = 1.7
                            .add(Attributes.ATTACK_SPEED,
                                    new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.3, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            // Дальность ближнего удара: базовые 3 игрока + 0.5
                            .add(Attributes.ENTITY_INTERACTION_RANGE,
                                    new AttributeModifier(
                                            ResourceLocation.fromNamespaceAndPath(CapyCoinMod.MOD_ID, "lightning_spear_reach"),
                                            0.5, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            .build())));

    // Шлем-бомба: через 14 секунд ношения взрывается.
    public static final DeferredItem<Item> BOMB_HELMET = ITEMS.register("bomb_helmet",
            () -> new BombHelmetItem(new Item.Properties().stacksTo(1)));

    // Материал ботинок. Имя "launch_boots" связывает его с файлом textures/models/armor/launch_boots_layer_1.png.
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LAUNCH_BOOTS_MATERIAL =
            ARMOR_MATERIALS.register("launch_boots", () -> new ArmorMaterial(
                    // Защита по типам брони. Значения взяты от алмаза.
                    Map.of(ArmorItem.Type.HELMET, 3, ArmorItem.Type.CHESTPLATE, 8,
                            ArmorItem.Type.LEGGINGS, 6, ArmorItem.Type.BOOTS, 3),
                    // Зачаровываемость. Чем выше число, тем лучше шанс получить сильные чары.
                    10,
                    // Звук при надевании.
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    // Предмет, которым ремонтируют ботинки в наковальне.
                    () -> Ingredient.of(Items.DIAMOND),
                    // Слои текстуры на персонаже. Для ботинок используется layer_1.
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(CapyCoinMod.MOD_ID, "launch_boots"))),
                    // Прочность брони против сильных ударов. Алмаз: 2.0.
                    2.0f,
                    // Сопротивление отбрасыванию. Алмаз: 0.
                    0.0f));

    // Ботинки. Защита и текстура берутся из LAUNCH_BOOTS_MATERIAL.
    public static final DeferredItem<Item> LAUNCH_BOOTS = ITEMS.register("launch_boots",
            () -> new ArmorItem(LAUNCH_BOOTS_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // Передаёт реестры в шину событий мода, чтобы предметы и материалы зарегистрировались при запуске.
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        ARMOR_MATERIALS.register(eventBus);
    }
}
