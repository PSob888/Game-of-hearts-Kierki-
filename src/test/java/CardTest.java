import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testEquals1() {
        Card card1 = new Card("2k");
        Card card2 = new Card("2t");
        assertFalse(card1.equals(card2));
    }

    @Test
    void testEquals2() {
        Card card1 = new Card("2k");
        Card card2 = new Card("3k");
        assertFalse(card1.equals(card2));
    }

    @Test
    void testEquals3() {
        Card card1 = new Card("2k");
        Card card2 = new Card("2k");
        assertTrue(card1.equals(card2));
    }
}