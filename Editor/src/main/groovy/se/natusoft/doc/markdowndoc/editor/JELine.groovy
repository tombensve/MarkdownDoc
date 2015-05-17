/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.9
 *     
 *     Description
 *         An editor that supports editing markdown with formatting preview.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2013-05-27: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor

import groovy.transform.CompileStatic
import se.natusoft.doc.markdowndoc.editor.api.Line

import javax.swing.text.BadLocationException
import javax.swing.text.JTextComponent

/**
 * Provides a Line implementation around a JEdtiorPane.
 */
@CompileStatic
public class JELine implements Line {
    //
    // Private Members
    //

    /** The JEditorPane we represent a line in. */
    private JTextComponent editor

    /** The starting position of the line */
    int startPos

    /** The ending position of the line. */
    int endPos

    //
    // Constructors
    //

    /**
     * Creates a new JELine.
     *
     * @param editor The editorPane we wrap.
     * @param startPos The starting position of the line.
     */
    public JELine(JTextComponent editor, int startPos) {
        this.editor = editor
        this.startPos = startPos

        this.endPos = this.startPos
        try {
            String check = this.editor.getText(this.endPos, 1)
            while (!check.equals("\n")) {
                ++this.endPos
                check = this.editor.getText(this.endPos, 1)
            }
        }
        catch (BadLocationException ble) {
            //--this.endPos
            if (this.endPos < this.startPos) {
                this.endPos = this.startPos
            }
        }
    }

    //
    // Methods
    //

    private boolean isEmpty() {
        return this.startPos == this.endPos
    }

    /**
     * Returns the text of the line.
     */
    public String getText() throws BadLocationException {
        return this.editor.getText(this.startPos, this.endPos - this.startPos)
    }

    /**
     * Sets the text of the line, replacing any previous text.
     *
     * @param text The text to set.
     */
    public void setText(String text) {
        int currSelStart = this.editor.getSelectionStart()
        int currSelEnd = this.editor.getSelectionEnd()
        this.editor.select(this.startPos, this.endPos)
        this.editor.replaceSelection(text)
        this.editor.select(currSelStart, currSelEnd)
    }

    /**
     * Returns the next line or null if this is the last line.
     */
    public Line getNextLine() {
        try {
            this.editor.getText(this.endPos + 1, 1) // Verify!
            return new JELine(this.editor, this.endPos + 1)
        }
        catch (BadLocationException ble) {
            return null
        }
    }

    /**
     * Returns the previous line or null if this is the first line.
     */
    public Line getPreviousLine() {
        try {
            this.editor.getText(this.startPos - 2, 1)
        }
        catch (BadLocationException ble) {
            return null
        }

        int sp
        try {
            sp = this.startPos - 2
            String check = this.editor.getText(sp, 1)
            while (!check.equals("\n")) {
                --sp
                check = this.editor.getText(sp, 1)
            }
            ++sp
        }
        catch (BadLocationException ble) {
            sp = 0
        }

        return new JELine(this.editor, sp)
    }

    /**
     * Return true if the line is the first line.
     */
    public boolean isFirstLine() {
        return this.startPos == 0
    }

    /**
     * Returns true if this line is the last line.
     */
    public boolean isLastLine() {
        try {
            int pos = this.startPos
            while (true) {
                String check = this.editor.getText(pos, 1)
                if (check.equals("\n")) {
                    return false
                }
                ++pos
            }
        }
        catch (BadLocationException ble) {
            return true
        }
    }

    /**
     * Returns the position of the beginning of the line.
     */
    public int getLineStartPost() {
        return this.startPos
    }

    /**
     * Returns the position of the end of the line.
     */
    public int getLineEndPos() {
        return this.endPos
    }

    /**
     * Same as getText().
     */
    public String toString() {
        try {
            return getText()
        }
        catch (BadLocationException ble) {
            return "<Bad line!>"
        }
    }
}
