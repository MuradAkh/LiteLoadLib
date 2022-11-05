package dev.murad.liteloadlib.internal.event;

import dev.murad.liteloadlib.internal.LLLConfig;
import dev.murad.liteloadlib.internal.LiteLoadLib;
import dev.murad.liteloadlib.internal.global.PlayerEntityChunkManager;
import dev.murad.liteloadlib.internal.global.EntityChunkManagerManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge-wide event bus
 */
@Mod.EventBusSubscriber(modid = LiteLoadLib.MOD_ID)
public class ForgeEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        // Don't do anything client side
        if (event.level instanceof ServerLevel serverLevel) {
            EntityChunkManagerManager.get(serverLevel.getServer()).getManagers(event.level.dimension()).forEach(PlayerEntityChunkManager::tick);

        }
    }

    @SubscribeEvent
    public static void onPlayerSignInEvent(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getEntity().level.isClientSide || LLLConfig.Server.OFFLINE_LOADING.get()) {
            return;
        }

        EntityChunkManagerManager.get(event.getEntity().level.getServer())
                .getManagers(event.getEntity().getUUID())
                .forEach(PlayerEntityChunkManager::activate);
    }

    @SubscribeEvent
    public static void onPlayerSignInEvent(PlayerEvent.PlayerLoggedOutEvent event){
        if (event.getEntity().level.isClientSide || LLLConfig.Server.OFFLINE_LOADING.get()) {
            return;
        }

        EntityChunkManagerManager.get(event.getEntity().level.getServer())
                .getManagers(event.getEntity().getUUID())
                .forEach(PlayerEntityChunkManager::deactivate);
    }
}
