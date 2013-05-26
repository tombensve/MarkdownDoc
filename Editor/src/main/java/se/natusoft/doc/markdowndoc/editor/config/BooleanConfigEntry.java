package se.natusoft.doc.markdowndoc.editor.config;

/**
 * This represents a boolean config value.
 */
public class BooleanConfigEntry extends ConfigEntry {

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
    public BooleanConfigEntry(String key, String description, ConfigChanged configChanged) {
        super(key, description, configChanged);
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default value of the config.
     * @param configChanged Called when config has changed.
     */
    public BooleanConfigEntry(String key, String description, boolean defaultValue, ConfigChanged configChanged) {
        super(key, description, "" + defaultValue, configChanged);
    }

    //
    // Methods
    //

    /**
     * Returns the value as a boolean.
     */
    public boolean getBooleanValue() {
        return Boolean.valueOf(getValue());
    }

    /**
     * Sets the value as a boolean.
     *
     * @param value The value to set.
     */
    public void setBooleanValue(boolean value) {
        setValue("" + value);
    }
}
