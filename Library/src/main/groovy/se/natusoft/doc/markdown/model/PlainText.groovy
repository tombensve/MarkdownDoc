/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.2.5
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

/**
 * This represents plain text.
 */
public class PlainText extends DocItem {
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
    public void addItem(String text) {
        if (text != null) {
            this.text = this.text + text
        }
    }

    /**
     * Sets the specified text replacing anything that was there before.
     *
     * @param text The text to set.
     */
    public void setText(String text) {
        this.text = text
    }

    /**
     * Returns the format this model represents.
     */
    @Override
    public DocFormat getFormat() {
        return DocFormat.PlainText
    }

    @Override
    public boolean validate() {
        return this.text != null && this.text.length() > 0;
    }

    @Override
    public String toString() {
        return this.text
    }
}

