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
