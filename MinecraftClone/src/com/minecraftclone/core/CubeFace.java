/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import com.minecraftclone.core.CubeGenerator.Direction;

public class CubeFace {
    // Vértices por cara, sentido horario desde esquina inferior izquierda
    public static float[][] getFaceVertices(Direction dir) {
        switch (dir) {
            case UP:
                return new float[][] {
                    {0, 1, 1}, {1, 1, 1}, {1, 1, 0}, {0, 1, 0}
                };
            case DOWN:
                return new float[][] {
                    {0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}
                };
            case FRONT:
                return new float[][] {
                    {0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}
                };
            case BACK:
                return new float[][] {
                    {1, 0, 0}, {0, 0, 0}, {0, 1, 0}, {1, 1, 0}
                };
            case LEFT:
                return new float[][] {
                    {0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}
                };
            case RIGHT:
                return new float[][] {
                    {1, 0, 1}, {1, 0, 0}, {1, 1, 0}, {1, 1, 1}
                };
        }
        return new float[0][0];
    }

    public static float[] getNormal(Direction dir) {
        switch (dir) {
            case UP: return new float[] {0, 1, 0};
            case DOWN: return new float[] {0, -1, 0};
            case FRONT: return new float[] {0, 0, 1};
            case BACK: return new float[] {0, 0, -1};
            case LEFT: return new float[] {-1, 0, 0};
            case RIGHT: return new float[] {1, 0, 0};
        }
        return new float[] {0, 0, 0};
    }
}

