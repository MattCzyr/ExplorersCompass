package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
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

public class StructureUtils {
	
	public static Map<Identifier, Identifier> structureIdsToGroupIds(ServerLevel level) {
		Map<Identifier, Identifier> structureIdsToGroupIds = new HashMap<Identifier, Identifier>();
		if (getStructureRegistry(level).isPresent()) {
			for (Structure structure : getStructureRegistry(level).get()) {
				structureIdsToGroupIds.put(getIdForStructure(level, structure), getGroupForStructure(level, structure));
			}
		}
		return structureIdsToGroupIds;
	}
	
	public static Identifier getGroupForStructure(ServerLevel level, Structure structure) {
		if (getStructureRegistry(level).isPresent()) {
			Registry<StructureSet> registry = getStructureSetRegistry(level).get();
			for (StructureSet set : registry) {
				for (StructureSelectionEntry entry : set.structures()) {
					if (entry.structure().value().equals(structure)) {
						return registry.getKey(set);
					}
				}
			}
		}
		return Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "none");
	}
	
	public static List<Identifier> getStructuresForGroup(ServerLevel level, Identifier groupId) {
		List<Identifier> structureIds = new ArrayList<Identifier>();
		if (getStructureSetRegistry(level).isPresent()) {	
			Registry<StructureSet> registry = getStructureSetRegistry(level).get();
			if (registry.containsKey(groupId)) {
				StructureSet set = registry.getValue(groupId);
				for (StructureSelectionEntry entry : set.structures()) {
					Identifier structureId = getIdForStructure(level, entry.structure().value());
					if (structureId != null) {
						structureIds.add(structureId);
					}
				}
			}
		}
		return structureIds;
	}

	public static Identifier getIdForStructure(ServerLevel level, Structure structure) {
		if (getStructureRegistry(level).isPresent()) {
			return getStructureRegistry(level).get().getKey(structure);
		}
		return null;
	}

	public static Structure getStructureForId(ServerLevel level, Identifier id) {
		if (getStructureRegistry(level).isPresent()) {
			return getStructureRegistry(level).get().getValue(id);
		}
		return null;
	}
	
	public static Holder<Structure> getHolderForStructure(ServerLevel level, Structure structure) {
		if (getStructureRegistry(level).isPresent()) {
			return getStructureRegistry(level).get().wrapAsHolder(structure);
		}
		return null;
	}

	public static List<Identifier> getAllowedStructureIds(ServerLevel level) {
		final List<Identifier> structureIDs = new ArrayList<Identifier>();
		if (getStructureRegistry(level).isPresent()) {
			for (Structure structure : getStructureRegistry(level).get()) {
				if (structure != null && getIdForStructure(level, structure) != null && !getIdForStructure(level, structure).getNamespace().isEmpty() && !getIdForStructure(level, structure).getPath().isEmpty() && !structureIsBlacklisted(level, structure) && !structureIsHidden(level, structure)) {
					structureIDs.add(getIdForStructure(level, structure));
				}
			}
		}

		return structureIDs;
	}
	
	public static boolean structureIsBlacklisted(ServerLevel level, Structure structure) {
		final List<String> structureBlacklist = ExplorersCompassConfig.structureBlacklist;
		for (String structureKey : structureBlacklist) {
			if (getIdForStructure(level, structure).toString().matches(convertToRegex(structureKey))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean structureIsHidden(ServerLevel level, Structure structure) {
		if (getStructureRegistry(level).isPresent()) {
			final Registry<Structure> structureRegistry = getStructureRegistry(level).get();
			final Holder<Structure> structureHolder = structureRegistry.wrapAsHolder(structure);
			return structureHolder.tags().anyMatch(tag -> tag.location().toString().equals("c:hidden_from_locator_selection"));
		}
		return false;
	}
	
	public static List<Identifier> getGeneratingDimensionIds(ServerLevel serverLevel, Structure structure) {
		final List<Identifier> dimensions = new ArrayList<Identifier>();
		for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
			ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
			Set<Holder<Biome>> biomeSet = chunkGenerator.getBiomeSource().possibleBiomes();
			if (!structure.biomes().stream().noneMatch(biomeSet::contains)) {
				dimensions.add(level.dimension().identifier());
			}
		}
		return dimensions;
	}

	public static ListMultimap<Identifier, Identifier> getGeneratingDimensionIdsForAllowedStructures(ServerLevel serverLevel, List<Identifier> allowedStructures) {
		ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures = ArrayListMultimap.create();
		for (Identifier id : allowedStructures) {
			Structure structure = getStructureForId(serverLevel, id);
			dimensionsForAllowedStructures.putAll(id, getGeneratingDimensionIds(serverLevel, structure));
		}
		return dimensionsForAllowedStructures;
	}
	
	public static int getXpLevelsForStructure(ServerLevel serverLevel, Identifier structureKey) {
		int xpLevels = ExplorersCompassConfig.defaultXpLevel;
		for (String structureRegex : ExplorersCompassConfig.perStructureXpLevels.keySet()) {
			if (structureKey.toString().matches(convertToRegex(structureRegex))) {
				xpLevels = ExplorersCompassConfig.perStructureXpLevels.get(structureRegex);
				if (xpLevels > 3) {
					xpLevels = 3;
				}
				break;
			}
		}
		return xpLevels;
	}

	public static Map<Identifier, Integer> getXpLevelsForAllowedStructures(ServerLevel serverLevel, List<Identifier> allowedStructures) {
		final Map<Identifier, Integer> xpLevels = new HashMap<Identifier, Integer>();
		for (Identifier structureKey : allowedStructures) {
			xpLevels.put(structureKey, getXpLevelsForStructure(serverLevel, structureKey));
		}
		return xpLevels;
	}

	public static int getHorizontalDistanceToLocation(Player player, int x, int z) {
		return getHorizontalDistanceToLocation(player.blockPosition(), x, z);
	}

	public static int getHorizontalDistanceToLocation(BlockPos startPos, int x, int z) {
		return (int) Mth.sqrt((float) startPos.distSqr(new BlockPos(x, startPos.getY(), z)));
	}

	@Environment(EnvType.CLIENT)
	public static String getStructureName(Identifier id) {
		if (id == null) {
			return "";
		}
		String name = id.toString();
		if (ExplorersCompassConfig.translateStructureNames) {
			name = I18n.get(Util.makeDescriptionId("structure", id));
		}
		if (name.equals(Util.makeDescriptionId("structure", id)) || !ExplorersCompassConfig.translateStructureNames) {
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
	
	private static Optional<Registry<Structure>> getStructureRegistry(ServerLevel level) {
		return level.registryAccess().lookup(Registries.STRUCTURE);
	}
	
	private static Optional<Registry<StructureSet>> getStructureSetRegistry(ServerLevel level) {
		return level.registryAccess().lookup(Registries.STRUCTURE_SET);
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