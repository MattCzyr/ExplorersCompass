package com.chaosthedude.explorerscompass.util;

import com.chaosthedude.explorerscompass.ExplorersCompass;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemUtils {

	public static boolean isCompass(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass;
	}

	public static ItemStack getHeldItem(Player player, Item item) {
		if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == item) {
			return player.getMainHandItem();
		} else if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() == item) {
			return player.getOffhandItem();
		}

		return ItemStack.EMPTY;
	}

}