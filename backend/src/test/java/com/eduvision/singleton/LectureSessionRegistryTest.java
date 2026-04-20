package com.eduvision.singleton;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LectureSessionRegistryTest {
    @Test void singletonReturnsOnlyOneInstance() {
        LectureSessionRegistry a = LectureSessionRegistry.getInstance();
        LectureSessionRegistry b = LectureSessionRegistry.getInstance();
        assertSame(a, b);
    }
}
