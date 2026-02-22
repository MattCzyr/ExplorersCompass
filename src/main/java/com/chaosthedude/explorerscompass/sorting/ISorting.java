package com.chaosthedude.explorerscompass.sorting;

import java.util.Comparator;

import net.minecraft.resources.Identifier;

public interface ISorting extends Comparator<Identifier> {

	@Override
	public int compare(Identifier key1, Identifier key2);

	public Object getValue(Identifier key);

	public ISorting next();

	public String getLocalizedName();

}