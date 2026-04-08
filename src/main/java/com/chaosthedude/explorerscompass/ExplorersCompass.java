package com.chaosthedude.explorerscompass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.SearchForNextPacket;
import com.chaosthedude.explorerscompass.network.SearchPacket;
import com.chaosthedude.explorerscompass.network.SyncPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ExplorersCompass implements ModInitializer {

	public static final String MODID = "explorerscompass";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static final ExplorersCompassItem EXPLORERS_COMPASS_ITEM = new ExplorersCompassItem();

	public static boolean synced;
	public static boolean canTeleport;
	public static int maxNextSearches;
	public static boolean infiniteXp;
	public static List<Identifier> allowedStructureIDs;
	public static Map<Identifier, Integer> xpLevelsForAllowedStructures;
	public static ListMultimap<Identifier, Identifier> allowedStructureIDsToDimensionIDs;
	public static Map<Identifier, Identifier> structureIDsToGroupIDs;

	@Override
	public void onInitialize() {
		ExplorersCompassConfig.load();

		Registry.register(Registries.ITEM, new Identifier(MODID, "explorerscompass"), EXPLORERS_COMPASS_ITEM);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(EXPLORERS_COMPASS_ITEM));

		ServerPlayNetworking.registerGlobalReceiver(SearchPacket.ID, SearchPacket::apply);
		ServerPlayNetworking.registerGlobalReceiver(SearchForNextPacket.ID, SearchForNextPacket::apply);
		ServerPlayNetworking.registerGlobalReceiver(TeleportPacket.ID, TeleportPacket::apply);

		allowedStructureIDs = new ArrayList<Identifier>();
		xpLevelsForAllowedStructures = new HashMap<Identifier, Integer>();
		allowedStructureIDsToDimensionIDs = ArrayListMultimap.create();
		structureIDsToGroupIDs = new HashMap<Identifier, Identifier>();
	}

}
