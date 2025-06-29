/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.system.MemoryStack;

public class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertexPath, String fragmentPath) {
        programId = glCreateProgram();

        int vertexShader = createShader(vertexPath, GL_VERTEX_SHADER);
        int fragmentShader = createShader(fragmentPath, GL_FRAGMENT_SHADER);

        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error al vincular programa: " + glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int createShader(String path, int type) {
        String code = "";
        try {
            code = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, code);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error en shader " + path + ": " + glGetShaderInfoLog(shaderId));
        }

        return shaderId;
    }

    public void use() {
        glUseProgram(programId);
    }

    public void setUniformMat4(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }

    public void setUniform3f(String name, Vector3f value) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform3f(location, value.x, value.y, value.z);
        } else {
            System.err.println("Uniform not found: " + name);
        }
    }

    
    public void setUniform1i(String name, int value) {
        int location = glGetUniformLocation(programId, name);
        if (location == -1) {
            System.err.println("Warning: uniform '" + name + "' doesn't exist!");
            return;
        }
        glUniform1i(location, value);
    }


    public void bind() {
    glUseProgram(programId);
    }
    
    public void unbind() {
    glUseProgram(0);
    }


    public void cleanup() {
        glDeleteProgram(programId);
    }
}

