package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GroupSorting implements ISorting {
	
	@Override
	public int compare(Identifier id1, Identifier id2) {
		return ExplorersCompass.configuredStructureIDsToStructureIDs.get(id1).compareTo(ExplorersCompass.configuredStructureIDsToStructureIDs.get(id2));
	}

	@Override
	public Object getValue(Identifier id) {
		return ExplorersCompass.configuredStructureIDsToStructureIDs.get(id);
	}

	@Override
	public ISorting next() {
		return new NameSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.explorerscompass.group");
	}

}