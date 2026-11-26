package ru.pablocapybar.capycoinmod.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import ru.pablocapybar.capycoinmod.CapyCoinMod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Серия отложенных взрывов вокруг цели, которые срабатывают с заданным интервалом.
@EventBusSubscriber(modid = CapyCoinMod.MOD_ID)
public class DelayedExplosions {
    private static final List<Pending> PENDING = new ArrayList<>();

    // Ставит серию из count взрывов вокруг цели: первый через delay тиков, дальше каждые interval тиков.
    public static void schedule(ServerLevel level, LivingEntity target, int count, int delay, int interval) {
        PENDING.add(new Pending(level, target, count, delay, interval));
    }

    // Каждый тик уменьшает таймеры и выполняет взрывы, которым пришло время.
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Iterator<Pending> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            Pending pending = iterator.next();
            if (pending.level != level) {
                continue;
            }
            if (--pending.ticksLeft > 0) {
                continue;
            }

            // Взрыв в случайной точке рядом с целью.
            double offsetX = (level.random.nextDouble() * 2 - 1) * 3;
            double offsetY = (level.random.nextDouble() * 2 - 1) * 1;
            double offsetZ = (level.random.nextDouble() * 2 - 1) * 3;
            level.explode(null,
                    pending.target.getX() + offsetX,
                    pending.target.getY() + offsetY,
                    pending.target.getZ() + offsetZ,
                    1.5f, Level.ExplosionInteraction.NONE);
            pending.explosionsLeft--;

            if (pending.explosionsLeft <= 0) {
                iterator.remove();
            } else {
                pending.ticksLeft = pending.interval;
            }
        }
    }

    // Состояние одной серии взрывов.
    private static class Pending {
        final ServerLevel level;
        final LivingEntity target;
        final int interval;
        int explosionsLeft;
        int ticksLeft;

        Pending(ServerLevel level, LivingEntity target, int count, int delay, int interval) {
            this.level = level;
            this.target = target;
            this.explosionsLeft = count;
            this.interval = interval;
            this.ticksLeft = delay;
        }
    }
}
