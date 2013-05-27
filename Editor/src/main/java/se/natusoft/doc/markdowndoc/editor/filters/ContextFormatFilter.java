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
                System.out.println("Context: currentLine: " + currentLine.getText());
                Line prevLine = currentLine;

                if (prevLine != null) {
                    prevLine = prevLine.getPreviousLine();
                }

                if (prevLine != null) {
                    System.out.println("Context: workLine: " + prevLine.getText());
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
        }
        catch (BadLocationException ble) {
            ble.printStackTrace(System.err);
        }
    }
}
