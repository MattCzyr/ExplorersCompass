package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.gui.ExplorersCompassScreen;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SourceSorting extends AbstractSorting {
	
	public SourceSorting(ExplorersCompassScreen parentScreen) {
		super(parentScreen);
	}
	
	@Override
	public int compare(Structure<?> structure1, Structure<?> structure2) {
		return StructureUtils.getStructureSource(parentScreen.world, structure1).compareTo(StructureUtils.getStructureSource(parentScreen.world, structure2));
	}

	@Override
	public Object getValue(Structure<?> structure) {
		return StructureUtils.getStructureSource(parentScreen.world, structure);
	}

	@Override
	public ISorting next() {
		return new CategorySorting(parentScreen);
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.explorerscompass.source");
	}

}
