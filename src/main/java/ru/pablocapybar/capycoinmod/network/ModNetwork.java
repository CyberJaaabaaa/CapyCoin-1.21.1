package ru.pablocapybar.capycoinmod.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import ru.pablocapybar.capycoinmod.event.LaunchBootsEvents;

// Регистрация сетевых пакетов мода. Вызывается из шины мода.
public final class ModNetwork {
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(LaunchBootsJumpPayload.TYPE, LaunchBootsJumpPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        LaunchBootsEvents.tryLaunch(player);
                    }
                }));
    }
}
