/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Este Thread se queda pendiente de una cola de "chunks a guardar". Cuando
 * aparezca un nuevo chunk en la cola, lo escribe en disco. De este modo, la
 * llamada a saveChunk() en el hilo principal solo encola, y no bloquea la
 * lógica de juego.
 */
public class ChunkSaveThread extends Thread {

    private final WorldManager worldManager;
    // Cola concurrente de chunks pendientes de guardado
    private final Set<Chunk> pendingSaves = ConcurrentHashMap.newKeySet();
    private volatile boolean running = true;

    public ChunkSaveThread(WorldManager worldManager) {
        this.worldManager = worldManager;
        setName("ChunkSaveThread");
        setDaemon(true); // para que no impida cerrar la app
    }

    /**
     * Encolar un chunk para guardarlo en disco. Si ya está en la cola, no se
     * vuelve a encolar para evitar duplicados.
     */
    public void requestSave(Chunk chunk) {
        pendingSaves.add(chunk);
    }

    @Override
    public void run() {
        while (running) {
            Chunk chunk = null;

            Iterator<Chunk> it = pendingSaves.iterator();
            if (it.hasNext()) {
                chunk = it.next();
                it.remove(); // Importante: eliminar antes de guardar
            }

            if (chunk != null) {
                try {
                    worldManager.saveChunkImmediate(chunk);
                } catch (IOException ex) {
                    System.err.println("Error al guardar el chunk " + chunk.getKey() + ": " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                // Si no hay chunks por guardar, duerme un poco
                try {
                    Thread.sleep(50); // Reduce uso de CPU
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void terminate() {
        running = false;
        this.interrupt();
    }
}
