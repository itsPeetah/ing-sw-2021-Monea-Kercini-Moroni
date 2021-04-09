package it.polimi.ingsw.model.general;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LevelTest {

    @Test
    public void getPreviousTest() {
        Level lev = Level.values()[1];
        Level prevLev = Level.values()[0];

        // Test on normal situation
        assertSame(prevLev, lev.getPrevious());

        // Test limit case
        assertSame(prevLev, prevLev.getPrevious());
    }

    @Test
    public void getNextTest() {
        int size = Level.values().length;
        Level nextLev = Level.values()[size - 1];
        Level lev = Level.values()[size - 2];

        // Test on normal situation
        assertSame(nextLev, lev.getNext());

        // Test limit case
        assertSame(nextLev, nextLev.getNext());
    }

    @Test
    public void toIntegerTest() {
        int expected = 0;
        Level lev = Level.values()[expected];

        assertEquals(expected, lev.toInteger());
    }
}
