package com.chaosthedude.explorerscompass.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

	@Shadow
	private ItemStack mainHand;

	@Shadow
	private ItemStack offHand;

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "updateHeldItems()V", at = @At("HEAD"))
	private void cancelCompassAnimation(CallbackInfo ci) {
		ItemStack newMainStack = client.player.getMainHandStack();
		if (newMainStack.getItem() instanceof ExplorersCompassItem && mainHand.getItem() instanceof ExplorersCompassItem) {
			ExplorersCompassItem newMainCompass = (ExplorersCompassItem) newMainStack.getItem();
			ExplorersCompassItem mainCompass = (ExplorersCompassItem) mainHand.getItem();
			if (newMainCompass.getState(newMainStack) == mainCompass.getState(mainHand)) {
				mainHand = newMainStack;
			}
		}

		ItemStack newOffStack = client.player.getOffHandStack();
		if (newOffStack.getItem() instanceof ExplorersCompassItem && offHand.getItem() instanceof ExplorersCompassItem) {
			ExplorersCompassItem newOffCompass = (ExplorersCompassItem) newOffStack.getItem();
			ExplorersCompassItem offCompass = (ExplorersCompassItem) offHand.getItem();
			if (newOffCompass.getState(newOffStack) == offCompass.getState(offHand)) {
				offHand = newOffStack;
			}
		}
	}

}