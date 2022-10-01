package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.worker.StructureSearchWorker;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

public class StructureUtils {
	
	public static ListMultimap<ResourceLocation, ResourceLocation> getStructureKeysToConfiguredStructureKeys(ServerLevel level) {
		ListMultimap<ResourceLocation, ResourceLocation> structureKeysToConfiguredStructureKeys = ArrayListMultimap.create();
		for (ConfiguredStructureFeature<?, ?> configuredStructure : getConfiguredStructureRegistry(level)) {
			structureKeysToConfiguredStructureKeys.put(getKeyForStructure(level, configuredStructure.feature), getKeyForConfiguredStructure(level, configuredStructure));
		}
		return structureKeysToConfiguredStructureKeys;
	}
	
	public static Map<ResourceLocation, ResourceLocation> getConfiguredStructureKeysToStructureKeys(ServerLevel level) {
		Map<ResourceLocation, ResourceLocation> configuredStructureKeysToStructureKeys = new HashMap<ResourceLocation, ResourceLocation>();
		for (ConfiguredStructureFeature<?, ?> configuredStructure : getConfiguredStructureRegistry(level)) {
			configuredStructureKeysToStructureKeys.put(getKeyForConfiguredStructure(level, configuredStructure), getKeyForStructure(level, configuredStructure.feature));
		}
		return configuredStructureKeysToStructureKeys;
	}
	
	public static Registry<ConfiguredStructureFeature<?, ?>> getConfiguredStructureRegistry(ServerLevel level) {
		return level.registryAccess().ownedRegistryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
	}

	public static ResourceLocation getKeyForConfiguredStructure(ServerLevel level, ConfiguredStructureFeature<?, ?> structure) {
		return getConfiguredStructureRegistry(level).getKey(structure);
	}

	public static ConfiguredStructureFeature<?, ?> getConfiguredStructureForKey(ServerLevel level, ResourceLocation key) {
		return getConfiguredStructureRegistry(level).get(key);
	}
	
	public static ResourceLocation getKeyForStructure(Level level, StructureFeature<?> structure) {
		return structure.getRegistryName();
	}

	public static List<ResourceLocation> getAllowedConfiguredStructureKeys(ServerLevel level) {
		final List<ResourceLocation> structures = new ArrayList<ResourceLocation>();
		for (ConfiguredStructureFeature<?, ?> structure : getConfiguredStructureRegistry(level)) {
			if (structure != null && getKeyForConfiguredStructure(level, structure) != null && !configuredStructureIsBlacklisted(level, structure)) {
				structures.add(getKeyForConfiguredStructure(level, structure));
			}
		}
		return structures;
	}
	
	public static boolean configuredStructureIsBlacklisted(ServerLevel level, ConfiguredStructureFeature<?, ?> structure) {
		final List<String> structureBlacklist = ConfigHandler.GENERAL.structureBlacklist.get();
		for (String structureKey : structureBlacklist) {
			if (getKeyForConfiguredStructure(level, structure).toString().matches(convertToRegex(structureKey))) {
				return true;
			}
		}
		return false;
	}
	
	public static Holder<ConfiguredStructureFeature<?, ?>> getHolderForStructure(ServerLevel level, ConfiguredStructureFeature<?, ?> structure) {
		Optional<ResourceKey<ConfiguredStructureFeature<?, ?>>> optional = getConfiguredStructureRegistry(level).getResourceKey(structure);
		if (optional.isPresent()) {
			return getConfiguredStructureRegistry(level).getHolderOrThrow(optional.get());
		}
		return null;
	}

	public static List<ResourceLocation> getGeneratingDimensionKeys(ServerLevel serverLevel, ConfiguredStructureFeature<?, ?> structure) {
		final List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
		for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
			ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
            Set<Holder<Biome>> biomeSet = chunkGenerator.getBiomeSource().possibleBiomes();
            if (!structure.biomes().stream().noneMatch(biomeSet::contains)) {
            	dimensions.add(level.dimension().location());
            }
		}
		// Fix empty dimensions for stronghold
		if (structure.feature == StructureFeature.STRONGHOLD && dimensions.isEmpty()) {
			dimensions.add(new ResourceLocation("minecraft:overworld"));
		}
		return dimensions;
	}

	public static ListMultimap<ResourceLocation, ResourceLocation> getGeneratingDimensionsForAllowedConfiguredStructures(ServerLevel serverLevel) {
		ListMultimap<ResourceLocation, ResourceLocation> dimensionsForAllowedConfiguredStructures = ArrayListMultimap.create();
		for (ResourceLocation structureKey : getAllowedConfiguredStructureKeys(serverLevel)) {
			ConfiguredStructureFeature<?, ?> structure = getConfiguredStructureForKey(serverLevel, structureKey);
			dimensionsForAllowedConfiguredStructures.putAll(structureKey, getGeneratingDimensionKeys(serverLevel, structure));
		}
		return dimensionsForAllowedConfiguredStructures;
	}
	
	public static int getHorizontalDistanceToLocation(Player player, int x, int z) {
		return getHorizontalDistanceToLocation(player.blockPosition(), x, z);
	}

	public static int getHorizontalDistanceToLocation(BlockPos startPos, int x, int z) {
		return (int) Mth.sqrt((float) startPos.distSqr(new BlockPos(x, startPos.getY(), z)));
	}
	
	@OnlyIn(Dist.CLIENT)
	public static String getPrettyStructureName(ResourceLocation key) {
		String name = key.toString();
		if (ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = I18n.get(Util.makeDescriptionId("structure", key));
		}
		if (name.equals(Util.makeDescriptionId("structure", key)) || !ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = key.toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	@OnlyIn(Dist.CLIENT)
	public static String getPrettyStructureSource(ResourceLocation key) {
		if (key == null) {
			return "";
		}
		String registryEntry = key.toString();
		String modid = registryEntry.substring(0, registryEntry.indexOf(":"));
		if (modid.equals("minecraft")) {
			return "Minecraft";
		}
		Optional<? extends ModContainer> sourceContainer = ModList.get().getModContainerById(modid);
		if (sourceContainer.isPresent()) {
			return sourceContainer.get().getModInfo().getDisplayName();
		}
		return modid;
	}

	@OnlyIn(Dist.CLIENT)
	public static String dimensionKeysToString(List<ResourceLocation> dimensions) {
		Set<String> dimensionNames = new HashSet<String>();
		dimensions.forEach((key) -> dimensionNames.add(getDimensionName(key)));
		return String.join(", ", dimensionNames);
	}

	@OnlyIn(Dist.CLIENT)
	private static String getDimensionName(ResourceLocation dimensionKey) {
		String name = I18n.get(Util.makeDescriptionId("dimension", dimensionKey));
		if (name.equals(Util.makeDescriptionId("dimension", dimensionKey))) {
			name = dimensionKey.toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	private static String convertToRegex(String glob) {
 		String regex = "^";
 		for (char i = 0; i < glob.length(); i++) {
 			char c = glob.charAt(i);
 			if (c == '*') {
 				regex += ".*";
 			} else if (c == '?') {
 				regex += ".";
 			} else if (c == '.') {
 				regex += "\\.";
 			} else {
 				regex += c;
 			}
 		}
 		regex += "$";
 		return regex;
 	}

}