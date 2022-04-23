package com.chaosthedude.explorerscompass.gui;

import java.util.Objects;

import com.chaosthedude.explorerscompass.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureSearchList extends ObjectSelectionList<StructureSearchEntry> {

	private final ExplorersCompassScreen parentScreen;

	public StructureSearchList(ExplorersCompassScreen parentScreen, Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
		super(mc, width, height, top, bottom, slotHeight);
		this.parentScreen = parentScreen;
		refreshList();
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 20;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	protected boolean isSelectedItem(int slotIndex) {
		return slotIndex >= 0 && slotIndex < children().size() ? children().get(slotIndex).equals(getSelected()) : false;
	}

	@Override
	public void render(PoseStack poseStack, int par1, int par2, float par3) {
		renderList(poseStack, getRowLeft(), y0 + 4 - (int) getScrollAmount(), par1, par2, par3);
	}

	@Override
	protected void renderList(PoseStack poseStack, int par1, int par2, int par3, int par4, float par5) {
		for (int j = 0; j < getItemCount(); ++j) {
			int rowTop = getRowTop(j);
			int rowBottom = getRowBottom(j);
			if (rowBottom >= y0 && rowTop <= y1) {
				int j1 = itemHeight - 4;
				StructureSearchEntry entry = getEntry(j);
				if (/*renderSelection*/ true && isSelectedItem(j)) {
					final int insideLeft = x0 + width / 2 - getRowWidth() / 2 + 2;
					RenderUtils.drawRect(insideLeft - 4, rowTop - 4, insideLeft + getRowWidth() + 4, rowTop + itemHeight, 255 / 2 << 24);
				}
				entry.render(poseStack, j, rowTop, getRowLeft(), getRowWidth(), j1, par3, par4, isMouseOver((double) par3, (double) par4) && Objects .equals(getEntryAtPosition((double) par3, (double) par4), entry), par5);
			}
		}

	}

	private int getRowBottom(int index) {
		return getRowTop(index) + itemHeight;
	}

	public void refreshList() {
		clearEntries();
		for (ResourceLocation key : parentScreen.sortStructures()) {
			addEntry(new StructureSearchEntry(this, key));
		}
		selectStructure(null);
		setScrollAmount(0);
	}

	public void selectStructure(StructureSearchEntry entry) {
		setSelected(entry);
		parentScreen.selectStructure(entry);
	}

	public boolean hasSelection() {
		return getSelected() != null;
	}

	public ExplorersCompassScreen getParentScreen() {
		return parentScreen;
	}

}
