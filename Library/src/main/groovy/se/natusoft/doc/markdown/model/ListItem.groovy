/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.2.2
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
 * This represents a list of text items.
 */
public class ListItem extends DocItem {

    /**
     * Handle bad usage.
     *
     * @param item
     */
    public void addItem(DocItem item) {
        throw new IllegalArgumentException("A ListItem can only take Paragraph objects!")
    }

    /**
     * Adds a paragraph to this list item.
     *
     * @param paragraph The paragraph to add.
     */
    public void addItem(Paragraph paragraph) {
        super.addItem(paragraph)
    }

    /**
     * Validates this ListItem.
     */
    @Override
    public boolean validate() {
        return super.items.size() > 0
    }

    /**
     * Return as indented string.
     *
     * @param indentLevel The indentation size.
     */
    public String toString(int indentLevel) {
        StringBuilder sb = new StringBuilder()

        for (DocItem paraItem : super.items) {
            indentLevel.times { sb.append(' ') }
            sb.append(paraItem.toString())
        }
        sb.toString()
    }
}
