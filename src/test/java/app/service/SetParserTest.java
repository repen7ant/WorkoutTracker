package app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SetParserTest {

    @Test
    void pickBestSet_allInvalid_returnsEmpty() {
        String best = SetParser.pickBestSet("ax5-100x-10z-5-xxx");
        assertEquals("", best);
    }

    @Test
    void pickBestSet_mixedValidAndInvalid_ignoresInvalid() {
        String best = SetParser.pickBestSet("abc-100x5-xyzx10");
        assertEquals("100x5", best);
    }

    @Test
    void pickBestSet_returnsEmpty_whenNullOrBlank() {
        assertEquals("", SetParser.pickBestSet(null));
        assertEquals("", SetParser.pickBestSet(""));
        assertEquals("", SetParser.pickBestSet("   "));
    }

    @Test
    void pickBestSet_prefersHeavierWeight() {
        String best = SetParser.pickBestSet("100x10-120x1");
        assertEquals("120x1", best);
    }

    @Test
    void pickBestSet_sameWeight_prefersMoreReps() {
        String best = SetParser.pickBestSet("100x5-100x8");
        assertEquals("100x8", best);
    }

    @Test
    void pickBestSet_supportsDecimalWeights() {
        String best = SetParser.pickBestSet("80x5-82.5x4-82.5x6");
        assertEquals("82.5x6", best);
    }

    @Test
    void pickBestSet_sameWeightAndReps_keepsFirst() {
        String best = SetParser.pickBestSet("100x5-100x5");
        assertEquals("100x5", best);
    }
}
