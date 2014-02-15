package se.natusoft.doc.markdowndoc.editor.config;

/**
 * This ConfigEntry represents a keyboard key to activate a function.
 */
public class KeyConfigEntry extends ConfigEntry {

    //
    // Constructors
    //

    /**
     * Creates a new KeyConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default value.
     * @param configGroup The config group this config belongs to.
     */
    public KeyConfigEntry(String key, String description, KeyboardKey defaultValue, String configGroup) {
        super(key, description, defaultValue.toString(), configGroup);
    }

    //
    // Methods
    //


    /**
     * Returns the keyboard key.
     */
    public KeyboardKey getKeyboardKey() {
        return new KeyboardKey(getValue());
    }

    /**
     * Sets a keyboard key.
     *
     * @param keyboardKey The keyboard key to set.
     */
    public void setKeyboardKey(KeyboardKey keyboardKey) {
        setValue(keyboardKey.toString());
    }
}
