package com.minecraftclone.core;

import static com.minecraftclone.core.Block.AIR;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

public class CubeGenerator {

    // Tamaño de cada celda de la textura (col: 3 columnas, row: 2 filas)
    private static final float UV_WIDTH = 1.0f / 3.0f;
    private static final float UV_HEIGHT = 1.0f / 2.0f;

    public enum Direction {
        UP(0, +1, 0),
        DOWN(0, -1, 0),
        FRONT(0, 0, +1),
        BACK(0, 0, -1),
        LEFT(-1, 0, 0),
        RIGHT(+1, 0, 0);

        public final int offsetX, offsetY, offsetZ;

        Direction(int dx, int dy, int dz) {
            this.offsetX = dx;
            this.offsetY = dy;
            this.offsetZ = dz;
        }
    }

    public static float[] createCube(int cx, int cy, int cz, Block[][][] blocks, float light) {

        List<Float> data = new ArrayList<>(6 * 10 * 6);

        //para cada cara
        for (Direction dir : Direction.values()) {

            int texCol = getTexCol(dir);
            int texRow = getTexRow(dir);

            float[][] positions = CubeFace.getFaceVertices(dir);
            float[] normal = CubeFace.getNormal(dir);

            float u0 = texCol * UV_WIDTH, v0 = texRow * UV_HEIGHT;
            float u1 = u0 + UV_WIDTH, v1 = v0 + UV_HEIGHT;

            // UVs: se asignan en sentido horario desde esquina inferior izquierda
            float[][] uvs = new float[][]{
                {u0, v1},
                {u1, v1},
                {u1, v0},
                {u0, v0}
            };

            int[][] triIndices = new int[][]{
                {0, 1, 2},
                {2, 3, 0}
            };

            for (int[] tri : triIndices) {
                for (int vi : tri) {
                    float px = positions[vi][0] + cx;
                    float py = positions[vi][1] + cy;
                    float pz = positions[vi][2] + cz;

                    int nx = (int) positions[vi][0];
                    int ny = (int) positions[vi][1];
                    int nz = (int) positions[vi][2];

                    float shadow = calcShadow(dir, cy);
                    if (positions[vi][1] == 1) {
                        shadow += 0.1f;
                    }
                    //AO
                    int ix = (int) Math.floor(px);
                    int iy = (int) Math.floor(py);
                    int iz = (int) Math.floor(pz);

                    float ao = calAO(nx, ny, nz, dir, blocks, cx, cy, cz);

                    //UV
                    float u = uvs[vi][0];
                    float v = uvs[vi][1];

                    data.add(px);
                    data.add(py);
                    data.add(pz);

                    data.add(normal[0]);
                    data.add(normal[1]);
                    data.add(normal[2]);

                    data.add(u);
                    data.add(v);

                    data.add(shadow);
                    data.add(ao);
                    data.add(light);
                }
            }
        }

        float[] result = new float[data.size()];
        for (int i = 0; i < data.size(); i++) {
            result[i] = data.get(i);
        }
        return result;
    }

    private static float calcShadow(Direction dir, int cy) {
        if (cy == 0) {
            return 0.1f;
        }
        if (cy == 1) {
            return 0.3f;
        }
        if (cy == 2) {
            return 0.5f;
        }
        if (cy == 3) {
            return 0.7f;
        }
        if (cy == 4) {
            if (dir.equals(dir.UP)) {
                return 1.0f;
            }
            return 0.9f;
        }
        return 1.0f;
    }

