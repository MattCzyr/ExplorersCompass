package com.chaosthedude.explorerscompass.gui;

import java.util.Objects;

import com.chaosthedude.explorerscompass.util.RenderUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class StructureSearchList extends AlwaysSelectedEntryListWidget<StructureSearchEntry> {

	private final ExplorersCompassScreen parentScreen;

	public StructureSearchList(ExplorersCompassScreen parentScreen, MinecraftClient client, int width, int height, int top, int bottom, int slotHeight) {
		super(client, width, height, top, bottom, slotHeight);
		this.parentScreen = parentScreen;
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
		return slotIndex >= 0 && slotIndex < children().size() ? children().get(slotIndex).equals(getSelectedOrNull()) : false;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderList(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderList(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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
					DrawableHelper.fill(matrixStack, insideLeft - 4, k - 4, insideLeft + getRowWidth() + 4, k + itemHeight, 255 / 2 << 24);
				}

				int j2 = getRowLeft();
				e.render(matrixStack, j, k, j2, k1, j1, mouseX, mouseY, isMouseOver((double) mouseX, (double) mouseY) && Objects .equals(getEntryAtPosition((double) mouseX, (double) mouseY), e), partialTicks);
			}
		}

		if (getMaxScroll() > 0) {
			int left = getScrollbarPositionX();
			int right = left + 6;
			int height = (int) ((float) ((bottom - top) * (bottom - top)) / (float) getMaxPosition());
			height = MathHelper.clamp(height, 32, bottom - top - 8);
			int scrollbarTop = (int) getScrollAmount() * (bottom - top - height) / getMaxScroll() + top;
			if (scrollbarTop < top) {
				scrollbarTop = top;
			}
			
			DrawableHelper.fill(matrixStack, left, scrollbarTop, right, bottom, (int) (2.35F * 255.0F) / 2 << 24);
			DrawableHelper.fill(matrixStack, left, scrollbarTop, right, scrollbarTop + height, (int) (1.9F * 255.0F) / 2 << 24);
		}
	}

	protected int getRowBottom(int index) {
		return getRowTop(index) + itemHeight;
	}

	public void refreshList() {
		clearEntries();
		for (Identifier id : parentScreen.sortStructures()) {
			addEntry(new StructureSearchEntry(this, id));
		}
		selectStructure(null);
		setScrollAmount(0);
	}

	public void selectStructure(StructureSearchEntry entry) {
		setSelected(entry);
		parentScreen.selectStructure(entry);
	}

	public boolean hasSelection() {
		return getSelectedOrNull() != null;
	}

	public ExplorersCompassScreen getParentScreen() {
		return parentScreen;
	}

}
