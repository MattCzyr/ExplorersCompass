package com.chaosthedude.explorerscompass.client;

import com.chaosthedude.explorerscompass.ExplorersCompass;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ExplorersCompass.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ExplorersCompassClient {

	@SubscribeEvent
	public static void registerItemModelProperty(RegisterRangeSelectItemModelPropertyEvent event) {
		event.register(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "angle"), ExplorersCompassAngle.MAP_CODEC);
	}

	@SubscribeEvent
	public static void registerOverlay(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "explorers_compass"), new ExplorersCompassOverlay());
	}

}
