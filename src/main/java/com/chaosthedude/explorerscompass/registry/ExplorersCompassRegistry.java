package com.chaosthedude.explorerscompass.registry;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;

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
	}

}