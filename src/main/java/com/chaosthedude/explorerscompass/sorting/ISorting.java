package com.chaosthedude.explorerscompass.sorting;

import java.util.Comparator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public interface ISorting extends Comparator<Identifier> {

	@Override
	public int compare(Identifier id1, Identifier id2);

	public Object getValue(Identifier id);

	public ISorting next();

	public String getLocalizedName();

}