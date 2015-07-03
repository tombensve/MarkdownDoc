package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic

/**
 * This represents a div.
 */
@CompileStatic
class Div extends DocItem {
    //
    // Properties
    //

    String name

    //
    // Methods
    //

    @Override
    public DocFormat getFormat() {
        return DocFormat.Div
    }

    boolean isStart() {
        return this.name != null && !this.name.empty
    }

    boolean isEnd() {
        return !isStart()
    }
}
