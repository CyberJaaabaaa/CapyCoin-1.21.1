package ru.pablocapybar.capycoinmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.pablocapybar.capycoinmod.registry.ModEntities;
import ru.pablocapybar.capycoinmod.registry.ModItems;

// Летящее копьё Резе: во время полёта бьёт по земле под собой, при попадании взрывается и возвращается владельцу.
public class ThrownLightningSpear extends AbstractArrow {
    private static final double SPREAD_RADIUS = 6.0;
    private static final int BLAST_COUNT = 8;
    private static final int FLIGHT_STRIKE_INTERVAL_TICKS = 2;
    private static final int FLIGHT_BLAST_COUNT = 1;
    private static final double FLIGHT_SPREAD_RADIUS = 4.0;
    private static final float BLAST_POWER = 3.0F;
    private static final int MAX_FLIGHT_TICKS = 190;
    // Взрывы не бьют по владельцу, если он ближе этого расстояния.
    private static final double OWNER_SAFE_DISTANCE = 20.0;
    private static final double KNOCKBACK_RADIUS = 4.5;
    private static final double KNOCKBACK_HORIZONTAL = 1;
    private static final double KNOCKBACK_UPWARD = 1;

    public ThrownLightningSpear(EntityType<? extends ThrownLightningSpear> type, Level level) {
        super(type, level);
    }

    public ThrownLightningSpear(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.THROWN_LIGHTNING_SPEAR.get(), owner, level, stack, null);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.LIGHTNING_SPEAR.get());
    }

    // Каждые несколько тиков в полёте бьёт по земле и отталкивает ближайших. Возвращается, если упал или улетел слишком далеко по времени.
    @Override
    public void tick() {
        super.tick();
        if (inGround || tickCount > MAX_FLIGHT_TICKS) {
            returnToOwner();
            return;
        }
        if (tickCount % FLIGHT_STRIKE_INTERVAL_TICKS == 0 && level() instanceof ServerLevel server) {
            blastGround(server, position(), FLIGHT_SPREAD_RADIUS, FLIGHT_BLAST_COUNT);
            knockbackNearby(server);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        strikeAt(result);
        returnToOwner();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        strikeAt(result);
        returnToOwner();
    }

    // Удар в точке попадания.
    private void strikeAt(HitResult result) {
        if (level() instanceof ServerLevel server) {
            blastGround(server, result.getLocation(), SPREAD_RADIUS, BLAST_COUNT);
        }
    }

    // Серия взрывов в случайных точках радиуса вокруг point.
    private void blastGround(ServerLevel server, Vec3 point, double radius, int count) {
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        if (owner != null && owner.position().distanceTo(point) < OWNER_SAFE_DISTANCE) return;

        BlockPos surface = server.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                BlockPos.containing(point.x, point.y, point.z));
        for (int i = 0; i < count; i++) {
            double angle = server.random.nextDouble() * Math.PI * 2;
            double dist = server.random.nextDouble() * radius;
            server.explode(this,
                    point.x + Math.cos(angle) * dist,
                    point.y + Math.cos(angle) * dist/2,      //surface.getY(),
                    point.z + Math.sin(angle) * dist,
                    BLAST_POWER,
                    Level.ExplosionInteraction.NONE);
        }
    }

    // Отбрасывает живых существ рядом с копьём, кроме владельца.
    private void knockbackNearby(ServerLevel server) {
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(KNOCKBACK_RADIUS),
                e -> e != owner && e.isAlive())) {
            Vec3 away = entity.position().subtract(position());
            double horizontal = Math.max(Math.sqrt(away.x * away.x + away.z * away.z), 0.01);
            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    away.x / horizontal * KNOCKBACK_HORIZONTAL,
                    KNOCKBACK_UPWARD,
                    away.z / horizontal * KNOCKBACK_HORIZONTAL));
            entity.hurtMarked = true;
        }
    }

    // Кладёт копьё владельцу в инвентарь (или на землю, если места нет) и удаляет сущность.
    private void returnToOwner() {
        if (!(level() instanceof ServerLevel)) return;
        ItemStack stack = getPickupItem();
        if (getOwner() instanceof Player player) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        } else {
            spawnAtLocation(stack);
        }
        discard();
    }
}
