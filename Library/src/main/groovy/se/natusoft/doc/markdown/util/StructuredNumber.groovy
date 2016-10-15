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
 *         2016-10-15: Created!
 *         
 */
package se.natusoft.doc.markdown.util

/**
 * Represents a Structured number of infinite levels.
 */
class StructuredNumber {

    //
    // Properties
    //

    /** The current digit of this instance */
    long digit = 0

    /** If true a dot will be appended in the end on toString() */
    boolean endWithDot = false

    /** Not null if there is another digit efter us. */
    StructuredNumber subDigit = null

    /** If not null then the digit before us. */
    StructuredNumber parentDigit = null

    /** The initial value of new digits. */
    long newDigitValue = 0

    //
    // Methods
    //

    /**
     * Increments this digit.
     */
    StructuredNumber increment() {
        ++this.digit
        this
    }

    /**
     * Creates a new digit after us and returns it.
     */
    StructuredNumber newDigit() {
        this.subDigit = new StructuredNumber(parentDigit: this, digit: this.newDigitValue, newDigitValue: this.newDigitValue)
    }

    /**
     * Deletes this specific digit and thereby also all subdigits.
     *
     * @return Our parent digit or null if current is root.
     */
    StructuredNumber deleteThisDigit() {
        StructuredNumber sn = this

        if (this.parentDigit != null) {
            this.parentDigit.subDigit = null
            sn = this.parentDigit
        }

        sn
    }

    /**
     * If the current level is not the specified level then the number is moved to that level.
     *
     * Unless new digits were created the number is incremented.
     *
     * @param level The level to move to.
     */
    StructuredNumber toLevelAndIncrement(int level) {
        StructuredNumber sn = getRoot()
        int current = 1
        boolean added = false

        while (current < level) {
            if (sn.subDigit != null) {
                sn = sn.subDigit
                ++current
            }
            else {
                sn = sn.newDigit()
                added = true
                ++current
            }
        }

        if (sn.subDigit != null) {
            sn.subDigit = null
        }
        if (!added) {
            sn.increment()
        }

        sn
    }

    /**
     * Returns the root digit.
     */
    StructuredNumber getRoot() {
        StructuredNumber sn = this
        while (sn.parentDigit != null) {
            sn = sn.parentDigit
        }

        sn
    }

    /**
     * Returns all digits ,'.' separated as a String.
     */
    String toString() {
        StringBuilder sb = new StringBuilder()
        if (this.parentDigit != null) {
            sb.append('.')
        }
        sb.append(this.digit)
        if (this.subDigit != null) {
            sb.append(this.subDigit.toString())
        }
        if (endWithDot) {
            sb.append('.')
        }
        sb.toString()
    }
}
