/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

/**
 *
 * @author hanss
 */
public class MeshBuilderThread extends Thread {

    private final WorldManager worldManager;
    private volatile boolean running = true;

    public MeshBuilderThread(WorldManager wm) {
        this.worldManager = wm;
        setName("MeshBuilderThread");
        setDaemon(true);
    }

    public void terminate() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (running) {
            Chunk chunk = worldManager.chunksToMesh.poll();
            if (chunk != null) {
                worldManager.enqueueChunkToAdd(chunk);
            }else{
                try{Thread.sleep(10);}catch(Exception e){}
            }
        }
    }
}
