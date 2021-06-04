package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.gui.ExplorersCompassScreen;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CategorySorting extends AbstractSorting {
	
	public CategorySorting(ExplorersCompassScreen parentScreen) {
		super(parentScreen);
	}
	
	@Override
	public int compare(Structure<?> structure1, Structure<?> structure2) {
		return structure1.getDecorationStage().toString().compareTo(structure2.getDecorationStage().toString());
	}

	@Override
	public Object getValue(Structure<?> structure) {
		return structure.getDecorationStage();
	}

	@Override
	public ISorting next() {
		return new NameSorting(parentScreen);
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.explorerscompass.category");
	}

}
