/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;

public class Mesh {

    private final int vaoId;
    private final int vboId;
    private final int vertexCount;

    public Mesh(float[] positions) {
        vertexCount = positions.length / 11;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(positions.length);
        buffer.put(positions).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        //Posicion de Vertice
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 11 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        //Normal
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 11 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        //UV
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 11 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
        //Sombra x Profundidad
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 11 * Float.BYTES, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);
        //AO
        glVertexAttribPointer(4, 1, GL_FLOAT, false, 11 * Float.BYTES, 9 * Float.BYTES);
        glEnableVertexAttribArray(4);
        //Sombra
        glVertexAttribPointer(5, 1, GL_FLOAT, false, 11 * Float.BYTES, 10 * Float.BYTES);
        glEnableVertexAttribArray(5);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        //glEnableVertexAttribArray(5);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        //glDisableVertexAttribArray(5);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glDisableVertexAttribArray(5);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
