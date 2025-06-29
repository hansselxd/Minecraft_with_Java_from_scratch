/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import static com.minecraftclone.core.Block.*;
import com.minecraftclone.core.CubeGenerator.Direction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.joml.Matrix4f;
import org.joml.Vector3i;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Chunk {

    // … campos existentes …
    private Mesh grassMesh;
    private Mesh dirtMesh;
    private Mesh stoneMesh;

    public final byte[][][] skyLight = new byte[SIZEx][SIZEy][SIZEz]; //Luz del sol o la luna
    public final byte[][][] blockLight = new byte[SIZEx][SIZEy][SIZEz]; //Luz emitida por bloques antorchas, lava, glowstone, etc.

    public static final int SIZEx = 16;
    public static final int SIZEy = 255;
    public static final int SIZEz = 16;

    public static final int terrainHeight = 8;

    private boolean dirty = false;

    public final int chunkX, chunkY, chunkZ;
    private final Block[][][] blocks = new Block[SIZEx][SIZEy][SIZEz];

    public Chunk(int chunkX, int chunkY, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
    }

    public byte[][][] getSkyLight() {
        return skyLight;
    }

    public byte[][][] getBlockLight() {
        return blockLight;
    }

    /**
     * Generación simple de bloques, por ahora piso sólido hasta y = 4
     */
    public void generate() {
        for (int x = 0; x < SIZEx; x++) {
            for (int y = 0; y < SIZEy; y++) {
                for (int z = 0; z < SIZEz; z++) {
                    if (y == terrainHeight) {
                        blocks[x][y][z] = Block.GRASS;
                    } else if (y < terrainHeight && y > 0) {
                        blocks[x][y][z] = DIRT;
                    } else if (y == 0) {
                        blocks[x][y][z] = STONE;
                    } else {
                        blocks[x][y][z] = AIR;
                    }
                }
            }
        }
        initSkyLight();
    }

    //Si el cubo esta de cara al cielo
    public boolean isExposedToSky(int x, int y, int z) {
        for (int ty = y + 1; ty < SIZEy; ty++) {
            if (blocks[x][ty][z] != null && blocks[x][ty][z].isSolid()) {
                return false; // hay un bloque encima
            }
        }
        return true;
    }

    /**
     * Propaga la luz del cielo de arriba hacia abajo, decreciendo según la
     * opacidad del bloque.
     */
    /**
     * Inicializa y propaga la luz del cielo en 3D: 1) Marca 15 en todos los
     * bloques expuestos al cielo. 2) BFS: desde esas fuentes, propaga hacia
     * vecinos restando 1 de nivel.
     */
    void initSkyLight() {
        // 1) Inicializar todo a 0
        for (int x = 0; x < SIZEx; x++) {
            for (int y = 0; y < SIZEy; y++) {
                for (int z = 0; z < SIZEz; z++) {
                    skyLight[x][y][z] = 0;
                }
            }
        }

        // 2) Cola para BFS
        Queue<Vector3i> queue = new ArrayDeque<>();

        // 3) Encuentra todas las fuentes de sky light (expuestas al cielo)
        for (int x = 0; x < SIZEx; x++) {
            for (int z = 0; z < SIZEz; z++) {
                for (int y = SIZEy - 1; y >= 0; y--) {
                    if (isExposedToSky(x, y, z)) {
                        skyLight[x][y][z] = 15;
                        queue.add(new Vector3i(x, y, z));
                    } else {
                        // una vez encuentras un bloque sólido deja de buscar más arriba
                        if (blocks[x][y][z] != null && blocks[x][y][z].isSolid()) {
                            break;
                        }
                    }
                }
            }
        }

        // 4) Propagación BFS en 6 direcciones
        while (!queue.isEmpty()) {
            Vector3i p = queue.remove();
            int cx = p.x, cy = p.y, cz = p.z;
            byte level = skyLight[cx][cy][cz];

            if (level <= 1) {
                continue; // ya no hay más luz que propagar
            }
            // 6 vecinos ortogonales
            int[][] dirs = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}, {0, 0, -1}
            };

            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1], nz = cz + d[2];
                // límites de chunk
                if (nx < 0 || nx >= SIZEx || ny < 0 || ny >= SIZEy || nz < 0 || nz >= SIZEz) {
                    continue;
                }

                // opacidad sencilla: 0 aire, 1 bloque sólido
                Block neigh = blocks[nx][ny][nz];
                int opacity = (neigh == null || !neigh.isSolid()) ? 0 : 1;
                byte newLevel = (byte) (level - Math.max(1, opacity));

                if (newLevel > skyLight[nx][ny][nz]) {
                    skyLight[nx][ny][nz] = newLevel;
                    queue.add(new Vector3i(nx, ny, nz));
                }
            }

        }
    }

    /**
     * Establece un bloque dentro del chunk local
     */
    public void setBlock(int x, int y, int z, Block blockId) {
        if (blockId == null) {
            System.err.println("ERROR! se intentra colocar un bloque null en (" + x + "," + y + "," + z + ")");
            return;
        }

        if (x >= 0 && x < SIZEx && y >= 0 && y < SIZEy && z >= 0 && z < SIZEz) {
            blocks[x][y][z] = blockId;
            dirty = true;
            if (!blockId.getType().equals(AIR) && blockId != null) {
                System.out.println("Bloque sobrescrito en" + x + "," + y + "," + z + "con " + blockId.getType());
            }
        }

    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean flag) {
        this.dirty = flag;
    }

    /**
     * Obtiene el bloque en coordenadas locales del chunk
     */
    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZEx || y < 0 || y >= SIZEy || z < 0 || z >= SIZEz) {
            return AIR;
        }
        if (blocks[x][y][z] == null) {
            return AIR;
        }
        if (x >= 0 && x < SIZEx && y >= 0 && y < SIZEy && z >= 0 && z < SIZEz) {
            return blocks[x][y][z];
        }
        return AIR;
    }

    /**
     * Construye o reconstruye la malla del chunk aplicando ocultación de caras
     */
    public void buildMesh() {
        // 1) Tres listas de floats para GRASS, DIRT y STONE:
        List<Float> grassVerts = new ArrayList<>();
        List<Float> dirtVerts = new ArrayList<>();
        List<Float> stoneVerts = new ArrayList<>();

        for (int x = 0; x < SIZEx; x++) {
            for (int y = 0; y < SIZEy; y++) {
                for (int z = 0; z < SIZEz; z++) {
                    Block block = blocks[x][y][z];
                    if (block != null && block.isSolid()) {
                        float sky = skyLight[x][y][z] / 15f;
                        float blk = blockLight[x][y][z] / 15f;
                        float light = Math.max(sky, blk);
                        float[] cube = CubeGenerator.createCube(x, y, z, blocks, light);

                        // ¿De qué tipo es este bloque?
                        switch (block.getType()) {
                            case GRASS:
                                for (float f : cube) {
                                    grassVerts.add(f);
                                }
                                break;
                            case DIRT:
                                for (float f : cube) {
                                    dirtVerts.add(f);
                                }
                                break;
                            case STONE:
                                for (float f : cube) {
                                    stoneVerts.add(f);
                                }
                                break;
                            default:
                            // (si añades más tipos, agrégalos aquí)
                            }
                    }
                }
            }
        }

        // 2) Convertir las tres listas a tres arrays float[]
        if (!grassVerts.isEmpty()) {
            float[] gArr = new float[grassVerts.size()];
            for (int i = 0; i < gArr.length; i++) {
                gArr[i] = grassVerts.get(i);
            }
            if (grassMesh != null) {
                grassMesh.cleanup();
            }
            grassMesh = new Mesh(gArr);
        } else {
            grassMesh = null;
        }

        if (!dirtVerts.isEmpty()) {
            float[] dArr = new float[dirtVerts.size()];
            for (int i = 0; i < dArr.length; i++) {
                dArr[i] = dirtVerts.get(i);
            }
            if (dirtMesh != null) {
                dirtMesh.cleanup();
            }
            dirtMesh = new Mesh(dArr);
        } else {
            dirtMesh = null;
        }

        if (!stoneVerts.isEmpty()) {
            float[] sArr = new float[stoneVerts.size()];
            for (int i = 0; i < sArr.length; i++) {
                sArr[i] = stoneVerts.get(i);
            }
            if (stoneMesh != null) {
                stoneMesh.cleanup();
            }
            stoneMesh = new Mesh(sArr);
        } else {
            stoneMesh = null;
        }

        // 3) Limpiar la “antigua” malla completa (si la tenías)
        // (opcional, en tu caso ya no usas 'this.mesh' sino las tres nuevas)
    }

    /**
     * Verifica si un bloque vecino es aire o está fuera del chunk
     */
    public boolean isTransparent(int x, int y, int z) {
        if (x < 0 || x >= SIZEx || y < 0 || y >= SIZEy || z < 0 || z >= SIZEz) {
            return true; // Borde del chunk
        }
        return blocks[x][y][z] == Block.AIR;
    }

    public String getKey() {
        return chunkX + ",0," + chunkZ;
    }

    public void render(Matrix4f projection, Matrix4f view, ShaderProgram shader,
            Texture grassTex,
            Texture dirtTex,
            Texture stoneTex) {
        // PROYECCIÓN y VISTA ya las pasaste desde WorldManager

        Matrix4f model = new Matrix4f().translate(
                chunkX * SIZEx,
                chunkY * SIZEy,
                chunkZ * SIZEz
        );

        Matrix4f mvp = new Matrix4f(projection).mul(view).mul(model);

        // 1) Dibuja GRASS
        if (grassMesh != null) {
            shader.bind();
            shader.setUniformMat4("model", model);
            shader.setUniformMat4("mvp", mvp);
            glActiveTexture(GL_TEXTURE0);
            grassTex.bind();
            shader.setUniform1i("textureSampler", 0);
            grassMesh.render();
            shader.unbind();
        }

        // 2) Dibuja DIRT
        if (dirtMesh != null) {
            shader.bind();
            shader.setUniformMat4("model", model);
            shader.setUniformMat4("mvp", mvp);
            glActiveTexture(GL_TEXTURE0);
            dirtTex.bind();
            shader.setUniform1i("textureSampler", 0);
            dirtMesh.render();
            shader.unbind();
        }
        // 3) Dibuja STONE
        if (stoneMesh != null) {
            shader.bind();
            shader.setUniformMat4("model", model);
            shader.setUniformMat4("mvp", mvp);
            glActiveTexture(GL_TEXTURE0);
            stoneTex.bind();
            shader.setUniform1i("textureSampler", 0);
            stoneMesh.render();
            shader.unbind();
        }
    }

    public Mesh getMeshGrass() {
        return grassMesh;
    }

    public Mesh getMeshDirt() {
        return dirtMesh;
    }

    public Mesh getMeshStone() {
        return stoneMesh;
    }

    public Mesh getMeshByBlockType(BlockType type) {
        if (type == BlockType.DIRT) {
            return dirtMesh;
        }
        if (type == BlockType.GRASS) {
            return grassMesh;
        }
        if (type == BlockType.STONE) {
            return stoneMesh;
        }
        return null;
    }

    public void cleanup() {
        if (grassMesh != null) {
            grassMesh.cleanup();
        }
        if (dirtMesh != null) {
            dirtMesh.cleanup();
        }
        if (stoneMesh != null) {
            stoneMesh.cleanup();
        }
    }
}
