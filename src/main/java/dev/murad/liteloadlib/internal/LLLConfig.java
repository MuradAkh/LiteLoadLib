package dev.murad.liteloadlib.internal;

import net.minecraftforge.common.ForgeConfigSpec;

public class LLLConfig {

    public static class Server {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.ConfigValue<Integer> CHUNK_LOADING_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> DISABLE_CHUNK_MANAGEMENT;
        public static final ForgeConfigSpec.ConfigValue<Integer> MAX_REGISTRERED_VEHICLES_PER_PLAYER;
        public static final ForgeConfigSpec.ConfigValue<Boolean> OFFLINE_LOADING;


        static {
            BUILDER.push("chunk management - requires restart");
            BUILDER.comment("By default, little loading library allows players to register entities that will be loaded automatically. This is not regular chunkloading, no other ticking will happen in this chunks and no surrounding chunks will be loaded. A very minimal number of chunks will be loaded as \"border chunks\" where only LLL enrolled entities are active by default.");

            CHUNK_LOADING_LEVEL = BUILDER.comment("Chunkloading level, from low perf impact to high. 0: no ticking (except LLL, recommended), 1: tile entity ticking, 2: entity ticking (regular).")
                    .defineInRange("chunkLoadingLevel", 0, 0, 2);

            DISABLE_CHUNK_MANAGEMENT = BUILDER.comment("Completely disable the chunk management system.")
                    .define("disableChunkManagement", false);

            MAX_REGISTRERED_VEHICLES_PER_PLAYER = BUILDER.comment("Maximum number of entities the player is able to register (extra entities such as passengers, part entities, and train cars etc do not count). Lowering this number will not de-register entities but will prevent the player from registering more.")
                    .defineInRange("maxEntitiesPerPlayer", 100, 0, 1000);

            OFFLINE_LOADING = BUILDER.comment("Load vehicles even when the player is offline")
                    .define("offlineLoading", false);

            BUILDER.pop();

            SPEC = BUILDER.build();

        }

    }

}
