package se.natusoft.doc.markdowndoc.editor.exceptions;

/**
 * This is thrown by functions on perform.
 */
public class FunctionException extends RuntimeException {

    /**
     * Creates a new FunctionException.
     *
     * @param message The exception message.
     */
    public FunctionException(String message) {
        super(message);
    }

    /**
     * Creates a new FunctionException.
     *
     * @param message The exception message.
     * @param cause The cause of the exception.
     */
    public FunctionException(String message, Throwable cause) {
        super(message, cause);
    }

}
