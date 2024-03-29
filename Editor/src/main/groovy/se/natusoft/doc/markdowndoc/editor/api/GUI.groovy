/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
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
package se.natusoft.doc.markdowndoc.editor.api

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

import javax.swing.*

/**
 * This interface provides an API towards the editorPane GUI.
 */
@CompileStatic
interface GUI {

    /**
     * Needed for popping upp dialogs.
     */
    @NotNull JFrame getWindowFrame()

    /**
     * Returns the panel above the editorPane and toolbar.
     */
    @NotNull JPanel getTopPanel()

    /**
     * Returns the panel below the editorPane and toolbar.
     */
    @NotNull JPanel getBottomPanel()

    /**
     * Returns the panel to the left of the editorPane and toolbar.
     */
    @NotNull JPanel getLeftPanel()

    /**
     * Returns the panel to the right of the editorPane and toolbar.
     */
    @NotNull JPanel getRightPanel()

    /**
     * Returns the editorPane panel. A toolbar can for example be added here!
     */
    @NotNull JPanel getEditorPanel()

    /**
     * Returns the y coordinate of the top of the scrollable editorPane view.
     */
    public int getEditorVisibleY()

    /**
     * Returns the styler for the editorPane.
     */
    @NotNull JTextComponentStyler getStyler()
}
