package se.natusoft.doc.markdowndoc.editor.functions.utils;

import se.natusoft.doc.markdowndoc.editor.api.Editor;

import java.awt.*;
import java.util.Properties;

/**
 * Wraps properties for editor window position and size.
 */
public class FileWindowProps {
    //
    // Constants
    //

    private static final String X = "window.x";
    private static final String Y = "window.y";
    private static final String WIDTH = "window.width";
    private static final String HEIGHT = "window.height";

    //
    // Private Members
    //

    /** The properties we are wrapping. */
    private Properties props = null;

    //
    // Constructors
    //

    /**
     * Creates a new FileWindowProps instance.
     */
    public FileWindowProps() {
        this.props = new Properties();
    }

    //
    // Methods
    //

    /**
     * Saves the specified bounds in the properties.
     *
     * @param bounds The bounds to save.
     */
    public void setBounds(Rectangle bounds) {
        this.props.setProperty(X, "" + (int)bounds.getX());
        this.props.setProperty(Y, "" + (int)bounds.getY());
        this.props.setProperty(WIDTH, "" + (int)bounds.getWidth());
        this.props.setProperty(HEIGHT, "" + (int)bounds.getHeight());
    }

    /**
     * Returns the bounds in the properties as a Rectangle.
     */
    public Rectangle getBounds() {
        Rectangle rectangle = new Rectangle(
            Integer.valueOf(this.props.getProperty(X)),
            Integer.valueOf(this.props.getProperty(Y)),
            Integer.valueOf(this.props.getProperty(WIDTH)),
            Integer.valueOf(this.props.getProperty(HEIGHT))
        );

        return rectangle;
    }

    /**
     * Saves the properties.
     *
     * @param editor The editor to save for.
     */
    public void saveBounds(Editor editor) {
        if (editor.getCurrentFile() != null) {
            editor.getPersistentProps().save(editor.getCurrentFile().getName().replace(".", "_") +
                    "_bounds", this.props);
        }
    }

    /**
     * Loads the properties.
     *
     * @param editor The editor to load for.
     */
    public void load(Editor editor) {
        if (editor.getCurrentFile() != null) {
            this.props = editor.getPersistentProps().load(editor.getCurrentFile().getName().replace(".", "_") +
                    "_bounds");
            if (this.props == null) {
                this.props = new Properties();
            }
        }
    }

    /**
     * Returns true if there are properties available.
     */
    public boolean hasProperties() {
        return !this.props.isEmpty();
    }
}
