/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.2.2
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

/**
 * This represents a header.
 */
public class Header extends PlainText {
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
    public Header() {}

    //
    // Methods
    //

    /**
     * Sets a text.
     *
     * @param text The text to set.
     */
    public void setText(String text) {
        addItem(text)
    }

    /**
     * Returns the format this model represents.
     */
    @Override
    public DocFormat getFormat() {
        return DocFormat.Header
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.level.name())
        sb.append(": ")
        sb.append(text)

        return sb.toString()
    }

    //
    // Inner Classes
    //

    /**
     * The possible levels.
     */
    public static enum Level {
        //
        // Enum Constants
        //

        H1(1), H2(2), H3(3), H4(4), H5(5), H6(6)

        //
        // Private Members
        //

        /** The indentLevel of this instance. */
        int level

        //
        // Constructors
        //

        /**
         * Creates a new Level.
         *
         * @param level The indentLevel of this indentLevel.
         */
        public Level(int level) {
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
        public Level getByLevel(int level) {
            return Level.valueOf(Level.class, "H" + level)
        }
    }
}
