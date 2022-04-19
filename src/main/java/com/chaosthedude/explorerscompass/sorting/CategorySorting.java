package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CategorySorting implements ISorting {
	
	private static final Minecraft mc = Minecraft.getInstance();
	
	@Override
	public int compare(ConfiguredStructureFeature<?, ?> structure1, ConfiguredStructureFeature<?, ?> structure2) {
		return StructureUtils.getKeyForStructure(mc.level, structure1.feature).compareTo(StructureUtils.getKeyForStructure(mc.level, structure2.feature));
	}

	@Override
	public Object getValue(ConfiguredStructureFeature<?, ?> structure) {
		return StructureUtils.getKeyForStructure(mc.level, structure.feature);
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
