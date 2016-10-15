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
 *     tommy ()
 *         Changes:
 *         2016-07-29: Created!
 *
 */
package se.natusoft.doc.markdown.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Handles not null check.
 */
@CompileStatic
@TypeChecked
trait NotNullTrait {

    static void notNull(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Argument '${name}' at '${getCaller()}' can't be null!")
        }
    }

    static void notNull(Object value) {
        notNull("?", value)
    }

    static private String getCaller() {
        StackTraceElement[] elements = new Exception().stackTrace
        return "${elements[3].className}.${elements[3].methodName}(...), line ${elements[3].lineNumber}}"
    }
}
