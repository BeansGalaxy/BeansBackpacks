package com.beansgalaxy.galaxybackpacks.entity;

import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.screen.BackSlot;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;

public class BackpackEntity extends Backpack {
    protected BlockPos pos;
    public double YPosRaw;
    public Direction direction;
    private int breakScore;

    public BackpackEntity(World world, int x, double y, int z, Direction direction, DefaultedList<ItemStack> stacks) {
        this(Main.ENTITY_BACKPACK, world);
        this.YPosRaw = y;
        this.pos = BlockPos.ofFloored(x, y, z);
        this.setDirection(direction);

        this.itemStacks.addAll(stacks);
        stacks.clear();
    }

    public BackpackEntity(EntityType<Entity> entityEntityType, World world) {
        super(entityEntityType, world);
        this.intersectionChecked = true;
    }

    public Backpack createMirror() {
        Backpack entity = new Backpack(getWorld());
        entity.setDisplay(this.getDisplay());
        entity.headPitch = this.headPitch;
        return entity;
    }

    protected float getEyeHeight(EntityPose p_31784_, EntityDimensions p_31785_) {
        return 6F / 16;
    }

    public Direction getHorizontalFacing() {
        return this.direction;
    }

    protected void setDirection(Direction direction) {
        if (direction != null) {
            this.direction = direction;
            if (direction.getAxis().isHorizontal()) {
                this.setNoGravity(true);
                this.setYaw((float) direction.getHorizontal() * 90);
            }
            this.prevPitch = this.getPitch();
            this.prevYaw = this.getYaw();
            this.recalculateBoundingBox();
        }
    }

    public void setPosition(double x, double y, double z) {
        this.YPosRaw = y;
        this.pos = BlockPos.ofFloored(x, y, z);
        this.recalculateBoundingBox();
        this.velocityDirty = true;
    }

    public static Box newBox(BlockPos blockPos, double y, double height, Direction direction) {
        double x1 = blockPos.getX() + 0.5D;
        double z1 = blockPos.getZ() + 0.5D;
        double Wx = 8D / 32;
        double Wz = 8D / 32;
        if (direction != null) {
            if (direction.getAxis().isHorizontal()) {
                double D = 4D / 32;
                double off = 6D / 16;
                int stepX = direction.getOffsetX();
                int stepZ = direction.getOffsetZ();
                Wx -= D * Math.abs(stepX);
                Wz -= D * Math.abs(stepZ);
                x1 -= off * stepX;
                z1 -= off * stepZ;
            } else {
                Wx -= 1D / 16;
                Wz -= 1D / 16;
            }
        }

        return new Box(x1 - Wx, y, z1 - Wz, x1 + Wx, y + height, z1 + Wz);
    }

    // BUILDS NEW BOUNDING BOX
    protected void recalculateBoundingBox() {
        double x = this.pos.getX() + 0.5D;
        double y = this.YPosRaw;
        double z = this.pos.getZ() + 0.5D;
        if (direction != null && direction.getAxis().isHorizontal()) {
            double off = 6D / 16;
            int stepX = this.direction.getOffsetX();
            int stepZ = this.direction.getOffsetZ();
            x -= off * stepX;
            z -= off * stepZ;
        }

        Box box = newBox(this.pos, y, 9D / 16, direction);

        this.setPos(x, y, z);
        this.setBoundingBox(box);
    }

    /** IMPLEMENTS GRAVITY WHEN HUNG BACKPACKS LOSES SUPPORTING BLOCK **/
    public void tick() {
        super.tick();
        this.setNoGravity(this.hasNoGravity() && !this.getWorld().isSpaceEmpty(this, this.getBoundingBox().expand(0.1, -0.1, 0.1)));
        boolean inLava = this.isInLava();
        Kind b$kind = getKind();
        if (!this.hasNoGravity()) {
            if (this.isTouchingWater()) {
                inWaterGravity();
            } else if (inLava) {
                if (b$kind == Kind.NETHERITE && this.isSubmergedIn(FluidTags.LAVA) && getVelocity().y < 0.1) {
                    this.setVelocity(this.getVelocity().add(0D, 0.02D, 0D));
                }
                this.setVelocity(this.getVelocity().multiply(0.6D));
            } else {
                this.setVelocity(this.getVelocity().add(0.0D, -0.03D, 0.0D));
                this.setVelocity(this.getVelocity().multiply(0.98D));
            }
        }
        if (breakScore > 0)
            breakScore -= 1;
        if (inLava && b$kind != Kind.NETHERITE)
            breakScore += 8;
        this.move(MovementType.SELF, this.getVelocity());
        this.baseTick();
    }

