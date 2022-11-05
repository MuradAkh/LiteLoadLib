package dev.murad.liteloadlib.api;

import net.minecraft.world.phys.Vec3;


public record EntityPosition(String type, int id, Vec3 pos, Vec3 oldPos) {

}
