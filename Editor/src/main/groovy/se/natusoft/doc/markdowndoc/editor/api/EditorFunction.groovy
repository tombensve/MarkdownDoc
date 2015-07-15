/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.4
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
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*

/**
 * This represents an editorPane function.
 */
@CompileStatic
@TypeChecked
interface EditorFunction extends EditorComponent {

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    @NotNull String getGroup()

    /**
     * Returns the name of the function.
     */
    @NotNull String getName()

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Nullable JComponent getToolBarButton()

    /**
     * Returns the keyboard shortcut for triggering the function via keyboard.
     */
    @NotNull KeyboardKey getKeyboardShortcut()

    /**
     * Performs the function.
     *
     * @throws FunctionException
     */
    void perform() throws FunctionException

}
