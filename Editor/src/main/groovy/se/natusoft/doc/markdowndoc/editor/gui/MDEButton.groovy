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
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic

import javax.swing.*

/**
 * Button component that allows for specifying text color.
 */
@CompileStatic
class MDEButton extends JButton {

    //
    // Private Members
    //

    private String color = null

    //
    // Methods
    //

    /**
     * Sets the text of the component.
     *
     * @param text The text to set.
     */
    @Override
    void setText(final String text) {
        // The groovy compiler (2.4.3 & 2.4.4) fails compiling with a BUG! message when property access
        // is used for "super.text = ...". setText(...) however compiles fine.
        if (this.color != null) {
            super.setText("<html><font color='${this.color}'>${text}</font></html>")
        }
        else {
            super.setText(text)
        }
    }

    /**
     * Sets the color for the component.
     *
     * @param color The color to set.
     */
    void setTextColor(final String color) {
        this.color = color
        if (super.text != null && !super.text.empty) {
            this.text = super.text
        }
    }
}
