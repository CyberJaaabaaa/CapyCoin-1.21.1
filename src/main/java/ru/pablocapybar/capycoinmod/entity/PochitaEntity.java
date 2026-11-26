package ru.pablocapybar.capycoinmod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.pablocapybar.capycoinmod.item.PochitaEggItem;
import ru.pablocapybar.capycoinmod.registry.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

// Почита: прирученный питомец. Следует за владельцем и защищает его.
public class PochitaEntity extends TamableAnimal implements GeoEntity {
    // Ключ в данных игрока: Почита уже призван (яйцо больше не призывает второго).
    public static final String SPAWNED_KEY = "capycoinmod_pochita_spawned";
    // Ключ в данных игрока: сохранённое состояние мёртвого Почиты для воскрешения.
    public static final String DEAD_KEY = "capycoinmod_pochita_saved";

    // Кэш анимаций GeckoLib.
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PochitaEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    @Override
    protected void registerGoals() {
        // Атака ближнего боя.
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        // Следование за владельцем.
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        // Случайные прогулки, избегая воды.
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        // Смотреть на игроков рядом.
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        // Случайно оглядываться.
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        // Атаковать тех, кто ударил владельца.
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        // Атаковать тех, кого ударил владелец.
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Контроллер "main": walk, когда Почита двигается, иначе idle. Число 5 - плавность перехода в тиках.
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.pochita.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.pochita.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // При смерти сохраняем состояние Почиты в данные владельца и помечаем яйца как воскрешаемые.
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && this.getOwner() instanceof Player owner) {
            owner.getPersistentData().put(DEAD_KEY, this.saveWithoutId(new CompoundTag()));
            PochitaEggItem.setRevivable(owner, true);
        }
        super.die(source);
    }

    // Звуки пока ванильные волчьи. Заменим на свои, когда будут файлы.
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.POCHITA_WOOF.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_HURT;
    }

    // Звук агрессии: играет, когда Почита выбирает новую цель.
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        boolean newTarget = target != null && target != this.getTarget();
        super.setTarget(target);
        if (newTarget && !this.level().isClientSide) {
            this.playSound(ModSounds.POCHITA_GROWL.get(), 1.0F, 1.0F);
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }
}
