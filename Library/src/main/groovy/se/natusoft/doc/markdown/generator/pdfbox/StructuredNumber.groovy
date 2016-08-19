package se.natusoft.doc.markdown.generator.pdfbox

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
        this.subDigit = new StructuredNumber(parentDigit: this, digit: 0)
    }

    /**
     * Deletes this specific digit and thereby also all subdigits.
     *
     * @return Our parent digit or null if current is root.
     */
    StructuredNumber deleteThisDigit() {
        this.parentDigit.subDigit = null
        this.parentDigit
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
        sb.append(digit)
        if (this.subDigit != null) {
            sb.append(this.subDigit.toString())
        }
        if (endWithDot) {
            sb.append('.')
        }
        sb.toString()
    }
}
