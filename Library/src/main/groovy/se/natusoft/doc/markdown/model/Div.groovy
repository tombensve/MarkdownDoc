/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.1.1
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
package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull

/**
 * This represents a div.
 */
@CompileStatic
@TypeChecked
class Div extends DocFormatItem {
    //
    // Properties
    //

    /**
     * The name/class of div. When set this model represents a start div. When null this model represents an end div.
     */
    String name

    //
    // Methods
    //

    @Override
    @NotNull DocFormat getFormat() {
        DocFormat.Div
    }

    boolean isStart() {
        this.name != null && !this.name.empty
    }

    boolean isEnd() {
        !isStart()
    }

    static Div startDiv(final String name) {
        return new Div(name: name)
    }

    static Div endDiv() {
        return new Div()
    }
}
