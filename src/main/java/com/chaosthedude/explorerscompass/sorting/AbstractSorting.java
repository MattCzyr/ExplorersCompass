package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.gui.ExplorersCompassScreen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSorting implements ISorting {
	
	protected ExplorersCompassScreen parentScreen;
	
	public AbstractSorting(ExplorersCompassScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

}
