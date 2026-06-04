package com.github.ezplugins.ezmine.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VeinMineUtilTest {

    @Test
    void testNullOriginReturnsEmptyList() {
        // This tests the null check fix - should not throw NPE
        var result = VeinMineUtil.findVein(null, 10);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Null origin should return empty list");
    }

    @Test
    void testMaxBlocksZeroOrNegativeReturnsEmptyList() {
        var result1 = VeinMineUtil.findVein(null, 0);
        assertTrue(result1.isEmpty(), "maxBlocks=0 should return empty list");

        var result2 = VeinMineUtil.findVein(null, -1);
        assertTrue(result2.isEmpty(), "maxBlocks<0 should return empty list");
    }
}