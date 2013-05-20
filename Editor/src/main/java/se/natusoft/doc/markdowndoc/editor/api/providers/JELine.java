package se.natusoft.doc.markdowndoc.editor.api.providers;

import se.natusoft.doc.markdowndoc.editor.api.Line;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;

/**
 * Provides a Line implementation around a JEdtiorPane.
 */
public class JELine implements Line {
    //
    // Private Members
    //

    /** The JEditorPane we represent a line in. */
    private JEditorPane editor;

    /** The starting position of the line */
    int startPos;

    /** The ending position of the line. */
    int endPos;

    //
    // Constructors
    //

    /**
     * Creates a new JELine.
     *
     * @param editor The editor we wrap.
     * @param startPos The starting position of the line.
     */
    public JELine(JEditorPane editor, int startPos) {
        this.editor = editor;
        this.startPos = startPos;

        this.endPos = this.startPos;
        try {
            String check = this.editor.getText(this.endPos, 1);
            while (!check.equals("\n")) {
                ++this.endPos;
                check = this.editor.getText(this.endPos, 1);
            }
        }
        catch (BadLocationException ble) {}
    }

    //
    // Methods
    //

    private boolean isEmpty() {
        return this.startPos == this.endPos;
    }

    /**
     * Returns the text of the line.
     */
    public String getText() throws BadLocationException {
        return this.editor.getText(this.startPos, this.endPos - this.startPos);
    }

    /**
     * Sets the text of the line, replacing any previous text.
     *
     * @param text The text to set.
     */
    public void setText(String text) {
        int currSelStart = this.editor.getSelectionStart();
        int currSelEnd = this.editor.getSelectionEnd();
        this.editor.select(this.startPos, this.endPos);
        this.editor.replaceSelection(text);
        this.editor.select(currSelStart, currSelEnd);
    }

    /**
     * Returns the next line or null if this is the last line.
     */
    public Line getNextLine() {
        try {
            String verify = this.editor.getText(this.endPos + 2, 1);
            return new JELine(this.editor, this.endPos + 2);
        }
        catch (BadLocationException ble) {
            return null;
        }
    }

    /**
     * Returns the previous line or null if this is the first line.
     */
    public Line getPreviousLine() {
        try {
            String verify = this.editor.getText(this.startPos - 2, 1);
        }
        catch (BadLocationException ble) {
            return null;
        }

        int sp;
        try {
            sp = this.startPos - 2;
            String check = this.editor.getText(sp, 1);
            while (!check.equals("\n")) {
                --sp;
                check = this.editor.getText(sp, 1);
            }
            ++sp;
        }
        catch (BadLocationException ble) {
            sp = 0;
        }

        return new JELine(this.editor, sp);
    }

    /**
     * Same as getText().
     */
    public String toString() {
        try {
            return getText();
        }
        catch (BadLocationException ble) {
            return "<Bad line!>";
        }
    }
}
