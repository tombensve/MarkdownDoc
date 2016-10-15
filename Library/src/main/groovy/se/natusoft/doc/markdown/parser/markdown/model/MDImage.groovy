/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.0.0
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
import se.natusoft.doc.markdown.model.Image

/**
 * This extends Image but provides internal markdown parsing of received data.
 */
@CompileStatic
@TypeChecked
class MDImage extends Image {

    //
    // Private Members
    //

    /** Keeps track of which part of the link is being received. */
    private int part = 0

    //
    // Methods
    //

    /**
     * Provides the left shift operator.
     *
     * @param object The object to left shift in.
     */
    @NotNull DocItem leftShift(@NotNull final Object object) {
        final char c = (char)object

        if (part == 0 && c == '(' as char) {
            part = 1
        }
        else if (part == 1 && c == ' ' as char) {
            part = 2
        }
        else {
            if (part == 0) {
                addItem(c.toString())
            }
            else if (part == 1) {
                this.url = this.url + c
            }
            else if (part == 2) {
                this.title = this.title + c
            }
        }

        this
    }
}
