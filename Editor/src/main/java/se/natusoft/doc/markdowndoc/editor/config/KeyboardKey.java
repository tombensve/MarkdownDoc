package se.natusoft.doc.markdowndoc.editor.config;

import java.awt.event.KeyEvent;

/**
 * Represents a keyboard key, possibly with modifiers in a format that can be saved to a properties file.
 */
public class KeyboardKey {

    //
    // Private Members
    //

    /** Our internal representation. */
    private String key;

    //
    // Constructors
    //

    /**
     * Creates a new KeyboardKey instance.
     *
     * @param keyEvent The key event to get key from.
     */
    public KeyboardKey(KeyEvent keyEvent) {
        if (keyEvent != null) { // For Linux/Ubutnu!
            this.key = KeyEvent.getKeyModifiersText(keyEvent.getModifiers()) + "+" + KeyEvent.getKeyText(keyEvent.getKeyCode());
        }
    }

    /**
     * Creates a new KeyboardKey instance.
     *
     * @param key The key in "internal" key format.
     */
    public KeyboardKey(String key) {
        this.key = key;
    }

    //
    // Methods
    //

    /**
     * Compares for equality.
     *
     * @param key The object to compare to.
     */
    public boolean equals(Object key) {
        return key instanceof KeyboardKey && ((KeyboardKey) key).key.equals(this.key);
    }

    /**
     * Return as string.
     */
    public String toString() {
        return key;
    }
}
