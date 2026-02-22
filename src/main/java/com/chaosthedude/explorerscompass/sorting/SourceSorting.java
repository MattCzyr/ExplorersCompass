package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

public class SourceSorting implements ISorting {

	@Override
	public int compare(Identifier key1, Identifier key2) {
		return StructureUtils.getStructureSource(key1).compareTo(StructureUtils.getStructureSource(key2));
	}

	@Override
	public Object getValue(Identifier key) {
		return StructureUtils.getStructureSource(key);
	}

	@Override
	public ISorting next() {
		return new DimensionSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.explorerscompass.source");
	}

}
