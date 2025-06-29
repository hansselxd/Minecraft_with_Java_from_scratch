/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class BlockSelector {

    private Vector3i selectedPos = new Vector3i(0, -999, 0);
    private MeshBorder outlineMesh;

    public BlockSelector() {
        float[] lines = createWireframeCube();

        outlineMesh = new MeshBorder(lines);

    }

    public void setPos(Vector3i Pos) {
        this.selectedPos.set(Pos);
    }

    private float[] createWireframeCube() {
    return new float[]{
        // Base (Y = 0)
        0, 0, 0,   1, 0, 0,  // Línea 1: (0,0,0) → (1,0,0)
        1, 0, 0,   1, 0, 1,  // Línea 2: (1,0,0) → (1,0,1)
        1, 0, 1,   0, 0, 1,  // Línea 3: (1,0,1) → (0,0,1)
        0, 0, 1,   0, 0, 0,  // Línea 4: (0,0,1) → (0,0,0)

        // Techo (Y = 1)
        0, 1, 0,   1, 1, 0,  // Línea 5: (0,1,0) → (1,1,0)
        1, 1, 0,   1, 1, 1,  // Línea 6: (1,1,0) → (1,1,1)
        1, 1, 1,   0, 1, 1,  // Línea 7: (1,1,1) → (0,1,1)
        0, 1, 1,   0, 1, 0,  // Línea 8: (0,1,1) → (0,1,0)

        // Aristas verticales
        0, 0, 0,   0, 1, 0,  // Línea 9:  (0,0,0) → (0,1,0)
        1, 0, 0,   1, 1, 0,  // Línea 10: (1,0,0) → (1,1,0)
        1, 0, 1,   1, 1, 1,  // Línea 11: (1,0,1) → (1,1,1)
        0, 0, 1,   0, 1, 1   // Línea 12: (0,0,1) → (0,1,1)
    };
}

    public void render(Vector3f pos, Matrix4f projection, Matrix4f view, ShaderProgram shader) {
        shader.bind();

        Matrix4f model = new Matrix4f().translate(pos).scale(1.f);
        Matrix4f mvp = new Matrix4f(projection).mul(view).mul(model);

        shader.setUniformMat4("mvp", mvp);
        
        glBindVertexArray(outlineMesh.getVaoId());
        glEnableVertexAttribArray(0);
        
        glLineWidth(3.0f);
        
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glEnable(GL_POLYGON_OFFSET_LINE);
        
        outlineMesh.render();
        
        glDisable(GL_POLYGON_OFFSET_LINE);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        
        shader.unbind();
    }

    public void cloanUp() {
        outlineMesh.cleanup();
    }
}
