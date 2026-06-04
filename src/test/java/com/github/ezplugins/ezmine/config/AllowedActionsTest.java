package com.github.ezplugins.ezmine.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AllowedActionsTest {

    @Test
    void testNullActionIsNotAllowed() {
        assertFalse(AllowedActions.isAllowed(null));
    }

    @Test
    void testValidActionIsAllowed() {
        assertTrue(AllowedActions.isAllowed("3x3"));
        assertTrue(AllowedActions.isAllowed("auto-smelt"));
        assertTrue(AllowedActions.isAllowed("ore-searcher"));
        assertTrue(AllowedActions.isAllowed("vein-miner"));
        assertTrue(AllowedActions.isAllowed("area-mining"));
        assertTrue(AllowedActions.isAllowed("smelting"));
        assertTrue(AllowedActions.isAllowed("ore-hunter"));
        assertTrue(AllowedActions.isAllowed("toggle"));
    }

    @Test
    void testInvalidActionIsNotAllowed() {
        assertFalse(AllowedActions.isAllowed("invalid"));
        assertFalse(AllowedActions.isAllowed("unknown-action"));
    }

    @Test
    void testActionCaseInsensitiveWithLocaleRoot() {
        // These would fail on Turkish locale without Locale.ROOT
        assertTrue(AllowedActions.isAllowed("3X3"));
        assertTrue(AllowedActions.isAllowed("AUTO-SMELT"));
        assertTrue(AllowedActions.isAllowed("Ore-Searcher"));
        assertTrue(AllowedActions.isAllowed("VEIN-MINER"));
    }

    @Test
    void testAllActionsReturnsUnmodifiableSet() {
        var all = AllowedActions.all();
        assertTrue(all.contains("3x3"));
        assertTrue(all.contains("vein-miner"));
        assertThrows(UnsupportedOperationException.class, () -> all.add("test"));
    }
}