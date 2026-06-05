package io.github.tobyrue.pet_charm;

import com.mojang.logging.LogUtils;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Objects;

@Mod(PetCharm.MODID)
public class PetCharm {


    public static final String MODID = "pet_charm";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<EntityType<?>> PET_CHARM_WHITELIST = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, ResourceLocation.fromNamespaceAndPath(MODID, "pet_charm_whitelist"));


    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> PET_CHARM = ITEMS.register("pet_charm", () -> new PetCharmItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1).fireResistant()));

    public PetCharm(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
//        LOGGER.info("HELLO FROM COMMON SETUP");
//
//        if (Config.logDirtBlock)
//            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
//
//        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
//
//        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
//        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }

        @SubscribeEvent
        public static void onColorHandler(RegisterColorHandlersEvent.Item event) {
            event.getItemColors().register((stack, tintIndex) -> {
                CompoundTag tag = stack.getOrCreateTag();

                if (tintIndex == 1) {
                    if (tag.hasUUID("StoredMobUUID")) {
                        try {
                            return 0xFF000000 | Integer.parseInt(Objects.requireNonNull(tag.getUUID("StoredMobUUID")).toString(), 0, 6, 16) & 0xFFF0F0F0;
                        } catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException ignored) {
                        }
                    } else {
                        return 0xFF00AA2C;
                    }
                }
                return 0xFFFFFFFF;
            }, PET_CHARM.get());
        }
    }
}
