package com.chaosthedude.explorerscompass.registry;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = ExplorersCompass.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ExplorersCompassRegistry {

	@SubscribeEvent
	public static void registerItems(RegisterEvent e) {
		e.register(BuiltInRegistries.ITEM.key(), helper -> {
			ExplorersCompass.explorersCompass = new ExplorersCompassItem();
            helper.register(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, ExplorersCompassItem.NAME), ExplorersCompass.explorersCompass);
        });
		
		e.register(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), registry -> {
			registry.register(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "structure_id"), ExplorersCompass.STRUCTURE_ID_COMPONENT);
			registry.register(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "compass_state"), ExplorersCompass.COMPASS_STATE_COMPONENT);
			registry.register(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "found_x"), ExplorersCompass.FOUND_X_COMPONENT);
			registry.register(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "found_z"), ExplorersCompass.FOUND_Z_COMPONENT);
			registry.register(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "search_radius"), ExplorersCompass.SEARCH_RADIUS_COMPONENT);
			registry.register(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "samples"), ExplorersCompass.SAMPLES_COMPONENT);
			registry.register(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "display_coords"), ExplorersCompass.DISPLAY_COORDS_COMPONENT);
		});
	}

}