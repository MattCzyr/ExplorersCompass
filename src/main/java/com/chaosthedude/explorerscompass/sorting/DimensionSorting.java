package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class DimensionSorting implements ISorting {
	
	@Override
	public int compare(Identifier id1, Identifier id2) {
		return StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(id1)).compareTo(StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(id2)));
	}

	@Override
	public Object getValue(Identifier id) {
		return StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(id));
	}

	@Override
	public ISorting next() {
		return new GroupSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.explorerscompass.dimension");
	}

}
