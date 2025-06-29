/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import static org.lwjgl.opengl.GL30.*;

public class CubeSelected {

    private int vaoId;
    private int vboId;
    private int vertexCount;

    public CubeSelected() {
        float[] vertices = {
            // Posiciones        // Normales       // UVs
            // Cara frontal
            0, 0, 1, 0, 0, 1, 0, 1,
            1, 0, 1, 0, 0, 1, 1, 1,
            1, 1, 1, 0, 0, 1, 1, 0,
            1, 1, 1, 0, 0, 1, 1, 0,
            0, 1, 1, 0, 0, 1, 0, 0,
            0, 0, 1, 0, 0, 1, 0, 1,

            // Cara trasera
            1, 0, 0, 0, 0, -1, 0, 1,
            0, 0, 0, 0, 0, -1, 1, 1,
            0, 1, 0, 0, 0, -1, 1, 0,
            0, 1, 0, 0, 0, -1, 1, 0,
            1, 1, 0, 0, 0, -1, 0, 0,
            1, 0, 0, 0, 0, -1, 0, 1,

            // Cara izquierda
            0, 0, 0, -1, 0, 0, 0, 1,
            0, 0, 1, -1, 0, 0, 1, 1,
            0, 1, 1, -1, 0, 0, 1, 0,
            0, 1, 1, -1, 0, 0, 1, 0,
            0, 1, 0, -1, 0, 0, 0, 0,
            0, 0, 0, -1, 0, 0, 0, 1,

            // Cara derecha
            1, 0, 1, 1, 0, 0, 0, 1,
            1, 0, 0, 1, 0, 0, 1, 1,
            1, 1, 0, 1, 0, 0, 1, 0,
            1, 1, 0, 1, 0, 0, 1, 0,
            1, 1, 1, 1, 0, 0, 0, 0,
            1, 0, 1, 1, 0, 0, 0, 1,

            // Cara superior
            0, 1, 1, 0, 1, 0, 0, 1,
            1, 1, 1, 0, 1, 0, 1, 1,
            1, 1, 0, 0, 1, 0, 1, 0,
            1, 1, 0, 0, 1, 0, 1, 0,
            0, 1, 0, 0, 1, 0, 0, 0,
            0, 1, 1, 0, 1, 0, 0, 1,

            // Cara inferior
            0, 0, 0, 0, -1, 0, 0, 1,
            1, 0, 0, 0, -1, 0, 1, 1,
            1, 0, 1, 0, -1, 0, 1, 0,
            1, 0, 1, 0, -1, 0, 1, 0,
            0, 0, 1, 0, -1, 0, 0, 0,
            0, 0, 0, 0, -1, 0, 0, 1
        };

        vertexCount = vertices.length / 8;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Posición (vec3)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Normal (vec3)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // UV (vec2)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}
