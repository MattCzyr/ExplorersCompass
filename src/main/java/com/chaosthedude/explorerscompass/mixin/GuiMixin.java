package com.chaosthedude.explorerscompass.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.item.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.RenderUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class GuiMixin {
	
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "TAIL"))
	private void renderCompassInfo(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
		if (minecraft.player != null && !minecraft.options.hideGui && !minecraft.getDebugOverlay().showDebugScreen() && (minecraft.screen == null || (ExplorersCompassConfig.displayWithChatOpen && minecraft.screen instanceof ChatScreen))) {
			final Player player = minecraft.player;
			final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.EXPLORERS_COMPASS_ITEM);
			if (stack != null && stack.getItem() instanceof ExplorersCompassItem) {
				final ExplorersCompassItem compass = (ExplorersCompassItem) stack.getItem();
				if (compass.getCompassState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.status"), 5, 5, 0xffffffff, 0);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.searching"), 5, 5, 0xffaaaaaa, 1);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.structure"), 5, 5, 0xffffffff, 3);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, StructureUtils.getStructureName(Identifier.parse(stack.getOrDefault(ExplorersCompass.STRUCTURE_ID, ""))), 5, 5, 0xffaaaaaa, 4);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.radius"), 5, 5, 0xffffffff, 6);
 					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(stack.getOrDefault(ExplorersCompass.SEARCH_RADIUS, 0)), 5, 5, 0xffaaaaaa, 7);
				} else if (compass.getCompassState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.status"), 5, 5, 0xffffffff, 0);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.found"), 5, 5, 0xffaaaaaa, 1);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.structure"), 5, 5, 0xffffffff, 3);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, StructureUtils.getStructureName(Identifier.parse(stack.getOrDefault(ExplorersCompass.STRUCTURE_ID, ""))), 5, 5, 0xffaaaaaa, 4);

					if (stack.getOrDefault(ExplorersCompass.DISPLAY_COORDS, false)) {
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.coordinates"), 5, 5, 0xffffffff, 6);
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, stack.getOrDefault(ExplorersCompass.FOUND_X, 0) + ", " + stack.getOrDefault(ExplorersCompass.FOUND_Z, 0), 5, 5, 0xffaaaaaa, 7);

						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.distance"), 5, 5, 0xffffffff, 9);
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(StructureUtils.getHorizontalDistanceToLocation(player, stack.getOrDefault(ExplorersCompass.FOUND_X, 0), stack.getOrDefault(ExplorersCompass.FOUND_Z, 0))), 5, 5, 0xffaaaaaa, 10);
					}
				} else if (compass.getCompassState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.status"), 5, 5, 0xffffffff, 0);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.notFound"), 5, 5, 0xffaaaaaa, 1);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.structure"), 5, 5, 0xffffffff, 3);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, StructureUtils.getStructureName(Identifier.parse(stack.getOrDefault(ExplorersCompass.STRUCTURE_ID, ""))), 5, 5, 0xffaaaaaa, 4);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.explorerscompass.radius"), 5, 5, 0xffffffff, 6);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(stack.getOrDefault(ExplorersCompass.SEARCH_RADIUS, 0)), 5, 5, 0xffaaaaaa, 7);
				}
			}
		}
	}

}