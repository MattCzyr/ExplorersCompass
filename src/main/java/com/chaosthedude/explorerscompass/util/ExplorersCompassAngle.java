package com.chaosthedude.explorerscompass.util;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ExplorersCompassAngle implements RangeSelectItemModelProperty {
	
	public static final MapCodec<ExplorersCompassAngle> MAP_CODEC = MapCodec.unit(new ExplorersCompassAngle());
	private final ExplorersCompassAngleState state;
	
	public ExplorersCompassAngle() {
		this(new ExplorersCompassAngleState());
	}
	
	private ExplorersCompassAngle(ExplorersCompassAngleState state) {
		this.state = state;
	}
	
	@Override
	public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		return state.get(stack, level, owner, seed);
	}
	
	@Override
	public MapCodec<ExplorersCompassAngle> type() {
		return MAP_CODEC;
	}
	
}