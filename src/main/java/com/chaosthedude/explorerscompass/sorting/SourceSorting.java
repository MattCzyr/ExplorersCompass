package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SourceSorting implements ISorting {
	
	@Override
	public int compare(Identifier id1, Identifier id2) {
		return StructureUtils.getStructureSource(id1).compareTo(StructureUtils.getStructureSource(id2));
	}

	@Override
	public Object getValue(Identifier id) {
		return StructureUtils.getStructureSource(id);
	}

	@Override
	public ISorting next() {
		return new DimensionSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.explorerscompass.source");
	}

}
