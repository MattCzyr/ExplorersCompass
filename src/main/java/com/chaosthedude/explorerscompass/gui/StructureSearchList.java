package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class StructureSearchList extends ObjectSelectionList<StructureSearchEntry> {

	private final ExplorersCompassScreen parentScreen;
	private Player player;
	private int itemHeight;

	public StructureSearchList(ExplorersCompassScreen parentScreen, Minecraft mc, Player player, int width, int height, int y, int itemHeight) {
		super(mc, width, height, y, itemHeight);
		this.parentScreen = parentScreen;
		this.player = player;
		this.itemHeight = itemHeight;
		refreshList();
	}

	@Override
	protected int scrollBarX() {
		return getRowLeft() + getRowWidth();
	}

	@Override
	public int getRowWidth() {
		return 270;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		enableScissor(guiGraphics);
		for (int i = 0; i < getItemCount(); ++i) {
			if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
				StructureSearchEntry entry = children().get(i);
				int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelected());
				guiGraphics.fill(getRowLeft(), getRowTop(i), getRowLeft() + getRowWidth(), getRowTop(i) + itemHeight, fillColor);
				entry.renderContent(guiGraphics, mouseX, mouseY, entry == getHovered(), partialTicks);
			}
		}
		guiGraphics.disableScissor();

		if (maxScrollAmount() > 0) {
			int left = scrollBarX();
			int right = left + 6;
			int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) contentHeight());
			height = Mth.clamp(height, 32, getBottom() - getY() - 8);
			int top = (int) scrollAmount() * (getBottom() - getY() - height) / maxScrollAmount() + getY();
			if (top < getY()) {
				top = getY();
			}

			guiGraphics.fill(left, getY(), right, getBottom(), (int) (2.35F * 255.0F) / 2 << 24);
			guiGraphics.fill(left, top, right, top + height, (int) (1.9F * 255.0F) / 2 << 24);
		}
	}

	@Override
	protected void enableScissor(GuiGraphics guiGraphics) {
		guiGraphics.enableScissor(getX(), getY(), getRight(), getBottom());
	}

	@Override
	public int getRowBottom(int index) {
		return getRowTop(index) + itemHeight;
	}

	public void refreshList() {
		clearEntries();
		for (Identifier structureId : parentScreen.sortStructures()) {
			addEntry(new StructureSearchEntry(this, structureId, player));
		}
		selectStructure(null);
		setScrollAmount(0);
	}

	public boolean selectStructure(StructureSearchEntry entry) {
		if (entry == null || entry.isEnabled()) {
			setSelected(entry);
			parentScreen.selectStructure(entry);
			return true;
		}
		return false;
	}

	public boolean hasSelection() {
		return getSelected() != null;
	}

	public ExplorersCompassScreen getParentScreen() {
		return parentScreen;
	}

}
