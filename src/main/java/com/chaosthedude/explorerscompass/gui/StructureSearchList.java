package com.chaosthedude.explorerscompass.gui;

import java.util.Objects;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class StructureSearchList extends AlwaysSelectedEntryListWidget<StructureSearchEntry> {

	private final ExplorersCompassScreen parentScreen;

	public StructureSearchList(ExplorersCompassScreen parentScreen, MinecraftClient client, int width, int height, int top, int bottom) {
		super(client, width, height, top, bottom);
		this.parentScreen = parentScreen;
		refreshList();
	}

	@Override
	protected int getDefaultScrollbarX() {
		return getRowLeft() + getRowWidth() - 2;
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
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		renderList(context, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderList(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		context.fill(getRowLeft() - 4, getY(), getRowLeft() + getRowWidth() + 4, getY() + getHeight() + 4, 255 / 2 << 24);
		
		enableScissor(context);
		
		int i = getEntryCount();
		for (int j = 0; j < i; ++j) {
			int k = getRowTop(j);
			int l = getRowBottom(j);
			if (l >= getY() && k <= getBottom()) {
				int j1 = this.itemHeight - 4;
				StructureSearchEntry e = getEntry(j);
				int k1 = getRowWidth();
				if (/*renderSelection*/ true && isSelectedEntry(j)) {
					final int insideLeft = getX() + width / 2 - getRowWidth() / 2 + 2;
					context.fill(insideLeft - 4, k - 4, insideLeft + getRowWidth() + 4, k + itemHeight, 255 / 2 << 24);
				}

				int j2 = getRowLeft();
				e.render(context, j, k, j2, k1, j1, mouseX, mouseY, isMouseOver((double) mouseX, (double) mouseY) && Objects .equals(getEntryAtPosition((double) mouseX, (double) mouseY), e), partialTicks);
			}
		}
		context.disableScissor();

		if (getMaxScroll() > 0) {
			int left = getDefaultScrollbarX();
			int right = left + 6;
			int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) getMaxPosition());
			height = MathHelper.clamp(height, 32, getBottom() - getY() - 8);
			int scrollbarTop = (int) getScrollAmount() * (getBottom() - getY() - height) / getMaxScroll() + getY();
			if (scrollbarTop < getY()) {
				scrollbarTop = getY();
			}
			
			context.fill(left, scrollbarTop, right, getBottom(), (int) (2.35F * 255.0F) / 2 << 24);
			context.fill(left, scrollbarTop, right, scrollbarTop + height, (int) (1.9F * 255.0F) / 2 << 24);
		}
	}
	
	@Override
	protected void enableScissor(DrawContext context) {
		context.enableScissor(getX(), getY(), getRight(), getBottom());
	}

	@Override
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
