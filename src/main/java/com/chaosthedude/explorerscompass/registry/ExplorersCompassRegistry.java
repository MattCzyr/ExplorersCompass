package com.chaosthedude.explorerscompass.registry;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.item.ExplorersCompassItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = ExplorersCompass.MODID)
public class ExplorersCompassRegistry {

	@SubscribeEvent
	public static void registerItems(RegisterEvent e) {
		e.register(BuiltInRegistries.ITEM.key(), helper -> {
			ExplorersCompass.explorersCompass = new ExplorersCompassItem();
            helper.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, ExplorersCompassItem.NAME), ExplorersCompass.explorersCompass);
        });
		
		e.register(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), registry -> {
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "structure_id"), ExplorersCompass.STRUCTURE_ID);
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "compass_state"), ExplorersCompass.COMPASS_STATE);
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "found_x"), ExplorersCompass.FOUND_X);
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "found_z"), ExplorersCompass.FOUND_Z);
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "search_radius"), ExplorersCompass.SEARCH_RADIUS);
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "samples"), ExplorersCompass.SAMPLES);
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "display_coords"), ExplorersCompass.DISPLAY_COORDS);
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "is_group"), ExplorersCompass.IS_GROUP);
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "prev_pos"), ExplorersCompass.PREV_POS);
			registry.register(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "damage"), ExplorersCompass.DAMAGE);
		});
	}

}