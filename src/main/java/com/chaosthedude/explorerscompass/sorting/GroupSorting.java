package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.ExplorersCompass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GroupSorting implements ISorting {
	
	private static final Minecraft mc = Minecraft.getInstance();
	
	@Override
	public int compare(ResourceLocation key1, ResourceLocation key2) {
		return ExplorersCompass.configuredStructureKeysToStructureKeys.get(key1).compareTo(ExplorersCompass.configuredStructureKeysToStructureKeys.get(key2));
	}

	@Override
	public Object getValue(ResourceLocation key) {
		return ExplorersCompass.configuredStructureKeysToStructureKeys.get(key);
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
