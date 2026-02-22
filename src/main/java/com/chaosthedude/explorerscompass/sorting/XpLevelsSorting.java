package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

public class XpLevelsSorting implements ISorting {

	@Override
	public int compare(Identifier key1, Identifier key2) {
		return getValue(key1).toString().compareTo(getValue(key2).toString());
	}

	@Override
	public Object getValue(Identifier key) {
		if (ExplorersCompass.xpLevelsForAllowedStructures.containsKey(key)) {
			return String.valueOf(ExplorersCompass.xpLevelsForAllowedStructures.get(key));
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
