/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 *
 * @author hanss
 */
public class BlockPreviewRenderer {
    private final CubeSelected cube;
    private final ShaderProgram shader;

    public BlockPreviewRenderer(ShaderProgram shader) throws Exception {
        this.shader = shader;
        this.cube = new CubeSelected(); // la misma clase usada para pruebas
    }

    public void render(Texture texture, int x, int y, int width, int height, int windowWidth, int windowHeight) {
        Matrix4f projection = new Matrix4f().ortho(0, windowWidth, windowHeight, 0, -1, 1);
        Matrix4f view = new Matrix4f(); // identidad
        Matrix4f model = new Matrix4f().translate(x, y, 0).scale(width, height, 1f);

        shader.bind();
        shader.setUniformMat4("projection", projection);
        shader.setUniformMat4("view", view);
        shader.setUniformMat4("model", model);

        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        shader.setUniform1i("textureSampler", 0);

        cube.render();

        shader.unbind();
    }

    public void cleanup() {
        cube.cleanup();
    }
}
