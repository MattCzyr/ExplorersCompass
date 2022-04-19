package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NameSorting implements ISorting {
	
	private static final Minecraft mc = Minecraft.getInstance();
	
	@Override
	public int compare(ConfiguredStructureFeature<?, ?> structure1, ConfiguredStructureFeature<?, ?> structure2) {
		return StructureUtils.getConfiguredStructureName(mc.level, structure1).compareTo(StructureUtils.getConfiguredStructureName(mc.level, structure2));
	}

	@Override
	public Object getValue(ConfiguredStructureFeature<?, ?> structure) {
		return StructureUtils.getConfiguredStructureName(mc.level, structure);
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
