package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents a style like bold, italics, etc.
 */
@CompileStatic
@TypeChecked
enum MSSFontStyle {
    NORMAL,
    BOLD,
    ITALIC,
    UNDERLINE
}