    private static float calAO(int x, int y, int z, Direction dir, Block[][][] blocks, int ix, int iy, int iz) {
        boolean lado1 = false, lado2 = false, esquina = false;

        String vertex = x + "," + y + "," + z;

        //System.out.println("Es el vertice: ("+vertex+") de la cara "+ dir.toString() );
        switch (dir) {
            case FRONT:
                switch (vertex) {
                    case "0,0,1":
                        lado1 = isSolidBlock(blocks, ix - 1, iy, iz + 1);
                        lado2 = isSolidBlock(blocks, ix, iy - 1, iz + 1);
                        esquina = isSolidBlock(blocks, ix - 1, iy - 1, iz + 1);
                        break;
                    case "1,0,1":
                        lado1 = isSolidBlock(blocks, ix + 1, iy, iz + 1);
                        lado2 = isSolidBlock(blocks, ix, iy - 1, iz + 1);
                        esquina = isSolidBlock(blocks, ix + 1, iy - 1, iz + 1);
                        break;
                    case "1,1,1":
                        lado1 = isSolidBlock(blocks, ix + 1, iy, iz + 1);
                        lado2 = isSolidBlock(blocks, ix, iy + 1, iz + 1);
                        esquina = isSolidBlock(blocks, ix + 1, iy + 1, iz + 1);
                        break;
                    case "0,1,1":
                        lado1 = isSolidBlock(blocks, ix - 1, iy, iz + 1);
                        lado2 = isSolidBlock(blocks, ix, iy + 1, iz + 1);
                        esquina = isSolidBlock(blocks, ix - 1, iy + 1, iz + 1);
                        break;
                }
                break;
            case BACK:
                switch (vertex) {
                    case "1,0,0":
                        lado1 = isSolidBlock(blocks, ix + 1, iy, iz - 1);
                        lado2 = isSolidBlock(blocks, ix, iy - 1, iz - 1);
                        esquina = isSolidBlock(blocks, ix + 1, iy - 1, iz - 1);
                        break;
                    case "0,0,0":
                        lado1 = isSolidBlock(blocks, ix - 1, iy, iz - 1);
                        lado2 = isSolidBlock(blocks, ix, iy - 1, iz - 1);
                        esquina = isSolidBlock(blocks, ix - 1, iy - 1, iz - 1);
                        break;
                    case "0,1,0":
                        lado1 = isSolidBlock(blocks, ix - 1, iy, iz - 1);
                        lado2 = isSolidBlock(blocks, ix, iy + 1, iz + -1);
                        esquina = isSolidBlock(blocks, ix - 1, iy + 1, iz - 1);
                        break;
                    case "1,1,0":
                        lado1 = isSolidBlock(blocks, ix + 1, iy, iz - 1);
                        lado2 = isSolidBlock(blocks, ix, iy + 1, iz - 1);
                        esquina = isSolidBlock(blocks, ix + 1, iy + 1, iz - 1);
                        break;
                }
                break;
            case LEFT:
                switch (vertex) {
                    case "0,0,0":
                        lado1 = isSolidBlock(blocks, ix - 1, iy, iz - 1);
                        lado2 = isSolidBlock(blocks, ix - 1, iy - 1, iz);
                        esquina = isSolidBlock(blocks, ix - 1, iy - 1, iz - 1);
                        break;
                    case "0,0,1":
                        lado1 = isSolidBlock(blocks, ix - 1, iy, iz + 1);
                        lado2 = isSolidBlock(blocks, ix - 1, iy - 1, iz);
                        esquina = isSolidBlock(blocks, ix - 1, iy - 1, iz + 1);
                        break;
                    case "0,1,1":
                        lado1 = isSolidBlock(blocks, ix - 1, iy, iz + 1);
                        lado2 = isSolidBlock(blocks, ix - 1, iy + 1, iz);
                        esquina = isSolidBlock(blocks, ix - 1, iy + 1, iz + 1);
                        break;
                    case "0,1,0":
                        lado1 = isSolidBlock(blocks, ix - 1, iy, iz - 1);
                        lado2 = isSolidBlock(blocks, ix - 1, iy + 1, iz);
                        esquina = isSolidBlock(blocks, ix - 1, iy + 1, iz - 1);
                        break;
                }
                break;
            case RIGHT:
                switch (vertex) {
                    case "1,0,1":
                        lado1 = isSolidBlock(blocks, ix + 1, iy, iz + 1);
                        lado2 = isSolidBlock(blocks, ix + 1, iy - 1, iz);
                        esquina = isSolidBlock(blocks, ix + 1, iy - 1, iz + 1);
                        break;
                    case "1,0,0":
                        lado1 = isSolidBlock(blocks, ix + 1, iy, iz - 1);
                        lado2 = isSolidBlock(blocks, ix + 1, iy - 1, iz);
                        esquina = isSolidBlock(blocks, ix + 1, iy - 1, iz - 1);
                        break;
                    case "1,1,0":
                        lado1 = isSolidBlock(blocks, ix + 1, iy, iz - 1);
                        lado2 = isSolidBlock(blocks, ix + 1, iy + 1, iz);
                        esquina = isSolidBlock(blocks, ix + 1, iy + 1, iz - 1);
                        break;
                    case "1,1,1":
                        lado1 = isSolidBlock(blocks, ix + 1, iy, iz + 1);
                        lado2 = isSolidBlock(blocks, ix + 1, iy + 1, iz);
                        esquina = isSolidBlock(blocks, ix + 1, iy + 1, iz + 1);
                        break;
                }
                break;
            case UP:
                switch (vertex) {
                    case "0,1,1":
                        lado1 = isSolidBlock(blocks, ix - 1, iy + 1, iz);
                        lado2 = isSolidBlock(blocks, ix, iy + 1, iz + 1);
                        esquina = isSolidBlock(blocks, ix - 1, iy + 1, iz + 1);
                        break;
                    case "1,1,1":
                        lado1 = isSolidBlock(blocks, ix + 1, iy + 1, iz);
                        lado2 = isSolidBlock(blocks, ix, iy + 1, iz + 1);
                        esquina = isSolidBlock(blocks, ix + 1, iy + 1, iz + 1);
                        break;
                    case "1,1,0":
                        lado1 = isSolidBlock(blocks, ix + 1, iy + 1, iz);
                        lado2 = isSolidBlock(blocks, ix, iy + 1, iz - 1);
                        esquina = isSolidBlock(blocks, ix + 1, iy + 1, iz - 1);
                        break;
                    case "0,1,0":
                        lado1 = isSolidBlock(blocks, ix - 1, iy + 1, iz);
                        lado2 = isSolidBlock(blocks, ix, iy + 1, iz - 1);
                        esquina = isSolidBlock(blocks, ix - 1, iy + 1, iz - 1);
                        break;
                }
                break;
            case DOWN:
                switch (vertex) {
                    case "0,0,0":
                        lado1 = isSolidBlock(blocks, ix - 1, iy - 1, iz);
                        lado2 = isSolidBlock(blocks, ix, iy - 1, iz - 1);
                        esquina = isSolidBlock(blocks, ix - 1, iy - 1, iz - 1);
                        break;
                    case "1,0,0":
                        lado1 = isSolidBlock(blocks, ix + 1, iy - 1, iz);
                        lado2 = isSolidBlock(blocks, ix, iy - 1, iz - 1);
                        esquina = isSolidBlock(blocks, ix + 1, iy - 1, iz - 1);
                        break;
                    case "1,0,1":
                        lado1 = isSolidBlock(blocks, ix + 1, iy - 1, iz);
                        lado2 = isSolidBlock(blocks, ix, iy - 1, iz + 1);
                        esquina = isSolidBlock(blocks, ix + 1, iy - 1, iz + 1);
                        break;
                    case "0,0,1":
                        lado1 = isSolidBlock(blocks, ix - 1, iy - 1, iz);
                        lado2 = isSolidBlock(blocks, ix, iy - 1, iz + 1);
                        esquina = isSolidBlock(blocks, ix - 1, iy - 1, iz + 1);
                        break;
                }
                break;
        }

        //ambos lados = sombra total
        if (lado1 && lado2) {
            return 0.4f;
        }

        //contamos cuantos de los 3 estan solidos
        int totalSolid = 0;
        if (lado1) {
            totalSolid++;
        }
        if (lado2) {
            totalSolid++;
        }
        if (esquina) {
            totalSolid++;
        }

        return (3 - totalSolid) / 3.0f;
    }

