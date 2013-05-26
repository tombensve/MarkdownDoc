package se.natusoft.doc.markdowndoc.editor.config;

/**
 * This is a double config entry.
 */
public class DoubleConfigEntry extends ConfigEntry {
    //
    // Private Members
    //

    private double min = Double.MIN_VALUE;
    private double max = Double.MAX_VALUE;

    //
    // Constructors
    //

    /**
     * Creates a new DoubleConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param configChanged Called when config has changed.
     */
    public DoubleConfigEntry(String key, String description, ConfigChanged configChanged) {
        super(key, description, configChanged);
    }

    /**
     * Creates a new DoubleConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default value of the config.
     * @param min The minimum value.
     * @param max The maximum value.
     * @param configChanged Called when config has changed.
     */
    public DoubleConfigEntry(String key, String description, double defaultValue, double min, double max, ConfigChanged configChanged) {
        super(key, description, "" + defaultValue, configChanged);
        this.min = min;
        this.max = max;
    }

    //
    // Methods
    //

    /**
     * Returns the value as a double.
     */
    public double getDoubleValue() {
        return Double.valueOf(getValue());
    }

    /**
     * Sets the value as a double.
     *
     * @param value The double value to set.
     */
    public void setDoubleValue(double value) {
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
