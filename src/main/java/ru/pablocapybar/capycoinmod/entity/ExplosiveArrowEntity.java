package ru.pablocapybar.capycoinmod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import ru.pablocapybar.capycoinmod.registry.ModEntities;

// Стрела, которая взрывается при первом попадании в сущность или блок.
public class ExplosiveArrowEntity extends Arrow {
    private boolean exploded;

    public ExplosiveArrowEntity(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    public ExplosiveArrowEntity(Level level, LivingEntity shooter) {
        super(ModEntities.EXPLOSIVE_ARROW.get(), level);
        setOwner(shooter);
        setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }

    // Используется раздатчиком: стрела создаётся в заданной точке.
    public ExplosiveArrowEntity(Level level, double x, double y, double z) {
        super(ModEntities.EXPLOSIVE_ARROW.get(), level);
        setPos(x, y, z);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        explodeOnce(result.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        explodeOnce(result.getLocation());
    }

    // Взрывает стрелу один раз и удаляет её.
    private void explodeOnce(Vec3 pos) {
        if (level().isClientSide || exploded) {
            return;
        }
        exploded = true;
        level().explode(this, pos.x, pos.y, pos.z, 1.0f, ExplosionInteraction.NONE);
        discard();
    }

    // Пока стрела в полёте, оставляет искры огня.
    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server && !inGround)
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 3, 0.05, 0.05, 0.05, 0.01);
    }
}
