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
 *         2014-10-12: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull

import javax.swing.*

/**
 * Base of all field values in PDFData
 */
@CompileStatic
@TypeChecked
abstract class ExportDataValue {

    //
    // Properties
    //

    /** The label for the value. */
    @NotNull JLabel labelComp

    /** The component for editing the value. */
    @NotNull JComponent valueComp

    //
    // Constructors
    //

    /**
     * Creates a new ExportDataValue.
     *
     * @param labelText The label text for this value.
     */
    ExportDataValue(@NotNull final String labelText) {
        this.labelComp = new JLabel("    " + labelText + " ")
    }

    //
    // Methods
    //

    /**
     * Returns a unique key for storing this value.
     */
    @NotNull String getKey() {
        this.labelComp.getText().trim().toLowerCase().replaceAll(" ", "-").replaceAll(":", "")
    }

    /**
     * This must be overridden by subclasses that wants to delay the creation of the valueComp.
     */
    protected @NotNull JComponent ensureValueComp() {
        this.valueComp
    }

    /**
     * Returns the value of this export data value.
     */
    abstract @NotNull String getValue()

    /**
     * Sets the value of this export data value.
     *
     * @param value The value to set.
     */
    abstract void setValue(@NotNull String value)
}
