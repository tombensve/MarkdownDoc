/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Code Version
 *         1.5.0
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
package se.natusoft.doc.markdown.generator.pdfbox.internal

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents a section number of 1 to 6 levels. All levels start at 1 and can only be incremented.
 */
@CompileStatic
@TypeChecked
class BadStructuredNumber {

    //
    // Private Members
    //

    /** The current structured number. */
    private Integer[] structNum = null

    /** The current level of the number to manipulate. */
    private int level = 0

    //
    // Constructor
    //

    /**
     * Creates a new StructuredNumber
     *
     * @param maxSize The maximum level of the number.
     */
    BadStructuredNumber(int maxSize) {
        this.structNum = new Integer[maxSize]
        this.structNum[0] = 1
    }

    /**
     * The copy constructor.
     *
     * @param toCopy The StructuredNumber to copy.
     */
    BadStructuredNumber(BadStructuredNumber toCopy) {
        this.structNum = new Integer[toCopy.structNum.length]
        System.arraycopy(toCopy.structNum, 0, this.structNum, 0, toCopy.structNum.length)
        this.level = toCopy.level
    }

    //
    // Methods
    //

    /**
     * Returns the max size of this number.
     */
    int getMaxSize() {
        this.structNum.length
    }

    /**
     * Moves down a level. The new level will be initialized to 1.
     */
    void downLevel() {
        ++this.level
        if (this.level >= this.maxSize) {
            this.level = this.maxSize - 1
        }
        else {
            this.structNum[this.level] = 1
        }
    }

    /**
     * Moves up a level. This will also increment the target level value.
     */
    void upLevel() {
        if (this.level > 0) {
            this.structNum[this.level] = null
            --this.level
            incrementCurrentLevel()
        }
    }

    /**
     * Returns the current level.
     */
    int getLevel() {
        this.level + 1
    }

    /**
     * Sets the current level.
     *
     * @param level The section level to set. Must be between 1 and maxSize + 1.
     */
    BadStructuredNumber setLevel(int level) {
        this.level = level - 1

        (this.level + 1)..(this.maxSize - 1).each { int lvl ->
            this.structNum[lvl] = null
        }

        this
    }

    /**
     * Increments the section number at the current level.
     */
    void incrementCurrentLevel() {
        incrementLevel(this.level)
    }

    /**
     * Increments the section number.
     *
     * @param sectionLevel The section level to increment.
     */
    void incrementLevel(int sectionLevel) {
        if (sectionLevel < 0) sectionLevel = 0
        if (sectionLevel > 5) sectionLevel = 5
        if (this.structNum[sectionLevel] == null) { this.structNum[sectionLevel] = 0 }
        ++this.structNum[sectionLevel]
    }

    /**
     * Returns the section number as a string.
     */
    String toString() {
        StringBuilder sb = new StringBuilder()
        int i = 0
        String dot = ""
        while (this.structNum[i] != null) {
            sb.append(dot)
            sb.append(this.structNum[i++])
            dot = "."
        }

        return sb.toString()
    }
}
