package se.natusoft.doc.markdowndoc.editor.api;

import javax.swing.text.BadLocationException;

/**
 * This represents an editor line.
 */
public interface Line {

    /**
     * Returns the text of the line.
     */
    String getText() throws BadLocationException;

    /**
     * Sets the text of the line, replacing any previous text.
     *
     * @param text The text to set.
     */
    void setText(String text);

    /**
     * Returns the next line or null if this is the last line.
     */
    Line getNextLine();

    /**
     * Returns the previous line or null if this is the first line.
     */
    Line getPreviousLine();

    /**
     * Same as getText().
     */
    String toString();
}