package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class StructureSearchList extends ObjectSelectionList<StructureSearchEntry> {

	private final ExplorersCompassScreen parentScreen;
	private Player player;

	public StructureSearchList(ExplorersCompassScreen parentScreen, Minecraft mc, Player player, Identifier structureIdToSelect, int x, int y, int width, int height, int itemHeight) {
		super(mc, width, height, y, itemHeight);
		this.parentScreen = parentScreen;
		this.player = player;
        setX(x);
		refreshList(structureIdToSelect);
	}

	@Override
	protected int scrollBarX() {
		return getRowLeft() + getRowWidth();
	}

	@Override
	public int getRowWidth() {
		return getWidth();
	}

	@Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        enableScissor(guiGraphics);
        extractListBackground(guiGraphics);
        extractListItems(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.disableScissor();
        extractScrollbar(guiGraphics, mouseX, mouseY);
    }
	
	@Override
	protected void extractListBackground(GuiGraphicsExtractor guiGraphics) {
		for (int i = 0; i < getItemCount(); ++i) {
			if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
				StructureSearchEntry entry = children().get(i);
				int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelected());
				guiGraphics.fill(getRowLeft(), getRowTop(i), getRowLeft() + getRowWidth(), getRowTop(i) + defaultEntryHeight, fillColor);
			}
		}
	}
	
	@Override
	protected void extractSelection(GuiGraphicsExtractor guiGraphics, StructureSearchEntry entry, int backgroundColor) {
		// Selection is rendered in renderListBackground()
	}
	
	@Override
	protected void extractScrollbar(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (scrollable()) {
			int left = scrollBarX();
			int right = left + 6;
			int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) contentHeight());
			height = Mth.clamp(height, 32, getBottom() - getY() - 8);
			int top = (int) scrollAmount() * (getBottom() - getY() - height) / maxScrollAmount() + getY();
			if (top < getY()) {
				top = getY();
			}
			
			int backgroundFillColor = RenderUtils.getBackgroundColor(false, false);
			int scrollbarFillColor = RenderUtils.getBackgroundColor(true, true);
			guiGraphics.fill(left, getY(), right, getBottom(), backgroundFillColor);
			guiGraphics.fill(left, top, right, top + height, scrollbarFillColor);
		}
	}
	
	@Override
	public void setSelected(StructureSearchEntry entry) {
		if (entry == null || entry.isEnabled()) {
			super.setSelected(entry);
		}
	}

	public void refreshList(Identifier structureIdToSelect) {
		clearEntries();
		for (Identifier structureId : parentScreen.sortStructures()) {
			StructureSearchEntry entry = new StructureSearchEntry(this, structureId, player);
			addEntry(entry);
			if (structureId.equals(structureIdToSelect)) {
				setSelected(entry);
			}
		}
		setScrollAmount(0);
	}
	
	public void refreshList(boolean maintainSelection) {
		Identifier select = maintainSelection && hasSelection() ? getSelected().getStructureId() : null;
		refreshList(select);
	}

	public boolean hasSelection() {
		return getSelected() != null;
	}

	public ExplorersCompassScreen getParentScreen() {
		return parentScreen;
	}

}
