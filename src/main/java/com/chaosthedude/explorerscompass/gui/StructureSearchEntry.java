package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class StructureSearchEntry extends AlwaysSelectedEntryListWidget.Entry<StructureSearchEntry> {

    private static final Identifier[] ENABLED_LEVEL_SPRITES = new Identifier[] {
            Identifier.of("container/enchanting_table/level_1"),
            Identifier.of("container/enchanting_table/level_2"),
            Identifier.of("container/enchanting_table/level_3")
    };

    private static final Identifier[] DISABLED_LEVEL_SPRITES = new Identifier[] {
            Identifier.of("container/enchanting_table/level_1_disabled"),
            Identifier.of("container/enchanting_table/level_2_disabled"),
            Identifier.of("container/enchanting_table/level_3_disabled")
    };

	private final MinecraftClient client;
	private final ExplorersCompassScreen parentScreen;
	private final PlayerEntity player;
	private final Identifier structureID;
	private final StructureSearchList structuresList;
	private int xpLevels;
	private long lastClickTime;

	public StructureSearchEntry(StructureSearchList structuresList, Identifier structureID, PlayerEntity player) {
		this.structuresList = structuresList;
		this.structureID = structureID;
		this.player = player;
		parentScreen = structuresList.getParentScreen();
		client = MinecraftClient.getInstance();

		this.xpLevels = 0;
		if (ExplorersCompass.xpLevelsForAllowedStructures.containsKey(structureID)) {
			int levels = ExplorersCompass.xpLevelsForAllowedStructures.get(structureID);
			if (levels > 3) {
				levels = 3;
			}
			this.xpLevels = levels;
		}
	}

	@Override
	public void render(DrawContext context, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovering, float partialTick) {
        if (xpLevels > 0) {
            int itemHeight = height + 4;
            int spriteSize = (int) (itemHeight * 0.4F);
            int spriteBorder = (itemHeight - spriteSize) / 2;
            int spriteIndex = xpLevels - 1;
            Identifier spriteId = isEnabled() ? ENABLED_LEVEL_SPRITES[spriteIndex] : DISABLED_LEVEL_SPRITES[spriteIndex];
            context.drawGuiTexture(spriteId, left + width - spriteSize - spriteBorder, top + spriteBorder - 2, spriteSize, spriteSize);
        }

        int nameColor = isEnabled() ? 0xffffff : 0x808080;
		int infoColor = isEnabled() ? 0x808080 : 0x555555;
		context.drawText(client.textRenderer, Text.literal(StructureUtils.getStructureName(structureID)), left + 5, top + (height / 2) - ((client.textRenderer.fontHeight + 2) * 2), nameColor, true);
		context.drawText(client.textRenderer, Text.translatable("string.explorerscompass.source").append(": " + StructureUtils.getStructureSource(structureID)), left + 5, top + (height / 2) - ((client.textRenderer.fontHeight + 2) * 1), infoColor, true);
		context.drawText(client.textRenderer, Text.translatable("string.explorerscompass.group").append(": " + StructureUtils.getStructureName(ExplorersCompass.structureIDsToGroupIDs.get(structureID))), left + 5, top + (height / 2) - ((client.textRenderer.fontHeight + 2) * 0), infoColor, true);
		context.drawText(client.textRenderer, Text.translatable("string.explorerscompass.dimension").append(": " + StructureUtils.structureDimensionsToString(ExplorersCompass.allowedStructureIDsToDimensionIDs.get(structureID))), left + 5, top + (height / 2) + ((client.textRenderer.fontHeight + 2) * 1), infoColor, true);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			structuresList.setSelected(this);
			if (Util.getMeasuringTimeMs() - lastClickTime < 250L && isEnabled()) {
				parentScreen.searchForStructure(structureID);
				return true;
			} else {
				lastClickTime = Util.getMeasuringTimeMs();
				return false;
			}
		}
		return false;
	}

	public boolean isEnabled() {
		return ExplorersCompass.infiniteXp || player.experienceLevel >= xpLevels;
	}

	public Identifier getStructureId() {
		return structureID;
	}

	public Identifier getGroupId() {
		return ExplorersCompass.structureIDsToGroupIDs.get(structureID);
	}

	@Override
	public Text getNarration() {
		return Text.literal(StructureUtils.getStructureName(structureID));
	}

}
