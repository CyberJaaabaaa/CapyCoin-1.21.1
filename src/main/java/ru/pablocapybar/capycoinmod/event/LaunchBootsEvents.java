package ru.pablocapybar.capycoinmod.event;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import ru.pablocapybar.capycoinmod.CapyCoinMod;
import ru.pablocapybar.capycoinmod.registry.ModItems;
import ru.pablocapybar.capycoinmod.registry.ModSounds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Ботинки с подбросом: двойное нажатие пробела в воздухе подбрасывает игрока, падение после этого не наносит урон.
@EventBusSubscriber(modid = CapyCoinMod.MOD_ID)
public final class LaunchBootsEvents {
    // Кулдаун 10 секунд: 20 тиков в секунде.
    private static final int COOLDOWN_TICKS = 10 * 20;
    // Время, в течение которого падение после подброса не наносит урон. С запасом больше полёта.
    private static final long FALL_PROTECTION_TICKS = 10 * 20;
    // Начальная вертикальная скорость, примерно дающая 10 блоков. Точное значение подбирается в игре.
    private static final double LAUNCH_VELOCITY = 1.2;
    // Игрок -> игровой тик подброса. Используется для отмены урона от падения.
    private static final Map<UUID, Long> LAUNCHED = new HashMap<>();

    // Подбрасывает игрока, если он в воздухе в ботинках и кулдаун прошёл. Вызывается из пакета прыжка.
    public static void tryLaunch(ServerPlayer player) {
        if (player.onGround() || !wearsLaunchBoots(player)) {
            return;
        }
        // Кулдаун хранится на сервере и синхронизируется с клиентом автоматически.
        if (player.getCooldowns().isOnCooldown(ModItems.LAUNCH_BOOTS.get())) {
            return;
        }
        WeaponCooldowns.scheduleReadySound(player, COOLDOWN_TICKS, ModSounds.LAUNCH_BOOTS_READY.get());

        player.getCooldowns().addCooldown(ModItems.LAUNCH_BOOTS.get(), COOLDOWN_TICKS);
        LAUNCHED.put(player.getUUID(), player.level().getGameTime());

        // Горизонтальная скорость сохраняется, меняется только вертикальная.
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, LAUNCH_VELOCITY, motion.z);
        // Клиент получает скорость сам, без этого пакета подъём будет виден только на сервере.
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.LAUNCH_BOOTS_JUMP.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // Отменяет урон от падения, если игрок недавно подброшен. Урон от падения приходит только при приземлении.
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getSource().is(DamageTypeTags.IS_FALL)) {
            Long launchTick = LAUNCHED.remove(player.getUUID());
            if (launchTick != null && player.level().getGameTime() - launchTick <= FALL_PROTECTION_TICKS) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean wearsLaunchBoots(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.LAUNCH_BOOTS.get());
    }
}
