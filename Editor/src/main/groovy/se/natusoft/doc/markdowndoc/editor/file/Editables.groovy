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
 *     tommy ()
 *         Changes:
 *         2015-08-03: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.file

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.api.Editable

/**
 * This handles all Editables.
 */
@CompileStatic
@TypeChecked
@ToString(includeNames = true)
class Editables extends HashMap<File, Editable> {

    /**
     * A singleton instance.
     */
    static Editables inst = new Editables()

    //
    // Methods
    //

    /**
     * Adds an editable to the editables.
     *
     * @param editable The editable to add.
     */
    void addEditable(final Editable editable) {
        put(editable.file, editable)
    }

    /**
     * Removes the specifed Editable from the Editables.
     *
     * @param file The file referencing the editable to remove.
     */
    void removeEditable(final File file) {
        remove(file)
    }

    /**
     * Returns the Editable for the specified File, or null if not found.
     *
     * @param file The File to get Editable for.
     */
    @Nullable Editable getEditable(final File file) {
        get(file)
    }

    @Nullable Editable getFirstEditable() {
        if (empty) {
            return null
        }
        get(files.first())
    }

    /**
     * Returns all files.
     */
    Set<File> getFiles() {
        keySet()
    }

    /**
     * Returns the number of editables.
     */
    int getCount() {
        return size()
    }

    /**
     * Returns the editable at the specified position.
     *
     * @param pos The position to get editable at.
     */
    Editable getByPosition(final int pos) {
        final File file = keySet().getAt(pos)
        return get(file)
    }
}
