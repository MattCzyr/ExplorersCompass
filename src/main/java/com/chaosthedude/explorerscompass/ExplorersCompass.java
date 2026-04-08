package com.chaosthedude.explorerscompass;

import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.CompassSearchForNextPacket;
import com.chaosthedude.explorerscompass.network.CompassSearchPacket;
import com.chaosthedude.explorerscompass.network.SyncPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(ExplorersCompass.MODID)
public class ExplorersCompass {

	public static final String MODID = "explorerscompass";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static SimpleChannel network;
	public static ExplorersCompassItem explorersCompass;

	public static boolean synced;
	public static boolean canTeleport;
	public static int maxNextSearches;
	public static boolean infiniteXp;
	public static List<ResourceLocation> allowedStructureKeys;
	public static Map<ResourceLocation, Integer> xpLevelsForAllowedStructureKeys;
	public static ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedStructureKeys;
	public static Map<ResourceLocation, ResourceLocation> structureKeysToTypeKeys;

	public ExplorersCompass() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::buildCreativeTabContents);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.GENERAL_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		network = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> "1.0", s -> true, s -> true);

		// Server packets
		network.registerMessage(0, CompassSearchPacket.class, CompassSearchPacket::toBytes, CompassSearchPacket::new, CompassSearchPacket::handle);
		network.registerMessage(1, TeleportPacket.class, TeleportPacket::toBytes, TeleportPacket::new, TeleportPacket::handle);
		network.registerMessage(3, CompassSearchForNextPacket.class, CompassSearchForNextPacket::toBytes, CompassSearchForNextPacket::new, CompassSearchForNextPacket::handle);

		// Client packet
		network.registerMessage(2, SyncPacket.class, SyncPacket::toBytes, SyncPacket::new, SyncPacket::handle);

		allowedStructureKeys = new ArrayList<ResourceLocation>();
		xpLevelsForAllowedStructureKeys = new HashMap<ResourceLocation, Integer>();
		dimensionKeysForAllowedStructureKeys = ArrayListMultimap.create();
		structureKeysToTypeKeys = new HashMap<ResourceLocation, ResourceLocation>();
	}

	private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(new ItemStack(explorersCompass));
		}
	}

}