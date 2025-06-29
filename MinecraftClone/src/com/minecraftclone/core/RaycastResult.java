/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import org.joml.Vector3f;

public class RaycastResult {
    public final Vector3f blockPos;
    public final Vector3f faceNormal;

    public RaycastResult(Vector3f blockPos, Vector3f faceNormal) {
        this.blockPos = blockPos;
        this.faceNormal = faceNormal;
    }
}

