package com.minecraftclone.core;

import java.util.Arrays;

public class FloatArray {
    private float[] data;
    private int size;

    public FloatArray() {
        data = new float[1024];
        size = 0;
    }

    public void add(float[] values) {
        ensureCapacity(size + values.length);
        for (float v : values) {
            data[size++] = v;
        }
    }

    public float[] toArray() {
        return Arrays.copyOf(data, size);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
            int newCapacity = Math.max(minCapacity, data.length * 2);
            data = Arrays.copyOf(data, newCapacity);
        }
    }
}
