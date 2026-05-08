package com.skyblockexp.ezmine.integration;

import com.skyblockexp.ezmine.EzMine;
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
