/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

public class Block {

    private BlockType type;
    public boolean isSolid;

    //Bloques
    public static final Block AIR = new Block(false, BlockType.AIR);
    public static final Block DIRT = new Block(true, BlockType.DIRT);
    public static final Block GRASS = new Block(true, BlockType.GRASS);
    public static final Block STONE = new Block(true, BlockType.STONE);
    public static final Block[] BLOCKS = new Block[256];

    static {
        BLOCKS[0] = AIR;
        BLOCKS[1] = GRASS;
        BLOCKS[2] = DIRT;
        BLOCKS[3] = STONE;
    }

    public Block(boolean isSolid, BlockType type) {
        this.isSolid = isSolid;
        this.type = type;
    }

    public BlockType getType() {
        return type;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public static int getBlockId(Block block) {
        for (int i = 0; i < BLOCKS.length; i++) {
            if (Block.BLOCKS[i] == block) {
                return i;
            }
        }
        return 0;
    }

    public static Block getBlockById(int id) {
        if (id >= 0 && id < Block.BLOCKS.length) {
            return BLOCKS[id];
        }
        return AIR;
    }

    public Texture getTexture() throws Exception {
        if (this == GRASS) {
            return new Texture("res/textures/grass_texture.png");
        }
        if (this == DIRT) {
            return new Texture("res/textures/dirt_texture.png");
        }
        if (this == STONE) {
            return new Texture("res/textures/stone_texture.png");
        } else {
            return null;
        }
    }

}
