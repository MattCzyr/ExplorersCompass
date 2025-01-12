package com.chaosthedude.explorerscompass.util;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ExplorersCompassAngle implements NumericProperty {

	public static final MapCodec<ExplorersCompassAngle> MAP_CODEC = MapCodec.unit(new ExplorersCompassAngle());
	private final ExplorersCompassAngleState state;

	public ExplorersCompassAngle() {
		this(new ExplorersCompassAngleState());
	}

	private ExplorersCompassAngle(ExplorersCompassAngleState state) {
		this.state = state;
	}

	@Override
	public float getValue(ItemStack stack, @Nullable ClientWorld level, @Nullable LivingEntity entity, int seed) {
		return state.getValue(stack, level, entity, seed);
	}

	@Override
	public MapCodec<ExplorersCompassAngle> getCodec() {
		return MAP_CODEC;
	}

}