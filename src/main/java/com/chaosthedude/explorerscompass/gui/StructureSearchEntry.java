package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureSearchEntry extends ObjectSelectionList.Entry<StructureSearchEntry> {

    private static final ResourceLocation[] ENABLED_LEVEL_SPRITES = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "textures/gui/level_1.png"),
            ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "textures/gui/level_2.png"),
            ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "textures/gui/level_3.png")
    };

    private static final ResourceLocation[] DISABLED_LEVEL_SPRITES = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "textures/gui/level_1_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "textures/gui/level_2_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "textures/gui/level_3_disabled.png")
    };

	private final Minecraft mc;
	private final ExplorersCompassScreen parentScreen;
	private final Player player;
	private final ResourceLocation structureKey;
	private final StructureSearchList structuresList;
	private final int xpLevels;
	private long lastClickTime;

	public StructureSearchEntry(StructureSearchList structuresList, ResourceLocation structureKey, Player player) {
		this.structuresList = structuresList;
		this.structureKey = structureKey;
		this.player = player;
		parentScreen = structuresList.getParentScreen();
		mc = Minecraft.getInstance();

		int levels = 0;
		if (ExplorersCompass.xpLevelsForAllowedStructureKeys != null && ExplorersCompass.xpLevelsForAllowedStructureKeys.containsKey(structureKey)) {
			levels = ExplorersCompass.xpLevelsForAllowedStructureKeys.get(structureKey);
			if (levels > 3) {
				levels = 3;
			}
		}
		this.xpLevels = levels;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
        if (xpLevels > 0) {
            int spriteSize = (int) (height * 0.4F);
            int spriteBorder = (height - spriteSize) / 2;
            int spriteIndex = xpLevels - 1;
            ResourceLocation spriteTexture = isEnabled() ? ENABLED_LEVEL_SPRITES[spriteIndex] : DISABLED_LEVEL_SPRITES[spriteIndex];
            guiGraphics.blit(spriteTexture, left + width - spriteSize - spriteBorder, top + spriteBorder, spriteSize, spriteSize, 0f, 0f, 16, 16, 16, 16);
        }

        int nameColor = isEnabled() ? 0xffffffff : 0xff808080;
		int infoColor = isEnabled() ? 0xff808080 : 0xff555555;
		guiGraphics.drawString(mc.font, Component.literal(StructureUtils.getPrettyStructureName(structureKey)), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 2), nameColor);
		guiGraphics.drawString(mc.font, Component.translatable(("string.explorerscompass.source")).append(Component.literal(": " + StructureUtils.getPrettyStructureSource(structureKey))), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 1), infoColor);
		guiGraphics.drawString(mc.font, Component.translatable(("string.explorerscompass.group")).append(Component.literal(": ")).append(Component.translatable(StructureUtils.getPrettyStructureName(ExplorersCompass.structureKeysToTypeKeys.get(structureKey)))), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 0), infoColor);
		guiGraphics.drawString(mc.font, Component.translatable(("string.explorerscompass.dimension")).append(Component.literal(": " + StructureUtils.dimensionKeysToString(ExplorersCompass.dimensionKeysForAllowedStructureKeys.get(structureKey)))), left + 5, top + (height / 2) + ((mc.font.lineHeight + 2) * 1), infoColor);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && isEnabled()) {
			structuresList.setSelected(this);
			if (Util.getMillis() - lastClickTime < 250L) {
				parentScreen.searchForStructure(structureKey);
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

	public boolean isEnabled() {
		return ExplorersCompass.infiniteXp || player.experienceLevel >= xpLevels;
	}

	public ResourceLocation getStructureKey() {
		return structureKey;
	}

	public ResourceLocation getGroupKey() {
		return ExplorersCompass.structureKeysToTypeKeys.get(structureKey);
	}

}
