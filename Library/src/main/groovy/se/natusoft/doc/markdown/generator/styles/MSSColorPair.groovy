package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.jetbrains.annotations.Nullable

/**
 * Represents a pair of colors: foreground, and background.
 */
@CompileStatic
@TypeChecked
@EqualsAndHashCode
@ToString
class MSSColorPair {
    //
    // Properties
    //

    MSSColor foreground = null
    MSSColor background = null

    //
    // Methods
    //

    void updateForegroundIfNotSet(@Nullable MSSColor foreground) {
        if (this.foreground == null) { this.foreground = foreground }
    }

    void updateBackgroundIfNotSet(@Nullable MSSColor background) {
        if (this.background == null) { this.background = background }
    }
}
