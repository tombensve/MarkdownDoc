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
 *         2012-11-04: Created!
 *
 */
package se.natusoft.doc.markdown.exception

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * This exception represents parsing failures.
 */
@CompileStatic
class ParseException extends DocException {
    //
    // Private Members
    //

    /** The file being parsed in which the exception occurred. */
    @NotNull String file = "[File not specified!]"

    /** The line of the fault. */
    int lineNo = 0

    /** The line that failed. */
    @Nullable String line = null

    //
    // Methods
    //

    /**
     * @return The exception message.
     */
    @NotNull String getMessage() {
        final StringBuilder sb = new StringBuilder()
        sb.append(super.message)
        if (line != null) {
            sb.append(" \"")
            sb.append(line)
            sb.append("\"")
        }
        sb.append(" [")
        sb.append(file)
        sb.append(":")
        sb.append(lineNo)
        sb.append("]")

        return sb.toString()
    }
}
