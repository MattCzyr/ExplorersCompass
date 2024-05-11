package com.chaosthedude.explorerscompass.util;

import com.chaosthedude.explorerscompass.ExplorersCompass;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemUtils {

	public static boolean isCompass(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM;
	}

	public static ItemStack getHeldItem(PlayerEntity player, Item item) {
		if (!player.getMainHandStack().isEmpty() && player.getMainHandStack().getItem() == item) {
			return player.getMainHandStack();
		} else if (!player.getOffHandStack().isEmpty() && player.getOffHandStack().getItem() == item) {
			return player.getOffHandStack();
		}

		return ItemStack.EMPTY;
	}

}