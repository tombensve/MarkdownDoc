package se.natusoft.doc.markdowndoc.editor.config;

/**
 * This is called when a config value is changed.
 */
public interface ConfigChanged {
    /**
     * This delivers the changed config entry.
     */
    void configChanged(ConfigEntry ce);
}
