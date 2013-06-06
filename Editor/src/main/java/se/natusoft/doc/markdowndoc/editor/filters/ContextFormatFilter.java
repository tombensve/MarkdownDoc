/* 
 * 
 * PROJECT
 *     Name
 *         Editor
 *     
 *     Code Version
 *         1.2.6
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
package se.natusoft.doc.markdowndoc.editor.filters;

import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorInputFilter;
import se.natusoft.doc.markdowndoc.editor.api.Line;

import javax.swing.text.BadLocationException;
import java.awt.event.KeyEvent;

/**
 * This filter provides context help in the editor.
 */
public class ContextFormatFilter implements EditorInputFilter {
    //
    // Private Members
    //

    private Editor editor;

    //
    // Methods
    //

    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        try {
            // Catch new lines
            if (keyEvent.getKeyChar() == '\n') {
                Line currentLine = this.editor.getCurrentLine();
                //System.out.println("Context: currentLine: " + currentLine.getText());
                Line prevLine = currentLine;

                if (prevLine != null) {
                    prevLine = prevLine.getPreviousLine();
                }

                if (prevLine != null) {
                    //System.out.println("Context: workLine: " + prevLine.getText());
                    String trimmedLine = prevLine.getText().trim();

                    // -- Handle list bullets --

                    // If the previous line only contains a list bullet and no text, blank the line.
                    if (trimmedLine.equals("*")) {
                        prevLine.setText("");
                    }
                    // Otherwise start the new line with a new list bullet.
                    else {
                        if (trimmedLine.startsWith("* ")) {
                            int indentPos = prevLine.getText().indexOf('*');
                            StringBuilder newLine = new StringBuilder();
                            while (indentPos > 0) {
                                newLine.append(' ');
                                --indentPos;
                            }
                            newLine.append("* ");
                            currentLine.setText(newLine.toString());
                            this.editor.moveCaretForward(newLine.length());
                        }
                    }

                    // -- Handle quotes --

                    // If the previous line only contains a quote (>) char and no text, blank the line
                    if (trimmedLine.equals(">")) {
                        prevLine.setText("");
                    }
                    // Otherwise start the new line with a quote (>) character.
                    else {
                        if (trimmedLine.startsWith("> ")) {
                            int indentPos = prevLine.getText().indexOf('>');
                            StringBuilder newLine = new StringBuilder();
                            while (indentPos > 0) {
                                newLine.append(' ');
                                --indentPos;
                            }
                            newLine.append("> ");
                            currentLine.setText(newLine.toString());
                            this.editor.moveCaretForward(newLine.length());
                        }
                    }
                }
            }
            else if (keyEvent.getKeyChar() == '\t') {
                // For some extremely strange reason the key-up event gets triggered twice
                // when shift is pressed! And to make things even more strange, the second
                // time the caret is at position 0, making "this.editor.getCurrentLine()"
                // return the first line, and then thus consequently "line.getPreviousLine()"
                // will of course return null. In the end when all event have been processed
                // the caret remains where it was and should be. This is extremely annoying
                // and this is a very crappy workaround for the lack of a better one!
                // A side effect of this workaround is that list indent level change with tab
                // and shift tab does not work on the first line. This is a minor inconvenience
                // since you usually don't start with a list on the first line.
                Line line = this.editor.getCurrentLine();
                if (!line.isFirstLine()) {
                    if (keyEvent.isShiftDown()) {
                        // JEditorPane does something weird on shift-tab
                        line = line.getPreviousLine();
                    }
                    if (line.getText().trim().startsWith("*")) {
                        if (keyEvent.isShiftDown()) {
                            if (line.getText().startsWith("   ")) {
                                line.setText(line.getText().substring(3));
                                // The moving of the caret in this case seems to happen automatically!
                                //this.editor.moveCaretBack(3);
                            }
                        }
                        else {
                            line.setText("   " + line.getText().substring(0, line.getText().length() - 1));
                            this.editor.moveCaretForward(3);
                        }
                    }
                }
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace(System.err);
        }
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
