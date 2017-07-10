/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.0.2
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
import org.jetbrains.annotations.NotNull

/**
 * This represents a list of text items.
 */
@CompileStatic
@TypeChecked
class List extends DocFormatItem {
    //
    // Private Members
    //

    /** Set to true for ordered list, false for unordered list. */
    boolean ordered = false;

    //
    // Constructors
    //

    /**
     * Creates a new ListItem.
     *
     * @param ordered true for an ordered list, false for an unordered.
     */
    List() {
        keepConsecutiveTogether = true
        isHierarchy = true
    }

    //
    // Methods
    //

    @Override
    boolean validate() {
        super.items.size() > 0
    }

    /**
     * Returns true if the passed docItem is the same type as this docItem.
     *
     * @param docItem The DocItem to test.
     */
    boolean isSameType(final DocItem docItem) {
        super.isSameType(docItem) && ((List)docItem).ordered == this.ordered
    }

    /**
     * Returns the format this model represents.
     */
    @Override
    @NotNull DocFormat getFormat() {
        DocFormat.List
    }

    @Override
    @NotNull String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("List: ordered:${ordered}\n")
        sb.append(toString(0))

        sb.toString()
    }

    @NotNull String toString(final int indentLevel) {
        final  sb = new StringBuilder()

        super.items.each {
            if (it instanceof List) {
                sb.append(((List)it).toString(indentLevel + 2))
            }
            else {
                indentLevel.times { sb.append ' ' }
                sb.append(it.toString())
                sb.append("\n\n")
            }
        }

        sb.toString()
    }
}
