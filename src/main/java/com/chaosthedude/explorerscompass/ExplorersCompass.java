package com.chaosthedude.explorerscompass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.item.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.SearchPacket;
import com.chaosthedude.explorerscompass.network.SyncPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.serialization.Codec;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;

public class ExplorersCompass implements ModInitializer {

	public static final String MODID = "explorerscompass";

	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static final ExplorersCompassItem EXPLORERS_COMPASS_ITEM = new ExplorersCompassItem();
	
	public static final DataComponentType<String> STRUCTURE_ID_COMPONENT = DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();
	public static final DataComponentType<Integer> COMPASS_STATE_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> FOUND_X_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> FOUND_Z_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> SEARCH_RADIUS_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> SAMPLES_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Boolean> DISPLAY_COORDS_COMPONENT = DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();

	public static boolean canTeleport;
	public static boolean infiniteXp;
	public static List<Identifier> allowedStructures;
	public static Map<Identifier, Integer> xpLevelsForAllowedStructures;
	public static ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures;
	public static Map<Identifier, Identifier> structureIdsToGroupIds;
	public static ListMultimap<Identifier, Identifier> groupIdsToStructureIds;

	@Override
	public void onInitialize() {
		ExplorersCompassConfig.load();
		
		Registry.register(BuiltInRegistries.ITEM, ExplorersCompassItem.KEY, EXPLORERS_COMPASS_ITEM);
		
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "structure_id"), STRUCTURE_ID_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "compass_state"), COMPASS_STATE_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "found_x"), FOUND_X_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "found_z"), FOUND_Z_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "search_radius"), SEARCH_RADIUS_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "samples"), SAMPLES_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "display_coords"), DISPLAY_COORDS_COMPONENT);
		
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(EXPLORERS_COMPASS_ITEM));
		
		PayloadTypeRegistry.playC2S().register(SearchPacket.TYPE, SearchPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportPacket.TYPE, TeleportPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(SyncPacket.TYPE, SyncPacket.CODEC);
		
		ServerPlayNetworking.registerGlobalReceiver(SearchPacket.TYPE, SearchPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(TeleportPacket.TYPE, TeleportPacket::handle);
		
		allowedStructures = new ArrayList<Identifier>();
		xpLevelsForAllowedStructures = new HashMap<Identifier, Integer>();
		dimensionsForAllowedStructures = ArrayListMultimap.create();
		structureIdsToGroupIds = new HashMap<Identifier, Identifier>();
		groupIdsToStructureIds = ArrayListMultimap.create();
	}

}