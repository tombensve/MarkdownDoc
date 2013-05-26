package se.natusoft.doc.markdowndoc.editor.config;

/**
 * This represents one config entry.
 */
public class ConfigEntry {
    //
    // Private Members
    //

    private String key;
    private String description;
    private String value = "";
    private ConfigChanged configChanged = null;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param configChanged Called when config has changed.
     */
    public ConfigEntry(String key, String description, ConfigChanged configChanged) {
        this.key = key;
        this.description = description;
        this.configChanged = configChanged;
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default value of the config.
     * @param configChanged Called when config has changed.
     */
    public ConfigEntry(String key, String description, String defaultValue, ConfigChanged configChanged) {
        this(key, description, configChanged);
        this.value = defaultValue;
    }

    //
    // Methods
    //

    /**
     * Returns the key of the config.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Returns the value of the config.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the value of the config as a boolean.
     */
    public boolean getBoolValue() {
        return Boolean.valueOf(this.value);
    }

    /**
     * Sets the value of the config.
     *
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
        this.configChanged.configChanged(this);
    }

    /**
     * Returns the description of the value.
     */
    public String getDescription() {
        return this.description;
    }

    //
    // Inner Classes
    //

    /**
     * This is called when a config value is changed.
     */
    public interface ConfigChanged {
        /**
         * This delivers the changed config entry.
         */
        public void configChanged(ConfigEntry ce);
    }
}
