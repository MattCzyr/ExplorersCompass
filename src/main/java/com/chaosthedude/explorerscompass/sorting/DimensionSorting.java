package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
		return I18n.get("string.explorerscompass.dimension");
	}

}
