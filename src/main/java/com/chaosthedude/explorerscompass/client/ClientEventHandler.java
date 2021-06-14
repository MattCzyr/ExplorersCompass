package com.chaosthedude.explorerscompass.client;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.RenderUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {

	private static final Minecraft mc = Minecraft.getInstance();

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase == Phase.END && mc.player != null && !mc.gameSettings.hideGUI && !mc.gameSettings.showDebugInfo && (mc.currentScreen == null || (ConfigHandler.CLIENT.displayWithChatOpen.get() && mc.currentScreen instanceof ChatScreen))) {
			final PlayerEntity player = mc.player;
			final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.explorersCompass);
			if (stack != null && stack.getItem() instanceof ExplorersCompassItem) {
				MatrixStack matrixStack = new MatrixStack();
				final ExplorersCompassItem compass = (ExplorersCompassItem) stack.getItem();
				if (compass.getState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.searching"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.structure"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, StructureUtils.getStructureName(compass.getStructureKey(stack)), 5, 5, 0xAAAAAA, 4);
					
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.radius"), 5, 5, 0xFFFFFF, 6);
 					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSearchRadius(stack)), 5, 5, 0xAAAAAA, 7);
				} else if (compass.getState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.found"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.structure"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, StructureUtils.getStructureName(compass.getStructureKey(stack)), 5, 5, 0xAAAAAA, 4);

					if (compass.shouldDisplayCoordinates(stack)) {
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.coordinates"), 5, 5, 0xFFFFFF, 6);
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, compass.getFoundStructureX(stack) + ", " + compass.getFoundStructureZ(stack), 5, 5, 0xAAAAAA, 7);

						RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.distance"), 5, 5, 0xFFFFFF, 9);
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(StructureUtils.getDistanceToStructure(player, compass.getFoundStructureX(stack), compass.getFoundStructureZ(stack))), 5, 5, 0xAAAAAA, 10);
					}
				} else if (compass.getState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.notFound"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.structure"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, StructureUtils.getStructureName(compass.getStructureKey(stack)), 5, 5, 0xAAAAAA, 4);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.radius"), 5, 5, 0xFFFFFF, 6);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSearchRadius(stack)), 5, 5, 0xAAAAAA, 7);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.explorerscompass.samples"), 5, 5, 0xFFFFFF, 9);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSamples(stack)), 5, 5, 0xAAAAAA, 10);
				}
			}
		}
	}

}