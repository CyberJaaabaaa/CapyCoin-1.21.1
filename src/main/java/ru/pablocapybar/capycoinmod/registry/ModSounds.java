package ru.pablocapybar.capycoinmod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static ru.pablocapybar.capycoinmod.CapyCoinMod.MOD_ID;

// Регистрация звуков мода. Имена совпадают с записями в assets/capycoinmod/sounds.json.
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> KNOCKBACK = register("knockback");
    public static final DeferredHolder<SoundEvent, SoundEvent> KNOCKBACK_SWORD_KD = register("knockback_sword_kd");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAUNCH_SWORD_KD = register("launch_sword_kd");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAUNCH_BOOTS_JUMP = register("launch_boots_jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAUNCH_BOOTS_READY = register("launch_boots_ready");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTNING_SPEAR_CHARGE = register("lightning_spear_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTNING_SPEAR_KD = register("lightning_spear_kd");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_HELMET_FUSE = register("bomb_helmet_fuse");
    public static final DeferredHolder<SoundEvent, SoundEvent> POCHITA_WOOF = register("pochita_woof");
    public static final DeferredHolder<SoundEvent, SoundEvent> POCHITA_GROWL = register("pochita_growl");

    // Звук с переменной дальностью слышимости, id = имя звука в пространстве capycoinmod.
    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, name)));
    }
}
