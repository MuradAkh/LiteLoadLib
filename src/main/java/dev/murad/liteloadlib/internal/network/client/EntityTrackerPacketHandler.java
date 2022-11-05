package dev.murad.liteloadlib.internal.network.client;

import dev.murad.liteloadlib.internal.LiteLoadLib;
import dev.murad.liteloadlib.api.EntityPosition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EntityTrackerPacketHandler {
    public static final ResourceLocation LOCATION = new ResourceLocation(LiteLoadLib.MOD_ID, "vehicle_tracker_channel");
    private static final String PROTOCOL_VERSION = "1";
    public static List<EntityPosition> toRender = new ArrayList<>();
    public static String toRenderDimension = "";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            LOCATION,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;
    public static void register() {
        // int index, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer
        INSTANCE.registerMessage(id++, EntityTrackerClientPacket.class, EntityTrackerClientPacket::encode, EntityTrackerClientPacket::new, EntityTrackerPacketHandler::handleData);
    }


    public static void handleData(EntityTrackerClientPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            EntityTrackerPacketHandler.toRender = packet.parse();
            EntityTrackerPacketHandler.toRenderDimension = packet.dimension;
        });

        ctx.get().setPacketHandled(true);
    }
}
