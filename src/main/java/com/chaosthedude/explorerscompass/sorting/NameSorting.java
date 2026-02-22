package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

public class NameSorting implements ISorting {

	@Override
	public int compare(Identifier key1, Identifier key2) {
		return StructureUtils.getStructureName(key1).compareTo(StructureUtils.getStructureName(key2));
	}

	@Override
	public Object getValue(Identifier key) {
		return StructureUtils.getStructureName(key);
	}

	@Override
	public ISorting next() {
		return new SourceSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.explorerscompass.name");
	}

}
