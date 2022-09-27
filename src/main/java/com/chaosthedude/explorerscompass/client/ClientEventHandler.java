package com.chaosthedude.explorerscompass.client;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.RenderUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
		if (event.phase == Phase.END && mc.player != null && mc.level != null && !mc.options.hideGui && !mc.options.renderDebug && (mc.screen == null || (ConfigHandler.CLIENT.displayWithChatOpen.get() && mc.screen instanceof ChatScreen))) {
			final Player player = mc.player;
			final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.explorersCompass);
			if (stack != null && stack.getItem() instanceof ExplorersCompassItem) {
				PoseStack poseStack = new PoseStack();
				final ExplorersCompassItem compass = (ExplorersCompassItem) stack.getItem();
				if (compass.getState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.searching"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.structure"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), 5, 5, 0xAAAAAA, 4);
					
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.radius"), 5, 5, 0xFFFFFF, 6);
 					RenderUtils.drawConfiguredStringOnHUD(poseStack, String.valueOf(compass.getSearchRadius(stack)), 5, 5, 0xAAAAAA, 7);
				} else if (compass.getState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.found"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.structure"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), 5, 5, 0xAAAAAA, 4);

					if (compass.shouldDisplayCoordinates(stack)) {
						RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.coordinates"), 5, 5, 0xFFFFFF, 6);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, compass.getFoundStructureX(stack) + ", " + compass.getFoundStructureZ(stack), 5, 5, 0xAAAAAA, 7);

						RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.distance"), 5, 5, 0xFFFFFF, 9);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, String.valueOf(StructureUtils.getHorizontalDistanceToLocation(player, compass.getFoundStructureX(stack), compass.getFoundStructureZ(stack))), 5, 5, 0xAAAAAA, 10);
					}
				} else if (compass.getState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.notFound"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.structure"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), 5, 5, 0xAAAAAA, 4);

					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.radius"), 5, 5, 0xFFFFFF, 6);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, String.valueOf(compass.getSearchRadius(stack)), 5, 5, 0xAAAAAA, 7);
				}
			}
		}
	}

}