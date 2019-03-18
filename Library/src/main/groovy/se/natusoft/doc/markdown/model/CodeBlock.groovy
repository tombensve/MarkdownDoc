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
 *         2012-10-29: Created!
 *
 */
package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull

/**
 * This represents a block of code that is pre-formatted.
 */
@CompileStatic
@TypeChecked
class CodeBlock extends DocFormatItem {

    //
    // Constructors
    //

    /**
     * Creates a new CodeBlock.
     */
    CodeBlock() {
        keepConsecutiveTogether = true
        addBetweenKeepTogether = "    "
    }

    //
    // Methods
    //

    /**
     * Returns the format this model represents.
     */
    @Override
    @NotNull DocFormat getFormat() {
        DocFormat.CodeBlock
    }

    /**
     * Validates this CodeBlock.
     *
     * @return true if valid.
     */
    @Override
    boolean validate() {
        super.items.size() > 0
    }

    /**
     * Returns a string representation of this model for debugging purposes.
     */
    @NotNull String toString() {
        final StringBuilder sb = new StringBuilder()
        sb.append("CodeBlock:\n")
        super.items.each { final DocItem item ->
            sb.append(item.toString())
            sb.append("\n")
        }

        sb.toString()
    }
}
