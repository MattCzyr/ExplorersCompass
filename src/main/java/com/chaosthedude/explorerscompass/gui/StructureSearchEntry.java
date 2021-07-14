package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.list.ExtendedList.AbstractListEntry;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureSearchEntry extends AbstractListEntry<StructureSearchEntry> {

	private final Minecraft mc;
	private final ExplorersCompassScreen parentScreen;
	private final Structure<?> structure;
	private final StructureSearchList structuresList;
	private long lastClickTime;

	public StructureSearchEntry(StructureSearchList structuresList, Structure<?> structure) {
		this.structuresList = structuresList;
		this.structure = structure;
		parentScreen = structuresList.getParentScreen();
		mc = Minecraft.getInstance();
	}

	@Override
	public void render(MatrixStack matrixStack, int par1, int par2, int par3, int par4, int par5, int par6, int par7, boolean par8, float par9) {
		mc.fontRenderer.func_243248_b(matrixStack, new StringTextComponent(StructureUtils.getStructureName(structure)), par3 + 1, par2 + 1, 0xffffff);
		mc.fontRenderer.func_243248_b(matrixStack, new TranslationTextComponent(("string.explorerscompass.source")).append(new StringTextComponent(": " + StructureUtils.getStructureSource(structure))), par3 + 1, par2 + mc.fontRenderer.FONT_HEIGHT + 3, 0x808080);
		mc.fontRenderer.func_243248_b(matrixStack, new TranslationTextComponent(("string.explorerscompass.category")).append(new StringTextComponent(": ")).append(new TranslationTextComponent(("string.explorerscompass." + structure.getDecorationStage().toString().toLowerCase()))), par3 + 1, par2 + mc.fontRenderer.FONT_HEIGHT + 14, 0x808080);
		mc.fontRenderer.func_243248_b(matrixStack, new TranslationTextComponent(("string.explorerscompass.dimension")).append(new StringTextComponent(": " + StructureUtils.structureDimensionsToString(ExplorersCompass.dimensionsForAllowedStructures.get(structure)))), par3 + 1, par2 + mc.fontRenderer.FONT_HEIGHT + 25, 0x808080);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			structuresList.selectStructure(this);
			if (Util.milliTime() - lastClickTime < 250L) {
				searchForBiome();
				return true;
			} else {
				lastClickTime = Util.milliTime();
				return false;
			}
		}
		return false;
	}

	public void searchForBiome() {
		mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForStructure(structure);
	}

}