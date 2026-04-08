package com.chaosthedude.explorerscompass.gui;

import java.util.Objects;

import com.chaosthedude.explorerscompass.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class StructureSearchList extends ObjectSelectionList<StructureSearchEntry> {

	private final ExplorersCompassScreen parentScreen;
	private Player player;

	public StructureSearchList(ExplorersCompassScreen parentScreen, Minecraft mc, Player player, ResourceLocation structureKeyToSelect, int x, int y, int width, int height, int itemHeight) {
		super(mc, width, height, y, itemHeight);
		this.parentScreen = parentScreen;
		this.player = player;
        setX(x);
		refreshList(structureKeyToSelect);
	}

	@Override
	protected int getScrollbarPosition() {
		return getX() + getWidth();
	}

	@Override
	public int getRowWidth() {
		return getWidth();
	}

	@Override
	protected boolean isSelectedItem(int slotIndex) {
		return slotIndex >= 0 && slotIndex < children().size() ? children().get(slotIndex).equals(getSelected()) : false;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        enableScissor(guiGraphics);
        // Render backgrounds
        for (int i = 0; i < getItemCount(); ++i) {
            if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
                StructureSearchEntry entry = getEntry(i);
                int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelected());
                guiGraphics.fill(getRowLeft(), getRowTop(i), getRight(), getRowBottom(i), fillColor);
            }
        }
        // Render entries
        for (int i = 0; i < getItemCount(); ++i) {
            int top = getRowTop(i);
            int bottom = getRowBottom(i);
            if (bottom >= getY() && top <= getBottom()) {
                StructureSearchEntry entry = getEntry(i);
                boolean isHovering = isMouseOver(mouseX, mouseY) && Objects.equals(getEntryAtPosition(mouseX, mouseY), entry);
                entry.render(guiGraphics, i, top, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, isHovering, partialTicks);
            }
        }
        guiGraphics.disableScissor();
        // Render scrollbar
        if (getMaxScroll() > 0) {
            int left = getScrollbarPosition();
            int right = left + 6;
            int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) getMaxPosition());
            height = Mth.clamp(height, 32, getBottom() - getY() - 8);
            int top = (int) getScrollAmount() * (getBottom() - getY() - height) / getMaxScroll() + getY();
            if (top < getY()) {
                top = getY();
            }
            guiGraphics.fill(left, getY(), right, getBottom(), RenderUtils.getBackgroundColor(false, false));
            guiGraphics.fill(left, top, right, top + height, RenderUtils.getBackgroundColor(true, true));
        }
	}

    @Override
    public void setSelected(StructureSearchEntry entry) {
        if (entry == null || entry.isEnabled()) {
            super.setSelected(entry);
        }
    }

    public void refreshList() {
		refreshList(null);
	}

	public void refreshList(ResourceLocation structureKeyToSelect) {
		clearEntries();
		setSelected(null);
		for (ResourceLocation key : parentScreen.sortStructures()) {
			StructureSearchEntry entry = new StructureSearchEntry(this, key, player);
			addEntry(entry);
			if (key.equals(structureKeyToSelect)) {
				setSelected(entry);
			}
		}
		setScrollAmount(0);
	}

	public void refreshList(boolean maintainSelection) {
		ResourceLocation select = maintainSelection && hasSelection() ? getSelected().getStructureKey() : null;
		refreshList(select);
	}

	public boolean hasSelection() {
		return getSelected() != null;
	}

	public ExplorersCompassScreen getParentScreen() {
		return parentScreen;
	}

}
