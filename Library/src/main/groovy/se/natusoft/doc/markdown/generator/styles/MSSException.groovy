package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * A general exception thrown on failure by MSS.
 */
@CompileStatic
@TypeChecked
class MSSException extends RuntimeException {

    //
    // Properties
    //

    /** The exception message */
    String message

    /** The exception cause. */
    Throwable cause
}
