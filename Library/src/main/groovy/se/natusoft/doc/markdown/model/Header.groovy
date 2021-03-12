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
 *         2012-10-28: Created!
 *
 */
package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * This represents a header.
 */
@CompileStatic
class Header extends PlainText {
    //
    // Private Members
    //

    /** The header indentLevel. Range is 1 - 6. */
    Level level;

    //
    // Constructors
    //

    /**
     * Creates a new Header.
     */
    Header() {}

    //
    // Methods
    //

    /**
     * Sets a text.
     *
     * @param text The text to set.
     */
    void setText(final String text) {
        addItem(text)
    }

    /**
     * Returns the format this model represents.
     */
    @Override
    @NotNull DocFormat getFormat() {
        DocFormat.Header
    }

    @NotNull String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.level.name())
        sb.append(": ")
        sb.append(text)

        sb.toString()
    }

    //
    // Inner Classes
    //

    /**
     * The possible levels.
     */
    @CompileStatic
    static enum Level {
        //
        // Enum Constants
        //

        H1(1), H2(2), H3(3), H4(4), H5(5), H6(6)

        //
        // Properties
        //

        /** The indentLevel of this instance. */
        final int level

        //
        // Constructors
        //

        /**
         * Creates a new Level.
         *
         * @param level The indentLevel of this indentLevel.
         */
        Level(final int level) {
            this.level = level
        }

        //
        // Methods
        //

        /**
         * Returns an enum constant value by its indentLevel integer.
         *
         * @param level The indentLevel integer to get indentLevel enum by.
         */
        @SuppressWarnings("GroovyUnusedDeclaration")
        static Level getByLevel(final int level) {
            valueOf(Level.class, "H" + level)
        }
    }
}
