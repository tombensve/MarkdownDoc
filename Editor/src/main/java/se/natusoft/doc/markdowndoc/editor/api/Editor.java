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
package se.natusoft.doc.markdowndoc.editor.api;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

/**
 * This is passed to EditorFunction.perform(...).
 */
public interface Editor {

    /**
     * Returns the editor GUI API.
     */
    GUI getGUI();

    /**
     * Returns the config API.
     */
    Config getConfig();

    /**
     * Returns the current file or null if none.
     */
    File getCurrentFile();

    /**
     * Sets the current file.
     *
     * @param file The file to set.
     */
    void setCurrentFile(File file);

    /**
     * Returns the contents of the editor.
     */
    String getEditorContent();

    /**
     * Returns the current selection or null if none.
     */
    String getEditorSelection();

    /**
     * Returns the current line.
     */
    Line getCurrentLine() throws BadLocationException;

    /**
     * Set/replace the entire content of the editor.
     *
     * @param content The new content to set.
     */
    void setEditorContent(String content);

    /**
     * Returns the current caret location.
     */
    Point getCaretLocation();

    /**
     * Moves the current caret location.
     *
     * @param location The new location.
     */
    void setCaretLocation(Point location);

    /**
     * Makes the editor view visible in the main scrollable view.
     */
    void showEditorComponent();

    /**
     * Makes the specified component visible in the main scrollable view.
     */
    void showOtherComponent(JComponent component);

    /**
     * Inserts new text into the editor or replaces current selection.
     *
     * @param text The text to insert.
     */
    void insertText(String text);

    /**
     * Moves the cared backwards.
     *
     * @param noChars The number of characters to move caret.
     */
    void moveCaretBack(int noChars);

    /**
     * Moves the caret forward.
     *
     * @param noChars The number of characters to move caret.
     */
    void moveCaretForward(int noChars);

    /**
     * Requests focus for the editor.
     */
    void requestEditorFocus();

    /**
     * Opens a new editor window.
     */
    void openNewEditorWindow();

    /**
     * Copies the currently selected text.
     */
    void copy();

    /**
     * Cuts the currently selected text.
     */
    void cut();

    /**
     * Pastes the currently copied/cut text.
     */
    void paste();

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to enable.
     */
    void enableToolBarGroup(String groupName);

    /**
     * Disables all button in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to disable.
     */
    void disableToolBarGroup(String groupName);

    /**
     * Opens the specified file in the editor.
     *
     * @param file The file to open.
     *
     * @throws java.io.IOException
     */
    public void loadFile(File file) throws IOException;

}