    private static boolean isAir(Block[][][] blocks, int x, int y, int z) {
        if (x < 0 || x >= blocks.length
                || y < 0 || y >= blocks[0].length
                || z < 0 || z >= blocks[0][0].length) {
            return true; // Consideramos aire fuera de los límites
        }
        if (blocks[x][y][z].isSolid && blocks[x][y][z] != null) {
            return false;
        }
        return true;
    }

    private static boolean isInShadow(int x, int y, int z, Direction dir) {
        final int MAX_DIST = 8;

        for (int i = 1; i <= MAX_DIST; i++) {
            int nx = x + dir.offsetX * i;
            int ny = y + dir.offsetY * i;
            int nz = z + dir.offsetZ * i;

            Block b = WorldManager.instance.getBlockIfLoader(nx, ny, nz);
            if (b == null) {
                continue;
            }

            if (b.isSolid()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Comprueba si en las coordenadas locales (x,y,z) existe un bloque sólido.
     * Debemos verificar límites del chunk:
     */
    private static boolean isSolidBlock(Block[][][] blocks, int lx, int ly, int lz) {
        if (lx < 0 || lx >= blocks.length
                || ly < 0 || ly >= blocks[0].length
                || lz < 0 || lz >= blocks[0][0].length) {
            return false;  // fuera de este chunk → considerar “vacío” para AO
        }
        Block b = blocks[lx][ly][lz];
        return (b != null && b.isSolid());
    }

    private static int getTexCol(Direction dir) {
        switch (dir) {
            case UP:
                return 0;
            case DOWN:
                return 2;
            case FRONT:
                return 1;
            case BACK:
                return 1;
            case LEFT:
                return 0;
            case RIGHT:
                return 2;
        }
        return 0;
    }

    private static int getTexRow(Direction dir) {
        switch (dir) {
            case UP:
                return 0;
            case DOWN:
                return 0;
            case FRONT:
                return 0;
            case BACK:
                return 1;
            case LEFT:
                return 1;
            case RIGHT:
                return 1;
        }
        return 0;
    }
}
