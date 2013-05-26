package se.natusoft.doc.markdowndoc.editor.config;

import se.natusoft.doc.markdowndoc.editor.api.Config;

import java.util.*;

/**
 * This class holds config entries.
 */
public class ConfigHolder implements Config, Iterable<ConfigEntry> {
    //
    // Private Members
    //

    private List<ConfigEntry> configList = new LinkedList<ConfigEntry>();
    private Map<String, ConfigEntry> configMap = new HashMap<String, ConfigEntry>();

    //
    // Constructors
    //

    /**
     * Creates a new ConfigHolder.
     */
    public ConfigHolder() {}

    //
    // Methods
    //

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<ConfigEntry> iterator() {
        return this.configList.iterator();
    }

    /**
     * This will populate the registered config entry with a user selected value.
     *
     * @param configEntry The config entry to make available and get populated.
     */
    @Override
    public void registerConfig(ConfigEntry configEntry) {
        this.configList.add(configEntry);
        this.configMap.put(configEntry.getKey(), configEntry);
    }

    /**
     * Returns a list of all registered configs.
     */
    @Override
    public List<ConfigEntry> getConfigs() {
        return this.configList;
    }

    /**
     * Looks up a config entry by its key.
     *
     * @param key The key of the config entry to get.
     */
    @Override
    public ConfigEntry lookupConfig(String key) {
        return this.configMap.get(key);
    }
}
