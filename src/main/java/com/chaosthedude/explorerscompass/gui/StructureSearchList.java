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
	private PlayerEntity player;

	public StructureSearchList(ExplorersCompassScreen parentScreen, MinecraftClient client, PlayerEntity player, Identifier structureIdToSelect, int x, int y, int width, int height, int itemHeight) {
		super(client, width, height, y, itemHeight);
		this.parentScreen = parentScreen;
		this.player = player;
        setX(x);
		refreshList(structureIdToSelect);
	}

	@Override
	public int getRowWidth() {
		return getWidth();
	}

    @Override
    protected int getDefaultScrollbarX() {
        return getX() + getWidth();
    }

	@Override
	protected boolean isSelectedEntry(int slotIndex) {
		return slotIndex >= 0 && slotIndex < children().size() ? children().get(slotIndex).equals(getSelectedOrNull()) : false;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        enableScissor(context);
        // Render backgrounds
        for (int i = 0; i < getEntryCount(); ++i) {
            if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
                StructureSearchEntry entry = getEntry(i);
                int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelectedOrNull());
                context.fill(getRowLeft(), getRowTop(i), getRight(), getRowBottom(i), fillColor);
            }
        }
        // Render entries
        for (int i = 0; i < getEntryCount(); ++i) {
            int top = getRowTop(i);
            int bottom = getRowBottom(i);
            if (bottom >= getY() && top <= getBottom()) {
                StructureSearchEntry entry = getEntry(i);
                boolean isHovering = isMouseOver(mouseX, mouseY) && Objects.equals(getEntryAtPosition(mouseX, mouseY), entry);
                entry.render(context, i, top, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, isHovering, partialTicks);
            }
        }
        context.disableScissor();
        // Render scrollbar
        if (getMaxScroll() > 0) {
            int left = getScrollbarX();
            int right = left + 6;
            int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) getMaxPosition());
            height = MathHelper.clamp(height, 32, getBottom() - getY() - 8);
            int top = (int) getScrollAmount() * (getBottom() - getY() - height) / getMaxScroll() + getY();
            if (top < getY()) {
                top = getY();
            }
            context.fill(left, getY(), right, getBottom(), RenderUtils.getBackgroundColor(false, false));
            context.fill(left, top, right, top + height, RenderUtils.getBackgroundColor(true, true));
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
		Identifier select = maintainSelection && hasSelection() ? getSelectedOrNull().getStructureId() : null;
		refreshList(select);
	}

	public boolean hasSelection() {
		return getSelectedOrNull() != null;
	}

	public ExplorersCompassScreen getParentScreen() {
		return parentScreen;
	}

}
