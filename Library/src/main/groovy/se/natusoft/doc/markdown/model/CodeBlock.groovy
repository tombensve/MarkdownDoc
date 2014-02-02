/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.3
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

/**
 * This represents a block of code that is pre-formatted.
 */
@CompileStatic
public class CodeBlock extends DocItem {

    //
    // Constructors
    //

    /**
     * Creates a new CodeBlock.
     */
    public CodeBlock() {
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
    public DocFormat getFormat() {
        return DocFormat.CodeBlock
    }

    /**
     * Validates this CodeBlock.
     *
     * @return true if valid.
     */
    @Override
    public boolean validate() {
        return super.items.size() > 0
    }

    /**
     * Returns a string representation of this model for debugging purposes.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("CodeBlock:\n")
        for (DocItem item : super.items) {
            sb.append(item.toString())
            sb.append("\n")
        }

        return sb.toString()
    }
}
