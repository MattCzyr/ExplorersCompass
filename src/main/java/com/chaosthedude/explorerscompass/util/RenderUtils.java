package com.chaosthedude.explorerscompass.util;

import com.chaosthedude.explorerscompass.client.OverlaySide;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderUtils {

	private static final Minecraft mc = Minecraft.getInstance();
	private static final Font font = mc.font;

	public static void drawStringLeft(PoseStack poseStack, String string, Font fontRenderer, int x, int y, int color) {
		fontRenderer.drawShadow(poseStack, string, x, y, color);
	}

	public static void drawStringRight(PoseStack poseStack, String string, Font fontRenderer, int x, int y, int color) {
		fontRenderer.drawShadow(poseStack, string, x - fontRenderer.width(string), y, color);
	}

	public static void drawConfiguredStringOnHUD(PoseStack poseStack, String string, int xOffset, int yOffset, int color, int relLineOffset) {
		yOffset += (relLineOffset + ConfigHandler.CLIENT.overlayLineOffset.get()) * 9;
		if (ConfigHandler.CLIENT.overlaySide.get() == OverlaySide.LEFT) {
			drawStringLeft(poseStack, string, font, xOffset + 2, yOffset + 2, color);
		} else {
			drawStringRight(poseStack, string, font, mc.getWindow().getGuiScaledWidth() - xOffset - 2, yOffset + 2, color);
		}
	}

	public static void drawRect(int left, int top, int right, int bottom, int color) {
		if (left < right) {
			int temp = left;
			left = right;
			right = temp;
		}

		if (top < bottom) {
			int temp = top;
			top = bottom;
			bottom = temp;
		}

		final float red = (float) (color >> 16 & 255) / 255.0F;
		final float green = (float) (color >> 8 & 255) / 255.0F;
		final float blue = (float) (color & 255) / 255.0F;
		final float alpha = (float) (color >> 24 & 255) / 255.0F;
		
		final Tesselator tesselator = Tesselator.getInstance();
		final BufferBuilder buffer = tesselator.getBuilder();

		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex((double) left, (double) bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.vertex((double) right, (double) bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.vertex((double) right, (double) top, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.vertex((double) left, (double) top, 0.0D).color(red, green, blue, alpha).endVertex();
		tesselator.end();
		RenderSystem.disableBlend();
	}

}