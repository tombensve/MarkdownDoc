/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.7
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
 *         2014-02-15: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.api

import groovy.transform.CompileStatic

/**
 * Defines a toolbar API.
 */
@CompileStatic
public interface ToolBar {
    /**
     * Adds a function to the toolbar.
     *
     * @param function The function to add.
     */
    void addFunction(EditorFunction function)

    /**
     * Creates the content of the toolbar. This cannot be done until all
     * functions have been provided.
     */
    void createToolBarContent()

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to enable.
     */
    void disableGroup(String group)

    /**
     * Disables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to disable.
     */
    void enableGroup(String group)

    /**
     * Provides the toolbar with the editorPane it is associated.
     *
     * @param editor The associated editorPane provided.
     */
    void attach(Editor editor)

    /**
     * Removes the association with the editorPane.
     *
     * @param editor The editorPane to detach from.
     */
    void detach(Editor editor)
}
