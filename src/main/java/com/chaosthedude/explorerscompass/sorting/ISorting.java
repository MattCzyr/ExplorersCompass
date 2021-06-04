package com.chaosthedude.explorerscompass.sorting;

import java.util.Comparator;

import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ISorting extends Comparator<Structure<?>> {

	@Override
	public int compare(Structure<?> structure1, Structure<?> structure2);

	public Object getValue(Structure<?> structure);

	public ISorting next();

	public String getLocalizedName();

}