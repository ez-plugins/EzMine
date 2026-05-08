package com.github.ezplugins.ezmine.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tracks the results of a configuration reload operation, including
 * which configuration files were successfully loaded and any errors or warnings
 * that occurred during the reload process.
 */
public class ConfigurationReloadResult {

    private final List<String> successMessages;
    private final List<String> warningMessages;
    private final List<String> errorMessages;
    private boolean overallSuccess;

    public ConfigurationReloadResult() {
        this.successMessages = new ArrayList<>();
        this.warningMessages = new ArrayList<>();
        this.errorMessages = new ArrayList<>();
        this.overallSuccess = true;
    }

    /**
     * Records a successful configuration file load.
     *
     * @param message Description of what was successfully loaded
     */
    public void addSuccess(String message) {
        this.successMessages.add(message);
    }

    /**
     * Records a warning during configuration reload.
     * Warnings don't prevent the configuration from being applied but indicate potential issues.
     *
     * @param message Description of the warning
     */
    public void addWarning(String message) {
        this.warningMessages.add(message);
    }

    /**
     * Records an error during configuration reload.
     * Errors indicate that a configuration failed to load or validate properly.
     *
     * @param message Description of the error
     */
    public void addError(String message) {
        this.errorMessages.add(message);
        this.overallSuccess = false;
    }

    public List<String> getSuccessMessages() {
        return Collections.unmodifiableList(this.successMessages);
    }

    public List<String> getWarningMessages() {
        return Collections.unmodifiableList(this.warningMessages);
    }

    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(this.errorMessages);
    }

    public boolean isOverallSuccess() {
        return this.overallSuccess;
    }

    public boolean hasWarnings() {
        return !this.warningMessages.isEmpty();
    }

    public boolean hasErrors() {
        return !this.errorMessages.isEmpty();
    }

    /**
     * Merges another ConfigurationReloadResult into this one.
     * All success, warning, and error messages from the other result are added to this result.
     * If the other result has errors, this result's overall success flag is set to false.
     *
     * @param other The result to merge into this one
     */
    public void merge(ConfigurationReloadResult other) {
        this.successMessages.addAll(other.successMessages);
        this.warningMessages.addAll(other.warningMessages);
        this.errorMessages.addAll(other.errorMessages);
        if (!other.overallSuccess) {
            this.overallSuccess = false;
        }
    }
}
