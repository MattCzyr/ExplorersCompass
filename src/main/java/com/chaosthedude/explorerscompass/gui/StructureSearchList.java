package com.chaosthedude.explorerscompass.gui;

import java.util.Objects;

import com.chaosthedude.explorerscompass.util.RenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class StructureSearchList extends AlwaysSelectedEntryListWidget<StructureSearchEntry> {

	private final ExplorersCompassScreen parentScreen;
	private final PlayerEntity player;

	public StructureSearchList(ExplorersCompassScreen parentScreen, MinecraftClient client, PlayerEntity player, Identifier structureIdToSelect, int x, int y, int width, int height, int slotHeight) {
		super(client, width, height, y, y + height, slotHeight);
		this.parentScreen = parentScreen;
		this.player = player;
        setLeftPos(x);
		refreshList(structureIdToSelect);
	}

	@Override
	protected int getScrollbarPositionX() {
		return left + width;
	}

	@Override
	public int getRowWidth() {
		return width;
	}

	@Override
	protected boolean isSelectedEntry(int slotIndex) {
		return slotIndex >= 0 && slotIndex < children().size() ? children().get(slotIndex).equals(getSelectedOrNull()) : false;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        enableScissor(context);
        // Render backgrounds
        for (int i = 0; i < getEntryCount(); ++i) {
            if (getRowBottom(i) >= top && getRowTop(i) <= bottom) {
                StructureSearchEntry entry = getEntry(i);
                int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelectedOrNull());
                context.fill(getRowLeft(), getRowTop(i), right, getRowBottom(i), fillColor);
            }
        }
        // Render entries
        for (int i = 0; i < getEntryCount(); ++i) {
            int entryTop = getRowTop(i);
            int entryBottom = getRowBottom(i);
            if (entryBottom >= top && entryTop <= bottom) {
                StructureSearchEntry entry = getEntry(i);
                boolean isHovering = isMouseOver(mouseX, mouseY) && Objects.equals(getEntryAtPosition(mouseX, mouseY), entry);
                entry.render(context, i, entryTop, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, isHovering, partialTicks);
            }
        }
        context.disableScissor();
        // Render scrollbar
        if (getMaxScroll() > 0) {
            int scrollbarLeft = getScrollbarPositionX();
            int scrollbarRight = scrollbarLeft + 6;
            int scrollbarHeight = (int) ((float) ((bottom - top) * (bottom - top)) / (float) getMaxPosition());
            scrollbarHeight = MathHelper.clamp(scrollbarHeight, 32, bottom - top - 8);
            int scrollbarTop = (int) getScrollAmount() * (bottom - top - scrollbarHeight) / getMaxScroll() + top;
            if (scrollbarTop < top) {
                scrollbarTop = top;
            }
            context.fill(scrollbarLeft, top, scrollbarRight, bottom, RenderUtils.getBackgroundColor(false, false));
            context.fill(scrollbarLeft, scrollbarTop, scrollbarRight, scrollbarTop + scrollbarHeight, RenderUtils.getBackgroundColor(true, true));
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
		for (Identifier id : parentScreen.sortStructures()) {
			StructureSearchEntry entry = new StructureSearchEntry(this, id, player);
			addEntry(entry);
			if (id.equals(structureIdToSelect)) {
				setSelected(entry);
			}
		}
		setScrollAmount(0);
	}

	public void refreshList(boolean maintainSelection) {
		Identifier select = maintainSelection && hasSelection() ? getSelectedOrNull().getStructureID() : null;
		refreshList(select);
	}

	public void refreshList() {
		refreshList(null);
	}

	public boolean hasSelection() {
		return getSelectedOrNull() != null;
	}

	public ExplorersCompassScreen getParentScreen() {
		return parentScreen;
	}

}
