package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

public class DimensionSorting implements ISorting {
	
	@Override
	public int compare(Identifier key1, Identifier key2) {
		return StructureUtils.dimensionIdsToString(ExplorersCompass.dimensionsForAllowedStructures.get(key1)).compareTo(StructureUtils.dimensionIdsToString(ExplorersCompass.dimensionsForAllowedStructures.get(key2)));
	}

	@Override
	public Object getValue(Identifier key) {
		return StructureUtils.dimensionIdsToString(ExplorersCompass.dimensionsForAllowedStructures.get(key));
	}

	@Override
	public ISorting next() {
		return new XpLevelsSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.explorerscompass.dimension");
	}

}
