package com.acromere.index;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HitTest {
    @Test
    void testHitDefaultConstructor() {
        Hit hit = new Hit();
        assertNotNull(hit);
    }

    @Test
    void testHitAllArgumentsConstructorAndGetters() {
        // Assuming a minimal Document object for testing
        Document doc = new Document() {
            @Override
            public String title() {
                return "Test Title";
            }
        };

        Hit hit = new Hit(
                doc,
                "Some context text.",
                "testword",
                10,
                Arrays.asList(1, 5),
                3,
                Hit.TAG_PRIORITY
        );

        assertEquals(doc, hit.getDocument());
        assertEquals("Some context text.", hit.getContext());
        assertEquals("testword", hit.getWord());
        assertEquals(10, hit.getLength());
        assertEquals(Arrays.asList(1, 5), hit.getCoordinates());
        assertEquals(3, hit.getPoints());
        assertEquals(Hit.TAG_PRIORITY, hit.getPriority());
    }

    @Test
    void testHitChaining() {
        // Test chained setters/accessors
        Hit hit = new Hit();
        hit.setDocument(new Document() {
            @Override
            public String title() {
                return "Chained Title";
            }
        }).setContext("Chained Context").setWord("chained").setLength(5);

        assertEquals("Chained Title", hit.getDocument().title());
        assertEquals("Chained Context", hit.getContext());
        assertEquals("chained", hit.getWord());
        assertEquals(5, hit.getLength());
    }

    @Test
    void testHitToString() {
        Document doc = new Document() {
            @Override
            public String title() {
                return "Test Title";
            }
        };
        
        List<Integer> coords = Arrays.asList(1, 5);

        Hit hit = new Hit(doc, "Some context text.", "testword", 10, coords, 3, Hit.TAG_PRIORITY);

        String expected = "{title=Test Title length=10 term=testword context=Some context text. coords=[1, 5]}";
        assertEquals(expected, hit.toString());
    }

    @Test
    void testPriorityConstants() {
        assertEquals(2, Hit.TAG_PRIORITY);
        assertEquals(1, Hit.TITLE_PRIORITY);
        assertEquals(0, Hit.CONTENT_PRIORITY);
        assertEquals(0, Hit.HIGHEST_PRIORITY);
        assertEquals(2, Hit.LOWEST_PRIORITY);
    }
}

// Minimal mock Document class needed for compilation and testing purposes
class Document {
    public String title() {
        return "Default Title";
    }
}