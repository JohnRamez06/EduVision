package com.eduvision.iterator;
import java.util.*;
public class SentimentHistoryBuffer implements Enumeration<Double> {
    private final List<Double> buffer;
    private int cursor = 0;
    public SentimentHistoryBuffer(List<Double> scores) { this.buffer = new ArrayList<>(scores); }
    @Override public boolean hasMoreElements() { return cursor < buffer.size(); }
    @Override public Double nextElement() { return buffer.get(cursor++); }
    public void reset() { cursor = 0; }
}
