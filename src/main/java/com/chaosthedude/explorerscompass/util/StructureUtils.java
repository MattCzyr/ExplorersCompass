package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.google.common.collect.ImmutableMultimap;

import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureUtils {

	public static ResourceLocation getKeyForStructure(StructureFeature<?> structure) {
		return ForgeRegistries.STRUCTURE_FEATURES.getKey(structure);
	}

	public static StructureFeature<?> getStructureForKey(ResourceLocation key) {
		return ForgeRegistries.STRUCTURE_FEATURES.getValue(key);
	}

	public static List<StructureFeature<?>> getAllowedStructures() {
		final List<StructureFeature<?>> structures = new ArrayList<StructureFeature<?>>();
		for (StructureFeature<?> structure : ForgeRegistries.STRUCTURE_FEATURES) {
			if (structure != null && getStructureForKey(structure.getRegistryName()) != null && getKeyForStructure(structure) != null && !structureIsBlacklisted(structure)) {
				structures.add(structure);
			}
		}

		return structures;
	}

	public static void searchForStructure(ServerLevel serverLevel, Player player, ItemStack stack, StructureFeature<?> structure, BlockPos startPos) {
		StructureSearchWorker worker = new StructureSearchWorker(serverLevel, player, stack, structure, startPos);
		worker.start();
	}

	public static int getDistanceToStructure(Player player, int biomeX, int biomeZ) {
		return getDistanceToStructure(player.blockPosition(), biomeX, biomeZ);
	}

	public static int getDistanceToStructure(BlockPos startPos, int structureX, int structureZ) {
		return (int) Mth.sqrt((float) startPos.distSqr(new BlockPos(structureX, startPos.getY(), structureZ)));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getStructureName(StructureFeature<?> structure) {
		if (getKeyForStructure(structure) == null) {
			return "";
		}
		String name = getKeyForStructure(structure).toString();
		if (ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = I18n.get(Util.makeDescriptionId("structure", getKeyForStructure(structure)));
		}
		if (name.equals(Util.makeDescriptionId("structure", getKeyForStructure(structure))) || !ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = getKeyForStructure(structure).toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	@OnlyIn(Dist.CLIENT)
	public static String getStructureName(ResourceLocation key) {
		return getStructureName(getStructureForKey(key));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getStructureSource(StructureFeature<?> structure) {
		if (getKeyForStructure(structure) == null) {
			return "";
		}
		String registryEntry = getKeyForStructure(structure).toString();
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

	public static List<ResourceLocation> getStructureDimensions(ServerLevel serverLevel, StructureFeature<?> structure) {
		final List<ResourceLocation> dimensions = new ArrayList<>();
		for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
			ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
			ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> multimap = chunkGenerator.getSettings().structures(structure);
			if (chunkGenerator.getSettings().getConfig(structure) != null && !multimap.isEmpty()) {
				Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
	            Set<ResourceKey<Biome>> set = chunkGenerator.getBiomeSource().possibleBiomes().stream().flatMap((biome) -> {
	               return registry.getResourceKey(biome).stream();
	            }).collect(Collectors.toSet());
	            if (!multimap.values().stream().noneMatch(set::contains)) {
	            	dimensions.add(level.dimension().location());
	            }
			}
		}
		// Fix empty dimensions for stronghold
		if (structure == StructureFeature.STRONGHOLD && dimensions.isEmpty()) {
			dimensions.add(new ResourceLocation("minecraft:overworld"));
		}
		return dimensions;
	}

	public static Map<StructureFeature<?>, List<ResourceLocation>> getDimensionsForAllowedStructures(ServerLevel serverLevel) {
		Map<StructureFeature<?>, List<ResourceLocation>> dimensionsForAllowedStructures = new HashMap<StructureFeature<?>, List<ResourceLocation>>();
		for (StructureFeature<?> structure : getAllowedStructures()) {
			dimensionsForAllowedStructures.put(structure, getStructureDimensions(serverLevel, structure));
		}
		return dimensionsForAllowedStructures;
	}

	@OnlyIn(Dist.CLIENT)
	public static String structureDimensionsToString(List<ResourceLocation> dimensions) {
		String str = "";
		if (dimensions != null && dimensions.size() > 0) {
			str = getDimensionName(dimensions.get(0));
			for (int i = 1; i < dimensions.size(); i++) {
				str += ", " + getDimensionName(dimensions.get(i));
			}
		}
		return str;
	}

	public static boolean structureIsBlacklisted(StructureFeature<?> structure) {
		final List<String> structureBlacklist = ConfigHandler.GENERAL.structureBlacklist.get();
		for (String structureKey : structureBlacklist) {
			if (getKeyForStructure(structure).toString().matches(convertToRegex(structureKey))) {
				return true;
			}
		}
		return false;
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