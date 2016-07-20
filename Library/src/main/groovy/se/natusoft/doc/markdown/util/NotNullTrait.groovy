package se.natusoft.doc.markdown.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Handles not null check.
 */
@CompileStatic
@TypeChecked
trait NotNullTrait {

    void notNull(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Argument '${name}' at '${getCaller()}' can't be null!")
        }
    }

    void notNull(Object value) {
        notNull("?", value)
    }

    private String getCaller() {
        StackTraceElement[] elements = new Exception().stackTrace
        return "${elements[3].className}.${elements[3].methodName}(...), line ${elements[3].lineNumber}}"
    }

}
