package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull

/**
 * This represents a div.
 */
@CompileStatic
@TypeChecked
class Div extends DocFormatItem {
    //
    // Properties
    //

    /**
     * The name/class of div. When set this model represents a start div. When null this model represents an end div.
     */
    String name

    //
    // Methods
    //

    @Override
    @NotNull DocFormat getFormat() {
        DocFormat.Div
    }

    boolean isStart() {
        this.name != null && !this.name.empty
    }

    boolean isEnd() {
        !isStart()
    }

    static Div startDiv(String name) {
        return new Div(name: name)
    }

    static Div endDiv() {
        return new Div()
    }
}
