package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GuiWrapper {
	
	public static void openGUI(World world, PlayerEntity player, ItemStack stack) {
		MinecraftClient.getInstance().setScreen(new ExplorersCompassScreen(world, player, stack, (ExplorersCompassItem) stack.getItem(), ExplorersCompass.allowedConfiguredStructureIDs));
	}

}
