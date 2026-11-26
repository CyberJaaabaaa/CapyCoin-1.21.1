package ru.pablocapybar.capycoinmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import ru.pablocapybar.capycoinmod.CapyCoinMod;
import ru.pablocapybar.capycoinmod.item.LightningBowItem;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Стрелы из молниевого лука с шансом бьют молнией по месту попадания и гасят огонь вокруг.
@EventBusSubscriber(modid = CapyCoinMod.MOD_ID)
public class LightningBowEvents {
    // Шанс удара молнии после попадания стрелы (30%).
    private static final float CHANCE = 0.3f;
    private static final int BOLT_COUNT = 6;
    private static final double RADIUS = 3.0;
    // Как часто (в тиках) убирать огонь и сколько тиков длится очистка.
    private static final int CLEANUP_INTERVAL_TICKS = 3;
    private static final int CLEANUP_DURATION_TICKS = 40;
    // Половина ширины области очистки огня от центра.
    private static final int CLEANUP_HALF_WIDTH = 5;

    // Активные очистки. CopyOnWriteArrayList позволяет удалять записи во время обхода.
    private static final List<PendingCleanup> PENDING = new CopyOnWriteArrayList<>();

    // Срабатывает после урона по живому существу. Проверяем, что урон нанесла стрела от нашего лука.
    @SubscribeEvent
    public static void onArrowDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) return;
        if (!arrow.getTags().contains(LightningBowItem.LIGHTNING_TAG)) return;
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (level.random.nextFloat() >= CHANCE) return;

        LivingEntity target = event.getEntity();
        strike(level, target.position(), RADIUS, BOLT_COUNT, target);
    }

    // Создаёт молнии в случайных точках вокруг center и ставит очистку огня.
    private static void strike(ServerLevel level, Vec3 center, double radius, int count, LivingEntity target) {
        for (int i = 0; i < count; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double dist = level.random.nextDouble() * radius;
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
            bolt.moveTo(center.x + Math.cos(angle) * dist, center.y, center.z + Math.sin(angle) * dist);
            level.addFreshEntity(bolt);
        }
        PENDING.add(new PendingCleanup(level, target, BlockPos.containing(center)));
    }

    // Вызывается в конце каждого серверного тика. Уменьшает таймеры и запускает очистку по интервалу.
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (PendingCleanup cleanup : PENDING) {
            cleanup.ticksLeft--;
            if (cleanup.ticksLeft % CLEANUP_INTERVAL_TICKS == 0) {
                cleanup.run();
            }
            if (cleanup.ticksLeft <= 0) {
                PENDING.remove(cleanup);
            }
        }
    }

    // Одна отложенная очистка: место удара и оставшееся время.
    private static class PendingCleanup {
        final ServerLevel level;
        final LivingEntity target;
        final BlockPos center;
        int ticksLeft = CLEANUP_DURATION_TICKS;

        PendingCleanup(ServerLevel level, LivingEntity target, BlockPos center) {
            this.level = level;
            this.target = target;
            this.center = center;
        }

        // Гасит цель и заменяет огонь на воздух в области вокруг места удара.
        void run() {
            if (target != null) {
                target.clearFire();
            }
            for (BlockPos pos : BlockPos.betweenClosed(
                    center.offset(-CLEANUP_HALF_WIDTH, -2, -CLEANUP_HALF_WIDTH),
                    center.offset(CLEANUP_HALF_WIDTH, 3, CLEANUP_HALF_WIDTH))) {
                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}
