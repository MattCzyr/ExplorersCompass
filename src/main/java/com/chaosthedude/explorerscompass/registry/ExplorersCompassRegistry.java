package com.chaosthedude.explorerscompass.registry;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@EventBusSubscriber(modid = ExplorersCompass.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ExplorersCompassRegistry {

	@SubscribeEvent
	public static void registerItems(RegisterEvent e) {
		e.register(ForgeRegistries.Keys.ITEMS, helper -> {
			ExplorersCompass.explorersCompass = new ExplorersCompassItem();
			helper.register(new ResourceLocation(ExplorersCompass.MODID, ExplorersCompassItem.NAME), ExplorersCompass.explorersCompass);
		});
		
		e.register(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), registry -> {
			registry.register(new ResourceLocation(ExplorersCompass.MODID, "structure_id"), ExplorersCompass.STRUCTURE_ID_COMPONENT);
			registry.register(new ResourceLocation(ExplorersCompass.MODID, "compass_state"), ExplorersCompass.COMPASS_STATE_COMPONENT);
			registry.register(new ResourceLocation(ExplorersCompass.MODID, "found_x"), ExplorersCompass.FOUND_X_COMPONENT);
			registry.register(new ResourceLocation(ExplorersCompass.MODID, "found_z"), ExplorersCompass.FOUND_Z_COMPONENT);
			registry.register(new ResourceLocation(ExplorersCompass.MODID, "search_radius"), ExplorersCompass.SEARCH_RADIUS_COMPONENT);
			registry.register(new ResourceLocation(ExplorersCompass.MODID, "samples"), ExplorersCompass.SAMPLES_COMPONENT);
			registry.register(new ResourceLocation(ExplorersCompass.MODID, "display_coords"), ExplorersCompass.DISPLAY_COORDS_COMPONENT);
		});
	}

}