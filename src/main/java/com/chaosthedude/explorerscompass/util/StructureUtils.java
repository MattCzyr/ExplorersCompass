package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.workers.StructureSearchWorker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;

public class StructureUtils {

	public static Identifier getIDForStructure(StructureFeature<?> structure) {
		return Registry.STRUCTURE_FEATURE.getId(structure);
	}

	public static StructureFeature<?> getStructureForID(Identifier key) {
		return Registry.STRUCTURE_FEATURE.get(key);
	}

	public static List<StructureFeature<?>> getAllowedStructures() {
		final List<StructureFeature<?>> structures = new ArrayList<StructureFeature<?>>();
		for (StructureFeature<?> structure : Registry.STRUCTURE_FEATURE) {
			if (structure != null && getIDForStructure(structure) != null && !getIDForStructure(structure).getNamespace().isEmpty() && !getIDForStructure(structure).getPath().isEmpty() &&  !structureIsBlacklisted(structure)) {
				structures.add(structure);
			}
		}

		return structures;
	}

	public static void searchForStructure(ServerWorld world, PlayerEntity player, ItemStack stack, StructureFeature<?> structure, BlockPos startPos) {
		StructureSearchWorker worker = new StructureSearchWorker(world, player, stack, structure, startPos);
		worker.start();
	}

	public static int getDistanceToStructure(PlayerEntity player, int biomeX, int biomeZ) {
		return getDistanceToStructure(player.getBlockPos(), biomeX, biomeZ);
	}

	public static int getDistanceToStructure(BlockPos startPos, int structureX, int structureZ) {
		return (int) MathHelper.sqrt((float) startPos.getSquaredDistance(new BlockPos(structureX, startPos.getY(), structureZ)));
	}

	@Environment(EnvType.CLIENT)
	public static String getStructureName(StructureFeature<?> structure) {
		if (getIDForStructure(structure) == null) {
			return "";
		}
		String name = getIDForStructure(structure).toString();
		if (ExplorersCompassConfig.translateStructureNames) {
			name = I18n.translate(Util.createTranslationKey("structure", getIDForStructure(structure)));
		}
		if (name.equals(Util.createTranslationKey("structure", getIDForStructure(structure))) || !ExplorersCompassConfig.translateStructureNames) {
			name = getIDForStructure(structure).toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	@Environment(EnvType.CLIENT)
	public static String getStructureName(Identifier key) {
		return getStructureName(getStructureForID(key));
	}

	@Environment(EnvType.CLIENT)
	public static String getStructureSource(StructureFeature<?> structure) {
		if (getIDForStructure(structure) == null) {
			return "";
		}
		String registryEntry = getIDForStructure(structure).toString();
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

	public static List<Identifier> getStructureDimensions(ServerWorld serverWorld, StructureFeature<?> structure) {
		final List<Identifier> dimensions = new ArrayList<>();
		for (ServerWorld world : serverWorld.getServer().getWorlds()) {
			ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
			if (chunkGenerator.getStructuresConfig().getForType(structure) != null && chunkGenerator.getBiomeSource().hasStructureFeature(structure)) {
				dimensions.add(world.getRegistryKey().getValue());
			}
		}
		return dimensions;
	}

	public static Map<StructureFeature<?>, List<Identifier>> getDimensionsForAllowedStructures(ServerWorld serverWorld) {
		Map<StructureFeature<?>, List<Identifier>> dimensionsForAllowedStructures = new HashMap<StructureFeature<?>, List<Identifier>>();
		for (StructureFeature<?> structure : getAllowedStructures()) {
			dimensionsForAllowedStructures.put(structure, getStructureDimensions(serverWorld, structure));
		}
		return dimensionsForAllowedStructures;
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

	public static boolean structureIsBlacklisted(StructureFeature<?> structure) {
		final List<String> structureBlacklist = ExplorersCompassConfig.structureBlacklist;
		for (String structureKey : structureBlacklist) {
			if (getIDForStructure(structure).toString().matches(convertToRegex(structureKey))) {
				return true;
			}
		}
		return false;
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