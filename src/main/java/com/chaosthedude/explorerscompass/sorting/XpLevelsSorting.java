package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class XpLevelsSorting implements ISorting {

	@Override
	public int compare(ResourceLocation key1, ResourceLocation key2) {
		return getValue(key1).toString().compareTo(getValue(key2).toString());
	}

	@Override
	public Object getValue(ResourceLocation key) {
		if (ExplorersCompass.xpLevelsForAllowedStructureKeys != null && ExplorersCompass.xpLevelsForAllowedStructureKeys.containsKey(key)) {
			return String.valueOf(ExplorersCompass.xpLevelsForAllowedStructureKeys.get(key));
		}
		return "";
	}

	@Override
	public ISorting next() {
		return new GroupSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.explorerscompass.levels");
	}

}
