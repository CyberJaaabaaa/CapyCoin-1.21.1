package ru.pablocapybar.capycoinmod.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import ru.pablocapybar.capycoinmod.event.WeaponCooldowns;
import ru.pablocapybar.capycoinmod.registry.ModSounds;

// Меч, который отбрасывает цель. ПКМ заряжает следующий удар, эффект в KnockbackEvents.
public class KnockbackSwordItem extends SwordItem {
    public KnockbackSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    // Вызывается при нажатии ПКМ с мечом в руке.
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        // Кулдаун 200 тиков (10 секунд), заряд 100 тиков (5 секунд), звук по окончании КД.
        if (!WeaponCooldowns.startCharge(player, stack, 200, 100, ModSounds.KNOCKBACK_SWORD_KD.get())) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.success(stack);
    }
}
