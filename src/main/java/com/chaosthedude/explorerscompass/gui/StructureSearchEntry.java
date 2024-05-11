package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureSearchEntry extends ObjectSelectionList.Entry<StructureSearchEntry> {

	private final Minecraft mc;
	private final ExplorersCompassScreen parentScreen;
	private final ResourceLocation structureKey;
	private final StructureSearchList structuresList;
	private long lastClickTime;

	public StructureSearchEntry(StructureSearchList structuresList, ResourceLocation structureKey) {
		this.structuresList = structuresList;
		this.structureKey = structureKey;
		parentScreen = structuresList.getParentScreen();
		mc = Minecraft.getInstance();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int par1, int par2, int par3, int par4, int par5, int par6, int par7, boolean par8, float par9) {
		guiGraphics.drawString(mc.font, Component.literal(StructureUtils.getPrettyStructureName(structureKey)), par3 + 1, par2 + 1, 0xffffff);
		guiGraphics.drawString(mc.font, Component.translatable(("string.explorerscompass.source")).append(Component.literal(": " + StructureUtils.getPrettyStructureSource(structureKey))), par3 + 1, par2 + mc.font.lineHeight + 3, 0x808080);
		guiGraphics.drawString(mc.font, Component.translatable(("string.explorerscompass.group")).append(Component.literal(": ")).append(Component.translatable(StructureUtils.getPrettyStructureName(ExplorersCompass.structureKeysToTypeKeys.get(structureKey)))), par3 + 1, par2 + mc.font.lineHeight + 14, 0x808080);
		guiGraphics.drawString(mc.font, Component.translatable(("string.explorerscompass.dimension")).append(Component.literal(": " + StructureUtils.dimensionKeysToString(ExplorersCompass.dimensionKeysForAllowedStructureKeys.get(structureKey)))), par3 + 1, par2 + mc.font.lineHeight + 25, 0x808080);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			structuresList.selectStructure(this);
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

}