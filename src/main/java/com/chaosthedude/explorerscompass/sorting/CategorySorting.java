package com.chaosthedude.explorerscompass.sorting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.gen.feature.StructureFeature;

@Environment(EnvType.CLIENT)
public class CategorySorting implements ISorting {
	
	@Override
	public int compare(StructureFeature<?> structure1, StructureFeature<?> structure2) {
		return structure1.getGenerationStep().toString().compareTo(structure2.getGenerationStep().toString());
	}

	@Override
	public Object getValue(StructureFeature<?> structure) {
		return structure.getGenerationStep();
	}

	@Override
	public ISorting next() {
		return new NameSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.explorerscompass.category");
	}

}
