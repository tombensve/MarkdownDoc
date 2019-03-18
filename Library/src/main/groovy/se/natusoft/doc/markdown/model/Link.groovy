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
import org.jetbrains.annotations.Nullable

/**
 * This represents a link.
 */
@CompileStatic
@TypeChecked
class Link extends PlainText {
    //
    // Private Members
    //

    /** The URL part of the link. */
    @NotNull String url = ""

    /** The link title */
    @Nullable String title = ""

    //
    // Methods
    //

    /**
     * Returns the format this model represents.
     */
    @Override
    @NotNull DocFormat getFormat() {
        DocFormat.Link
    }

    @Override
    boolean validate() {
        this.url != null
    }

    @NotNull String toString() {
        final StringBuilder sb  = new StringBuilder();

        sb.append("<a href='" + url + "'")
        if (title != null) {
            sb.append(" title='" + this.title + "'")
        }

        sb.append(">" + text + "</a>")

        sb.toString()
    }
}
