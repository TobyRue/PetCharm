package io.github.tobyrue.pet_charm;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "pet_charm", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Entity entity = event.getTarget();
        InteractionHand hand = event.getHand();

        if (stack.is(PetCharm.PET_CHARM.get())) {
            CompoundTag tag = stack.getOrCreateTag();

            if (entity instanceof Mob mob && mob.getType().is(PetCharm.PET_CHARM_WHITELIST)) {
                LivingEntity owner = null;
                boolean ownable = false;

                if (entity instanceof TamableAnimal tameable) {
                    owner = tameable.getOwner();
                    ownable = true;
                } else if (entity instanceof OwnableEntity ownableEntity) {
                    owner = (LivingEntity) ownableEntity.getOwner();
                    ownable = true;
                }

                if ((owner == player || !ownable) && !tag.hasUUID("StoredMobUUID")) {
                    UUID uuid = mob.getUUID();

                    mob.setInvulnerable(true);
                    mob.setPersistenceRequired();

                    tag.putUUID("StoredMobUUID", uuid);

                    CompoundTag mobNbt = new CompoundTag();
                    mob.saveWithoutId(mobNbt);
                    tag.put("StoredMobNBT", mobNbt);

                    tag.putString("StoredMobType", EntityType.getKey(mob.getType()).toString());
                    tag.putString("StoredMobName", mob.getName().getString());
                    tag.putString("StoredOwnerName", player.getName().getString());

                    player.displayClientMessage(Component.translatable(PetCharm.PET_CHARM.get().getDescriptionId() + ".action.bind"), true);

                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }
}