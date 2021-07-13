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
import com.chaosthedude.explorerscompass.network.TeleportPacket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;

public class ExplorersCompass implements ModInitializer {

	public static final String MODID = "explorerscompass";

	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static final ExplorersCompassItem EXPLORERS_COMPASS_ITEM = new ExplorersCompassItem();

	public static boolean canTeleport;
	public static List<StructureFeature<?>> allowedStructures;
	public static Map<StructureFeature<?>, List<Identifier>> dimensionsForAllowedStructures;

	@Override
	public void onInitialize() {
		ExplorersCompassConfig.load();
		
		Registry.register(Registry.ITEM, new Identifier(MODID, "explorerscompass"), EXPLORERS_COMPASS_ITEM);
		
		ServerPlayNetworking.registerGlobalReceiver(SearchPacket.ID, SearchPacket::apply);
		ServerPlayNetworking.registerGlobalReceiver(TeleportPacket.ID, TeleportPacket::apply);
		
		allowedStructures = new ArrayList<StructureFeature<?>>();
		dimensionsForAllowedStructures = new HashMap<StructureFeature<?>, List<Identifier>>();
	}

}