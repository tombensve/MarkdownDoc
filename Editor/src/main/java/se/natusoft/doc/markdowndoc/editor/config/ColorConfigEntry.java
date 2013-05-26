package se.natusoft.doc.markdowndoc.editor.config;

/**
 * This is a config entry holding a color value making it available as
 * separate red, green and blue values that can individually be changed.
 * <p/>
 * The config value is stored as "red:green:blue".
 */
public class ColorConfigEntry extends ConfigEntry {
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
    public ColorConfigEntry(String key, String description, ConfigChanged configChanged) {
        super(key, description, configChanged);
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultRed The default red color part.
     * @param defaultGreen The default green color part.
     * @param defaultBlue The default blue color part.
     * @param configChanged Called when config has changed.
     */
    public ColorConfigEntry(String key, String description, int defaultRed, int defaultGreen,
                            int defaultBlue, ConfigChanged configChanged) {
        super(key, description, toValue(defaultRed, defaultGreen, defaultBlue), configChanged);
    }

    //
    // Methods
    //

    /**
     * Converts red, green, and blue int values to a config value string.
     *
     * @param red The red color part.
     * @param green The green color part.
     * @param blue The blue color part.
     */
    private static String toValue(int red, int green, int blue) {
        return "" + red + ":" + green + ":" + blue;
    }

    /**
     * Returns the integer value of the color part specified for the current config value.
     *
     * @param colorPart The color part to get.
     */
    private int valueToColor(ColorPart colorPart) {
        return Integer.valueOf(getValue().split(":")[colorPart.ordinal()]);
    }

    /**
     * Returns the red color part.
     */
    public int getRed() {
        return valueToColor(ColorPart.RED);
    }

    /**
     * Returns the green color part.
     */
    public int getGreen() {
        return valueToColor(ColorPart.GREEN);
    }

    /**
     * Returns the blue color part.
     */
    public int getBlue() {
        return valueToColor(ColorPart.BLUE);
    }

    /**
     * Sets the red color part.
     *
     * @param red The red value to set.
     */
    public void setRed(int red) {
        setValue(toValue(red, getGreen(), getBlue()));
    }

    /**
     * Sets the green color part.
     * @param green The green value to set.
     */
    public void setGreen(int green) {
        setValue(toValue(getRed(), green, getBlue()));
    }

    /**
     * Sets the blue color part.
     *
     * @param blue The blur value to set.
     */
    public void setBlue(int blue) {
        setValue(toValue(getRed(), getGreen(), blue));
    }
}
