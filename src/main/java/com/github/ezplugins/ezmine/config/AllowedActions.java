package com.github.ezplugins.ezmine.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Registry of allowed custom tool actions. Keeps a centralized list so unknown actions
 * can be detected at configuration time.
 */
public final class AllowedActions {

    private AllowedActions() {}

    private static final Set<String> ACTIONS;

    static {
        Set<String> s = new HashSet<>();
        // Known action names used by existing code
        s.add("3x3");
        s.add("auto-smelt");
        s.add("ore-searcher");
        s.add("vein-miner");
        s.add("area-mining");
        s.add("smelting");
        s.add("ore-hunter");
        s.add("toggle");
        // add more as features are implemented
        ACTIONS = Collections.unmodifiableSet(s);
    }

    public static boolean isAllowed(String action) {
        if (action == null) {
            return false;
        }
        return ACTIONS.contains(action.toLowerCase());
    }

    public static Set<String> all() {
        return ACTIONS;
    }
}
