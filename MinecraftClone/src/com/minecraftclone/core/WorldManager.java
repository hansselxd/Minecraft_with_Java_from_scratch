package com.minecraftclone.core;

import static com.minecraftclone.core.Block.*;
import static com.minecraftclone.core.Chunk.terrainHeight;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class WorldManager {

    // Mapa para almacenar chunks con clave: "chunkX,chunkY,chunkZ"
    final Map<String, Chunk> chunks = new HashMap<>();
    private static final int LOAD_RADIUS = 2;
    public Camera camera;
    private int cargaDeRadio = LOAD_RADIUS;

    public static File getWorldFolder() {
        return worldFolder;
    }
    private final ShaderProgram shader;

    //TEXTURAAAAS
    private Texture grass;
    private Texture dirt;
    private Texture stone;

    //Colas de hilos
    protected static final ConcurrentLinkedQueue<Chunk> chunksToAdd = new ConcurrentLinkedQueue<>();
    protected static final ConcurrentLinkedQueue<Chunk> chunksToMesh = new ConcurrentLinkedQueue<>();
    protected static final ConcurrentLinkedQueue<Chunk> chunksToIntegrate = new ConcurrentLinkedQueue<>();

    public static final File worldFolder = new File("world");
    private ChunkLoaderThread chunkLoader;
    private ChunkSaveThread saveThread;
    private MeshBuilderThread meshBuilder;

    public static WorldManager instance;

    public WorldManager(Camera camera, ShaderProgram shader, Texture grass, Texture dirt, Texture stone) {
        this.camera = camera;
        instance = this;
        this.shader = shader;
        this.grass = grass;
        this.dirt = dirt;
        this.stone = stone;

        if (!worldFolder.exists()) {
            worldFolder.mkdirs();
        }
        saveThread = new ChunkSaveThread(this);

    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera c) {
        camera = c;
    }

    public void setMeshBuilder(MeshBuilderThread meshBuilder) {
        this.meshBuilder = meshBuilder;
    }

    /**
     * Obtiene un chunk por coordenadas de chunk, si no existe lo crea y genera.
     */
    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        String key = key(chunkX, chunkY, chunkZ);
        Chunk chunk = chunks.get(key);
        if (chunk == null) {
            chunk = new Chunk(chunkX, chunkY, chunkZ);
            chunk.generate(); // Genera el bloque con generación simple o procedural
            chunk.buildMesh();
            chunks.put(key, chunk);
        }
        return chunk;
    }

    public Map<String, Chunk> getChunks() {
        return chunks;
    }

    public void addChunk(Chunk chunk) {
        chunks.put(chunk.getKey(), chunk);
    }

    /**
     * Devuelve el bloque en coordenadas globales (x,y,z en bloques). Retorna 0
     * (aire) si fuera de chunks cargados.
     */
    public Block getBlockIfLoader(int x, int y, int z) {
        // 1) Calcular correctamente las coordenadas de chunk en X, Y, Z
        int chunkX = Math.floorDiv(x, Chunk.SIZEx);
        int chunkY = 0;
        int chunkZ = Math.floorDiv(z, Chunk.SIZEz);

        String key = chunkX + "," + chunkY + "," + chunkZ;
        Chunk chunk = chunks.get(key);

        if (chunk == null) {
            return AIR;
        }

        int localX = Math.floorMod(x, Chunk.SIZEx);
        int localY = Math.floorMod(y, Chunk.SIZEy);
        int localZ = Math.floorMod(z, Chunk.SIZEz);

        Block b = chunk.getBlock(localX, localY, localZ);
        if (b == null) {
            return AIR;
        }
        return b;
    }

    public Chunk getChunkWithoutMesh(String key) {
        return chunks.get(key);
    }

    /**
     * Establece un bloque en coordenadas globales. Reconstruye la malla del
     * chunk modificado.
     */
    public void setBlock(int x, int y, int z, Block blockId) {
        int chunkX = Math.floorDiv(x, Chunk.SIZEx);
        int chunkY = Math.floorDiv(y, Chunk.SIZEy);
        int chunkZ = Math.floorDiv(z, Chunk.SIZEz);

        Chunk chunk = getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) {
            return;
        }

        int localX = Math.floorMod(x, Chunk.SIZEx);
        int localY = Math.floorMod(y, Chunk.SIZEy);
        int localZ = Math.floorMod(z, Chunk.SIZEz);

        chunk.setBlock(localX, localY, localZ, blockId);

        // Reemplazo dinámico de GRASS por DIRT si se coloca un bloque encima
        if (blockId != Block.AIR) {
            int belowY = y - 1;
            if (belowY >= 0) {
                Block blockBelow = getBlockIfLoader(x, belowY, z);
                if (blockBelow == GRASS) {
                    setBlock(x, belowY, z, DIRT);
                }
            }
        }

        chunk.setDirty(true);
        chunk.buildMesh();

        saveThread.requestSave(chunk);
    }

    public void setBlockIfChunkExists(int x, int y, int z, Block blockId) {
        int chunkX = Math.floorDiv(x, Chunk.SIZEx);
        int chunkY = Math.floorDiv(y, Chunk.SIZEy);
        int chunkZ = Math.floorDiv(z, Chunk.SIZEz);

        String key = key(chunkX, chunkY, chunkZ);
        if (!chunks.containsKey(key)) {
            return; // No generes un chunk nuevo
        }
        Chunk chunk = chunks.get(key);
        int localX = Math.floorMod(x, Chunk.SIZEx);
        int localY = Math.floorMod(y, Chunk.SIZEy);
        int localZ = Math.floorMod(z, Chunk.SIZEz);

        chunk.setBlock(localX, localY, localZ, blockId);

        // Reemplazo dinámico de GRASS por DIRT si se coloca un bloque encima
        if (blockId != Block.AIR) {
            int belowY = y - 1;
            int upperY = y + 1;
            if (belowY >= 0) {
                Block blockBelow = getBlockIfLoader(x, belowY, z);
                if (blockBelow == GRASS) {
                    setBlock(x, belowY, z, DIRT);
                }
            }
            if (upperY >= 0) {
                Block blockUpper = getBlockIfLoader(x, upperY, z);
                if (blockUpper.isSolid() && blockUpper != null && blockId == GRASS) {
                    setBlock(x, y, z, DIRT);
                }
            }
        }
        // 1) Recalcula el skyLight porque cambia el “techo”:
        chunk.initSkyLight();
        chunk.setDirty(true);
        chunk.buildMesh();

        saveThread.requestSave(chunk);
    }

    //si tiene soporte
    public boolean hasSupport(int x, int y, int z) {
        return getBlockIfLoader(x, y - 1, z) != AIR
                || getBlockIfLoader(x + 1, y, z) != AIR
                || getBlockIfLoader(x - 1, y, z) != AIR
                || getBlockIfLoader(x, y, z + 1) != AIR
                || getBlockIfLoader(x, y, z - 1) != AIR;
    }

    //Limitar chunks
    public void updateChunks(Vector3f playerPos) {
        int playerChunkX = Math.floorDiv((int) playerPos.x, Chunk.SIZEx);
        int playerChunkZ = Math.floorDiv((int) playerPos.z, Chunk.SIZEz);

        Set<String> stillNeeded = new HashSet<>();
        List<int[]> toLoad = new ArrayList<>();

        for (int dx = -LOAD_RADIUS; dx <= LOAD_RADIUS; dx++) {
            for (int dz = -LOAD_RADIUS; dz <= LOAD_RADIUS; dz++) {
                int cx = playerChunkX + dx;
                int cz = playerChunkZ + dz;

                stillNeeded.add(key(cx, 0, cz));
                toLoad.add(new int[]{cx, cz});
            }
        }

        // Ordenar por distancia al jugador
        toLoad.sort(Comparator.comparingDouble(coord -> {
            float dx = playerChunkX - coord[0];
            float dz = playerChunkZ - coord[1];
            return dx * dx + dz * dz;
        }));

        // Encolar en orden de cercanía
        for (int[] coord : toLoad) {
            String k = key(coord[0], 0, coord[1]);
            if (!chunks.containsKey(k)) {
                chunkLoader.requestLoad(coord[0], coord[1]);
            }
        }

        // Descargar chunks fuera del radio
        chunks.keySet().removeIf(k -> {
            if (!stillNeeded.contains(k)) {
                saveChunk(chunks.get(k));
                chunks.get(k).cleanup();
                return true;
            }
            return false;
        });
    }

    public static List<Vector2i> getSpiralLoadOrder(int radius, int centerX, int centerZ) {
        List<Vector2i> result = new ArrayList<>();
        int x = 0, z = 0, dx = 0, dz = -1;
        int max = (radius * 2 + 1) * (radius * 2 + 1);

        for (int i = 0; i < max; i++) {
            if (Math.abs(x) <= radius && Math.abs(z) <= radius) {
                result.add(new Vector2i(centerX + x, centerZ + z));
            }

            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int tmp = dx;
                dx = -dz;
                dz = tmp;
            }

            x += dx;
            z += dz;
        }

        return result;
    }

    public void saveChunk(Chunk chunk) {
        saveThread.requestSave(chunk);
    }

    public void saveChunkImmediate(Chunk chunk) throws IOException {
        String key = chunk.getKey();                  // por ejemplo: "0,0,0"
        File fileTmp = new File(worldFolder, key + ".tmp");
        File fileDat = new File(worldFolder, key + ".dat");

        // 1) Crear el .tmp y escribir sólo las capas no vacías:
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(fileTmp))) {
            // 1.1) Detectar qué capas Y tienen al menos un bloque ≠ AIR
            List<Integer> nonEmptyLayers = new ArrayList<>();
            for (int y = 0; y < Chunk.SIZEy; y++) {
                boolean hasBlock = false;
                for (int x = 0; x < Chunk.SIZEx && !hasBlock; x++) {
                    for (int z = 0; z < Chunk.SIZEz && !hasBlock; z++) {
                        if (chunk.getBlock(x, y, z) != Block.AIR) {
                            hasBlock = true;
                        }
                    }
                }
                if (hasBlock) {
                    nonEmptyLayers.add(y);
                }
            }
            // 1.2) Escribir cuántas capas no vacías vamos a guardar
            out.writeInt(nonEmptyLayers.size());

            // 1.3) Por cada capa Y, escribir primero el índice Y, y luego todos los bloques de esa capa
            for (int y : nonEmptyLayers) {
                out.writeInt(y); // índice de la capa
                for (int x = 0; x < Chunk.SIZEx; x++) {
                    for (int z = 0; z < Chunk.SIZEz; z++) {
                        Block b = chunk.getBlock(x, y, z);
                        out.writeInt(Block.getBlockId(b));
                    }
                }
            }
            // El try-with-resources se encarga de cerrar 'out' aquí al salir del bloque
        }

        // 2) Una vez fuera del try, el flujo ya está cerrado. Ahora podemos renombrar.
        //    Primero borramos el .dat viejo (si existe)
        if (fileDat.exists()) {
            if (!fileDat.delete()) {
                System.err.println("No se pudo borrar el archivo antiguo: " + fileDat.getPath());
                // Si quieres, puedes abortar aquí o programar un retry.
            }
        }

        // 3) Finalmente renombrar .tmp → .dat
        if (!fileTmp.renameTo(fileDat)) {
            System.err.println("No se pudo renombrar " + fileTmp.getName() + " a " + fileDat.getName());
            // Opcional: podrías volver a intentar más tarde, o moverlo a otra carpeta “retry/”
        }
    }

    public boolean loadChunk(Chunk chunk) {

        String filename = chunk.getKey() + ".dat";
        File file = new File(worldFolder, filename);
        File tmp = new File(worldFolder, chunk.getKey() + ".tmp");
        if (!file.exists() || tmp.exists()) {
            return false;
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {

            // Inicializar todo con AIR primero para evitar nulos
            for (int x = 0; x < Chunk.SIZEx; x++) {
                for (int y = terrainHeight; y < Chunk.SIZEy; y++) {
                    for (int z = 0; z < Chunk.SIZEz; z++) {
                        chunk.setBlock(x, y, z, Block.AIR);
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
                        chunk.setBlock(x, y, z, b);
                        
                        chunk.initSkyLight();
                    }
                }
            }
            return true;

        } catch (IOException e) {
            System.err.println(" Error leyendo chunk " + chunk.getKey() + ": " + e.getMessage());
            return false;
        }
    }

    public Chunk pollChunkToMesh() {
        return chunksToMesh.poll();
    }

    public void loadOrGenerateChunk(int chunkX, int chunkZ) {
        String key = key(chunkX, 0, chunkZ);

        // Si ya está cargado en memoria, no hacemos nada.
        if (chunks.containsKey(key)) {
            return;
        }

        // Si ya se pidió su carga (o está en proceso), tampoco hacemos nada.
        if (chunkLoader.hasPendingRequest(chunkX, chunkZ)) {
            return;
        }

        //Reinicio para evitar errores de caida
        // Comprobación del chunk del jugador
        int playerChunkX = Math.floorDiv((int) camera.getPosition().x, Chunk.SIZEx);
        int playerChunkZ = Math.floorDiv((int) camera.getPosition().z, Chunk.SIZEz);

        // Si no, pedimos al hilo de carga que lo maneje
        chunkLoader.requestLoad(chunkX, chunkZ);

    }

    public void integrateLoadedChunks() {
        synchronized (chunksToAdd) {
            for (Chunk chunk : chunksToAdd) {
                if (chunk == null) {
                    continue;
                }

                //Reinicio para evitar errores de caida
                // Comprobación del chunk del jugador
                int playerChunkX = Math.floorDiv((int) camera.getPosition().x, Chunk.SIZEx);
                int playerChunkZ = Math.floorDiv((int) camera.getPosition().z, Chunk.SIZEz);

                //if (chunk.chunkX == playerChunkX && chunk.chunkZ == playerChunkZ) {
                //    Game.gravityDelayTime = Game.GRAVITY_COUNTDOWN_TIME;
                //}
                chunk.buildMesh(); // ahora sí con GL activo
                chunks.put(chunk.getKey(), chunk);
            }
            chunksToAdd.clear();
        }
    }

    public void setChunkLoader(ChunkLoaderThread loader) {
        this.chunkLoader = loader;
    }

    public void enqueueChunkToAdd(Chunk c) {
        synchronized (chunksToAdd) {
            chunksToAdd.add(c);
        }
    }

    public void enqueueChunkToMesh(Chunk c) {
        chunksToMesh.add(c);
    }

    public void enqueueChunkToIntegrate(Chunk c) {
        chunksToIntegrate.add(c);
    }

    /**
     * Renderiza todos los chunks cargados
     */
    public void render() {
        // 1) Calcula proyección y vista (o mantenlas en campos si no cambian cada frame)
        Matrix4f projection = camera.getProjectionMatrix(70, 1280f / 720f, 0.01f, 100f);
        Matrix4f view = camera.getViewMatrix();

        // 1.1) Actualiza el frustum
        Frustum frustum = new Frustum();
        frustum.update(projection, view);

        // 2) Bindea TU shader antes de setear uniforms
        shader.bind();
        // Ahora sí, OpenGL sabe que “viewPos” y “lightPos” van a este shader
        //shader.setUniform3f("viewPos", camera.getEyePosition());
        shader.setUniform3f("lightPos", new Vector3f(-0.5f, -1.0f, -0.5f).normalize());
        shader.setUniformMat4("view", view);
        shader.setUniformMat4("projection", projection);
        // (También podrías setear aquí las matrices view/projection si no lo haces dentro de Chunk.render)

        // 3) Por cada textura/tipo en orden: dibujamos todos los chunks
        //    que tengan malla de ese tipo y estén dentro del frustum.
        // 3.1) Césped (BlockType.GRASS)
        glActiveTexture(GL_TEXTURE0);
        grass.bind();               // un solo bind de textura "grass"
        shader.setUniform1i("textureSampler", 0);

        for (Chunk chunk : chunks.values()) {
            // Frustum culling
            float minX = chunk.chunkX * Chunk.SIZEx;
            float minY = chunk.chunkY * Chunk.SIZEy;
            float minZ = chunk.chunkZ * Chunk.SIZEz;
            float maxX = minX + Chunk.SIZEx;
            float maxY = minY + Chunk.SIZEy;
            float maxZ = minZ + Chunk.SIZEz;
            if (frustum.isBoxOutsideFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
                continue;
            }
            // Recuperamos solo la malla de césped
            Mesh grassMesh = chunk.getMeshByBlockType(BlockType.GRASS);
            if (grassMesh != null) {
                // Calculamos la matriz “model” para este chunk
                Matrix4f model = new Matrix4f().translate(minX, minY, minZ);
                shader.setUniformMat4("model", model);
                // Dibujamos únicamente el VAO de césped de este chunk
                grassMesh.render();
            }
        }

        // 3.2) Tierra (BlockType.DIRT)
        glActiveTexture(GL_TEXTURE0);
        dirt.bind();                // un solo bind de textura "dirt"
        shader.setUniform1i("textureSampler", 0);

        for (Chunk chunk : chunks.values()) {
            // (Reusar frustum resultaría en ligera sobrecarga si no lo guardaste;
            //  podrías extraer la lista de “chunks dentro del frustum” primero, pero aquí lo repetimos por claridad)
            float minX = chunk.chunkX * Chunk.SIZEx;
            float minY = chunk.chunkY * Chunk.SIZEy;
            float minZ = chunk.chunkZ * Chunk.SIZEz;
            float maxX = minX + Chunk.SIZEx;
            float maxY = minY + Chunk.SIZEy;
            float maxZ = minZ + Chunk.SIZEz;
            if (frustum.isBoxOutsideFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
                continue;
            }
            Mesh dirtMesh = chunk.getMeshByBlockType(BlockType.DIRT);
            if (dirtMesh != null) {
                Matrix4f model = new Matrix4f().translate(minX, minY, minZ);
                shader.setUniformMat4("model", model);
                dirtMesh.render();
            }
        }

        // 3.3) Piedra (BlockType.STONE)
        glActiveTexture(GL_TEXTURE0);
        stone.bind();               // un solo bind de textura "stone"
        shader.setUniform1i("textureSampler", 0);

        for (Chunk chunk : chunks.values()) {
            float minX = chunk.chunkX * Chunk.SIZEx;
            float minY = chunk.chunkY * Chunk.SIZEy;
            float minZ = chunk.chunkZ * Chunk.SIZEz;
            float maxX = minX + Chunk.SIZEx;
            float maxY = minY + Chunk.SIZEy;
            float maxZ = minZ + Chunk.SIZEz;
            if (frustum.isBoxOutsideFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
                continue;
            }
            Mesh stoneMesh = chunk.getMeshByBlockType(BlockType.STONE);
            if (stoneMesh != null) {
                Matrix4f model = new Matrix4f().translate(minX, minY, minZ);
                shader.setUniformMat4("model", model);
                stoneMesh.render();
            }
        }

        // 4) Desbindea el shader cuando termines
        shader.unbind();
    }

    private String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private static class Frustum {
        // Cada plano = (a, b, c, d) representa ax + by + cz + d = 0
        // Almacenamos cada plano normalizado.

        public final float[][] planes = new float[6][4];

        /**
         * Llenar `planes[0..5]` a partir de M = projection × view. plano 0 =
         * left, 1 = right, 2 = bottom, 3 = top, 4 = near, 5 = far.
         */
        public void update(Matrix4f projection, Matrix4f view) {
            // 1) Obtenemos M = projection × view en un solo FloatBuffer de 16 floats.
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            (new Matrix4f(projection)).mul(view).get(fb);
            float[] m = new float[16];
            fb.get(m);

            // m está en formato column-major:
            //   m[0]  m[4]  m[8]  m[12]
            //   m[1]  m[5]  m[9]  m[13]
            //   m[2]  m[6]  m[10] m[14]
            //   m[3]  m[7]  m[11] m[15]
            // Extraer los planos:
            // Left plane   =  row4 + row1
            planes[0][0] = m[3] + m[0];
            planes[0][1] = m[7] + m[4];
            planes[0][2] = m[11] + m[8];
            planes[0][3] = m[15] + m[12];
            normalizePlane(planes[0]);

            // Right plane  =  row4 - row1
            planes[1][0] = m[3] - m[0];
            planes[1][1] = m[7] - m[4];
            planes[1][2] = m[11] - m[8];
            planes[1][3] = m[15] - m[12];
            normalizePlane(planes[1]);

            // Bottom plane =  row4 + row2
            planes[2][0] = m[3] + m[1];
            planes[2][1] = m[7] + m[5];
            planes[2][2] = m[11] + m[9];
            planes[2][3] = m[15] + m[13];
            normalizePlane(planes[2]);

            // Top plane    =  row4 - row2
            planes[3][0] = m[3] - m[1];
            planes[3][1] = m[7] - m[5];
            planes[3][2] = m[11] - m[9];
            planes[3][3] = m[15] - m[13];
            normalizePlane(planes[3]);

            // Near plane   =  row4 + row3
            planes[4][0] = m[3] + m[2];
            planes[4][1] = m[7] + m[6];
            planes[4][2] = m[11] + m[10];
            planes[4][3] = m[15] + m[14];
            normalizePlane(planes[4]);

            // Far plane    =  row4 - row3
            planes[5][0] = m[3] - m[2];
            planes[5][1] = m[7] - m[6];
            planes[5][2] = m[11] - m[10];
            planes[5][3] = m[15] - m[14];
            normalizePlane(planes[5]);
        }

        /**
         * Normaliza (a,b,c,d) para que sqrt(a²+b²+c²)==1
         */
        private void normalizePlane(float[] plane) {
            float invLen = (float) (1.0 / Math.sqrt(plane[0] * plane[0] + plane[1] * plane[1] + plane[2] * plane[2]));
            plane[0] *= invLen;
            plane[1] *= invLen;
            plane[2] *= invLen;
            plane[3] *= invLen;
        }

        /**
         * Comprueba si el AABB dado (minX,minY,minZ) - (maxX,maxY,maxZ) queda
         * fuera de al menos uno de los planos → entonces está totalmente fuera.
         */
        public boolean isBoxOutsideFrustum(float minX, float minY, float minZ,
                float maxX, float maxY, float maxZ) {
            // Para cada plano: si todos los 8 vértices de la caja quedan "detrás" 
            // (distance < 0), la caja está fuera del frustum.
            for (int i = 0; i < 6; i++) {
                float a = planes[i][0], b = planes[i][1], c = planes[i][2], d = planes[i][3];
                // Verificamos los 8 puntos de la AABB:
                if (a * minX + b * minY + c * minZ + d > 0 // frente
                        || a * maxX + b * minY + c * minZ + d > 0
                        || a * minX + b * maxY + c * minZ + d > 0
                        || a * maxX + b * maxY + c * minZ + d > 0
                        || a * minX + b * minY + c * maxZ + d > 0
                        || a * maxX + b * minY + c * maxZ + d > 0
                        || a * minX + b * maxY + c * maxZ + d > 0
                        || a * maxX + b * maxY + c * maxZ + d > 0) {
                    // Al menos un vértice está frente a este plano → puede intersectar/estar dentro.
                    continue;
                }
                // Si llegamos aquí, ¡todos los vértices están detrás del plano i! → está fuera.
                return true;
            }
            return false;
        }
    }

    public void propagateBlockLight(int wx, int wy, int wz, int initialLevel) {
        // Convierte coordenadas globales a locales de chunk
        int cx = Math.floorDiv(wx, Chunk.SIZEx);
        int cz = Math.floorDiv(wz, Chunk.SIZEz);
        Chunk chunk = getChunk(cx, 0, cz);
        int lx = Math.floorMod(wx, Chunk.SIZEx);
        int ly = wy; // asumes solo altura 0..SIZEy-1
        int lz = Math.floorMod(wz, Chunk.SIZEz);

        chunk.blockLight[lx][ly][lz] = (byte) initialLevel;
        Queue<Vector3i> q = new ArrayDeque<>();
        q.add(new Vector3i(wx, wy, wz));

        while (!q.isEmpty()) {
            Vector3i p = q.remove();
            byte lvl = getBlockLight(p.x, p.y, p.z);
            for (Vector3i d : new Vector3i[]{
                new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0),
                new Vector3i(0, 1, 0), new Vector3i(0, -1, 0),
                new Vector3i(0, 0, 1), new Vector3i(0, 0, -1)
            }) {
                int nx = p.x + d.x, ny = p.y + d.y, nz = p.z + d.z;
                Chunk ch2 = getChunk(
                        Math.floorDiv(nx, Chunk.SIZEx),
                        0,
                        Math.floorDiv(nz, Chunk.SIZEz)
                );
                int lx2 = Math.floorMod(nx, Chunk.SIZEx),
                        ly2 = ny,
                        lz2 = Math.floorMod(nz, Chunk.SIZEz);
                if (ly2 < 0 || ly2 >= Chunk.SIZEy) {
                    continue;
                }

                Block neighbor = getBlockIfLoader(nx, ny, nz);
                int opacity = (neighbor == null || !neighbor.isSolid()) ? 0 : 1;
                byte nl = (byte) (lvl - Math.max(1, opacity));
                if (nl > ch2.blockLight[lx2][ly2][lz2]) {
                    ch2.blockLight[lx2][ly2][lz2] = nl;
                    q.add(new Vector3i(nx, ny, nz));
                }
            }
        }
    }

    // Métodos auxiliares:
    public byte getBlockLight(int wx, int wy, int wz) {
        Chunk c = getChunk(
                Math.floorDiv(wx, Chunk.SIZEx), 0,
                Math.floorDiv(wz, Chunk.SIZEz)
        );
        return c.blockLight[Math.floorMod(wx, Chunk.SIZEx)][wy][Math.floorMod(wz, Chunk.SIZEz)];
    }

    /**
     * Limpia todos los recursos de chunks
     */
    public void cleanup() {
        for (Chunk chunk : chunks.values()) {
            chunk.cleanup();
        }
        chunks.clear();
    }

    public void startThreads() {
        meshBuilder.start();
        saveThread.start();
    }

}
