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

import javax.swing.JCheckBox

/**
 * PDFData boolean valueComp fields.
 */
@CompileStatic
@TypeChecked
class ExportDataSelectValue extends ExportDataValue {

    //
    // Constructors
    //

    /**
     * Creates a new ExportDataSelectValue.
     *
     * @param labelText The text in the label.
     */
    ExportDataSelectValue(@NotNull final String labelText) {
        super(labelText)
        super.valueComp = new JCheckBox()
    }

    /**
     * Creates a new ExportDataSelectValue.
     *
     * @param labelText The text in the label.
     * @param defaultValue The default value.
     */
    ExportDataSelectValue(@NotNull final String labelText, final boolean defaultValue) {
        this(labelText)
        ((JCheckBox)ensureValueComp()).setSelected(defaultValue)
    }

    //
    // Methods
    //

    /**
     * Returns the current value.
     */
    String getValue() {
        "" + ((JCheckBox)super.valueComp).isSelected()
    }

    /**
     * Sets the current value.
     *
     * @param value The value to set.
     */
    void setValue(@NotNull final String value) {
        final boolean selected = Boolean.valueOf(value)
        ((JCheckBox)ensureValueComp()).setSelected(selected)
    }
}
