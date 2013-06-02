package se.natusoft.doc.markdowndoc.editor.api.providers;

import se.natusoft.doc.markdowndoc.editor.api.PersistentProps;

import java.io.*;
import java.util.Properties;

/**
 * Holds a set of persistent properties providers.
 */
public class PersistentPropertiesProvider implements PersistentProps {
    //
    // Constructors
    //

    /**
     * Creates a new PersistentPropertiesProvider.
     */
    public PersistentPropertiesProvider() {}

    //
    // Methods
    //

    /**
     * Sets up and returns the properties directory.
     */
    private File getPropsDir() {
        String userHome = System.getProperties().getProperty("user.home");
        File propsDir = new File(userHome);
        propsDir = new File(propsDir, ".markdownDoc");
        if (!propsDir.exists()) {
            propsDir.mkdirs();
        }
        return propsDir;
    }

    /**
     * Loads the named properties.
     *
     * @param name The name of the properties to load. Please note that this is only a name, not a path!
     *
     * @return A Properties object or null if not available (or failure to read it!).
     */
    @Override
    public Properties load(String name) {
        File propsFile = new File(getPropsDir(), name + ".properties");
        Properties props = null;

        FileReader propsReader = null;
        try {
            propsReader = new FileReader(propsFile);
            props = new Properties();
            props.load(propsReader);
        }
        catch (FileNotFoundException fnfe) {}
        catch (IOException ioe) {
            System.err.println("ERROR: " + ioe.getMessage());
        }
        finally {
            try {if (propsReader != null) propsReader.close();} catch (IOException cieo) {}
        }

        return props;
    }

    /**
     * Saves the given properties with the given name.
     *
     * @param name  The name of the properties to save.
     * @param props The properties to save.
     */
    @Override
    public void save(String name, Properties props) {
        File propsFile = new File(getPropsDir(), name + ".properties");

        FileWriter writer = null;
        try {
            writer = new FileWriter(propsFile);
            props.store(writer, "Properties for '" + name + "'.");
        }
        catch (IOException ioe) {
            System.err.println("ERROR: " + ioe.getMessage());
        }
        finally {
            try { if (writer != null) writer.close(); } catch (IOException cioe) {}
        }
    }
}
