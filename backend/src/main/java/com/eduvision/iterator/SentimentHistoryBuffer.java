package com.eduvision.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

public class SentimentHistoryBuffer implements Enumeration<Double> {
    private final double[] ring;
    private int size;
    private int writeIndex;
    private int iterationIndex;

    public SentimentHistoryBuffer(List<Double> scores) {
        this(Math.max(1, scores.size()));
        for (Double score : scores) {
            add(score);
        }
    }

    public SentimentHistoryBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        this.ring = new double[capacity];
    }

    public void add(double score) {
        ring[writeIndex] = score;
        writeIndex = (writeIndex + 1) % ring.length;
        if (size < ring.length) {
            size++;
        }
    }

    public int size() {
        return size;
    }

    public List<Double> toList() {
        if (size == 0) {
            return Collections.emptyList();
        }
        List<Double> values = new ArrayList<>(size);
        int start = size == ring.length ? writeIndex : 0;
        for (int i = 0; i < size; i++) {
            values.add(ring[(start + i) % ring.length]);
        }
        return values;
    }

    @Override
    public boolean hasMoreElements() {
        return iterationIndex < size;
    }

    @Override
    public Double nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException("No more sentiment values available");
        }
        List<Double> snapshot = toList();
        return snapshot.get(iterationIndex++);
    }

    public void reset() {
        iterationIndex = 0;
    }
}
