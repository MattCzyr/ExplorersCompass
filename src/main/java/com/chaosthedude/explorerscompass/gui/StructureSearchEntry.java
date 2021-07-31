package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureSearchEntry extends ObjectSelectionList.Entry<StructureSearchEntry> {

	private final Minecraft mc;
	private final ExplorersCompassScreen parentScreen;
	private final StructureFeature<?> structure;
	private final StructureSearchList structuresList;
	private long lastClickTime;

	public StructureSearchEntry(StructureSearchList structuresList, StructureFeature<?> structure) {
		this.structuresList = structuresList;
		this.structure = structure;
		parentScreen = structuresList.getParentScreen();
		mc = Minecraft.getInstance();
	}

	@Override
	public void render(PoseStack matrixStack, int par1, int par2, int par3, int par4, int par5, int par6, int par7, boolean par8, float par9) {
		mc.font.draw(matrixStack, new TextComponent(StructureUtils.getStructureName(structure)), par3 + 1, par2 + 1, 0xffffff);
		mc.font.draw(matrixStack, new TranslatableComponent(("string.explorerscompass.source")).append(new TextComponent(": " + StructureUtils.getStructureSource(structure))), par3 + 1, par2 + mc.font.lineHeight + 3, 0x808080);
		mc.font.draw(matrixStack, new TranslatableComponent(("string.explorerscompass.category")).append(new TextComponent(": ")).append(new TranslatableComponent(("string.explorerscompass." + structure.step().toString().toLowerCase()))), par3 + 1, par2 + mc.font.lineHeight + 14, 0x808080);
		mc.font.draw(matrixStack, new TranslatableComponent(("string.explorerscompass.dimension")).append(new TextComponent(": " + StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(structure)))), par3 + 1, par2 + mc.font.lineHeight + 25, 0x808080);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			structuresList.selectStructure(this);
			if (Util.getMillis() - lastClickTime < 250L) {
				searchForBiome();
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
		return new TextComponent(StructureUtils.getStructureName(structure));
	}

	public void searchForBiome() {
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForStructure(structure);
	}

}