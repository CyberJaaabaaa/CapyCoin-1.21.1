    package ru.pablocapybar.capycoinmod.item;

    import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
    import net.minecraft.server.level.ServerLevel;
    import net.minecraft.server.level.ServerPlayer;
    import net.minecraft.sounds.SoundEvents;
    import net.minecraft.sounds.SoundSource;
    import net.minecraft.world.InteractionHand;
    import net.minecraft.world.InteractionResultHolder;
    import net.minecraft.world.entity.LivingEntity;
    import net.minecraft.world.entity.player.Player;
    import net.minecraft.world.item.Item;
    import net.minecraft.world.item.ItemStack;
    import net.minecraft.world.item.UseAnim;
    import net.minecraft.world.level.Level;
    import ru.pablocapybar.capycoinmod.entity.ThrownLightningSpear;
    import ru.pablocapybar.capycoinmod.registry.ModSounds;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.UUID;

    // Копьё Резе: удерживается для замаха, при отпускании бросается. Есть кулдаун 3 минуты.
    public class LightningSpearItem extends Item {
        // 3 минуты в тиках.
        public static final int COOLDOWN_TICKS = 3 * 60 * 20;
        // Минимальное время замаха в тиках. Короче этого бросок не происходит.
        private static final int MIN_CHARGE_TICKS = 20;
        // Максимальная длительность удержания, как у трезубца.
        private static final int MAX_USE_TICKS = 72000;
        // Как часто (в тиках) играет звук зарядки.
        private static final int CHARGE_SOUND_INTERVAL = 5;
        // Тик игры, когда кончается кулдаун, для каждого игрока.
        private static final Map<UUID, Long> COOLDOWN_END = new HashMap<>();

        public LightningSpearItem(Properties properties) {
            super(properties);
        }


        // Начинает замах, если кулдаун не идёт.
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);

            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(stack);
            }

            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity entity) {
            return MAX_USE_TICKS;
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.SPEAR;
        }

        // Пока копьё заряжается, периодически играет звук зарядки.
        @Override
        public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
            int charged = getUseDuration(stack, entity) - remainingUseDuration;

            if (!level.isClientSide && charged > 0 && charged % CHARGE_SOUND_INTERVAL == 0) {
                float pitch = 0.5F + Math.min(charged, 40) * 0.01F;
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        ModSounds.LIGHTNING_SPEAR_CHARGE.get(), SoundSource.PLAYERS, 0.5F, pitch);
            }
        }

        // Вызывается при отпускании ПКМ. timeLeft — сколько тиков оставалось до конца использования.
        @Override
        public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
            if (!(entity instanceof Player player) || !(level instanceof ServerLevel server)) return;
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundStopSoundPacket(
                        ModSounds.LIGHTNING_SPEAR_CHARGE.getId(), SoundSource.PLAYERS));
            }
            int charged = getUseDuration(stack, entity) - timeLeft;
            if (charged < MIN_CHARGE_TICKS) return;

            ThrownLightningSpear spear = new ThrownLightningSpear(server, player, stack.copyWithCount(1));
            spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            spear.setNoGravity(true);
            server.addFreshEntity(spear);

            server.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

            stack.shrink(1);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            COOLDOWN_END.put(player.getUUID(), server.getGameTime() + COOLDOWN_TICKS);
        }

        // Вызывается из PlayerTickHandler. Проигрывает звук окончания кулдауна копья.
        public static void tickCooldownSound(Player player) {
            if (player.level().isClientSide) return;
            Long end = COOLDOWN_END.get(player.getUUID());
            if (end != null && player.level().getGameTime() >= end) {
                COOLDOWN_END.remove(player.getUUID());
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.LIGHTNING_SPEAR_KD.get(), SoundSource.PLAYERS, 0.6F, 1.5F);
            }
        }
    }
