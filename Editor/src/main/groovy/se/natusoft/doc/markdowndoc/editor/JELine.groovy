/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.1
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
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.api.Line

import javax.swing.text.BadLocationException
import javax.swing.text.JTextComponent

/**
 * Provides a Line implementation around a JEdtiorPane.
 */
@CompileStatic
@TypeChecked
class JELine implements Line {
    //
    // Private Members
    //

    /** The JEditorPane we represent a line in. */
    private JTextComponent editor

    //
    // Properties
    //

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
    JELine(@NotNull final JTextComponent editor, final int startPos) {
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
        catch (final BadLocationException ignored) {
            //--this.endPos
            if (this.endPos < this.startPos) {
                this.endPos = this.startPos
            }
        }
    }

    //
    // Methods
    //

    /**
     * Returns the text of the line.
     */
    @NotNull String getText() throws BadLocationException {
        this.editor.getText(this.startPos, this.endPos - this.startPos)
    }

    /**
     * Sets the text of the line, replacing any previous text.
     *
     * @param text The text to set.
     */
    void setText(@NotNull final String text) {
        final int currSelStart = this.editor.getSelectionStart()
        final int currSelEnd = this.editor.getSelectionEnd()
        this.editor.select(this.startPos, this.endPos)
        this.editor.replaceSelection(text)
        this.editor.select(currSelStart, currSelEnd)
    }

    /**
     * Returns the next line or null if this is the last line.
     */
    @Nullable Line getNextLine() {
        try {
            this.editor.getText(this.endPos + 1, 1) // Verify!
            new JELine(this.editor, this.endPos + 1)
        }
        catch (final BadLocationException ignore) {
            null
        }
    }

    /**
     * Returns the previous line or null if this is the first line.
     */
    @Nullable Line getPreviousLine() {
        try {
            this.editor.getText(this.startPos - 2, 1)
        }
        catch (final BadLocationException ignore) {
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
        catch (final BadLocationException ignore) {
            sp = 0
        }

        new JELine(this.editor, sp)
    }

    /**
     * Return true if the line is the first line.
     */
    boolean isFirstLine() {
        this.startPos == 0
    }

    /**
     * Returns true if this line is the last line.
     */
    boolean isLastLine() {
        boolean _lastLine = true
        try {
            int pos = this.startPos
            while (true) {
                String check = this.editor.getText(pos, 1)
                if (check.equals("\n")) {
                    _lastLine = false
                    break
                }
                ++pos
            }
        }
        catch (final BadLocationException ignore) {}

        _lastLine
    }

    /**
     * Returns the position of the beginning of the line.
     */
    int getLineStartPost() {
        this.startPos
    }

    /**
     * Returns the position of the end of the line.
     */
    int getLineEndPos() {
        this.endPos
    }

    /**
     * Same as getText().
     */
    @NotNull String toString() {
        try {
            getText()
        }
        catch (final BadLocationException ignore) {
            "<Bad line!>"
        }
    }
}
