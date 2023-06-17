package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class StructureSearchEntry extends AlwaysSelectedEntryListWidget.Entry<StructureSearchEntry> {

	private final MinecraftClient client;
	private final ExplorersCompassScreen parentScreen;
	private final Identifier structureID;
	private final StructureSearchList structuresList;
	private long lastClickTime;

	public StructureSearchEntry(StructureSearchList structuresList, Identifier structureID) {
		this.structuresList = structuresList;
		this.structureID = structureID;
		parentScreen = structuresList.getParentScreen();
		client = MinecraftClient.getInstance();
	}

	@Override
	public void render(DrawContext context, int par1, int par2, int par3, int par4, int par5, int par6, int par7, boolean par8, float par9) {
		context.drawText(client.textRenderer, Text.literal(StructureUtils.getStructureName(structureID)), par3 + 1, par2 + 1, 0xffffff, false);
		context.drawText(client.textRenderer, Text.translatable("string.explorerscompass.source").append(": " + StructureUtils.getStructureSource(structureID)), par3 + 1, par2 + client.textRenderer.fontHeight + 3, 0x808080, false);
		context.drawText(client.textRenderer, Text.translatable("string.explorerscompass.group").append(": " + StructureUtils.getStructureName(ExplorersCompass.structureIDsToGroupIDs.get(structureID))), par3 + 1, par2 + client.textRenderer.fontHeight + 14, 0x808080, false);
		context.drawText(client.textRenderer, Text.translatable("string.explorerscompass.dimension").append(": " + StructureUtils.structureDimensionsToString(ExplorersCompass.allowedStructureIDsToDimensionIDs.get(structureID))), par3 + 1, par2 + client.textRenderer.fontHeight + 25, 0x808080, false);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			structuresList.selectStructure(this);
			if (Util.getMeasuringTimeMs() - lastClickTime < 250L) {
				searchForStructure();
				return true;
			} else {
				lastClickTime = Util.getMeasuringTimeMs();
				return false;
			}
		}
		return false;
	}

	public void searchForStructure() {
		client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForStructure(structureID);
	}
	
	public void searchForStructureGroup() {
		client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForStructureGroup(ExplorersCompass.structureIDsToGroupIDs.get(structureID));
	}

	@Override
	public Text getNarration() {
		return Text.literal(StructureUtils.getStructureName(structureID));
	}

}