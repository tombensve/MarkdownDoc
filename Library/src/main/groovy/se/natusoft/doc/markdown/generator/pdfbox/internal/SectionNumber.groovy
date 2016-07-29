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
class SectionNumber {

    //
    // Private Members
    //

    /** The current section number. */
    private Integer[] secNo = new Integer[6]

    /** The current level of the section number to manipulate. */
    private int sectionLevel = 0

    //
    // Constructor
    //

    SectionNumber() {
        this.secNo[0] = 1
    }

    //
    // Methods
    //

    /**
     * Moves up a section.
     */
    void sectionUp() {
        ++this.sectionLevel
        if (this.sectionLevel > 5) {
            this.sectionLevel = 5
        }
        else {
            this.secNo[this.sectionLevel] = 1
        }
    }

    /**
     * Moves a section down.
     */
    void sectionDown() {
        if (this.sectionLevel > 0) {
            this.secNo[this.sectionLevel] = null
            --this.sectionLevel
        }
    }

    /**
     * Sets a section level.
     *
     * @param sectionLevel The section level to set. Must be between 1 and 6.
     */
    SectionNumber sectionAtLevel(int sectionLevel) {
        this.sectionLevel = sectionLevel - 1
        (6 - (this.sectionLevel+1)).times { int it ->
            this.secNo[this.sectionLevel + 1 + it] = null
        }
        int beg = this.sectionLevel
        beg.times { int it ->
            if (this.secNo[it] == null) this.secNo[it] = 1
        }

        this
    }

    /**
     * Increments the section number at the current level.
     */
    void incrementCurrentLevel() {
        incrementLevel(this.sectionLevel)
    }

    /**
     * Increments the section number.
     *
     * @param sectionLevel The section level to increment.
     */
    void incrementLevel(int sectionLevel) {
        if (sectionLevel < 0) sectionLevel = 0
        if (sectionLevel > 5) sectionLevel = 5
        if (this.secNo[sectionLevel] == null) { this.secNo[sectionLevel] = 0 }
        ++this.secNo[sectionLevel]
    }

    /**
     * Returns the section number as a string.
     */
    String toString() {
        StringBuilder sb = new StringBuilder()
        int i = 0
        String dot = ""
        while (this.secNo[i] != null) {
            sb.append(dot)
            sb.append(this.secNo[i++])
            dot = "."
        }

        return sb.toString()
    }
}
