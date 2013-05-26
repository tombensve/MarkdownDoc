package se.natusoft.doc.markdowndoc.editor.config;

/**
 * This is a ConfigEntry with a set of valid values.
 */
public class ValidSelectionConfigEntry extends ConfigEntry {
    //
    // Private Members
    //

    private ValidValues validValues;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param validValues The valid values for the config.
     * @param configChanged Called when config has changed.
     */
    public ValidSelectionConfigEntry(String key, String description, ValidValues validValues, ConfigChanged configChanged) {
        super(key, description, configChanged);
        this.validValues = validValues;
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default value of the config.
     * @param validValues The valid values for the config.
     * @param configChanged Called when config has changed.
     */
    public ValidSelectionConfigEntry(String key, String description, String defaultValue, ValidValues validValues, ConfigChanged configChanged) {
        super(key, description, defaultValue, configChanged);
        this.validValues = validValues;
    }

    /**
     * Returns the valid values.
     */
    public String[] getValidValues() {
        return this.validValues.validValues();
    }

    //
    // Inner Classes
    //

    /**
     * Defines the valid values.
     */
    public interface ValidValues {
        String[] validValues();
    }
}
