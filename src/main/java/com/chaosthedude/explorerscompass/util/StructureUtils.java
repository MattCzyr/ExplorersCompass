package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;

public class StructureUtils {
	
	public static ListMultimap<Identifier, Identifier> getStructureIDsToConfiguredStructureIDs(ServerWorld world) {
		ListMultimap<Identifier, Identifier> structureKeysToConfiguredStructureKeys = ArrayListMultimap.create();
		for (ConfiguredStructureFeature<?, ?> configuredStructure : getConfiguredStructureRegistry(world)) {
			structureKeysToConfiguredStructureKeys.put(getIDForStructure(world, configuredStructure.feature), getIDForConfiguredStructure(world, configuredStructure));
		}
		return structureKeysToConfiguredStructureKeys;
	}
	
	public static Map<Identifier, Identifier> getConfiguredStructureIDsToStructureIDs(ServerWorld world) {
		Map<Identifier, Identifier> configuredStructureKeysToStructureKeys = new HashMap<Identifier, Identifier>();
		for (ConfiguredStructureFeature<?, ?> configuredStructure : getConfiguredStructureRegistry(world)) {
			configuredStructureKeysToStructureKeys.put(getIDForConfiguredStructure(world, configuredStructure), getIDForStructure(world, configuredStructure.feature));
		}
		return configuredStructureKeysToStructureKeys;
	}
	
	public static Registry<ConfiguredStructureFeature<?, ?>> getConfiguredStructureRegistry(ServerWorld world) {
		return world.getRegistryManager().get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY);
	}

	public static Identifier getIDForConfiguredStructure(ServerWorld world, ConfiguredStructureFeature<?, ?> configuredStructure) {
		return getConfiguredStructureRegistry(world).getId(configuredStructure);
	}

	public static ConfiguredStructureFeature<?, ?> getConfiguredStructureForID(ServerWorld world, Identifier id) {
		return getConfiguredStructureRegistry(world).get(id);
	}
	
	public static Identifier getIDForStructure(ServerWorld world, StructureFeature<?> structure) {
		return StructureContext.from(world).registryManager().get(Registry.STRUCTURE_FEATURE_KEY).getId(structure);
	}

	public static List<Identifier> getAllowedConfiguredStructureIDs(ServerWorld world) {
		final List<Identifier> configuredStructureIDs = new ArrayList<Identifier>();
		for (ConfiguredStructureFeature<?, ?> configuredStructure : getConfiguredStructureRegistry(world)) {
			if (configuredStructure != null && getIDForConfiguredStructure(world, configuredStructure) != null && !getIDForConfiguredStructure(world, configuredStructure).getNamespace().isEmpty() && !getIDForConfiguredStructure(world, configuredStructure).getPath().isEmpty() && !structureIsBlacklisted(world, configuredStructure)) {
				configuredStructureIDs.add(getIDForConfiguredStructure(world, configuredStructure));
			}
		}

		return configuredStructureIDs;
	}
	
	public static RegistryEntry<ConfiguredStructureFeature<?, ?>> getEntryForStructure(ServerWorld world, ConfiguredStructureFeature<?, ?> structure) {
		Optional<RegistryKey<ConfiguredStructureFeature<?, ?>>> optional = getConfiguredStructureRegistry(world).getKey(structure);
		if (optional.isPresent()) {
			return getConfiguredStructureRegistry(world).getEntry(optional.get()).get();
		}
		return null;
	}
	
	public static boolean structureIsBlacklisted(ServerWorld world, ConfiguredStructureFeature<?, ?> structure) {
		final List<String> structureBlacklist = ExplorersCompassConfig.structureBlacklist;
		for (String structureKey : structureBlacklist) {
			if (getIDForConfiguredStructure(world, structure).toString().matches(convertToRegex(structureKey))) {
				return true;
			}
		}
		return false;
	}
	
	public static List<Identifier> getGeneratingDimensionIDs(ServerWorld serverWorld, ConfiguredStructureFeature<?, ?> structure) {
		final List<Identifier> dimensions = new ArrayList<Identifier>();
		for (ServerWorld world : serverWorld.getServer().getWorlds()) {
			ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
			Set<RegistryEntry<Biome>> biomeSet = chunkGenerator.getBiomeSource().getBiomes();
			if (!structure.getBiomes().stream().noneMatch(biomeSet::contains)) {
				dimensions.add(world.getRegistryKey().getValue());
			}
		}
		// Fix empty dimensions for stronghold
		if (structure.feature == StructureFeature.STRONGHOLD && dimensions.isEmpty()) {
			dimensions.add(new Identifier("minecraft:overworld"));
		}
		return dimensions;
	}

	public static ListMultimap<Identifier, Identifier> getGeneratingDimensionIDsForAllowedConfiguredStructures(ServerWorld serverWorld) {
		ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures = ArrayListMultimap.create();
		for (Identifier id : getAllowedConfiguredStructureIDs(serverWorld)) {
			ConfiguredStructureFeature<?, ?> configuredStructure = getConfiguredStructureForID(serverWorld, id);
			dimensionsForAllowedStructures.putAll(id, getGeneratingDimensionIDs(serverWorld, configuredStructure));
		}
		return dimensionsForAllowedStructures;
	}

	public static int getHorizontalDistanceToLocation(PlayerEntity player, int x, int z) {
		return getHorizontalDistanceToLocation(player.getBlockPos(), x, z);
	}

	public static int getHorizontalDistanceToLocation(BlockPos startPos, int x, int z) {
		return (int) MathHelper.sqrt((float) startPos.getSquaredDistance(new BlockPos(x, startPos.getY(), z)));
	}

	@Environment(EnvType.CLIENT)
	public static String getStructureName(Identifier id) {
		if (id == null) {
			return "";
		}
		String name = id.toString();
		if (ExplorersCompassConfig.translateStructureNames) {
			name = I18n.translate(Util.createTranslationKey("structure", id));
		}
		if (name.equals(Util.createTranslationKey("structure", id)) || !ExplorersCompassConfig.translateStructureNames) {
			name = id.toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	@Environment(EnvType.CLIENT)
	public static String getStructureSource(Identifier id) {
		if (id == null) {
			return "";
		}
		String registryEntry = id.toString();
		String modid = registryEntry.substring(0, registryEntry.indexOf(":"));
		if (modid.equals("minecraft")) {
			return "Minecraft";
		}
		Optional<? extends ModContainer> sourceContainer = FabricLoader.getInstance().getModContainer(modid);
		if (sourceContainer.isPresent()) {
			return sourceContainer.get().getMetadata().getName();
		}
		return modid;
	}

	@Environment(EnvType.CLIENT)
	public static String structureDimensionsToString(List<Identifier> dimensions) {
		String str = "";
		if (dimensions != null && dimensions.size() > 0) {
			str = getDimensionName(dimensions.get(0));
			for (int i = 1; i < dimensions.size(); i++) {
				str += ", " + getDimensionName(dimensions.get(i));
			}
		}
		return str;
	}

	@Environment(EnvType.CLIENT)
	private static String getDimensionName(Identifier dimensionKey) {
		String name = I18n.translate(Util.createTranslationKey("dimension", dimensionKey));
		if (name.equals(Util.createTranslationKey("dimension", dimensionKey))) {
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