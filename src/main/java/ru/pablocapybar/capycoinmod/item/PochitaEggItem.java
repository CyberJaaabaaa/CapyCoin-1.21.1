package ru.pablocapybar.capycoinmod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.pablocapybar.capycoinmod.entity.PochitaEntity;
import ru.pablocapybar.capycoinmod.registry.ModEntities;
import ru.pablocapybar.capycoinmod.registry.ModItems;

// Яйцо Почиты: первый призыв, затем воскрешение после смерти. Не тратится.
public class PochitaEggItem extends Item {
    // Ключ в NBT предмета: яйцо может воскресить Почиту (используется для предиката модели).
    private static final String REVIVABLE_KEY = "capycoinmod_pochita_revivable";

    public PochitaEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        return activate(context.getLevel(), player, Vec3.atBottomCenterOf(context.getClickedPos().above()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getPersistentData().contains(PochitaEntity.DEAD_KEY)) {
            return InteractionResultHolder.pass(stack);
        }
        return new InteractionResultHolder<>(activate(level, player, player.position()), stack);
    }

    public static boolean isRevivable(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(REVIVABLE_KEY);
    }

    // Помечает все яйца в инвентаре игрока как воскрешаемые или нет.
    public static void setRevivable(Player player, boolean revivable) {
        for (ItemStack stack : player.getInventory().items) markStack(stack, revivable);
        for (ItemStack stack : player.getInventory().offhand) markStack(stack, revivable);
    }

    private static void markStack(ItemStack stack, boolean revivable) {
        if (stack.is(ModItems.POCHITA_EGG.get())) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(REVIVABLE_KEY, revivable));
        }
    }

    // Призывает Почиту или воскрешает сохранённую. Возвращает результат для игры.
    private InteractionResult activate(Level level, Player player, Vec3 pos) {
        CompoundTag data = player.getPersistentData();

        if (data.contains(PochitaEntity.DEAD_KEY)) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
            PochitaEntity pochita = new PochitaEntity(ModEntities.POCHITA.get(), serverLevel);
            pochita.load(data.getCompound(PochitaEntity.DEAD_KEY));
            pochita.setHealth(pochita.getMaxHealth());
            pochita.setPos(pos.x, pos.y, pos.z);
            serverLevel.addFreshEntity(pochita);
            data.remove(PochitaEntity.DEAD_KEY);
            setRevivable(player, false);
            return InteractionResult.CONSUME;
        }

        // Повторный призыв живой Почиты запрещён.
        if (data.getBoolean(PochitaEntity.SPAWNED_KEY)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.capycoinmod.pochita_already_spawned"), true);
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
        PochitaEntity pochita = new PochitaEntity(ModEntities.POCHITA.get(), serverLevel);
        pochita.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0F);
        pochita.setTame(true, true);
        pochita.setOwnerUUID(player.getUUID());
        serverLevel.addFreshEntity(pochita);
        data.putBoolean(PochitaEntity.SPAWNED_KEY, true);
        return InteractionResult.CONSUME;
    }
}
