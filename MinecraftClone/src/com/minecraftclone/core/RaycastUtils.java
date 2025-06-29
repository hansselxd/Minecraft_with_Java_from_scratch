/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import com.minecraftclone.core.WorldManager;
import org.joml.Vector3f;

public class RaycastUtils {

    public static RaycastResult raycast(WorldManager world, Vector3f origin, Vector3f direction, float maxDistance) {
        Vector3f ray = new Vector3f(direction).normalize();

        Vector3f pos = new Vector3f(origin);
        int lastX = (int) Math.floor(pos.x);
        int lastY = (int) Math.floor(pos.y);
        int lastZ = (int) Math.floor(pos.z);

        for (int i = 0; i < maxDistance * 10; i++) {
            pos.add(new Vector3f(ray).mul(0.1f));

            int x = (int) Math.floor(pos.x);
            int y = (int) Math.floor(pos.y);
            int z = (int) Math.floor(pos.z);

            if ((x != lastX || y != lastY || z != lastZ)
                    && world.getBlockIfLoader(x, y, z) != null
                    && world.getBlockIfLoader(x, y, z) != Block.AIR) {

                Vector3f blockPos = new Vector3f(x, y, z);
                Vector3f faceNormal = new Vector3f(lastX - x, lastY - y, lastZ - z);

                return new RaycastResult(blockPos, faceNormal);
            }

            lastX = x;
            lastY = y;
            lastZ = z;
        }

        return null;
    }
}
