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

public class MeshBorder {

    private final int vaoId;
    private final int vboId;
    private final int vertexCount;

    public MeshBorder(float[] positions) {
        vertexCount = positions.length / 3;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(positions.length);
        buffer.put(positions).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVboId() {
        return vboId;
    }

    public void render() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);

        glDrawArrays(GL_LINES, 0, vertexCount);

        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
