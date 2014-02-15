package se.natusoft.doc.markdowndoc.editor.api;

import java.awt.event.MouseMotionListener;

/**
 * Indicates provision of mouse motion events.
 */
public interface MouseMotionProvider {

    /**
     * Adds a mouse motion listener to receive mouse motion events.
     *
     * @param listener The listener to add.
     */
    void addMouseMotionListener(MouseMotionListener listener);

    /**
     * Removes a mouse motion listener.
     *
     * @param listener The listener to remove.
     */
    void removeMouseMotionListener(MouseMotionListener listener);
}
