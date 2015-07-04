/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Code Version
 *         1.3.9
 *
 *     Description
 *         Parses markdown and generates HTML and PDF.
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
 *         2012-10-28: Created!
 *
 */
package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents plain text.
 */
@CompileStatic
@TypeChecked
class PlainText extends DocItem {
    //
    // Private Members
    //

    /** The text of this item. */
    String text = ""

    //
    // Methods
    //

    /**
     * Adds text to this plain text.
     *
     * @param text The text to add.
     */
    @Override
    void addItem(String text) {
        if (text != null) {
            this.text = this.text + text
        }
    }

    /**
     * Sets the specified text replacing anything that was there before.
     *
     * @param text The text to set.
     */
    void setText(String text) {
        this.text = text
    }

    /**
     * Returns the format this model represents.
     */
    @Override
    DocFormat getFormat() {
        DocFormat.PlainText
    }

    @Override
    boolean validate() {
        this.text != null && this.text.length() > 0;
    }

    @Override
    String toString() {
        this.text
    }
}

