package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.exception.GenerateException

/**
 * This is a base class for all models representing a markdown format, which must override
 * getFormat().
 */
@CompileStatic
@TypeChecked
abstract class DocFormatItem extends DocItem {

    DocFormat getFormat() {
        throw new GenerateException(message: "BUG: ${this.class.name} has not overridden getFormat()!")
    }

}
