package com.chaosthedude.explorerscompass.sorting;

import java.util.Comparator;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ISorting extends Comparator<ResourceLocation> {

	@Override
	public int compare(ResourceLocation key1, ResourceLocation key2);

	public Object getValue(ResourceLocation key);

	public ISorting next();

	public String getLocalizedName();

}