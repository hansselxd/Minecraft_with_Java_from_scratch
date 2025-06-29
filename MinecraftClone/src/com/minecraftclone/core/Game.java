/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import static com.minecraftclone.core.Block.*;
import static com.minecraftclone.core.Chunk.terrainHeight;
import static com.minecraftclone.core.WorldManager.worldFolder;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game {

    private long window;
    private int width;
    private int height;
    private String title;
    public WorldManager worldManager;

    private Camera camera;
    private ShaderProgram shaderProgram;
    private ShaderProgram selectorShader;
    private Cube cubePreview;

    //Fuente
    private TextRenderer textRenderer;

    //TEXTURAAAAS
    private Texture grassT;
    private Texture dirtT;
    private Texture stoneT;

    //BLOQUES DE STEVE
    private final Block[] availableBlocks = {Block.GRASS, Block.DIRT, Block.STONE};
    private int selectedBlockIndex = 0;
    private Block selectedBlock = availableBlocks[selectedBlockIndex];

    private float mouseSensitivity = 0.1f;
    private float movementSpeed = 5f;

    private float floatingCheckTimer = 0;
    public static float gravityDelayTime = 1.0f;
    public static final float GRAVITY_COUNTDOWN_TIME = 0.25f;

    private final List<Vector3i> fallingBlocks = new ArrayList<>();

    //Click x default
    private boolean leftClickPressed = false;
    private boolean rightClickPressed = false;
    private boolean leftClickHeld = false;
    private float destroyCountDown = 0f;
    private float placeCountDown = 0f;
    private float fallCountDown = 0f;
    private final float DESTROY_COUNTDOWN_TIME = 0.25f;
    private final float PLACE_COUNTDOWN_TIME = 0.25f;
    private final float FALL_COUNTDOWN_TIME = 0.20f;

    private BlockSelector blockSelector;

    private ChunkLoaderThread chunkLoader;

    // Para movimiento del mouse
    private double lastMouseX = width / 2.0;
    private double lastMouseY = height / 2.0;
    private boolean firstMouse = true;
    private float scrollOffsetY = 0;

    private Vector3f velocity = new Vector3f(0, 0, 0);
    private final float gravity = -30;
    private final float jumpForce = 10f;
    private boolean isOnGround = false;
    
    public Game(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void run() throws Exception {
        init();
        loop();
        cleanup();
    }

    private void init() throws Exception {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = GLFW.glfwCreateWindow(width, height,title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("No se pudo crear la ventana GLFW");
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.53f, 0.8f, 0.92f, 1.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glViewport(0, 0, width, height);

        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            if (yoffset > 0) {
                selectedBlockIndex = (selectedBlockIndex + 1) % availableBlocks.length;
            } else if (yoffset < 0) {
                selectedBlockIndex = (selectedBlockIndex - 1 + availableBlocks.length) % availableBlocks.length;
            }

            selectedBlock = availableBlocks[selectedBlockIndex];
            textRenderer.updateText(selectedBlock.getType().name());
            System.out.println("Bloque seleccionado (scroll): " + selectedBlock.getType());
        });

        camera = new Camera();
        blockSelector = new BlockSelector();
        shaderProgram = new ShaderProgram("res/shaders/vertex.glsl", "res/shaders/fragment.glsl");
        selectorShader = new ShaderProgram("res/shaders/selector_vertex.glsl", "res/shaders/selector_fragment.glsl");
        grassT = new Texture("res/textures/grass_texture.png");
        dirtT = new Texture("res/textures/dirt_texture.png");
        stoneT = new Texture("res/textures/stone_texture.png");
        cubePreview = new Cube();

        // Font font = new Font("Arial", Font.BOLD, 32);
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/MinecraftRegular-Bmg3.otf")).deriveFont(32f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
        } catch (Exception e) {
            e.printStackTrace();
            font = new Font("Arial", Font.BOLD, 32); // fallback
        }

        textRenderer = new TextRenderer(selectedBlock.getType().toString(), font, Color.WHITE);

        // 1) Crear el WorldManager
        worldManager = new WorldManager(camera, shaderProgram, grassT, dirtT, stoneT);

        camera.setPosition(8f, 8.1f, 8f);

        isOnGround = true;

        //2) **Crear SINCRÓNICAMENTE el chunk base (0,0,0) para que nunca caigamos al vacío**:
        Chunk baseChunk = new Chunk(0, 0, 0);

        //Confirmar si existe ya y cargarlo en ese caso:
        File file = new File(worldFolder, "0,0,0.dat");
        if (file.exists()) {

            try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {

                // Inicializar todo con AIR primero para evitar nulos
                for (int x = 0; x < Chunk.SIZEx; x++) {
                    for (int y = terrainHeight; y < Chunk.SIZEy; y++) {
                        for (int z = 0; z < Chunk.SIZEz; z++) {
                            baseChunk.setBlock(x, y, z, Block.AIR);
                        }
                    }
                }

                int layerCount = in.readInt(); // cuántas capas hay

                for (int i = 0; i < layerCount; i++) {
                    int y = in.readInt(); // índice de la capa

                    for (int x = 0; x < Chunk.SIZEx; x++) {
                        for (int z = 0; z < Chunk.SIZEz; z++) {
                            int id = in.readInt();
                            Block b = Block.getBlockById(id);
                            baseChunk.setBlock(x, y, z, b);
                        }
                    }
                }

                baseChunk.buildMesh();     // crea VAO/VBO para dibujar
                String key = baseChunk.getKey();
                worldManager.chunks.put(key, baseChunk);  // mete el chunk base en el mapa de chunks
            }

        } else { //no existe, lo hacemos nuevo 

            baseChunk.generate();
            baseChunk.buildMesh();     // crea VAO/VBO para dibujar
            String key = baseChunk.getKey();
            worldManager.chunks.put(key, baseChunk);  // mete el chunk base en el mapa de chunks    
        }

        // 4) Ahora sí creo y arranco el hilo para cargar el resto de chunks en segundo plano
        chunkLoader = new ChunkLoaderThread(worldManager);
        worldManager.setChunkLoader(chunkLoader);

        MeshBuilderThread meshThread = new MeshBuilderThread(worldManager);
        worldManager.setMeshBuilder(meshThread);

        chunkLoader.start();

        worldManager.startThreads();
    }

    private void detectFloatingBlocksInCurrentChunk() {
        Vector3f camPos = camera.getPosition();

        int chunkX = Math.floorDiv((int) camPos.x, Chunk.SIZEx);
        int chunkY = Math.floorDiv((int) camPos.y, Chunk.SIZEy);
        int chunkZ = Math.floorDiv((int) camPos.z, Chunk.SIZEz);

        Chunk chunk = worldManager.getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) {
            return;
        }

        for (int x = 0; x < Chunk.SIZEx; x++) {
            for (int y = 0; y < Chunk.SIZEy; y++) {
                for (int z = 0; z < Chunk.SIZEz; z++) {
                    Block blockId = chunk.getBlock(x, y, z);
                    if (blockId == AIR) {
                        continue;
                    }

                    int worldX = chunkX * Chunk.SIZEx + x;
                    int worldY = chunkY * Chunk.SIZEy + y;
                    int worldZ = chunkZ * Chunk.SIZEz + z;

                    if (!worldManager.hasSupport(worldX, worldY, worldZ)) {
                        Vector3i pos = new Vector3i(worldX, worldY, worldZ);
                        if (!fallingBlocks.contains(pos)) {
                            fallingBlocks.add(pos);
                        }
                    }
                }
            }
        }
    }

    private void applyGravity(float deltaTime) {
        if (gravityDelayTime > 0) {
            gravityDelayTime -= deltaTime;
            return;
        }
        
        Block check = worldManager.getBlockIfLoader((int)camera.getPosition().x, (int) Math.floor(camera.getPosition().y), (int) camera.getPosition().z);

        if (!isOnGround) {
            velocity.y += gravity * deltaTime;
        }
        
        if (check.isSolid()) {
            isOnGround = true;
            velocity.y = 0;

            // Ajustamos para evitar flotar o atravesar el suelo
            float alignedY = (float) Math.floor(camera.getPosition().y);
            camera.setPosition(camera.getPosition().x, alignedY, camera.getPosition().z);
            return;
        }

        Vector3f nextPos = new Vector3f(camera.getPosition());
        nextPos.y += velocity.y * deltaTime;

        if (collidesWithWorldY(nextPos)) {
            if (velocity.y < 0) {
                isOnGround = true;
                velocity.y = 0;

                // Ajustamos para evitar flotar o atravesar el suelo
                float alignedY = (float) Math.floor(camera.getPosition().y);
                camera.setPosition(camera.getPosition().x, alignedY, camera.getPosition().z);
            }
        } else {
            camera.setPosition(camera.getPosition().x, nextPos.y, camera.getPosition().z);
            isOnGround = false;
        }

    }

    private void loop() throws Exception {
        float lastTime = (float) glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            float currentTime = (float) glfwGetTime();
            float deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            glfwPollEvents();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            handleMouse();
            handleKeyboard(deltaTime);
            applyGravity(deltaTime);

            RaycastResult selectedblock = RaycastUtils.raycast(worldManager, camera.getEyePosition(), camera.getFront(), 5.0f);
            if (selectedblock != null) {

                blockSelector.render(selectedblock.blockPos, camera.getProjectionMatrix(70, (float) width / height, 0.01f, 1000f),
                        camera.getViewMatrix(), selectorShader);

                System.out.println("Apuntas al bloque: " + selectedblock.blockPos + " cara: " + selectedblock.faceNormal);

                Vector3f blockPos = selectedblock.blockPos;
                Vector3f normal = selectedblock.faceNormal;

                destroyCountDown -= deltaTime;
                placeCountDown -= deltaTime;

                // Clic izquierdo = destruir bloque
                boolean isLeftPressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
                if (destroyCountDown <= 0 && isLeftPressed) {
                    worldManager.setBlockIfChunkExists((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, Block.AIR);// 0 = aire

                    destroyCountDown = DESTROY_COUNTDOWN_TIME;
                }
                leftClickPressed = isLeftPressed;

                // Clic derecho = colocar bloque al lado opuesto de la cara
                boolean isRightPressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;
                if (placeCountDown <= 0 && isRightPressed) {
                    int x = (int) (blockPos.x + normal.x);
                    int y = (int) (blockPos.y + normal.y);
                    int z = (int) (blockPos.z + normal.z);

                    Vector3f placement = new Vector3f(x, y, z);
                    Vector3f playerPos = camera.getPosition();

                    float playerSize = Camera.PLAYER_WIDTH; // anchura del jugador
                    float playerHeight = Camera.PLAYER_HEIGHT;

                    boolean insidePlayer = placement.x + 1 > playerPos.x - playerSize / 2f
                            && placement.x < playerPos.x + playerSize / 2f
                            && placement.y + 1 > playerPos.y
                            && placement.y < playerPos.y + playerHeight
                            && placement.z + 1 > playerPos.z - playerSize / 2f
                            && placement.z < playerPos.z + playerSize / 2f;

                    //gravityDelayTime = GRAVITY_COUNTDOWN_TIME;
                    if (!insidePlayer) {
                        worldManager.setBlockIfChunkExists(x, y, z, selectedBlock);
                    }

                    placeCountDown = PLACE_COUNTDOWN_TIME;

                }
                rightClickPressed = isRightPressed;

                floatingCheckTimer += deltaTime;
                if (floatingCheckTimer < 0.5f) {
                    detectFloatingBlocksInCurrentChunk();
                    floatingCheckTimer = 0;
                }

            }

            Iterator<Vector3i> it = fallingBlocks.iterator();
            while (it.hasNext()) {
                Vector3i pos = it.next();
                Block bellow = worldManager.getBlockIfLoader(pos.x, pos.y - 1, pos.z);
                Block act = worldManager.getBlockIfLoader(pos.x, pos.y, pos.z);

                fallCountDown -= deltaTime;
                if (fallCountDown <= 0) {
                    if (bellow == AIR) {
                        worldManager.setBlock(pos.x, pos.y, pos.z, AIR);
                        worldManager.setBlock(pos.x, pos.y - 1, pos.z, act);
                        pos.y -= 1;

                    } else {
                        it.remove();
                    }
                    fallCountDown = FALL_COUNTDOWN_TIME;
                }
            }

            //Integrar chunks
            worldManager.integrateLoadedChunks();
            //UpdateChunks
            worldManager.updateChunks(camera.getPosition());
            // Renderizar chunks
            worldManager.render();

            //Bloque seleccionado en pantalla
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            glOrtho(0, width, height, 0, -1, 1); // coordenadas en píxeles
            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            glLoadIdentity();

            textRenderer.render(20, 20); // dibuja en pantalla

            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);

            //Cubo seleccionado imagen
            Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45), (float) width / height, 0.1f, 100f);
            Matrix4f view = new Matrix4f()
                    .translate(0, 0, -3f); // Cámara que apunta a (0,0,0) desde Z = -3

            //cube en pantalla
            cubePreview.setRotation(new Vector3f(30f, currentTime * 50f, 0f)); // Gira suavemente

            cubePreview.render(selectedBlock.getTexture(), projection,
                    view, shaderProgram);

            glfwSwapBuffers(window);

            System.out.println("IsOnGround: " + isOnGround);
        }
    }

    private void handleMouse() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer xpos = stack.mallocDouble(1);
            DoubleBuffer ypos = stack.mallocDouble(1);
            glfwGetCursorPos(window, xpos, ypos);

            double x = xpos.get(0);
            double y = ypos.get(0);

            if (firstMouse) {
                lastMouseX = x;
                lastMouseY = y;
                firstMouse = false;
            }

            float xoffset = (float) (x - lastMouseX);
            float yoffset = (float) (y - lastMouseY);

            lastMouseX = x;
            lastMouseY = y;

            camera.processMouseMovement(xoffset, yoffset, mouseSensitivity);

            if (scrollOffsetY != 0) {
                int direction = (int) Math.signum(scrollOffsetY);
                scrollOffsetY = 0; // Reiniciamos tras usarlo

                selectedBlockIndex = (selectedBlockIndex + direction + availableBlocks.length) % availableBlocks.length;
                selectedBlock = availableBlocks[selectedBlockIndex];
                System.out.println("Bloque seleccionado: " + selectedBlock.getType());
            }
        }
    }

    private void handleKeyboard(float deltaTime) {
        if (gravityDelayTime > 0) {
            gravityDelayTime -= deltaTime;
            return;
        }
        float speed = movementSpeed * deltaTime;
        Vector3f foward = new Vector3f(camera.getFront()).setComponent(1, 0).normalize();
        Vector3f right = new Vector3f(camera.getRight()).setComponent(1, 0).normalize();

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            move(foward, speed);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            move(foward.negate(), speed);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            move(right.negate(), speed);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            move(right, speed);
        }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && isOnGround) {
            velocity.y = jumpForce;
            isOnGround = false;
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            move(new Vector3f(0, -1, 0), speed);
        }
    }

    private void move(Vector3f offset, float speed) {
        Vector3f proposedPos = new Vector3f(camera.getPosition()).add(offset.mul(speed));
        if (!collidesWithWorld(proposedPos)) {
            camera.setPosition(proposedPos.x, proposedPos.y, proposedPos.z);
        }
    }

    private boolean collidesWithWorld(Vector3f pos) {
        Vector3f min = new Vector3f(
                pos.x - Camera.PLAYER_WIDTH / 2f,
                pos.y,
                pos.z - Camera.PLAYER_WIDTH / 2f
        );
        Vector3f max = new Vector3f(
                pos.x + Camera.PLAYER_WIDTH / 2f,
                pos.y + Camera.PLAYER_HEIGHT,
                pos.z + Camera.PLAYER_WIDTH / 2f
        );

        // Debug
        System.out.println("Colisión check de AABB entre " + min + " y " + max);

        for (int x = (int) Math.floor(min.x); x <= Math.floor(max.x); x++) {
            for (int y = (int) Math.floor(min.y); y <= Math.floor(max.y); y++) {
                for (int z = (int) Math.floor(min.z); z <= Math.floor(max.z); z++) {
                    Block block = worldManager.getBlockIfLoader(x, y, z);
                    if (block != null && block.isSolid()) {
                        System.out.println("COLISIÓN DETECTADA en: (" + x + "," + y + "," + z + ")");
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    private boolean collidesWithWorldY(Vector3f pos) {
        Vector3f min = new Vector3f(
                pos.x - Camera.PLAYER_WIDTH / 2f,
                pos.y,
                pos.z - Camera.PLAYER_WIDTH / 2f
        );
        Vector3f max = new Vector3f(
                pos.x + Camera.PLAYER_WIDTH / 2f,
                pos.y + Camera.PLAYER_HEIGHT,
                pos.z + Camera.PLAYER_WIDTH / 2f
        );

        // Debug
        System.out.println("Colisión check de AABB entre " + min + " y " + max);

        for (int x = (int) Math.floor(min.x); x <= Math.floor(max.x); x++) {
            for (int y = (int) Math.floor(min.y - 0.01f); y <= Math.floor(max.y); y++) {
                for (int z = (int) Math.floor(min.z); z <= Math.floor(max.z); z++) {
                    Block block = worldManager.getBlockIfLoader(x, y, z);
                    if (block != null && block.isSolid()) {
                        System.out.println("COLISIÓN DETECTADA en: (" + x + "," + y + "," + z + ")");
                        return true;
                    }
                }
            }
        }

        return false;
    }



    private void cleanup() {
        worldManager.cleanup();
        shaderProgram.cleanup();

        textRenderer.cleanup();

        grassT.cleanup();
        dirtT.cleanup();
        stoneT.cleanup();

        chunkLoader.terminate();
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

}
