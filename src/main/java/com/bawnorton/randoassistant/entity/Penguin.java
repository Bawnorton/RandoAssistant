package com.bawnorton.randoassistant.entity;

import com.bawnorton.randoassistant.config.ServerConfig;
import com.bawnorton.randoassistant.registry.Registrar;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class Penguin extends TameableEntity {
    private static final List<Item> temptItems = List.of(Items.TROPICAL_FISH, Items.SALMON, Items.COD);
    private static final Ingredient FOOD_ITEMS = Ingredient.ofStacks(Items.TROPICAL_FISH.getDefaultStack(), Items.SALMON.getDefaultStack(), Items.COD.getDefaultStack());
    private float slideAnimationProgress;
    private float lastSlideAnimationProgress;
    private float swimAnimationProgress;
    private float lastSwimAnimationProgress;
    private int ticksSinceEaten;

    public Penguin(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
        this.moveControl = new AquaticMoveControl(this, 85, 10, 0.4F, 1.0F, true);
        this.lookControl = new PenguinLookControl(this, 20);
        this.setStepHeight(1.0f);
        this.setCanPickUpLoot(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.2D));
        this.goalSelector.add(2, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.add(3, new PenguinSearchForItemsGoal(this));
        this.goalSelector.add(4, new PenguinMeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.add(5, new PenguinFollowOwnerGoal(this, 1.0D, 10.0f, 2.0f, false));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(7, new PenguinRandomSwimmingGoal(this, 1.0D, 60));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(9, new LookAroundGoal(this));
    }

    @Override
    protected EntityNavigation createNavigation(World pLevel) {
        return new PenguinPathNavigation(this, pLevel);
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    public void travel(Vec3d pTravelVector) {
        if (this.canMoveVoluntarily() && this.isSubmergedInWater()) {
            this.updateVelocity(this.getMovementSpeed(), pTravelVector);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9D));
        } else {
            super.travel(pTravelVector);
        }

    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return Registrar.PENGUIN.create(world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
    }

    @Override
    public void onDamaged(DamageSource damageSource) {
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        setHealth(getMaxHealth());
    }

    @Override
    protected void onGrowUp() {
        super.onGrowUp();
        if (!this.isBaby() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            this.dropStack(Items.FEATHER.getDefaultStack(), 1);
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (this.world.isClient) {
            return (this.isOwner(player) || this.isTamed() || temptItems.contains(item) && !this.isTamed() && this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) ? ActionResult.CONSUME : ActionResult.PASS;
        }
        if(temptItems.contains(item) && this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }

            this.equipStack(EquipmentSlot.MAINHAND, itemStack.copy().split(1));
            this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 2.0F;
            this.ticksSinceEaten = 0;

            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void tickMovement() {
        if (world.getTime() % 80L == 0L) {
            if (this.world.getNonSpectatingEntities(Penguin.class, this.getBoundingBox().expand(20.0D)).size() > 4) {
                for (PlayerEntity player : this.world.getNonSpectatingEntities(PlayerEntity.class, this.getBoundingBox().expand(10.0D))) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0, true, true));
                }
            }
        }
        if (!this.world.isClient && this.isAlive() && this.canMoveVoluntarily()) {
            ++this.ticksSinceEaten;
            ItemStack stack = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (this.canEat(stack)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack finishedStack = stack.finishUsing(this.world, this);
                    if (!finishedStack.isEmpty()) {
                        this.equipStack(EquipmentSlot.MAINHAND, finishedStack);
                    }
                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1f) {
                    this.playSound(this.getEatSound(stack), 1.0f, 1.0f);
                    this.world.sendEntityStatus(this, (byte)45);
                }
            }
        }
        super.tickMovement();
    }

    private boolean canEat(ItemStack itemStack) {
        return itemStack.getItem().isFood() && this.getTarget() == null && this.onGround;
    }

    @Override
    public void handleStatus(byte id) {
        if (id == 45) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (!itemStack.isEmpty()) {
                for (int i = 0; i < 8; ++i) {
                    Vec3d vec3 = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0).rotateX(-this.getPitch() * ((float)Math.PI / 180)).rotateY(-this.getRoll() * ((float)Math.PI / 180));
                    this.world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack), this.getX() + this.getRotationVector().x / 2.0, this.getY(), this.getZ() + this.getRotationVector().z / 2.0, vec3.x, vec3.y + 0.05, vec3.z);
                }
            }
        } else {
            super.handleStatus(id);
        }
    }

    // FISH

    @Override
    public boolean canEquip(ItemStack pItemstack) {
        EquipmentSlot equipmentslot = MobEntity.getPreferredEquipmentSlot(pItemstack);
        if (!this.getEquippedStack(equipmentslot).isEmpty() || this.isBaby()) {
            return false;
        } else {
            return equipmentslot == EquipmentSlot.MAINHAND && super.canEquip(pItemstack);
        }
    }

    @Override
    public boolean canPickupItem(ItemStack pStack) {
        ItemStack itemstack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        return itemstack.isEmpty() && temptItems.contains(pStack.getItem()) && !this.isBaby();
    }

    @Override
    protected void loot(ItemEntity pItemEntity) {
        ItemStack itemstack = pItemEntity.getStack();
        if (this.canPickupItem(itemstack)) {
            int count = itemstack.getCount();
            if (count > 1) {
                this.dropItemStack(itemstack.split(count - 1));
            }

            this.triggerItemPickedUpByEntityCriteria(pItemEntity);
            this.equipStack(EquipmentSlot.MAINHAND, itemstack.split(1));
            this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 2.0F;
            this.sendPickup(pItemEntity, itemstack.getCount());
            pItemEntity.discard();
            this.ticksSinceEaten = 0;
        }
    }

    private void dropItemStack(ItemStack pStack) {
        ItemEntity itementity = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), pStack);
        this.world.spawnEntity(itementity);
    }

    // SOUNDS

    @Override
    protected SoundEvent getAmbientSound() {
        return Registrar.PENGUIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return Registrar.PENGUIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Registrar.PENGUIN_DEATH;
    }

    @Override
    public void tick() {
        if(!ServerConfig.getInstance().donkEnabled) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        super.tick();
        if (this.world.isClient) {
            if (this.slideAnimationProgress != this.lastSlideAnimationProgress || this.swimAnimationProgress != this.lastSwimAnimationProgress) {
                this.refreshPosition();
            }
        }
        this.updateSwimmingAnimation();
        this.updateSlidingAnimation();
    }

    public boolean canSlide() {
        return this.world.getBlockState(this.getBlockPos().down()).isOf(Blocks.ICE) && !this.isInLove() && this.getVelocity().horizontalLengthSquared() > 1.0E-6;
    }

    private void updateSlidingAnimation() {
        this.lastSlideAnimationProgress = this.slideAnimationProgress;
        this.slideAnimationProgress = this.canSlide() ? Math.min(1.0f, this.slideAnimationProgress + 0.15f) : Math.max(0.0f, this.slideAnimationProgress - 0.15f);
    }

    public float getSlidingAnimationProgress(float ticks) {
        return MathHelper.lerp(ticks, this.lastSlideAnimationProgress, this.slideAnimationProgress);
    }

    private void updateSwimmingAnimation() {
        this.lastSwimAnimationProgress = this.swimAnimationProgress;
        this.swimAnimationProgress = this.isSubmergedInWater() ? Math.min(1.0f, this.swimAnimationProgress + 0.15f) : Math.max(0.0f, this.swimAnimationProgress - 0.15f);
    }

    public float getSwimmingAnimationProgress(float ticks) {
        return MathHelper.lerp(ticks, this.lastSwimAnimationProgress, this.swimAnimationProgress);
    }

    @Override
    public void mobTick() {
        if (this.swimAnimationProgress > 0) {
            this.setPose(EntityPose.SWIMMING);
        } else if(this.slideAnimationProgress > 0) {
            this.setPose(EntityPose.CROUCHING);
        } else {
            this.setPose(EntityPose.STANDING);
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        float progress = this.slideAnimationProgress > 0 ? slideAnimationProgress : this.swimAnimationProgress > 0 ? swimAnimationProgress : 0.0f;
        if (progress > 0) {
            return super.getDimensions(pose).scaled(this.isBaby() ? 1.0f + progress : 1.0f + progress * 0.3F, 1.0f - progress / 2);
        }
        return super.getDimensions(pose).scaled(1.0f, this.isBaby() ? 1.4f : 1.0f);
    }

    @Override
    public int getMaxHeadRotation() {
        return 40;
    }

    @Override
    public EntityView method_48926() {
        return world;
    }

    static class PenguinPathNavigation extends MobNavigation {

        public PenguinPathNavigation(Penguin penguin, World world) {
            super(penguin, world);
        }

        @Override
        protected boolean isAtValidPosition() {
            return true;
        }

        @Override
        protected PathNodeNavigator createPathNodeNavigator(int p_149222_) {
            this.nodeMaker = new AmphibiousPathNodeMaker(false);
            return new PathNodeNavigator(this.nodeMaker, p_149222_);
        }

        @Override
        public boolean isValidPosition(BlockPos p_149224_) {
            return !this.world.getBlockState(p_149224_.down()).isAir();
        }
    }

    static class PenguinLookControl extends LookControl {
        private final int maxYRotFromCenter;

        public PenguinLookControl(MobEntity entity, int maxYRotFromCenter) {
            super(entity);
            this.maxYRotFromCenter = maxYRotFromCenter;
        }

        @Override
        public void tick() {
            if (this.lookAtTimer > 0) {
                --this.lookAtTimer;
                this.getTargetYaw().ifPresent(headYaw -> {
                    this.entity.headYaw = this.changeAngle(this.entity.headYaw, headYaw, this.maxYawChange);
                });
                this.getTargetPitch().ifPresent(xRot -> this.entity.setPitch(this.changeAngle(this.entity.getPitch(), xRot, this.maxPitchChange)));
            } else {
                if (this.entity.getNavigation().isIdle()) {
                    this.entity.setPitch(this.changeAngle(this.entity.getPitch(), 0.0f, 5.0f));
                }
                this.entity.headYaw = this.changeAngle(this.entity.headYaw, this.entity.bodyYaw, this.maxYawChange);
            }
            float f = MathHelper.wrapDegrees(this.entity.headYaw - this.entity.bodyYaw);
            if (f < (float)(-this.maxYRotFromCenter)) {
                this.entity.bodyYaw -= 4.0f;
            } else if (f > (float)this.maxYRotFromCenter) {
                this.entity.bodyYaw += 4.0f;
            }
        }
    }

    static class PenguinRandomSwimmingGoal extends SwimAroundGoal {
        private final Penguin penguin;

        public PenguinRandomSwimmingGoal(Penguin pMobEntity, double pSpeedModifier, int pInterval) {
            super(pMobEntity, pSpeedModifier, pInterval);
            this.penguin = pMobEntity;
        }

        @Override
        public boolean canStart() {
            if (penguin.isBaby() || !penguin.getMainHandStack().isEmpty()) {
                return false;
            } else {
                return super.canStart();
            }
        }
    }

    static class PenguinSearchForItemsGoal extends Goal {
        private final Penguin penguin;

        public PenguinSearchForItemsGoal(Penguin penguin) {
            this.setControls(EnumSet.of(Control.MOVE));
            this.penguin = penguin;
        }

        @Override
        public boolean canStart() {
            if (!penguin.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty() || penguin.isBaby()) {
                return false;
            } else {
                List<ItemEntity> list = penguin.world.getEntitiesByClass(ItemEntity.class, penguin.getBoundingBox().expand(8.0D, 8.0D, 8.0D), itemEntity -> temptItems.contains(itemEntity.getStack().getItem()));
                return !list.isEmpty() && penguin.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
            }
        }
        
        @Override
        public void tick() {
            List<ItemEntity> list = penguin.world.getEntitiesByClass(ItemEntity.class, penguin.getBoundingBox().expand(8.0D, 8.0D, 8.0D), itemEntity -> temptItems.contains(itemEntity.getStack().getItem()));
            ItemStack itemstack = penguin.getEquippedStack(EquipmentSlot.MAINHAND);
            if (itemstack.isEmpty() && !list.isEmpty()) {
                penguin.getNavigation().startMovingTo(list.get(0), 1.0F);
            }

        }
        
        @Override
        public void start() {
            List<ItemEntity> list = penguin.world.getEntitiesByClass(ItemEntity.class, penguin.getBoundingBox().expand(8.0D, 8.0D, 8.0D), itemEntity -> temptItems.contains(itemEntity.getStack().getItem()));
            if (!list.isEmpty()) {
                penguin.getNavigation().startMovingTo(list.get(0), 1.0F);
            }

        }
    }

    static class PenguinMeleeAttackGoal extends MeleeAttackGoal {
        private final Penguin penguin;

        public PenguinMeleeAttackGoal(Penguin pMobEntity, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
            super(pMobEntity, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
            this.penguin = pMobEntity;
        }

        @Override
        public boolean canStart() {
            if (!penguin.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty() || penguin.isBaby()) {
                return false;
            } else {
                return super.canStart();
            }
        }
    }

    static class PenguinFollowOwnerGoal extends FollowOwnerGoal {
        public PenguinFollowOwnerGoal(TameableEntity tameable, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
            super(tameable, speed, minDistance, maxDistance, leavesAllowed);
        }

        public boolean canStart() {
            LivingEntity livingEntity = this.tameable.getOwner();
            if (livingEntity == null) {
                return false;
            } else if (livingEntity.isSpectator()) {
                return false;
            } else if (this.cannotFollow()) {
                return false;
            } else if (this.tameable.squaredDistanceTo(livingEntity) < (double)(this.minDistance * this.minDistance)) {
                return false;
            } else {
                this.owner = livingEntity;
                return true;
            }
        }

        private boolean cannotFollow() {
            return this.tameable.isSitting() || this.tameable.hasVehicle() || this.tameable.isLeashed();
        }

        @Override
        protected boolean canTeleportTo(BlockPos pos) {
            PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this.world, pos.mutableCopy());
            if (pathNodeType != PathNodeType.WALKABLE && pathNodeType != PathNodeType.WATER) {
                return false;
            }
            BlockState blockState = this.world.getBlockState(pos.down());
            if (!this.leavesAllowed && blockState.getBlock() instanceof LeavesBlock) {
                return false;
            }
            BlockPos blockPos = pos.subtract(this.tameable.getBlockPos());
            return this.world.isSpaceEmpty(this.tameable, this.tameable.getBoundingBox().offset(blockPos));
        }
    }
}
