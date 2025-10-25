package com.chaosthedude.explorerscompass.gui;

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
	protected int getScrollbarX() {
		return getRowLeft() + getRowWidth() - 2;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
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
			if (getRowBottom(j) >= getY() && getRowTop(j) <= getBottom()) {
				StructureSearchEntry e = children().get(j);
				if (e == getSelectedOrNull()) {
					context.fill(getRowLeft() - 4, getRowTop(j) - 4, getRowLeft() + getRowWidth() + 4, getRowTop(j) + itemHeight, 255 / 2 << 24);
				}

				e.render(context, mouseX, mouseY, e == getHoveredEntry(), partialTicks);
			}
		}
		context.disableScissor();

		if (getMaxScrollY() > 0) {
			int left = getScrollbarX();
			int right = left + 6;
			int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) getContentsHeightWithPadding());
			height = MathHelper.clamp(height, 32, getBottom() - getY() - 8);
			int scrollbarTop = (int) getScrollY() * (getBottom() - getY() - height) / getMaxScrollY() + getY();
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
	public int getRowBottom(int index) {
		return getRowTop(index) + itemHeight;
	}

	public void refreshList() {
		clearEntries();
		for (Identifier id : parentScreen.sortStructures()) {
			addEntry(new StructureSearchEntry(this, id));
		}
		selectStructure(null);
		setScrollY(0);
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
