package com.chaosthedude.explorerscompass.network;

import java.util.function.Supplier;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.ItemUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class CompassSearchPacket {

	private ResourceLocation structureKey;
	private int x;
	private int y;
	private int z;

	public CompassSearchPacket() {}

	public CompassSearchPacket(ResourceLocation structureKey, BlockPos pos) {
		this.structureKey = structureKey;

		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
	}

	public CompassSearchPacket(PacketBuffer buf) {
		structureKey = buf.readResourceLocation();

		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeResourceLocation(structureKey);

		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			final ItemStack stack = ItemUtils.getHeldItem(ctx.get().getSender(), ExplorersCompass.explorersCompass);
			if (!stack.isEmpty()) {
				final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
				final World world = ctx.get().getSender().world;
				explorersCompass.searchForStructure(world, ctx.get().getSender(), structureKey, new BlockPos(x, y, z), stack);
			}
		});
		ctx.get().setPacketHandled(true);
	}

}
