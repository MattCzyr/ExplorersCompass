package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class StructureSearchEntry extends ObjectSelectionList.Entry<StructureSearchEntry> {

	private static final Identifier[] ENABLED_LEVEL_SPRITES = new Identifier[] {
        Identifier.withDefaultNamespace("container/enchanting_table/level_1"),
        Identifier.withDefaultNamespace("container/enchanting_table/level_2"),
        Identifier.withDefaultNamespace("container/enchanting_table/level_3")
    };

    private static final Identifier[] DISABLED_LEVEL_SPRITES = new Identifier[] {
        Identifier.withDefaultNamespace("container/enchanting_table/level_1_disabled"),
        Identifier.withDefaultNamespace("container/enchanting_table/level_2_disabled"),
        Identifier.withDefaultNamespace("container/enchanting_table/level_3_disabled")
    };

	private final Minecraft mc;
	private final ExplorersCompassScreen parentScreen;
	private final Player player;
	private final Identifier structureId;
	private final StructureSearchList structuresList;
	private int xpLevels;

	public StructureSearchEntry(StructureSearchList structuresList, Identifier structureId, Player player) {
		this.structuresList = structuresList;
		this.structureId = structureId;
		this.player = player;
		parentScreen = structuresList.getParentScreen();
		mc = Minecraft.getInstance();

		// Get XP levels to consume
		this.xpLevels = 0;
		if (ExplorersCompass.xpLevelsForAllowedStructures.containsKey(structureId)) {
			int levels = ExplorersCompass.xpLevelsForAllowedStructures.get(structureId);
			if (levels > 3) {
				levels = 3;
			}
			this.xpLevels = levels;
		}
	}

	@Override
	public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
		int maxTextWidth = getWidth() - 10;

		if (xpLevels > 0) {
			int spriteSize = (int) (getHeight() * 0.4F);
			int spriteBorder = (getHeight() - spriteSize) / 2;
			int spriteIndex = xpLevels - 1;
			Identifier spriteId = isEnabled() ? ENABLED_LEVEL_SPRITES[spriteIndex] : DISABLED_LEVEL_SPRITES[spriteIndex];
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, getX() + getWidth() - spriteSize - spriteBorder, getY() + spriteBorder, spriteSize, spriteSize);

			// XP sprite is rendered, need extra room for it
			maxTextWidth = getWidth() - getHeight() - 5;
		}

		int nameColor = isEnabled() ? 0xffffffff : 0xff808080;
		int infoColor = isEnabled() ? 0xff808080 : 0xff555555;
		guiGraphics.drawString(mc.font, Component.literal(StructureUtils.getStructureName(structureId)), getX() + 5, getY() + (getHeight() / 2) - ((mc.font.lineHeight + 2) * 2), nameColor);
		guiGraphics.drawString(mc.font, Component.translatable(("string.explorerscompass.source")).append(Component.literal(": " + StructureUtils.getStructureSource(structureId))), getX() + 5, getY() + (getHeight() / 2) - ((mc.font.lineHeight + 2) * 1), infoColor);
		guiGraphics.drawString(mc.font, Component.translatable(("string.explorerscompass.group")).append(Component.literal(": ")).append(Component.translatable(StructureUtils.getStructureName(ExplorersCompass.structureIdsToGroupIds.get(structureId)))), getX() + 5, getY() + (getHeight() / 2) + ((mc.font.lineHeight + 2) * 0), infoColor);
		guiGraphics.drawString(mc.font, Component.translatable(("string.explorerscompass.dimension")).append(Component.literal(": " + StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(structureId)))), getX() + 5, getY() + (getHeight() / 2) + ((mc.font.lineHeight + 2) * 1), infoColor);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		boolean selected = structuresList.selectStructure(this);
		if (doubleClick && selected) {
			searchForStructure();
		}
		return true;
	}

	@Override
	public Component getNarration() {
		return Component.literal(StructureUtils.getStructureName(structureId));
	}

	public void searchForStructure() {
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForStructure(structureId);
	}

	public void searchForGroup() {
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForStructureGroup(ExplorersCompass.structureIdsToGroupIds.get(structureId));
	}

	public boolean isEnabled() {
		return ExplorersCompass.infiniteXp || player.experienceLevel >= xpLevels;
	}

}
