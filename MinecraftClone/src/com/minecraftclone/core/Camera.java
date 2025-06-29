/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Math;

public class Camera {

    private Vector3f position;
    private float pitch; // Rotación vertical
    private float yaw;   // Rotación horizontal
    private final Vector3f front;
    private final Vector3f up;
    private final Vector3f right;
    private final Vector3f worldUp;

    public static final float PLAYER_WIDTH = 0.4f;
    public static final float PLAYER_HEIGHT = 1.8f;

    public Camera() {
        position = new Vector3f();
        pitch = 0.0f;
        yaw = -90.0f; // mirando hacia adelante (eje -Z)
        front = new Vector3f(0, 0, -1);
        up = new Vector3f(0, 1, 0);
        right = new Vector3f(1, 0, 0);
        worldUp = new Vector3f(0, 1, 0);
        updateCameraVectors();
    }

    public Matrix4f getViewMatrix() {
        Vector3f eye = new Vector3f(position).add(0,1.6f,0); //posicion cabeza
        return new Matrix4f().lookAt(eye, new Vector3f(eye).add(front),new Vector3f(0,1,0));
    }

    public Matrix4f getProjectionMatrix(float fov, float aspect, float near, float far) {
        return new Matrix4f().perspective((float) Math.toRadians(fov), aspect, near, far);
    }

    public void processKeyboard(Vector3f direction, float speed) {
        Vector3f velocity = new Vector3f(direction).mul(speed);
        position.add(velocity);
    }

    public void processMouseMovement(float xoffset, float yoffset, float sensitivity) {
        yaw += xoffset * sensitivity;
        pitch -= yoffset * sensitivity;

        // Limitar la inclinación vertical
        if (pitch > 89.0f) {
            pitch = 89.0f;
        }
        if (pitch < -89.0f) {
            pitch = -89.0f;
        }

        updateCameraVectors();
    }

    private void updateCameraVectors() {
        // Calcula la dirección del frente con yaw/pitch
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.normalize();

        right.set(front).cross(worldUp).normalize();
        up.set(right).cross(front).normalize();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getFront() {
        return front;
    }

    public Vector3f getRight() {
        return right;
    }

    public Vector3f getUp() {
        return up;
    }

    public void moveForward(float speed) {
        position.add(new Vector3f(front).mul(speed));
    }

    public void moveBackward(float speed) {
        position.sub(new Vector3f(front).mul(speed));
    }

    public void moveLeft(float speed) {
        position.sub(new Vector3f(right).mul(speed));
    }

    public void moveRight(float speed) {
        position.add(new Vector3f(right).mul(speed));
    }

    public void moveUp(float speed) {
        position.add(new Vector3f(worldUp).mul(speed));
    }

    public void moveDown(float speed) {
        position.sub(new Vector3f(worldUp).mul(speed));
    }

    public Vector3f getBoundingBoxMin() {
        return new Vector3f(
                position.x - PLAYER_WIDTH / 2f,
                position.y,
                position.z - PLAYER_WIDTH / 2f
        );
    }

    public Vector3f getBoundingBoxMax() {
        return new Vector3f(
                position.x + PLAYER_WIDTH / 2f,
                position.y + PLAYER_HEIGHT,
                position.z + PLAYER_WIDTH / 2f
        );
    }
    
    public void setPosition(float x, float y, float z){
        position = new Vector3f(x, y, z);
    }
    
    public Vector3f getEyePosition(){
        return new Vector3f(position).add(0,1.6f,0);
    }
}
