package ru.pablocapybar.capycoinmod.item;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import ru.pablocapybar.capycoinmod.entity.ExplosiveArrowEntity;
import ru.pablocapybar.capycoinmod.registry.ModItems;

import javax.annotation.Nullable;

// Взрывная стрела: при выстреле из лука создаёт ExplosiveArrowEntity.
public class ExplosiveArrowItem extends ArrowItem {
    public ExplosiveArrowItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new ExplosiveArrowEntity(level, shooter);
    }

    // Раздатчик выпускает взрывную стрелу по своему направлению. Вызывается из common setup.
    public static void registerDispenseBehavior() {
        DispenserBlock.registerBehavior(ModItems.EXPLOSIVE_ARROW.get(), (source, stack) -> {
            Level level = source.level();
            Direction facing = source.state().getValue(DispenserBlock.FACING);
            // Точка спавна: центр раздатчика, сдвинутый вперёд на 0.7 блока, чтобы стрела не застряла внутри.
            Vec3 center = source.center().add(facing.getStepX() * 0.7, facing.getStepY() * 0.7, facing.getStepZ() * 0.7);
            ExplosiveArrowEntity arrow = new ExplosiveArrowEntity(level, center.x, center.y, center.z);
            // Скорость 1.1 и разброс 6.0.
            arrow.shoot(facing.getStepX(), facing.getStepY() + 0.1, facing.getStepZ(), 1.1f, 6.0f);
            level.addFreshEntity(arrow);
            stack.shrink(1);
            return stack;
        });
    }
}
