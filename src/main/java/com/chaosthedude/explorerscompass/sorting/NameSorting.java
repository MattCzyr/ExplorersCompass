package com.chaosthedude.explorerscompass.sorting;

import com.chaosthedude.explorerscompass.gui.ExplorersCompassScreen;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NameSorting extends AbstractSorting {
	
	public NameSorting(ExplorersCompassScreen parentScreen) {
		super(parentScreen);
	}
	
	@Override
	public int compare(Structure<?> structure1, Structure<?> structure2) {
		return StructureUtils.getStructureName(parentScreen.world, structure1).compareTo(StructureUtils.getStructureName(parentScreen.world, structure2));
	}

	@Override
	public Object getValue(Structure<?> structure) {
		return StructureUtils.getStructureName(parentScreen.world, structure);
	}

	@Override
	public ISorting next() {
		return new SourceSorting(parentScreen);
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.explorerscompass.name");
	}

}
