package se.natusoft.doc.markdowndoc.editor.api;

import java.awt.event.KeyEvent;

/**
 * This receives key presses and an editor and can manipulate the editor
 * based on keys pressed.
 */
public interface EditorInputFilter {

    /**
     * Provide the filter with an Editor instance.
     *
     * @param editor The editor instance to provide.
     */
    void setEditor(Editor editor);

    /**
     * Receives a key event.
     *
     * @param keyEvent The key event.
     */
    void keyPressed(KeyEvent keyEvent);
}