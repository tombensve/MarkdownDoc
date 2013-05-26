package se.natusoft.doc.markdowndoc.editor.config;

/**
 * This is a integer config entry.
 */
public class IntegerConfigEntry extends ConfigEntry {
    //
    // Private Members
    //

    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

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
    public IntegerConfigEntry(String key, String description, ConfigChanged configChanged) {
        super(key, description, configChanged);
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default value of the config.
     * @param min The minimum value.
     * @param max The maximum value.
     * @param configChanged Called when config has changed.
     */
    public IntegerConfigEntry(String key, String description, int defaultValue, int min, int max, ConfigChanged configChanged) {
        super(key, description, "" + defaultValue, configChanged);
        this.min = min;
        this.max = max;
    }

    //
    // Methods
    //

    /**
     * Returns the value as an int.
     */
    public int getIntValue() {
        return Integer.valueOf(getValue());
    }

    /**
     * Sets the value as an int.
     *
     * @param value The int value to set.
     */
    public void setIntValue(int value) {
        setValue("" + value);
    }

    /**
     * Returns the minimum value.
     */
    public double getMinValue() {
        return this.min;
    }

    /**
     * Returns the maximum value.
     */
    public double getMaxValue() {
        return this.max;
    }
}
