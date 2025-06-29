/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

public enum BlockType {
    AIR(false),
    DIRT(true),
    GRASS(true),
    STONE(true);

    private final boolean isSolid;

    BlockType(boolean isSolid) {
        this.isSolid = isSolid;
    }

    public boolean isSolid() {
        return isSolid;
    }
}

