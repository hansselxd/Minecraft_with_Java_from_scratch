/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Cube {

    private final Mesh mesh;
    private Vector3f rotation = new Vector3f();
    private Texture texture;
    private float[] vertices;

    public Cube() throws Exception {
        vertices = new float[]{
            // Cara frontal (Columna 1, Fila 0)
            -0.5f, 0.5f, 0.5f, 0f, -1f, 0f, 0.33f, 0.00f,1.0f,1.0f,1.0f,
            -0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 0.33f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 0.66f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 0.66f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, 0.5f, 0.5f, 0f, -1f, 0f, 0.66f, 0.00f,1.0f,1.0f,1.0f,
            -0.5f, 0.5f, 0.5f, 0f, -1f, 0f, 0.33f, 0.00f,1.0f,1.0f,1.0f,
            // Cara trasera (Columna 1, Fila 1)
            0.5f, 0.5f, -0.5f, 0f, 0f, -1f, 0.66f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, -0.5f, -0.5f, 0f, 0f, -1f, 0.66f, 1.00f,1.0f,1.0f,1.0f,
            -0.5f, -0.5f, -0.5f, 0f, 0f, -1f, 0.33f, 1.00f,1.0f,1.0f,1.0f,
            -0.5f, -0.5f, -0.5f, 0f, 0f, -1f, 0.33f, 1.00f,1.0f,1.0f,1.0f,
            -0.5f, 0.5f, -0.5f, 0f, 0f, -1f, 0.33f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, 0.5f, -0.5f, 0f, 0f, -1f, 0.66f, 0.50f,1.0f,1.0f,1.0f,
            // Cara izquierda (Columna 0, Fila 1)
            -0.5f, 0.5f, -0.5f, -1f, 0f, 0f, 0.00f, 0.50f,1.0f,1.0f,1.0f,
            -0.5f, -0.5f, -0.5f, -1f, 0f, 0f, 0.00f, 1.00f,1.0f,1.0f,1.0f,
            -0.5f, -0.5f, 0.5f, -1f, 0f, 0f, 0.33f, 1.00f,1.0f,1.0f,1.0f,
            -0.5f, -0.5f, 0.5f, -1f, 0f, 0f, 0.33f, 1.00f,1.0f,1.0f,1.0f,
            -0.5f, 0.5f, 0.5f, -1f, 0f, 0f, 0.33f, 0.50f,1.0f,1.0f,1.0f,
            -0.5f, 0.5f, -0.5f, -1f, 0f, 0f, 0.00f, 0.50f,1.0f,1.0f,1.0f,
            // Cara derecha (Columna 2, Fila 1)
            0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 0.66f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, -0.5f, 0.5f, 1f, 0f, 0f, 0.66f, 1.00f,1.0f,1.0f,1.0f,
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1.00f, 1.00f,1.0f,1.0f,1.0f,
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1.00f, 1.00f,1.0f,1.0f,1.0f,
            0.5f, 0.5f, -0.5f, 1f, 0f, 0f, 1.00f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 0.66f, 0.50f,1.0f,1.0f,1.0f,
            // Cara superior (Columna 0, Fila 0)
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0.00f, 0.00f,1.0f,1.0f,1.0f,
            -0.5f, 0.5f, 0.5f, 0f, 1f, 0f, 0.00f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, 0.5f, 0.5f, 0f, 1f, 0f, 0.33f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, 0.5f, 0.5f, 0f, 1f, 0f, 0.33f, 0.50f,1.0f,1.0f,1.0f,
            0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0.33f, 0.00f,1.0f,1.0f,1.0f,
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0.00f, 0.00f,1.0f,1.0f,1.0f,
            // Cara inferior (Columna 2, Fila 0)
            -0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 0.66f, 0.50f,1.0f,1.0f,1.0f,
            -0.5f, -0.5f, -0.5f, 0f, -1f, 0f, 0.66f, 0.00f,1.0f,1.0f,1.0f,
            0.5f, -0.5f, -0.5f, 0f, -1f, 0f, 1.00f, 0.00f,1.0f,1.0f,1.0f,
            0.5f, -0.5f, -0.5f, 0f, -1f, 0f, 1.00f, 0.00f,1.0f,1.0f,1.0f,
            0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 1.00f, 0.50f,1.0f,1.0f,1.0f,
            -0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 0.66f, 0.50f,1.0f,1.0f,1.0f
        };

        mesh = new Mesh(vertices);
    }

    public void setRotation(Vector3f rotation) {
        this.rotation.set(rotation);
    }

    public void render(Texture texture, Matrix4f projection, Matrix4f view, ShaderProgram shader) {
        this.texture = texture;
        shader.bind();

        Matrix4f model = new Matrix4f()
                .translate(-1.7f, 0.7f, 0f)
                .scale(0.1f)
                .rotateLocalY(0.35f)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z));

        shader.setUniformMat4("model", model);
        shader.setUniformMat4("view", view);
        shader.setUniformMat4("projection", projection);

        glActiveTexture(GL_TEXTURE0);
        this.texture.bind();
        shader.setUniform1i("textureSampler", 0);

        mesh.render();

        shader.unbind();
    }

    public float[] getVErt() {
        return vertices;
    }

    public void cleanup() {
        mesh.cleanup();
    }
}
