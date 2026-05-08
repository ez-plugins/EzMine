package com.skyblockexp.ezmine.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.Map;

/**
 * Manages custom messages for the EzMine plugin.
 * Provides methods to retrieve and format messages with placeholders.
 */
public class MessageManager {

    private final FileConfiguration messagesConfig;

    public MessageManager(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
    }

    /**
     * Gets a message from the configuration and translates color codes.
     *
     * @param key          The message key in the configuration
     * @param defaultValue The default value if the key is not found
     * @return The formatted message with color codes translated
     */
    public String getMessage(String key, String defaultValue) {
        String message = this.messagesConfig.getString(key, defaultValue);
        if (message == null) {
            message = defaultValue != null ? defaultValue : "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Gets a message from the configuration and replaces placeholders.
     *
     * @param key          The message key in the configuration
     * @param defaultValue The default value if the key is not found
     * @param placeholders A map of placeholder keys and their replacement values
     * @return The formatted message with placeholders replaced and color codes translated
     */
    public String getMessage(String key, String defaultValue, Map<String, String> placeholders) {
        String message = this.messagesConfig.getString(key, defaultValue);
        if (message == null) {
            message = defaultValue != null ? defaultValue : "";
        }
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    message = message.replace("%" + entry.getKey() + "%", entry.getValue());
                }
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Convenience method to get a message with a single placeholder.
     *
     * @param key           The message key in the configuration
     * @param defaultValue  The default value if the key is not found
     * @param placeholderKey The placeholder key
     * @param placeholderValue The placeholder value
     * @return The formatted message with placeholder replaced and color codes translated
     */
    public String getMessage(String key, String defaultValue, String placeholderKey, String placeholderValue) {
        if (placeholderKey == null || placeholderValue == null) {
            return this.getMessage(key, defaultValue);
        }
        Map<String, String> placeholders = Collections.singletonMap(placeholderKey, placeholderValue);
        return this.getMessage(key, defaultValue, placeholders);
    }
}
