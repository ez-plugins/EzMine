package com.github.ezplugins.ezmine.integration;

import com.github.ezplugins.ezmine.EzMine;
import org.bstats.bukkit.Metrics;

public class MetricsIntegration {

    private Metrics metrics;

    public void start(EzMine plugin) {
        if (this.metrics != null) {
            return;
        }
        int pluginId = 28501;
        this.metrics = new Metrics(plugin, pluginId);
    }
}
