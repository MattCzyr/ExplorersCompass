package com.chaosthedude.explorerscompass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.explorerscompass.client.ClientEventHandler;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.CompassSearchPacket;
import com.chaosthedude.explorerscompass.network.SyncPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.chaosthedude.explorerscompass.util.CompassState;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

@Mod(ExplorersCompass.MODID)
public class ExplorersCompass {

	public static final String MODID = "explorerscompass";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static SimpleChannel network;
	public static ExplorersCompassItem explorersCompass;

	public static boolean canTeleport;
	public static List<StructureFeature<?>> allowedStructures;
	public static Map<StructureFeature<?>, List<ResourceLocation>> dimensionsForAllowedStructures;

	public ExplorersCompass() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
		});
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.GENERAL_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
	}

	private void preInit(FMLCommonSetupEvent event) {
		network = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> "1.0", s -> true, s -> true);

		// Server packets
		network.registerMessage(0, CompassSearchPacket.class, CompassSearchPacket::toBytes, CompassSearchPacket::new, CompassSearchPacket::handle);
		network.registerMessage(1, TeleportPacket.class, TeleportPacket::toBytes, TeleportPacket::new, TeleportPacket::handle);

		// Client packet
		network.registerMessage(3, SyncPacket.class, SyncPacket::toBytes, SyncPacket::new, SyncPacket::handle);

		allowedStructures = new ArrayList<StructureFeature<?>>();
		dimensionsForAllowedStructures = new HashMap<StructureFeature<?>, List<ResourceLocation>>();
	}
	
	@OnlyIn(Dist.CLIENT)
	public void clientInit(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		
		ItemProperties.register(explorersCompass, new ResourceLocation("angle"), new ClampedItemPropertyFunction() {
			@OnlyIn(Dist.CLIENT)
			private double rotation;
			@OnlyIn(Dist.CLIENT)
			private double rota;
			@OnlyIn(Dist.CLIENT)
			private long lastUpdateTick;

			@OnlyIn(Dist.CLIENT)
			@Override
			public float unclampedCall(ItemStack stack, ClientLevel world, LivingEntity entityLiving, int seed) {
				if (entityLiving == null && !stack.isFramed()) {
					return 0.0F;
				} else {
					final boolean entityExists = entityLiving != null;
					final Entity entity = (Entity) (entityExists ? entityLiving : stack.getFrame());
					if (world == null && entity.level instanceof ClientLevel) {
						world = (ClientLevel) entity.level;
					}

					double rotation = entityExists ? (double) entity.getYRot() : getFrameRotation((ItemFrame) entity);
					rotation = rotation % 360.0D;
					double adjusted = Math.PI - ((rotation - 90.0D) * 0.01745329238474369D - getAngle(world, entity, stack));

					if (entityExists) {
						adjusted = wobble(world, adjusted);
					}

					final float f = (float) (adjusted / (Math.PI * 2D));
					return Mth.positiveModulo(f, 1.0F);
				}
			}

			@OnlyIn(Dist.CLIENT)
			private double wobble(ClientLevel world, double amount) {
				if (world.getGameTime() != lastUpdateTick) {
					lastUpdateTick = world.getGameTime();
					double d0 = amount - rotation;
					d0 = d0 % (Math.PI * 2D);
					d0 = Mth.clamp(d0, -1.0D, 1.0D);
					rota += d0 * 0.1D;
					rota *= 0.8D;
					rotation += rota;
				}

				return rotation;
			}

			@OnlyIn(Dist.CLIENT)
			private double getFrameRotation(ItemFrame itemFrame) {
				Direction direction = itemFrame.getDirection();
				int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
				return (double)Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + itemFrame.getRotation() * 45 + i);
			}

			@OnlyIn(Dist.CLIENT)
			private double getAngle(ClientLevel world, Entity entity, ItemStack stack) {
				if (stack.getItem() == explorersCompass) {
					ExplorersCompassItem compassItem = (ExplorersCompassItem) stack.getItem();
					BlockPos pos;
					if (compassItem.getState(stack) == CompassState.FOUND) {
						pos = new BlockPos(compassItem.getFoundStructureX(stack), 0, compassItem.getFoundStructureZ(stack));
					} else {
						pos = world.getSharedSpawnPos();
					}
					return Math.atan2((double) pos.getZ() - entity.position().z(), (double) pos.getX() - entity.position().x());
				}
				return 0.0D;
			}
		});
	}

}