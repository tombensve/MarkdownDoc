package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents information about a TTF font.
 */
@CompileStatic
@TypeChecked
class MSSExtFont {
    //
    // Properties
    //

    /** The path to the font. */
    String fontPath

    /** The encoding of the font. */
    String encoding
}
