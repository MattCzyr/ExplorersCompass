package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.gen.feature.StructureFeature;

@Environment(EnvType.CLIENT)
public class StructureSearchEntry extends AlwaysSelectedEntryListWidget.Entry<StructureSearchEntry> {

	private final MinecraftClient client;
	private final ExplorersCompassScreen parentScreen;
	private final StructureFeature<?> structure;
	private final StructureSearchList structuresList;
	private long lastClickTime;

	public StructureSearchEntry(StructureSearchList structuresList, StructureFeature<?> structure) {
		this.structuresList = structuresList;
		this.structure = structure;
		parentScreen = structuresList.getParentScreen();
		client = MinecraftClient.getInstance();
	}

	@Override
	public void render(MatrixStack matrixStack, int par1, int par2, int par3, int par4, int par5, int par6, int par7, boolean par8, float par9) {
		client.textRenderer.draw(matrixStack, new LiteralText(StructureUtils.getStructureName(structure)), par3 + 1, par2 + 1, 0xffffff);
		client.textRenderer.draw(matrixStack, new LiteralText(I18n.translate("string.explorerscompass.source") + ": " + StructureUtils.getStructureSource(structure)), par3 + 1, par2 + client.textRenderer.fontHeight + 3, 0x808080);
		client.textRenderer.draw(matrixStack, new LiteralText(I18n.translate("string.explorerscompass.category") + ": " + I18n.translate("string.explorerscompass." + structure.getGenerationStep().toString().toLowerCase())), par3 + 1, par2 + client.textRenderer.fontHeight + 14, 0x808080);
		client.textRenderer.draw(matrixStack, new LiteralText(I18n.translate("string.explorerscompass.dimension") + ": " + StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(structure))), par3 + 1, par2 + client.textRenderer.fontHeight + 25, 0x808080);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			structuresList.selectStructure(this);
			if (Util.getMeasuringTimeMs() - lastClickTime < 250L) {
				searchForBiome();
				return true;
			} else {
				lastClickTime = Util.getMeasuringTimeMs();
				return false;
			}
		}
		return false;
	}

	public void searchForBiome() {
		client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForStructure(structure);
	}

	@Override
	public Text getNarration() {
		return new LiteralText(StructureUtils.getStructureName(structure));
	}

}