package ru.pablocapybar.capycoinmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.pablocapybar.capycoinmod.CapyCoinMod;

// Пустой пакет: клиент сообщает серверу только то, что игрок нажал пробел в ботинках.
public record LaunchBootsJumpPayload() implements CustomPacketPayload {
    public static final Type<LaunchBootsJumpPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CapyCoinMod.MOD_ID, "launch_boots_jump"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LaunchBootsJumpPayload> CODEC =
            StreamCodec.unit(new LaunchBootsJumpPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
