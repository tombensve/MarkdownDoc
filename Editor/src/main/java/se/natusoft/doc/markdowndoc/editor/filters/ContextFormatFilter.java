/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.2.10
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

                if (currentLine != null) {
                    //System.out.println("Context: workLine: " + prevLine.getText());
                    String trimmedLine = currentLine.getText().trim();

                    // -- Handle list bullets --

                    // If the previous line only contains a list bullet and no text, blank the line.
                    if (trimmedLine.equals("*")) {
                        currentLine.setText("");
                    }
                    // Otherwise start the new line with a new list bullet.
                    else {
                        if (trimmedLine.startsWith("* ")) {
                            int indentPos = currentLine.getText().indexOf('*');
                            StringBuilder newLine = new StringBuilder();
                            while (indentPos > 0) {
                                newLine.append(' ');
                                --indentPos;
                            }
                            newLine.append("* ");
                            Line nextLine = currentLine.getNextLine();
                            if (nextLine != null) {
                                nextLine.setText(newLine.toString());
                                this.editor.moveCaretForward(newLine.length());
                            }
                        }
                    }

                    // -- Handle quotes --

                    // If the previous line only contains a quote (>) char and no text, blank the line
                    if (trimmedLine.equals(">")) {
                        currentLine.setText("");
                    }
                    // Otherwise start the new line with a quote (>) character.
                    else {
                        if (trimmedLine.startsWith("> ")) {
                            int indentPos = currentLine.getText().indexOf('>');
                            StringBuilder newLine = new StringBuilder();
                            while (indentPos > 0) {
                                newLine.append(' ');
                                --indentPos;
                            }
                            newLine.append("> ");
                            Line nextLine = currentLine.getNextLine();
                            if (nextLine != null) {
                                nextLine.setText(newLine.toString());
                                this.editor.moveCaretForward(newLine.length());
                            }
                        }
                    }
                }
            }
            else if (keyEvent.getKeyChar() == '\t') {
                Line line = this.editor.getCurrentLine();
                boolean isLastLine = line.isLastLine();
                if (keyEvent.isShiftDown()) {
                    // JEditorPane does something weird on shift-tab
                    if (isLastLine) {
                        line = line.getPreviousLine();
                    }
                }
                if (line.getText().trim().startsWith("*")) {
                    if (keyEvent.isShiftDown()) {
                        if (line.getText().startsWith("   ")) {
                            line.setText(line.getText().substring(3));
                            if (!isLastLine) {
                                this.editor.moveCaretBack(3);
                            }
                        }
                    }
                    else {
                        int startPos = line.getLineStartPost();
                        int caretLoc = this.editor.getCaretDot();
                        int tabIx = line.getText().indexOf("\t");
                        int moveChars = 3;

                        if ((startPos + tabIx) <= caretLoc) {
                            moveChars = 2;
                        }

                        line.setText("   " + line.getText().replace("\t", ""));
                        this.editor.moveCaretForward(moveChars);
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
