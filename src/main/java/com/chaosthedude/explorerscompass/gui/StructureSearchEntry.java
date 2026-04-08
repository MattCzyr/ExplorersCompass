package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureSearchEntry extends ObjectSelectionList.Entry<StructureSearchEntry> {

	private static final ResourceLocation[] ENABLED_LEVEL_SPRITES = new ResourceLocation[] {
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1"),
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2"),
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3")
	};

	private static final ResourceLocation[] DISABLED_LEVEL_SPRITES = new ResourceLocation[] {
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1_disabled"),
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2_disabled"),
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3_disabled")
	};

	private final Minecraft mc;
	private final ExplorersCompassScreen parentScreen;
	private final ResourceLocation structureKey;
	private final StructureSearchList structuresList;
	private final Player player;
	private int xpLevels;
	private long lastClickTime;

	public StructureSearchEntry(StructureSearchList structuresList, ResourceLocation structureKey, Player player) {
		this.structuresList = structuresList;
		this.structureKey = structureKey;
		this.player = player;
		parentScreen = structuresList.getParentScreen();
		mc = Minecraft.getInstance();

		// Get XP levels to consume
		this.xpLevels = 0;
		if (ExplorersCompass.xpLevelsForAllowedStructureKeys.containsKey(structureKey)) {
			int levels = ExplorersCompass.xpLevelsForAllowedStructureKeys.get(structureKey);
			if (levels > 3) {
				levels = 3;
			}
			this.xpLevels = levels;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovering, float partialTick) {
		if (xpLevels > 0) {
			int spriteSize = (int) (height * 0.4F);
			int spriteBorder = (height - spriteSize) / 2;
			int spriteIndex = xpLevels - 1;
			ResourceLocation spriteId = isEnabled() ? ENABLED_LEVEL_SPRITES[spriteIndex] : DISABLED_LEVEL_SPRITES[spriteIndex];
			guiGraphics.blitSprite(spriteId, left + width - spriteSize - spriteBorder, top + spriteBorder, spriteSize, spriteSize);
		}

		int nameColor = isEnabled() ? 0xffffff : 0x808080;
		int infoColor = isEnabled() ? 0x808080 : 0x555555;
		guiGraphics.drawString(mc.font, Component.literal(StructureUtils.getPrettyStructureName(structureKey)), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 2), nameColor, true);
		guiGraphics.drawString(mc.font, Component.translatable("string.explorerscompass.source").append(": " + StructureUtils.getPrettyStructureSource(structureKey)), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 1), infoColor, true);
		guiGraphics.drawString(mc.font, Component.translatable("string.explorerscompass.group").append(": " + StructureUtils.getPrettyStructureName(ExplorersCompass.structureKeysToTypeKeys.get(structureKey))), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 0), infoColor, true);
		guiGraphics.drawString(mc.font, Component.translatable("string.explorerscompass.dimension").append(": " + StructureUtils.dimensionKeysToString(ExplorersCompass.dimensionKeysForAllowedStructureKeys.get(structureKey))), left + 5, top + (height / 2) + ((mc.font.lineHeight + 2) * 1), infoColor, true);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && isEnabled()) {
			structuresList.setSelected(this);
			if (Util.getMillis() - lastClickTime < 250L) {
				searchForStructure();
				return true;
			} else {
				lastClickTime = Util.getMillis();
				return false;
			}
		}
		return false;
	}

	@Override
	public Component getNarration() {
		return Component.literal(StructureUtils.getPrettyStructureName(structureKey));
	}

	public void searchForStructure() {
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForStructure(structureKey);
	}

	public void searchForGroup() {
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForGroup(ExplorersCompass.structureKeysToTypeKeys.get(structureKey));
	}

	public boolean isEnabled() {
		return ExplorersCompass.infiniteXp || player.experienceLevel >= xpLevels;
	}

	public ResourceLocation getStructureKey() {
		return structureKey;
	}

	public ResourceLocation getTypeKey() {
		return ExplorersCompass.structureKeysToTypeKeys.get(structureKey);
	}

}