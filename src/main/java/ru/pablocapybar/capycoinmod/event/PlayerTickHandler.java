package ru.pablocapybar.capycoinmod.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import ru.pablocapybar.capycoinmod.CapyCoinMod;
import ru.pablocapybar.capycoinmod.item.BombHelmetItem;
import ru.pablocapybar.capycoinmod.item.LightningSpearItem;

// Серверный тик игрока: звук окончания кулдауна копья и таймер шлема-бомбы.
@EventBusSubscriber(modid = CapyCoinMod.MOD_ID)
public final class PlayerTickHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            LightningSpearItem.tickCooldownSound(player);
            BombHelmetItem.tickFuse(player);
        }
    }
}
