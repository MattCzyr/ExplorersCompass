package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DimensionSorting implements ISorting {
	
	@Override
	public int compare(ResourceLocation key1, ResourceLocation key2) {
		return StructureUtils.dimensionKeysToString(ExplorersCompass.dimensionKeysForAllowedConfiguredStructureKeys.get(key1)).compareTo(StructureUtils.dimensionKeysToString(ExplorersCompass.dimensionKeysForAllowedConfiguredStructureKeys.get(key2)));
	}

	@Override
	public Object getValue(ResourceLocation key) {
		return StructureUtils.dimensionKeysToString(ExplorersCompass.dimensionKeysForAllowedConfiguredStructureKeys.get(key));
	}

	@Override
	public ISorting next() {
		return new GroupSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.explorerscompass.dimension");
	}

}
