package com.github.ezplugins.ezmine.tool;

import com.github.ezplugins.ezmine.config.CustomToolDefinition;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomToolManager {

    private final Map<UUID, Set<String>> activeActions = new ConcurrentHashMap<>();

    public void setActiveActions(UUID playerId, Collection<String> actions) {
        if (playerId == null) {
            return;
        }
        if (actions == null || actions.isEmpty()) {
            this.activeActions.remove(playerId);
            return;
        }
        Set<String> lowered = new HashSet<>();
        for (String action : actions) {
            if (action != null) {
                lowered.add(action.toLowerCase(Locale.ROOT));
            }
        }
        if (lowered.isEmpty()) {
            this.activeActions.remove(playerId);
        } else {
            this.activeActions.put(playerId, Collections.unmodifiableSet(new HashSet<>(lowered)));
        }
    }

    public void setActiveTool(UUID playerId, CustomToolDefinition definition) {
        if (definition == null) {
            this.activeActions.remove(playerId);
        } else {
            this.setActiveActions(playerId, definition.getActions());
        }
    }

    public Set<String> getActiveActions(UUID playerId) {
        Set<String> actions = this.activeActions.get(playerId);
        if (actions == null) {
            return Collections.emptySet();
        }
        return actions;
    }

    public boolean hasAction(UUID playerId, String action) {
        if (playerId == null || action == null) {
            return false;
        }
        Set<String> actions = this.activeActions.get(playerId);
        return actions != null && actions.contains(action.toLowerCase(Locale.ROOT));
    }

    public void clear(UUID playerId) {
        if (playerId != null) {
            this.activeActions.remove(playerId);
        }
    }

    public void clearAll() {
        this.activeActions.clear();
    }
}
