package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DimensionSorting implements ISorting {
	
	private static final Minecraft mc = Minecraft.getInstance();
	
	@Override
	public int compare(ConfiguredStructureFeature<?, ?> structure1, ConfiguredStructureFeature<?, ?> structure2) {
		ResourceLocation key1 = StructureUtils.getKeyForConfiguredStructure(mc.level, structure1);
		ResourceLocation key2 = StructureUtils.getKeyForConfiguredStructure(mc.level, structure2);
		return StructureUtils.dimensionKeysToString(ExplorersCompass.dimensionKeysForAllowedConfiguredStructureKeys.get(key1)).compareTo(StructureUtils.dimensionKeysToString(ExplorersCompass.dimensionKeysForAllowedConfiguredStructureKeys.get(key2)));
	}

	@Override
	public Object getValue(ConfiguredStructureFeature<?, ?> structure) {
		return StructureUtils.dimensionKeysToString(ExplorersCompass.dimensionKeysForAllowedConfiguredStructureKeys.get(StructureUtils.getKeyForConfiguredStructure(mc.level, structure)));
	}

	@Override
	public ISorting next() {
		return new CategorySorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.explorerscompass.dimension");
	}

}
