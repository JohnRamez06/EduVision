package com.eduvision.iterator;
import java.util.*;
public class StudentTimelineIterator implements Iterator<Object> {
    private final List<Object> snapshots;
    private int index = 0;
    public StudentTimelineIterator(List<Object> snapshots) { this.snapshots = snapshots; }
    @Override public boolean hasNext() { return index < snapshots.size(); }
    @Override public Object next() { return snapshots.get(index++); }
}
