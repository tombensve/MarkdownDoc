/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.2
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
 *     tommy ()
 *         Changes:
 *         2015-08-03: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.api

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import javax.swing.JTextPane

/**
 * This represents an object that is editable. Basically a JTextPane and a File.
 */
@CompileStatic
@TypeChecked
interface Editable extends MouseMotionProvider {

    /**
     * A JTextPane instance that can be put in the editor main editing view for editing.
     */
    @Nullable JTextPane getEditorPane()

    /**
     * Returns the styler for this editable.
     */
    @Nullable JTextComponentStyler getStyler()

    /**
     * Sets saved state to true or false.
     *
     * @param state The state to set.
     */
    void setSaved(boolean state)

    /**
     * Returns the saved state.
     */
    boolean getSaved()

    /**
     * Loads a file and creates an editor pane. After this call getEditorPane() should be non null.
     * @param file
     *
     * @throws IOException
     */
    void load() throws IOException

    /**
     * Returns the loaded file or null if no file have been loaded.
     */
    @Nullable File getFile()

    /**
     * Saves the file.
     *
     * @throws IOException on failure to selectNewFile.
     */
    void save() throws IOException

}
