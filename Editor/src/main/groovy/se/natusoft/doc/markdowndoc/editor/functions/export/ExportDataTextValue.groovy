/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.1.1
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

import javax.swing.JTextField

/**
 * PDFData text valueComp fields.
 */
@CompileStatic
@TypeChecked
class ExportDataTextValue extends ExportDataValue {

    //
    // Constructors
    //

    /**
     * Creates a new ExportDataTextValue.
     *
     * @param labelText The label for the value.
     */
    ExportDataTextValue(@NotNull final String labelText) {
        super(labelText)
        setValueComp(new JTextField(25))
    }

    /**
     * Creates a new ExportDataTextValue.
     *
     * @param labelText The label for the value.
     * @param defaultValue The default text.
     */
    ExportDataTextValue(@NotNull final String labelText, @NotNull final String defaultValue) {
        this(labelText)
        setValue(defaultValue)
    }

    //
    // Methods
    //

    /**
     * Returns the current text.
     */
    @NotNull String getValue() {
        ((JTextField)ensureValueComp()).getText()
    }

    /**
     * Sets the current text.
     *
     * @param value The new text to set.
     */
    void setValue(@NotNull final String value) {
        ((JTextField)ensureValueComp()).setText(value)
    }
}
