package com.eduvision.iterator;

import com.eduvision.model.EmotionSnapshot;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SentimentHistoryBufferTest {

    @Test
    void iteratesAllElements() {
        EmotionSnapshot s1 = new EmotionSnapshot();
        s1.setId("1");
        EmotionSnapshot s2 = new EmotionSnapshot();
        s2.setId("2");
        EmotionSnapshot s3 = new EmotionSnapshot();
        s3.setId("3");

        SentimentHistoryBuffer buf = new SentimentHistoryBuffer(3);
        buf.add(s1);
        buf.add(s2);
        buf.add(s3);

        int count = 0;
        while (buf.hasMoreElements()) {
            buf.nextElement();
            count++;
        }
        assertEquals(3, count);
    }
}
