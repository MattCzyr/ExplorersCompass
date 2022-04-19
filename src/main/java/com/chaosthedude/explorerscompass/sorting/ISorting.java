package com.chaosthedude.explorerscompass.sorting;

import java.util.Comparator;

import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ISorting extends Comparator<ConfiguredStructureFeature<?, ?>> {

	@Override
	public int compare(ConfiguredStructureFeature<?, ?> structure1, ConfiguredStructureFeature<?, ?> structure2);

	public Object getValue(ConfiguredStructureFeature<?, ?> structure);

	public ISorting next();

	public String getLocalizedName();

}