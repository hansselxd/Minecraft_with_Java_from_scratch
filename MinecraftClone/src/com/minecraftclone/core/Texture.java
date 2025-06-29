/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture {

    private final int id;

    public Texture(String fileName) throws Exception {
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        // Parámetros de textura (filtrado y wrap)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Cargar imagen
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            // La ruta debe ser relativa al directorio del proyecto o ruta absoluta
            ByteBuffer image = STBImage.stbi_load(fileName, width, height, channels, 4);
            if (image == null) {
                throw new Exception("Failed to load a texture file!" + System.lineSeparator() + STBImage.stbi_failure_reason());
            }

            int texWidth = width.get(0);
            int texHeight = height.get(0);
            System.out.println("Texture loaded: " + texWidth + "x" + texHeight);

            // Subir textura a OpenGL
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, image);

            STBImage.stbi_image_free(image);
        }
    }

    public int getId() {
        return id;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void cleanup() {
        glDeleteTextures(id);
    }
}
