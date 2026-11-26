package ru.pablocapybar.capycoinmod.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import ru.pablocapybar.capycoinmod.CapyCoinMod;
import ru.pablocapybar.capycoinmod.item.KnockbackSwordItem;
import ru.pablocapybar.capycoinmod.registry.ModSounds;

import java.util.HashMap;
import java.util.Map;

// Отслеживает урон и применяет отталкивание, когда игрок бьёт заряженным мечом отталкивания.
@EventBusSubscriber(modid = CapyCoinMod.MOD_ID)
public class KnockbackEvents {
    // Урон, если отброшенная цель врезалась в стену (10 = 5 сердец).
    private static final float WALL_DAMAGE = 10f;
    // Сколько тиков после отталкивания ждём удара о стену (20 тиков = 1 секунда).
    private static final int WALL_WINDOW_TICKS = 40;

    // Цели, которые отбросило и которые ещё могут удариться о стену.
    private static final Map<LivingEntity, WallCheck> WALL_CHECKS = new HashMap<>();

    // Момент, до которого проверяем стену, и тот, кто нанёс удар.
    private record WallCheck(long expireTick, LivingEntity attacker) {}

    // Вызывается после того, как урон уже нанесён.
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        // Эффект работает только на сервере.
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        // Нас интересуют только удары живых существ.
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        // Проверяем, что атакующий держит именно меч отталкивания.
        if (!(attacker.getMainHandItem().getItem() instanceof KnockbackSwordItem)) {
            return;
        }
        // Отталкивание только если меч заряжен через ПКМ.
        if (!(attacker instanceof Player player)
                || !WeaponCooldowns.consumeCharge(player, attacker.getMainHandItem().getItem())) {
            return;
        }
        // Направление отталкивания берём из поворота атакующего.
        double yaw = Math.toRadians(attacker.getYRot());
        // Игроков отбрасываем слабее, чем мобов.
        double strength = victim instanceof Player ? 15 : 50;
        victim.knockback(strength, Math.sin(yaw), -Math.cos(yaw));
        // Сообщаем игре, что скорость жертвы изменилась, и её нужно отправить клиентам.
        victim.hurtMarked = true;
        // Запоминаем цель, чтобы проверить, не врежется ли она в стену.
        WALL_CHECKS.put(victim, new WallCheck(level.getGameTime() + WALL_WINDOW_TICKS, attacker));
        // Дымовые частицы вокруг жертвы.
        level.sendParticles(ParticleTypes.SMOKE,
                victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(),
                300, 1, 1, 1, 0.1);
        // Звук отталкивания.
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                ModSounds.KNOCKBACK.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // Вызывается после каждого тика сущности, когда движение уже посчитано.
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity victim)
                || !(victim.level() instanceof ServerLevel level)) {
            return;
        }
        WallCheck check = WALL_CHECKS.get(victim);
        if (check == null) {
            return;
        }
        if (level.getGameTime() > check.expireTick()) {
            WALL_CHECKS.remove(victim);
            return;
        }
        // Флаг горизонтального столкновения ставится, если цель упёрлась в блок во время движения.
        if (victim.horizontalCollision) {
            WALL_CHECKS.remove(victim);
            victim.invulnerableTime = 0;
            victim.hurt(check.attacker().damageSources().mobAttack(check.attacker()), WALL_DAMAGE);
        }
    }
}