    private void inWaterGravity() {
        Box thisBox = this.getBoundingBox();
        Box box = new Box(thisBox.maxX, thisBox.maxY + 6D / 16D, thisBox.maxZ, thisBox.minX, thisBox.maxY, thisBox.minZ);
        List<Entity> entityList = this.getEntityWorld().getOtherEntities(this, box);
        if (!entityList.isEmpty()) {
            Entity entity = entityList.get(0);
            double velocity = this.YPosRaw - entity.getY();
            if (entityList.get(0) instanceof PlayerEntity)
                this.setVelocity(0, velocity / 10, 0);
            else if (velocity < -0.6)
                inWaterBob();
            else this.setVelocity(0, velocity / 20, 0);
        } else inWaterBob();
    }

    private void inWaterBob() {
        if (this.isSubmergedInWater()) {
            this.setVelocity(this.getVelocity().multiply(0.95D));
            this.setVelocity(this.getVelocity().add(0D, 0.003D, 0D));
        } else if (this.isTouchingWater() && getVelocity().y < 0.01) {
            this.setVelocity(this.getVelocity().multiply(0.9D));
            this.setVelocity(this.getVelocity().add(0D, -0.01D, 0D));
        }
    }

    public boolean doesRenderOnFire() {
        return getKind() == Kind.NETHERITE ? false : this.isOnFire() && !this.isSpectator();
    }

    public boolean isFireImmune() {
        return getKind() == Kind.NETHERITE ? true : this.getType().isFireImmune();
    }

