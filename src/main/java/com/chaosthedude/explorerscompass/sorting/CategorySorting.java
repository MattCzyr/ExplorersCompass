package com.chaosthedude.explorerscompass.sorting;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CategorySorting implements ISorting {
	
	@Override
	public int compare(StructureFeature<?> structure1, StructureFeature<?> structure2) {
		return structure1.step().toString().compareTo(structure2.step().toString());
	}

	@Override
	public Object getValue(StructureFeature<?> structure) {
		return structure.step();
	}

	@Override
	public ISorting next() {
		return new NameSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.explorerscompass.category");
	}

}
