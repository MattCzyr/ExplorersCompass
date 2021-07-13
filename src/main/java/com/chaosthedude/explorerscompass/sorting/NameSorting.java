package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.gen.feature.StructureFeature;

@Environment(EnvType.CLIENT)
public class NameSorting implements ISorting {
	
	@Override
	public int compare(StructureFeature<?> structure1, StructureFeature<?> structure2) {
		return StructureUtils.getStructureName(structure1).compareTo(StructureUtils.getStructureName(structure2));
	}

	@Override
	public Object getValue(StructureFeature<?> structure) {
		return StructureUtils.getStructureName(structure);
	}

	@Override
	public ISorting next() {
		return new SourceSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.explorerscompass.name");
	}

}
