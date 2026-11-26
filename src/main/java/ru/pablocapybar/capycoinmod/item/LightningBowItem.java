package ru.pablocapybar.capycoinmod.item;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

// Лук, который помечает выпущенные стрелы тегом. По тегу LightningBowEvents отличает эти стрелы от обычных.
public class LightningBowItem extends BowItem {
    // Тег, который ставится на стрелу при выстреле из этого лука.
    public static final String LIGHTNING_TAG = "capycoinmod_lightning";

    public LightningBowItem(Properties properties) {
        super(properties);
    }

    // Вызывается при создании стрелы. Добавляем тег, чтобы потом узнать стрелу при попадании.
    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack ammo, ItemStack weapon) {
        arrow.addTag(LIGHTNING_TAG);
        return arrow;
    }
}
