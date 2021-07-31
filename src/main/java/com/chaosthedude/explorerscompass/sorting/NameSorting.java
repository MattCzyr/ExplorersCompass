package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
		return I18n.get("string.explorerscompass.name");
	}

}
