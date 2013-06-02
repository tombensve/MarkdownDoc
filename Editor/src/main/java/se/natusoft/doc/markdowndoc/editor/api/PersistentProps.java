package se.natusoft.doc.markdowndoc.editor.api;

import java.util.Properties;

/**
 * This api allows for getting and saving properties.
 */
public interface PersistentProps {

    /**
     * Loads the named properties.
     *
     * @param name The name of the properties to load. Please note that this is only a name, not a path!
     */
    Properties load(String name);

    /**
     * Saves the given properties with the given name.
     *
     * @param name The name of the properties to save.
     * @param props The properties to save.
     */
    void save(String name, Properties props);
}
