package se.natusoft.doc.markdown.generator.styles

/**
 * A general exception thrown on failure by MSS.
 */
class MSSException extends RuntimeException {

    MSSException(String message) {
        super(message)
    }

    MSSException(String message, Throwable cause) {
        super(message, cause)
    }
}
