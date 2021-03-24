/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
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
 *         2012-11-16: Created!
 *
 */
package se.natusoft.doc.markdown.parser.markdown.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.model.DocItem
import se.natusoft.doc.markdown.model.List

/**
 * Adds markdown specifics to List.
 */
@CompileStatic
class MDList extends List {

    //
    // Properties
    //

    /** The indent level which determines hierarchy position. */
    int indentLevel

    //
    // Methods
    //

    /**
     * Returns true if the hierarchy should move one indentLevel down. This is only valid when isHierarchy is true.
     *
     * @param prevItem The previous item to compare to.
     */
    boolean isHierarchyDown(@NotNull final DocItem prevItem) {
        (prevItem instanceof  MDList) && this.indentLevel > ((MDList)prevItem).indentLevel
    }

    /**
     * Returns true if the hierarchy should move one indentLevel up. This is only valid when isHierarchy is true.
     *
     * @param prevItem The previous item to compare to.
     */
    boolean isHierarchyUp(@NotNull final DocItem prevItem) {
        (prevItem instanceof MDList) && this.indentLevel < ((MDList)prevItem).indentLevel
    }

    @Override
    @NotNull String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("List: ordered:${ordered}\n")
        sb.append(toString(this.indentLevel))

        sb.toString()
    }

    @NotNull String toString(final int indentLevel) {
        final StringBuilder sb = new StringBuilder()

        super.items.each {
            if (it instanceof MDList) {
                sb.append(((MDList)it).toString(((MDList)it).indentLevel))
            }
            else {
                (indentLevel as Integer).times { sb.append ' ' }
                sb.append(it.toString())
                sb.append("\n\n")
            }
        }

        sb.toString()
    }

}
