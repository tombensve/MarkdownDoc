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
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.api.ToolBar

import javax.swing.*
import java.awt.*
import java.util.List

/**
 * This is the editorPane toolbar.
 * <p/>
 * Note: For some reason, when this toolbar is used, it steals the focus away for the JTextPanel editor!
 *       It is impossible to type in the editor when this toolbar is used! I haven't figured out why yet
 *       so this toolbar is currently not part of the available toolbars.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
class WindowToolBar extends JToolBar implements ToolBar {
    //
    // Private Members
    //

    private List<String> toolBarGroups = new LinkedList<>()

    private Map<String, List<EditorFunction>> functions = new HashMap<>()

    //
    // Constructors
    //

    WindowToolBar() {
        setRollover(true)
        setFocusable(false)
    }

    //
    // Methods
    //

    /**
     * Adds a function to the toolbar.
     *
     * @param function The function to add.
     */
    @Override
    void addFunction(@NotNull final EditorFunction function) {
        if (!this.toolBarGroups.contains(function.getGroup())) {
            this.toolBarGroups.add(function.getGroup())
        }

        List<EditorFunction> groupFunctions = this.functions.get(function.getGroup())
        if (groupFunctions == null) {
            groupFunctions = new LinkedList<>()
            this.functions.put(function.getGroup(), groupFunctions)
        }

        groupFunctions.add(function)
    }

    /**
     * Creates the content of the toolbar. This cannot be done until all
     * functions have been provided.
     */
    @SuppressWarnings("UnnecessaryQualifiedReference")
    @Override
    void createToolBarContent() {
        final Iterator<String> groupIterator = this.toolBarGroups.iterator()
        while (groupIterator.hasNext()) {
            String group = groupIterator.next()

            List<EditorFunction> functions = this.functions.get(group)
            functions.each { final EditorFunction function ->
                add(function.getToolBarButton())
            }

            if (groupIterator.hasNext()) {
                add(new JToolBar.Separator())
            }
        }
    }

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to enable.
     */
    @Override
    void disableGroup(@NotNull final String group) {
        final List<EditorFunction> functions = this.functions.get(group)
        if (functions != null) {
            functions.each { final EditorFunction function ->
                function.getToolBarButton().setEnabled(false)
            }
        }
        else {
            throw new RuntimeException("Cannot disable non existent group '" + group + "'!")
        }
    }

    /**
     * Disables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to disable.
     */
    @Override
    void enableGroup(@NotNull final String group) {
        final List<EditorFunction> functions = this.functions.get(group)
        if (functions != null) {
            functions.each { final EditorFunction function ->
                function.getToolBarButton().setEnabled(true)
            }
        }
        else {
            throw new RuntimeException("Cannot enable non existent group '" + group + "'!")
        }
    }

    /**
     * Provides the toolbar with the editorPane it is associated.
     *
     * @param editor The associated editorPane provided.
     */
    @Override
    void attach(@NotNull final Editor editor) {
        editor.getGUI().getEditorPanel().add(this, BorderLayout.NORTH)
    }

    /**
     * Removes the association with the editorPane.
     *
     * @param editor The editorPane to detach from.
     */
    @Override
    void detach(@NotNull final Editor editor) {
        editor.getGUI().getEditorPanel().remove(this)
    }

}
