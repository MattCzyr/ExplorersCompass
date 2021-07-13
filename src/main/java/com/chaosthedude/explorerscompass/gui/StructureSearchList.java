package com.chaosthedude.explorerscompass.gui;

import java.util.Objects;

import com.chaosthedude.explorerscompass.util.RenderUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.gen.feature.StructureFeature;

@Environment(EnvType.CLIENT)
public class StructureSearchList extends EntryListWidget<StructureSearchEntry> {

	private final ExplorersCompassScreen guiExplorersCompass;

	public StructureSearchList(ExplorersCompassScreen guiExplorersCompass, MinecraftClient mc, int width, int height, int top, int bottom, int slotHeight) {
		super(mc, width, height, top, bottom, slotHeight);
		this.guiExplorersCompass = guiExplorersCompass;
		refreshList();
	}

	@Override
	protected int getScrollbarPositionX() {
		return super.getScrollbarPositionX() + 20;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	protected boolean isSelectedEntry(int slotIndex) {
		return slotIndex >= 0 && slotIndex < children().size() ? children().get(slotIndex).equals(getSelected()) : false;
	}

	@Override
	public void render(MatrixStack matrixStack, int par1, int par2, float par3) {
		int i = getScrollbarPositionX();
		int k = getRowLeft();
		int l = top + 4 - (int) getScrollAmount();

		renderList(matrixStack, k, l, par1, par2, par3);
	}

	@Override
	protected void renderList(MatrixStack matrixStack, int par1, int par2, int par3, int par4, float par5) {
		int i = getEntryCount();
		for (int j = 0; j < i; ++j) {
			int k = getRowTop(j);
			int l = getRowBottom(j);
			if (l >= top && k <= bottom) {
				int j1 = this.itemHeight - 4;
				StructureSearchEntry e = getEntry(j);
				int k1 = getRowWidth();
				if (/*renderSelection*/ true && isSelectedEntry(j)) {
					final int insideLeft = left + width / 2 - getRowWidth() / 2 + 2;
					RenderUtils.drawRect(insideLeft - 4, k - 4, insideLeft + getRowWidth() + 4, k + itemHeight, 255 / 2 << 24);
				}

				int j2 = getRowLeft();
				e.render(matrixStack, j, k, j2, k1, j1, par3, par4, isMouseOver((double) par3, (double) par4) && Objects .equals(getEntryAtPosition((double) par3, (double) par4), e), par5);
			}
		}

	}

	private int getRowBottom(int p_getRowBottom_1_) {
		return this.getRowTop(p_getRowBottom_1_) + this.itemHeight;
	}

	public void refreshList() {
		clearEntries();
		for (StructureFeature<?> structure : guiExplorersCompass.sortStructures()) {
			addEntry(new StructureSearchEntry(this, structure));
		}
		selectStructure(null);
		setScrollAmount(0);
	}

	public void selectStructure(StructureSearchEntry entry) {
		setSelected(entry);
		guiExplorersCompass.selectStructure(entry);
	}

	public boolean hasSelection() {
		return getSelected() != null;
	}

	public ExplorersCompassScreen getExplorersCompassGui() {
		return guiExplorersCompass;
	}

}
