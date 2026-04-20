package com.eduvision.iterator;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class SentimentHistoryBufferTest {
    @Test void iteratesAllElements() {
        SentimentHistoryBuffer buf = new SentimentHistoryBuffer(List.of(0.4, 0.6, 0.8));
        int count = 0;
        while (buf.hasMoreElements()) { buf.nextElement(); count++; }
        assertEquals(3, count);
    }
}
