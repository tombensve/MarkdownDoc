package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents a div.
 */
@CompileStatic
@TypeChecked
class Div extends DocItem {
    //
    // Properties
    //

    String name

    //
    // Methods
    //

    @Override
    DocFormat getFormat() {
        DocFormat.Div
    }

    boolean isStart() {
        this.name != null && !this.name.empty
    }

    boolean isEnd() {
        !isStart()
    }
}
