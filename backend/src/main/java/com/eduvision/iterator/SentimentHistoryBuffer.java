package com.eduvision.iterator;

import com.eduvision.model.EmotionSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

public class SentimentHistoryBuffer implements Enumeration<EmotionSnapshot> {

    private final EmotionSnapshot[] buffer;
    private int size;
    private int writeIndex;
    private int cursor;

    public SentimentHistoryBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.buffer = new EmotionSnapshot[capacity];
    }

    public SentimentHistoryBuffer(List<EmotionSnapshot> snapshots) {
        this(Math.max(1, snapshots.size()));
        for (EmotionSnapshot snapshot : snapshots) {
            add(snapshot);
        }
    }

    public void add(EmotionSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        buffer[writeIndex] = snapshot;
        writeIndex = (writeIndex + 1) % buffer.length;
        if (size < buffer.length) {
            size++;
        }
    }

    public List<EmotionSnapshot> asList() {
        if (size == 0) {
            return Collections.emptyList();
        }
        List<EmotionSnapshot> snapshots = new ArrayList<>(size);
        int start = size == buffer.length ? writeIndex : 0;
        for (int i = 0; i < size; i++) {
            snapshots.add(buffer[(start + i) % buffer.length]);
        }
        return snapshots;
    }

    @Override
    public boolean hasMoreElements() {
        return cursor < size;
    }

    @Override
    public EmotionSnapshot nextElement() {
        List<EmotionSnapshot> ordered = asList();
        if (cursor >= ordered.size()) {
            throw new NoSuchElementException("No more snapshots in sentiment history");
        }
        return ordered.get(cursor++);
    }

    public void reset() {
        cursor = 0;
    }
}
