package com.chaosthedude.explorerscompass.client;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
	public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
		return state.get(stack, level, entity, seed);
	}
	
	@Override
	public MapCodec<ExplorersCompassAngle> type() {
		return MAP_CODEC;
	}
	
}