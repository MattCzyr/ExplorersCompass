package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSet.StructureSelectionEntry;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public class StructureUtils {

	public static ListMultimap<Identifier, Identifier> groupIdsToStructureIds(ServerLevel level) {
		ListMultimap<Identifier, Identifier> groupIdsToStructureIds = ArrayListMultimap.create();
		for (Structure structure : getStructureRegistry(level)) {
			groupIdsToStructureIds.put(getGroupForStructure(level, structure), getIdForStructure(level, structure));
		}
		return groupIdsToStructureIds;
	}

	public static Map<Identifier, Identifier> structureIdsToGroupIds(ServerLevel level) {
		Map<Identifier, Identifier> structureIdsToGroupIds = new HashMap<Identifier, Identifier>();
		for (Structure structure : getStructureRegistry(level)) {
			structureIdsToGroupIds.put(getIdForStructure(level, structure), getGroupForStructure(level, structure));
		}
		return structureIdsToGroupIds;
	}

	public static Identifier getGroupForStructure(ServerLevel level, Structure structure) {
		Registry<StructureSet> registry = getStructureSetRegistry(level);
		for (StructureSet set : registry) {
			for (StructureSelectionEntry entry : set.structures()) {
				if (entry.structure().value().equals(structure)) {
					return registry.getKey(set);
				}
			}
		}
		return Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "none");
	}

	public static Identifier getIdForStructure(ServerLevel level, Structure structure) {
		return getStructureRegistry(level).getKey(structure);
	}

	public static Structure getStructureForId(ServerLevel level, Identifier structureId) {
		return getStructureRegistry(level).getValue(structureId);
	}
	
	public static Holder<Structure> getHolderForStructure(ServerLevel level, Structure structure) {
		return getStructureRegistry(level).wrapAsHolder(structure);
	}

	public static List<Identifier> getAllowedStructureIds(ServerLevel level) {
		final List<Identifier> structures = new ArrayList<Identifier>();
		for (Structure structure : getStructureRegistry(level)) {
			if (structure != null && getIdForStructure(level, structure) != null && !structureIsBlacklisted(level, structure) && !structureIsHidden(level, structure)) {
				structures.add(getIdForStructure(level, structure));
			}
		}
		return structures;
	}

	public static boolean structureIsBlacklisted(ServerLevel level, Structure structure) {
		final List<String> structureBlacklist = ConfigHandler.GENERAL.structureBlacklist.get();
		for (String structureRegex : structureBlacklist) {
			if (getIdForStructure(level, structure).toString().matches(convertToRegex(structureRegex))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean structureIsHidden(ServerLevel level, Structure structure) {
		final Registry<Structure> structureRegistry = getStructureRegistry(level);
		return structureRegistry.wrapAsHolder(structure).tags().anyMatch(tag -> tag.location().getPath().equals("c:hidden_from_locator_selection"));
	}

	public static List<Identifier> getGeneratingDimensions(ServerLevel serverLevel, Structure structure) {
		final List<Identifier> dimensions = new ArrayList<Identifier>();
		for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
			ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
			Set<Holder<Biome>> biomeSet = chunkGenerator.getBiomeSource().possibleBiomes();
			if (!structure.biomes().stream().noneMatch(biomeSet::contains)) {
				dimensions.add(level.dimension().identifier());
			}
		}
		// Fix empty dimensions for stronghold
		if (structure == StructureType.STRONGHOLD && dimensions.isEmpty()) {
			dimensions.add(Identifier.parse("minecraft:overworld"));
		}
		return dimensions;
	}

	public static ListMultimap<Identifier, Identifier> getGeneratingDimensionsForAllowedStructures(ServerLevel serverLevel, List<Identifier> allowedStructures) {
		ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures = ArrayListMultimap.create();
		for (Identifier structureId : allowedStructures) {
			Structure structure = getStructureForId(serverLevel, structureId);
			dimensionsForAllowedStructures.putAll(structureId, getGeneratingDimensions(serverLevel, structure));
		}
		return dimensionsForAllowedStructures;
	}

	public static int getXpLevelsForStructure(ServerLevel serverLevel, Identifier structureId) {
		int xpLevels = ConfigHandler.GENERAL.defaultXpLevels.get();
		final Map<String, Integer> xpLevelOverrides = parseXpLevelOverridesConfig();
		for (String structureRegex : xpLevelOverrides.keySet()) {
			if (structureId.toString().matches(convertToRegex(structureRegex))) {
				xpLevels = xpLevelOverrides.get(structureRegex);
				if (xpLevels > 3) {
					xpLevels = 3;
				}
				break;
			}
		}
		return xpLevels;
	}

	public static Map<String, Integer> parseXpLevelOverridesConfig() {
		final List<String> xpLevelOverrides = ConfigHandler.GENERAL.perStructureXpLevels.get();
		Map<String, Integer> parsedOverrides = new HashMap<String, Integer>();
		for (String override : xpLevelOverrides) {
			String[] split = override.split(",");
			if (split.length != 2) {
				continue;
			}
			String structureRegex = split[0];
			String xpLevelsStr = split[1];
			try {
				int xpLevels = Integer.valueOf(xpLevelsStr);
				parsedOverrides.put(structureRegex, xpLevels);
			} catch (NumberFormatException e) {
				continue;
			}
		}
		return parsedOverrides;
	}

	public static Map<Identifier, Integer> getXpLevelsForAllowedStructures(ServerLevel serverLevel, List<Identifier> allowedStructures) {
		final Map<Identifier, Integer> xpLevels = new HashMap<Identifier, Integer>();
		for (Identifier structureId : allowedStructures) {
			xpLevels.put(structureId, getXpLevelsForStructure(serverLevel, structureId));
		}
		return xpLevels;
	}

	public static int getHorizontalDistanceToLocation(Player player, int x, int z) {
		return getHorizontalDistanceToLocation(player.blockPosition(), x, z);
	}

	public static int getHorizontalDistanceToLocation(BlockPos startPos, int x, int z) {
		return (int) Mth.sqrt((float) startPos.distSqr(new BlockPos(x, startPos.getY(), z)));
	}

	public static String getStructureName(Identifier structureId) {
		String name = structureId.toString();
		if (ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = I18n.get(Util.makeDescriptionId("structure", structureId));
		}
		if (name.equals(Util.makeDescriptionId("structure", structureId)) || !ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = structureId.toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	public static String getStructureSource(Identifier structureId) {
		if (structureId == null) {
			return "";
		}
		String registryEntry = structureId.toString();
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

	public static String dimensionIdsToString(List<Identifier> dimensions) {
		Set<String> dimensionNames = new HashSet<String>();
		dimensions.forEach((id) -> dimensionNames.add(getDimensionName(id)));
		return String.join(", ", dimensionNames);
	}

	private static String getDimensionName(Identifier dimensionId) {
		String name = I18n.get(Util.makeDescriptionId("dimension", dimensionId));
		if (name.equals(Util.makeDescriptionId("dimension", dimensionId))) {
			name = dimensionId.toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	private static Registry<Structure> getStructureRegistry(ServerLevel level) {
		return level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
	}

	private static Registry<StructureSet> getStructureSetRegistry(ServerLevel level) {
		return level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
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