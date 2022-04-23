package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NameSorting implements ISorting {

	@Override
	public int compare(ResourceLocation key1, ResourceLocation key2) {
		return StructureUtils.getPrettyStructureName(key1).compareTo(StructureUtils.getPrettyStructureName(key2));
	}

	@Override
	public Object getValue(ResourceLocation key) {
		return StructureUtils.getPrettyStructureName(key);
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
