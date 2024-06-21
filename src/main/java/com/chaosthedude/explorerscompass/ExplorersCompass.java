package com.chaosthedude.explorerscompass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.SearchPacket;
import com.chaosthedude.explorerscompass.network.SyncPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(ExplorersCompass.MODID)
public class ExplorersCompass {

	public static final String MODID = "explorerscompass";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static ExplorersCompassItem explorersCompass;
	
	public static final DataComponentType<String> STRUCTURE_ID_COMPONENT = DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();
	public static final DataComponentType<Integer> COMPASS_STATE_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> FOUND_X_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> FOUND_Z_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> SEARCH_RADIUS_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> SAMPLES_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Boolean> DISPLAY_COORDS_COMPONENT = DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();

	public static boolean canTeleport;
	public static List<ResourceLocation> allowedStructureKeys;
	public static ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedStructureKeys;
	public static Map<ResourceLocation, ResourceLocation> structureKeysToTypeKeys;
	public static ListMultimap<ResourceLocation, ResourceLocation> typeKeysToStructureKeys;
	
	public ExplorersCompass(ModContainer modContainer) {
		modContainer.getEventBus().addListener(this::commonSetup);
		modContainer.getEventBus().addListener(this::buildCreativeTabContents);
		modContainer.getEventBus().addListener(this::registerPayloads);
		
		modContainer.registerConfig(ModConfig.Type.COMMON, ConfigHandler.GENERAL_SPEC);
		modContainer.registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		allowedStructureKeys = new ArrayList<ResourceLocation>();
		dimensionKeysForAllowedStructureKeys = ArrayListMultimap.create();
		structureKeysToTypeKeys = new HashMap<ResourceLocation, ResourceLocation>();
		typeKeysToStructureKeys = ArrayListMultimap.create();
	}
	
	private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(new ItemStack(explorersCompass));
		}
	}
	
	private void registerPayloads(RegisterPayloadHandlersEvent event) {
	    final PayloadRegistrar registrar = event.registrar(MODID);
	    registrar.playToServer(SearchPacket.TYPE, SearchPacket.CODEC, SearchPacket::handle);
	    registrar.playToServer(TeleportPacket.TYPE, TeleportPacket.CODEC, TeleportPacket::handle);
	    registrar.playToClient(SyncPacket.TYPE, SyncPacket.CODEC, SyncPacket::handle);
	}

}