package com.chaosthedude.explorerscompass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
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
import net.minecraft.component.DataComponentType;
import net.minecraft.item.ItemGroups;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ExplorersCompass implements ModInitializer {

	public static final String MODID = "explorerscompass";

	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static final ExplorersCompassItem EXPLORERS_COMPASS_ITEM = new ExplorersCompassItem();
	
	public static final DataComponentType<String> STRUCTURE_ID_COMPONENT = DataComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build();
	public static final DataComponentType<Integer> COMPASS_STATE_COMPONENT = DataComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build();
	public static final DataComponentType<Integer> FOUND_X_COMPONENT = DataComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build();
	public static final DataComponentType<Integer> FOUND_Z_COMPONENT = DataComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build();
	public static final DataComponentType<Integer> SEARCH_RADIUS_COMPONENT = DataComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build();
	public static final DataComponentType<Integer> SAMPLES_COMPONENT = DataComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build();
	public static final DataComponentType<Boolean> DISPLAY_COORDS_COMPONENT = DataComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build();
	
	public static boolean canTeleport;
	public static List<Identifier> allowedStructureIDs;
	public static ListMultimap<Identifier, Identifier> allowedStructureIDsToDimensionIDs;
	public static Map<Identifier, Identifier> structureIDsToGroupIDs;
	public static ListMultimap<Identifier, Identifier> groupIDsToStructureIDs;

	@Override
	public void onInitialize() {
		ExplorersCompassConfig.load();
		
		Registry.register(Registries.ITEM, new Identifier(MODID, "explorerscompass"), EXPLORERS_COMPASS_ITEM);
		
		Registry.register(Registries.DATA_COMPONENT_TYPE, new Identifier(MODID, "structure_id"), STRUCTURE_ID_COMPONENT);
		Registry.register(Registries.DATA_COMPONENT_TYPE, new Identifier(MODID, "compass_state"), COMPASS_STATE_COMPONENT);
		Registry.register(Registries.DATA_COMPONENT_TYPE, new Identifier(MODID, "found_x"), FOUND_X_COMPONENT);
		Registry.register(Registries.DATA_COMPONENT_TYPE, new Identifier(MODID, "found_z"), FOUND_Z_COMPONENT);
		Registry.register(Registries.DATA_COMPONENT_TYPE, new Identifier(MODID, "search_radius"), SEARCH_RADIUS_COMPONENT);
		Registry.register(Registries.DATA_COMPONENT_TYPE, new Identifier(MODID, "samples"), SAMPLES_COMPONENT);
		Registry.register(Registries.DATA_COMPONENT_TYPE, new Identifier(MODID, "display_coords"), DISPLAY_COORDS_COMPONENT);
		
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(EXPLORERS_COMPASS_ITEM));
		
		PayloadTypeRegistry.playC2S().register(SearchPacket.PACKET_ID, SearchPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportPacket.PACKET_ID, TeleportPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(SyncPacket.PACKET_ID, SyncPacket.PACKET_CODEC);
		
		ServerPlayNetworking.registerGlobalReceiver(SearchPacket.PACKET_ID, SearchPacket::apply);
		ServerPlayNetworking.registerGlobalReceiver(TeleportPacket.PACKET_ID, TeleportPacket::apply);
		
		allowedStructureIDs = new ArrayList<Identifier>();
		allowedStructureIDsToDimensionIDs = ArrayListMultimap.create();
		structureIDsToGroupIDs = new HashMap<Identifier, Identifier>();
		groupIDsToStructureIDs = ArrayListMultimap.create();
	}

}