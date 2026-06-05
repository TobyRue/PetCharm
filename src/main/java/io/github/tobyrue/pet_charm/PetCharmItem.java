package io.github.tobyrue.pet_charm;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PetCharmItem extends SimpleFoiledItem {
    public PetCharmItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        CompoundTag tag = stack.getOrCreateTag();

        if (tag.hasUUID("StoredMobUUID") && world instanceof ServerLevel serverWorld) {
            UUID uuid = tag.getUUID("StoredMobUUID");
            var entity = serverWorld.getEntity(uuid);

            if (entity != null) {
                if (entity instanceof Mob mob && mob.getType().is(PetCharm.PET_CHARM_WHITELIST)) {
                    if (tryTeleportToOwner(mob, player)) {
                        player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".action.teleport"), true);
                    }
                    CompoundTag mobNbt = new CompoundTag();
                    mob.saveWithoutId(mobNbt);
                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
                    tag.put("StoredMobNBT", mobNbt);
                }
            } else {
                if (tag.contains("StoredMobNBT") && tag.contains("StoredMobType")) {
                    String typeId = tag.getString("StoredMobType");
                    Optional<EntityType<?>> type = EntityType.byString(typeId);

                    if (type.isPresent()) {
                        var newEntity = type.get().create(serverWorld);
                        if (newEntity instanceof Mob mob && mob.getType().is(PetCharm.PET_CHARM_WHITELIST)) {
                            mob.load(tag.getCompound("StoredMobNBT"));

                            mob.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

                            mob.setInvulnerable(true);
                            mob.setPersistenceRequired();
                            serverWorld.addFreshEntity(mob);

                            tag.putUUID("StoredMobUUID", mob.getUUID());
                            player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".action.revive"), true);
                        }
                    }
                }
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    public boolean tryTeleportToOwner(Mob entity, LivingEntity owner) {
        if (owner != null) {
            return this.tryTeleportNear(owner.blockPosition(), entity);
        }
        return false;
    }

    private boolean tryTeleportNear(BlockPos pos, Mob entity) {
        for (int i = 0; i < 10; ++i) {
            int j = entity.getRandom().nextInt(7) - 3; // nextBetween equivalent
            int k = entity.getRandom().nextInt(7) - 3;
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = entity.getRandom().nextInt(3) - 1;
                return this.tryTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k, entity);
            }
        }
        return false;
    }

    private boolean tryTeleportTo(int x, int y, int z, Mob entity) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!this.canTeleportTo(pos, entity)) {
            return false;
        } else {
            entity.moveTo((double) x + 0.5, (double) y, (double) z + 0.5, entity.getYRot(), entity.getXRot());
            entity.getNavigation().stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos, Mob entity) {
        BlockPathTypes pathNodeType = WalkNodeEvaluator.getBlockPathTypeStatic(entity.level, pos.mutable());
        if (pathNodeType != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState blockState = entity.level.getBlockState(pos.below());
            if (!this.canTeleportOntoLeaves() && blockState.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                return entity.level.noCollision(entity, entity.getBoundingBox().move(pos.subtract(entity.blockPosition())));
            }
        }
    }

    protected boolean canTeleportOntoLeaves() {
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return false;
//        return tag.hasUUID("StoredMobUUID");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> text, TooltipFlag type) {
        super.appendHoverText(stack, level, text, type);
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID("StoredMobUUID")) {
            text.add(Component.translatable(this.getDescriptionId() + ".tooltip.stored_mob", tag.getString("StoredMobName"), tag.getString("StoredMobType")).withStyle(ChatFormatting.GRAY));
            text.add(Component.translatable(this.getDescriptionId() + ".tooltip.stored_owner", tag.getString("StoredOwnerName")).withStyle(ChatFormatting.GRAY));
            if (type.isAdvanced()) {
                text.add(Component.translatable(this.getDescriptionId() + ".tooltip.stored_uuid", tag.getUUID("StoredMobUUID")).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}

