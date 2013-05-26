package se.natusoft.doc.markdowndoc.editor.api;

import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry;

import java.util.List;

/**
 * Allows for registering and getting config values.
 */
public interface Config {

    /**
     * This will populate the registered config entry with a user selected value.
     *
     * @param configEntry The config entry to make available and get populated.
     */
    void registerConfig(ConfigEntry configEntry);

    /**
     * Returns a list of all registered configs.
     */
    List<ConfigEntry> getConfigs();

    /**
     * Looks up a config entry by its key.
     *
     * @param key The key of the config entry to get.
     */
    ConfigEntry lookupConfig(String key);
}
