package com.chaosthedude.explorerscompass.sorting;

import java.util.Comparator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.gen.feature.StructureFeature;

@Environment(EnvType.CLIENT)
public interface ISorting extends Comparator<StructureFeature<?>> {

	@Override
	public int compare(StructureFeature<?> structure1, StructureFeature<?> structure2);

	public Object getValue(StructureFeature<?> structure);

	public ISorting next();

	public String getLocalizedName();

}