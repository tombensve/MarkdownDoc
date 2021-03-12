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
 *     tommy ()
 *         Changes:
 *         2015-07-15: Created!
 *
 */
package se.natusoft.doc.markdown.util

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Provide resources that works:
 *
 * <ul>
 *     <li>Runtime</li>
 *     <li>In maven run JUnit test</li>
 *     <li>In IDEA run JUnit test</li>
 * </ul>
 *
 * That this is needed is bloody annoying!!
 */
@CompileStatic
class TestSafeResource {

    static @Nullable InputStream getResource(@NotNull final String path) {

        InputStream is = TestSafeResource.class.getClassLoader().getResourceAsStream(path)
        if (is == null) {
            is = TestSafeResource.class.getClassLoader().getResourceAsStream("src/main/resources/" + path)
        }
        if (is == null) {
            is = TestSafeResource.class.getClassLoader().getResourceAsStream("Library/src/main/resources/" + path)
        }

        is
    }
}
