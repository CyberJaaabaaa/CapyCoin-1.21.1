package ru.pablocapybar.capycoinmod.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import ru.pablocapybar.capycoinmod.event.DelayedExplosions;
import ru.pablocapybar.capycoinmod.event.WeaponCooldowns;
import ru.pablocapybar.capycoinmod.registry.ModSounds;

// Меч, который подбрасывает цель и через секунду взрывает её. ПКМ заряжает следующий удар.
public class LaunchSwordItem extends SwordItem {
    public LaunchSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    // Вызывается при нажатии ПКМ с мечом в руке.
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        // Кулдаун 600 тиков (30 секунд), заряд 1000 тиков (50 секунд). В комментарии было 100 тиков, см. вопрос.
        if (!WeaponCooldowns.startCharge(player, stack, 600, 1000, ModSounds.LAUNCH_SWORD_KD.get())) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    // Вызывается при ударе этим мечом.
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Эффект только на сервере и только если меч заряжен.
        if (attacker instanceof Player player
                && attacker.level() instanceof ServerLevel level
                && WeaponCooldowns.consumeCharge(player, this)) {
            // Подбрасываем цель вверх и отправляем новую скорость клиентам.
            target.setDeltaMovement(0, 2, 0);
            target.hurtMarked = true;
            // Взрыв через 15 тиков, с интервалом 1.
            DelayedExplosions.schedule(level, target, 15, 15, 1);
        }
        // Обычный урон проходит в любом случае.
        return super.hurtEnemy(stack, target, attacker);
    }
}