    /** DATA MANAGEMENT **/
    // CLIENT
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this, this.direction.getId());
    }
    public void onSpawnPacket(EntitySpawnS2CPacket p_149626_) {
        super.onSpawnPacket(p_149626_);
        this.setDirection(Direction.byId(p_149626_.getEntityData()));
    }

    // NBT
    protected void writeCustomDataToNbt(NbtCompound tag) {
        this.writeNbt(tag, this.getItemStacks().isEmpty());
        tag.putByte("Facing", (byte)this.direction.getId());
        tag.put("Display", getDisplay());
    }

    protected void readCustomDataFromNbt(NbtCompound tag) {
        this.readStackNbt(tag);
        this.setDirection(Direction.byId(tag.getByte("Facing")));
        this.setDisplay(tag.getCompound("Display"));
    }

    // LOCAL
    public NbtCompound getDisplay() {
        NbtCompound tag = new NbtCompound();
        tag.putString("Kind", this.dataTracker.get(BACKPACK_KIND));
        tag.putInt("Color", this.dataTracker.get(BACKPACK_COLOR));
        tag.putString("Material", this.dataTracker.get(BACKPACK_MATERIAL));
        tag.putString("Pattern", this.dataTracker.get(BACKPACK_PATTERN));
        return tag;
    }

    /** COLLISIONS AND INTERACTIONS **/
    public boolean collidesWith(Entity that) {
        return (that.isCollidable() || that.isPushable()) && !this.isConnectedThroughVehicle(that);
    }
    public boolean isCollidable() {
        return true;
    }

    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof PlayerEntity player) {
            return this.damage(this.getDamageSources().playerAttack(player), 0.0f);
        }
        return false;
    }

    public boolean damage(DamageSource damageSource, float amount) {
        if ((damageSource.isOf(DamageTypes.IN_FIRE) || damageSource.isOf(DamageTypes.ON_FIRE) || damageSource.isOf(DamageTypes.LAVA)) && this.isFireImmune())
            return false;
        double height = 0.05D;
        if (damageSource.isOf(DamageTypes.EXPLOSION) || damageSource.isOf(DamageTypes.PLAYER_EXPLOSION)) {
            height += Math.sqrt(amount) / 10;
            return hop(height);
        }
        if (damageSource.isOf(DamageTypes.ARROW) || damageSource.isOf(DamageTypes.THROWN) || damageSource.isOf(DamageTypes.TRIDENT) || damageSource.isOf(DamageTypes.MOB_PROJECTILE))
            return hop(height * 2);
        if (damageSource.isOf(DamageTypes.PLAYER_ATTACK) && damageSource.getSource() instanceof PlayerEntity player) {
            if (player.isCreative()) {
                this.kill();
                this.scheduleVelocityUpdate();
            }
            else {
                Slot backSlot = BackSlot.get(player);
                if (!backSlot.hasStack() && this.getItemStacks().isEmpty())
                    return attemptEquip(player, this);

                double d = Math.sqrt(getItemStacks().size());
                double damage = (d * -8 + 40);
                breakScore += Math.min(Math.max(8, damage), 30) / 2;

                if (breakScore > 45) {
                    breakAndDropContents();
                    return true;
                }
                else {
                    PlaySound.HIT.at(this);
                    return hop(height);
                }
            }
        }

        if (amount > 30)
            breakScore += 100;
        else hop(height);
        return true;
    }

    private void breakAndDropContents() {
        PlaySound.BREAK.at(this);
        boolean dropItems = getWorld().getGameRules().getBoolean(GameRules.DO_TILE_DROPS);
        if (dropItems) {
            while (!getItemStacks().isEmpty()) {
                ItemStack stack = getItemStacks().remove(0);
                this.dropStack(stack);
            }
        }
        ItemStack backpack = Kind.toStack(this);
        if (!this.isRemoved() && !this.getWorld().isClient()) {
            this.kill();
            this.scheduleVelocityUpdate();
            if (dropItems) this.dropStack(backpack);
        }
    }

    public boolean hop(double height) {
        if (this.hasNoGravity())
            this.setNoGravity(false);
        else {
            this.setVelocity(this.getVelocity().add(0.0D, height, 0.0D));
            if (!this.direction.getAxis().isHorizontal())
                this.setYaw(this.getYaw() + random.nextInt(10) - 4);
        }
        return true;
    }

    // PREFORMS THIS ACTION WHEN IT IS RIGHT-CLICKED
    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        boolean sprintKeyPressed = BackSlot.get(player).sprintKeyIsPressed;
        ItemStack backStack = BackSlot.get(player).getStack();
        ItemStack handStack = player.getMainHandStack();
        ItemStack backpackStack = sprintKeyPressed ? backStack : handStack;

        if (Kind.isBackpack(backpackStack))
            return BackpackItem.useOnBackpack(player, this, backpackStack, sprintKeyPressed);

        if (!sprintKeyPressed) {
            if (viewers < 1)
                PlaySound.OPEN.at(this);
            player.openHandledScreen(this);
            return ActionResult.SUCCESS;
        }

        attemptEquip(player, this);
        return ActionResult.SUCCESS;
    }

    public static boolean attemptEquip(PlayerEntity player, BackpackEntity backpackEntity) {
        Slot backSlot = BackSlot.get(player);
        if (backSlot.hasStack() && !backpackEntity.isRemoved()) {
            if (backpackEntity.getItemStacks().isEmpty()) {
                if (!player.getWorld().isClient())
                    backpackEntity.dropStack(Kind.toStack(backpackEntity));
                PlaySound.BREAK.at(backpackEntity);
            }
            else {
                PlaySound.HIT.at(backpackEntity);
                return backpackEntity.hop(.1);
            }
        }
        else {
/*                  Equips Backpack only if...
                      - damage source is player.
                      - player is not creative.
                      - backSlot is not occupied */
            DefaultedList<ItemStack> playerInventoryStacks = BackSlot.getInventory(player).getItemStacks();
            DefaultedList<ItemStack> backpackEntityStacks = backpackEntity.getItemStacks();
            playerInventoryStacks.clear();
            playerInventoryStacks.addAll(backpackEntityStacks);
            backSlot.setStack(Kind.toStack(backpackEntity));
            PlaySound.EQUIP.at(player);
        }
        if (!backpackEntity.isRemoved() && !player.getWorld().isClient()) {
            backpackEntity.kill();
            backpackEntity.scheduleVelocityUpdate();
        }
        return true;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return Kind.toStack(this);
    }
    /** REQUIRED FEILDS **/
    protected boolean shouldSetPositionOnLoad() {
        return false;
    }

    public boolean canHit() {
        return true;
    }

    public void onOpen(PlayerEntity player) {
        this.addViewer(player);
    }

    public Vec3d position() {
        return new Vec3d(this.pos.getX(), this.YPosRaw, this.pos.getZ());
    }

}
