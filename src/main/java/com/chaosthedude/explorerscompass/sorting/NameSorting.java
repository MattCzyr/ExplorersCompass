package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class NameSorting implements ISorting {
	
	@Override
	public int compare(Identifier id1, Identifier id2) {
		return StructureUtils.getStructureName(id1).compareTo(StructureUtils.getStructureName(id2));
	}

	@Override
	public Object getValue(Identifier id) {
		return StructureUtils.getStructureName(id);
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
