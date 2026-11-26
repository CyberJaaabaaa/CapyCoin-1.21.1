package ru.pablocapybar.capycoinmod.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import ru.pablocapybar.capycoinmod.registry.ModSounds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Шлем-бомба: нельзя снять (проклятие несъёмности), через 14 секунд взрывает владельца.
public class BombHelmetItem extends ArmorItem {
    private static final int FUSE_TICKS = 14 * 20;
    private static final float EXPLOSION_POWER = 15.0F;
    // Момент, с которого игрок носит шлем, для каждого игрока.
    private static final Map<UUID, Long> WEAR_START = new HashMap<>();

    public BombHelmetItem(Properties properties) {
        super(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET,
                properties.component(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE));
    }

    // Вызывается из PlayerTickHandler. Ведёт таймер шлема и запускает взрыв.
    public static void tickFuse(Player player) {
        if (player.level().isClientSide) return;
        ServerLevel server = (ServerLevel) player.level();

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        Long start = WEAR_START.get(player.getUUID());

        if (!(helmet.getItem() instanceof BombHelmetItem)) {
            if (start != null) {
                WEAR_START.remove(player.getUUID());
                stopFuseSound(server, player);
            }
            return;
        }

        // Накладываем проклятие несъёмности, чтобы шлем нельзя было снять до взрыва.
        Holder<Enchantment> binding = server.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.BINDING_CURSE);
        if (EnchantmentHelper.getItemEnchantmentLevel(binding, helmet) == 0) {
            helmet.enchant(binding, 1);
        }

        long now = server.getGameTime();
        if (start == null) {
            WEAR_START.put(player.getUUID(), now);
            playFuseSound(server, player);
            return;
        }
        if (now - start < FUSE_TICKS) return;

        WEAR_START.remove(player.getUUID());
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        server.explode(player, player.getX(), player.getY(), player.getZ(),
                EXPLOSION_POWER, Level.ExplosionInteraction.NONE);
        player.hurt(server.damageSources().genericKill(), Float.MAX_VALUE);
    }

    // Отправляет игрокам звук фитиля от имени владельца.
    private static void playFuseSound(ServerLevel server, Player player) {
        ClientboundSoundEntityPacket packet = new ClientboundSoundEntityPacket(
                ModSounds.BOMB_HELMET_FUSE, SoundSource.PLAYERS, player, 1.0F, 1.0F, server.getRandom().nextLong());
        server.getChunkSource().broadcastAndSend(player, packet);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(packet);
        }
    }

    // Останавливает звук фитиля, если шлем сняли до взрыва.
    private static void stopFuseSound(ServerLevel server, Player player) {
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(
                ModSounds.BOMB_HELMET_FUSE.getId(), SoundSource.PLAYERS);
        server.getChunkSource().broadcastAndSend(player, packet);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(packet);
        }
    }
}
