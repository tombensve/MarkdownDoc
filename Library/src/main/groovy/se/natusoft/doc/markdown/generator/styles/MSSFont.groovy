package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.Nullable

/**
 * This represents a font.
 */
@CompileStatic
@TypeChecked
class MSSFont {

    //
    // Properties
    //

    String family = null
    int size = -1
    MSSFontStyle style = null

    //
    // Methods
    //

    void updateFamilyIfNotSet(@Nullable String family) {
        if (this.family == null) this.family = family
    }

    void updateSizeIfNotSet(int size) {
        if (this.size == -1) this.size = size
    }

    void updateStyleIfNotSet(@Nullable MSSFontStyle style) {
        if (this.style == null) this.style = style
    }

    //
    // Static methods
    //

    static MSSFont createDefaultFont() {
        return new MSSFont(family: "HELVETICA", size: 10, style: MSSFontStyle.NORMAL)
    }
}
