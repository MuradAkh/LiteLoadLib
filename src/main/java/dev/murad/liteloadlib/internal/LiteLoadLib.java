package dev.murad.liteloadlib.internal;

import dev.murad.liteloadlib.internal.network.EnrolledEntityPacketHandler;
import dev.murad.liteloadlib.internal.network.client.EntityTrackerPacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LiteLoadLib.MOD_ID)
public class LiteLoadLib
{
    public static final String MOD_ID = "liteloadlib";
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public LiteLoadLib() {

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, LLLConfig.Server.SPEC, "liteloadlib-server.toml");

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        EnrolledEntityPacketHandler.register();
        EntityTrackerPacketHandler.register();
    }
}
