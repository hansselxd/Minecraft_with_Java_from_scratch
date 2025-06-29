/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.joml.Vector2i;

public class ChunkLoaderThread extends Thread {

    /**
     * Cola de coordenadas (chunkX, chunkZ) pendientes de cargar/generar
     */
    private final Queue<Vector2i> pending = new ConcurrentLinkedQueue<>();

    private final ConcurrentHashMap<Vector2i, Boolean> pendingMap = new ConcurrentHashMap<>();
    
    private int count = 1;
    /**
     * Referencia al WorldManager, para delegar carga/generación y encolar el
     * chunk
     */
    private final WorldManager worldManager;

    /**
     * Bandera para detener el hilo de forma limpia
     */
    private volatile boolean running = true;

    public ChunkLoaderThread(WorldManager worldManager) {
        this.worldManager = worldManager;
        setName("ChunkLoaderThread");
        setDaemon(true);
    }

    /**
     * Solicita la carga o generación de un chunk (solo lo encola). No vuelve a
     * encolar la misma coordenada si ya está pendiente.
     */
    public void requestLoad(int chunkX, int chunkZ) {
        Vector2i key = new Vector2i(chunkX, chunkZ);
        if (!pendingMap.containsKey(key) && !isAlreadyLoaded(chunkX, chunkZ)) {
            pending.add(key);
            pendingMap.put(key, true);
        }
    }

    public boolean isAlreadyLoaded(int ChunkX, int ChunkZ) {
        return worldManager.getChunks().containsKey(ChunkX + ",0," + ChunkZ);
    }

    /**
     * Marca el hilo para que deje de ejecutarse y lo interrumpe si está dormido
     */
    public void terminate() {
        running = false;
        this.interrupt();
    }

    public boolean hasPendingRequest(int chunkX, int chunkZ) {
        return pending.contains(new Vector2i(chunkX, chunkZ));
    }

    @Override
    public void run() {
        while (running) {
            int processed = 0;

            // Procesamos hasta 3 chunks por iteración (prioridad a los más cercanos)
            while (processed < 3) {
                Vector2i pos = pending.poll();
                if (pos == null) {
                    break; // No hay más peticiones en esta ronda
                }

                String key = pos.x + ",0," + pos.y;
                if (worldManager.getChunks().containsKey(key)) {
                    // Ya está cargado, lo saltamos
                    pendingMap.remove(pos);
                    continue;
                }

                // Si no está cargado, lo generamos
                Chunk chunk = new Chunk(pos.x, 0, pos.y);
                boolean loadedFromDisk = worldManager.loadChunk(chunk);
                if (!loadedFromDisk) {
                    chunk.generate();
                }
                worldManager.enqueueChunkToMesh(chunk);
                pendingMap.remove(pos);

                pendingMap.remove(pos);

                processed++;
            }

            // Dormimos un breve rato para no saturar la CPU
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
                // Si nos interrumpen, revisamos la bandera 'running' en la siguiente vuelta
            }
        }
    }
}
