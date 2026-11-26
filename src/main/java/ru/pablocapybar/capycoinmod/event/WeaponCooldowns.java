package ru.pablocapybar.capycoinmod.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import ru.pablocapybar.capycoinmod.CapyCoinMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Кулдаун, заряд и звук окончания КД для способностей оружия.
@EventBusSubscriber(modid = CapyCoinMod.MOD_ID)
public final class WeaponCooldowns {
    // Заряд: какое оружие заряжено у игрока и до какого тика действует.
    private record Charge(Item weapon, long until) {}

    // Напоминание о том, когда кулдаун закончится и какой звук проиграть.
    private record ReadyNotice(UUID playerId, long readyTick, SoundEvent sound) {}

    private static final Map<UUID, Charge> CHARGES = new HashMap<>();
    private static final List<ReadyNotice> NOTICES = new ArrayList<>();

    // Запускает кулдаун, заряд и напоминание о звуке. Возвращает false, если оружие ещё на кулдауне.
    public static boolean startCharge(Player player, ItemStack stack, int cooldownTicks, int chargeTicks, SoundEvent readySound) {
        Item weapon = stack.getItem();
        if (player.getCooldowns().isOnCooldown(weapon)) {
            return false;
        }
        long now = player.level().getGameTime();
        player.getCooldowns().addCooldown(weapon, cooldownTicks);
        CHARGES.put(player.getUUID(), new Charge(weapon, now + chargeTicks));
        scheduleReadySound(player, cooldownTicks, readySound);
        return true;
    }

    // Ставит звук на момент окончания кулдауна без заряда.
    public static void scheduleReadySound(Player player, int cooldownTicks, SoundEvent sound) {
        long now = player.level().getGameTime();
        NOTICES.add(new ReadyNotice(player.getUUID(), now + cooldownTicks, sound));
    }

    // Если заряд есть и не истёк, снимает его и возвращает true. Заряд работает только на один удар.
    public static boolean consumeCharge(Player player, Item weapon) {
        Charge charge = CHARGES.remove(player.getUUID());
        return charge != null
                && charge.weapon() == weapon
                && player.level().getGameTime() <= charge.until();
    }

    // Проверяет каждый тик, не закончился ли чей-то КД, и проигрывает звук владельцу.
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        NOTICES.removeIf(notice -> {
            if (!notice.playerId().equals(player.getUUID()) || now < notice.readyTick()) {
                return false;
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    notice.sound(), SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        });
    }
}
