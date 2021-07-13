package com.chaosthedude.explorerscompass.util;

import com.chaosthedude.explorerscompass.ExplorersCompass;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemUtils {

	public static boolean verifyNBT(ItemStack stack) {
		if (stack.isEmpty() || stack.getItem() != ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
			return false;
		} else if (!stack.hasTag()) {
			stack.setTag(new NbtCompound());
		}

		return true;
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