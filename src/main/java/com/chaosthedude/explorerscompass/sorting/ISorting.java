package com.chaosthedude.explorerscompass.sorting;

import java.util.Comparator;

import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ISorting extends Comparator<StructureFeature<?>> {

	@Override
	public int compare(StructureFeature<?> structure1, StructureFeature<?> structure2);

	public Object getValue(StructureFeature<?> structure);

	public ISorting next();

	public String getLocalizedName();

}