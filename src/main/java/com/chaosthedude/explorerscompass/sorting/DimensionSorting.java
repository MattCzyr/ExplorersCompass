package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.gen.feature.StructureFeature;

@Environment(EnvType.CLIENT)
public class DimensionSorting implements ISorting {
	
	@Override
	public int compare(StructureFeature<?> structure1, StructureFeature<?> structure2) {
		return StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(structure1)).compareTo(StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(structure2)));
	}

	@Override
	public Object getValue(StructureFeature<?> structure) {
		return StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(structure));
	}

	@Override
	public ISorting next() {
		return new CategorySorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.explorerscompass.dimension");
	}

}
