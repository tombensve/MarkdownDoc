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
 *         2014-10-12: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull

import javax.swing.JComponent

@CompileStatic
@TypeChecked
/**
 * Manages a FileSelector and the path for loading and saving.
 */
class ExportFileValue extends ExportDataValue {

    //
    // Private Members
    //

    /** The name of the file to handle. */
    private String whatFile

    //
    // Properties
    //

    /** Information needed to read and write files among other things. */
    @NotNull DelayedServiceData delayedServiceData

    //
    // Constructors
    //

    /**
     * Creates a new ExportFileValue instance.
     *
     * @param labelText The label to show for this.
     * @param whatFile A file path.
     */
    ExportFileValue(String labelText, String whatFile) {
        super(labelText)
        this.whatFile = whatFile
    }

    /**
     * Provides a FileSelector instance.
     */
    @Override
    protected @NotNull JComponent ensureValueComp() {
        if (super.valueComp == null) {
            super.valueComp = new FileSelector(this.whatFile, delayedServiceData)
        }

        super.valueComp
    }

    /**
     * Returns the FileSelector file.
     */
    @NotNull String getValue() {
        if (whatFile == null || delayedServiceData == null) {
            throw new IllegalStateException("'whatFile' and 'gui' properties must have been provided before this call" +
                    " can be made!")
        }
        ((FileSelector)ensureValueComp()).getFile()
    }

    /**
     * Sets the FileSelector file.
     *
     * @param value The file path to set.
     */
    void setValue(@NotNull String value) {
        ((FileSelector)ensureValueComp()).setFile(value)
    }
}
