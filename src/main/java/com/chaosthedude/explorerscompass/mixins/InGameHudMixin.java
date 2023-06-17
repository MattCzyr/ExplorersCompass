package com.chaosthedude.explorerscompass.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.RenderUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;F)V", at = @At(value = "TAIL"))
	private void renderCompassInfo(DrawContext context, float tickDelta, CallbackInfo info) {
		if (client.player != null && !client.options.hudHidden && !client.options.debugEnabled && (client.currentScreen == null || (ExplorersCompassConfig.displayWithChatOpen && client.currentScreen instanceof ChatScreen))) {
			final PlayerEntity player = client.player;
			final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.EXPLORERS_COMPASS_ITEM);
			if (stack != null && stack.getItem() instanceof ExplorersCompassItem) {
				final ExplorersCompassItem compass = (ExplorersCompassItem) stack.getItem();
				if (compass.getState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.status"), client.textRenderer, 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.searching"), client.textRenderer, 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.structure"), client.textRenderer, 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(context, StructureUtils.getStructureName(compass.getStructureID(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 4);
					
					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.radius"), client.textRenderer, 5, 5, 0xFFFFFF, 6);
 					RenderUtils.drawConfiguredStringOnHUD(context, String.valueOf(compass.getSearchRadius(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 7);
				} else if (compass.getState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.status"), client.textRenderer, 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.found"), client.textRenderer, 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.structure"), client.textRenderer, 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(context, StructureUtils.getStructureName(compass.getStructureID(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 4);

					if (compass.shouldDisplayCoordinates(stack)) {
						RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.coordinates"), client.textRenderer, 5, 5, 0xFFFFFF, 6);
						RenderUtils.drawConfiguredStringOnHUD(context, compass.getFoundStructureX(stack) + ", " + compass.getFoundStructureZ(stack), client.textRenderer, 5, 5, 0xAAAAAA, 7);

						RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.distance"), client.textRenderer, 5, 5, 0xFFFFFF, 9);
						RenderUtils.drawConfiguredStringOnHUD(context, String.valueOf(StructureUtils.getHorizontalDistanceToLocation(player, compass.getFoundStructureX(stack), compass.getFoundStructureZ(stack))), client.textRenderer, 5, 5, 0xAAAAAA, 10);
					}
				} else if (compass.getState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.status"), client.textRenderer, 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.notFound"), client.textRenderer, 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.structure"), client.textRenderer, 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(context, StructureUtils.getStructureName(compass.getStructureID(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 4);

					RenderUtils.drawConfiguredStringOnHUD(context, I18n.translate("string.explorerscompass.radius"), client.textRenderer, 5, 5, 0xFFFFFF, 6);
					RenderUtils.drawConfiguredStringOnHUD(context, String.valueOf(compass.getSearchRadius(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 7);
				}
			}
		}
	}

}