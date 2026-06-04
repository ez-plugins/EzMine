package com.github.ezplugins.ezmine.config;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomToolDefinitionTest {

    @Test
    void testNegativeShopCostIsClampedToZero() {
        var definition = new CustomToolDefinition(
            "test-tool",
            Material.DIAMOND_PICKAXE,
            "Test Tool",
            null,
            null,
            Set.of("3x3"),
            true,
            0,
            -50.0
        );

        assertEquals(0.0, definition.getShopCost(), "Negative shop cost should be clamped to 0");
    }

    @Test
    void testPositiveShopCostPreserved() {
        var definition = new CustomToolDefinition(
            "test-tool",
            Material.DIAMOND_PICKAXE,
            "Test Tool",
            null,
            null,
            Set.of("3x3"),
            true,
            0,
            100.0
        );

        assertEquals(100.0, definition.getShopCost());
    }

    @Test
    void testInvalidShopSlotWarning() {
        var definition = new CustomToolDefinition(
            "test-tool",
            Material.DIAMOND_PICKAXE,
            "Test Tool",
            null,
            null,
            Set.of("3x3"),
            true,
            -5, // Invalid shop slot
            100.0
        );

        var result = definition.validate();
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarningMessages().stream().anyMatch(
            msg -> msg.contains("invalid shop slot")));
    }

    @Test
    void testNegativeShopSlotWarningRemoved() {
        // The shopCost validation was removed as redundant since constructor clamps it
        var definition = new CustomToolDefinition(
            "test-tool",
            Material.DIAMOND_PICKAXE,
            "Test Tool",
            null,
            null,
            Set.of("3x3"),
            true,
            0,
            -50.0
        );

        var result = definition.validate();
        // shopCost < 0 warning should not appear since constructor already clamps
        assertFalse(result.hasWarnings());
    }
}