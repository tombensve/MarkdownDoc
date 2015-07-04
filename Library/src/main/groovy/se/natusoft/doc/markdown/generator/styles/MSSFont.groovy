package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.jetbrains.annotations.Nullable

/**
 * This represents a font.
 */
@CompileStatic
@TypeChecked
@EqualsAndHashCode
@ToString
class MSSFont {

    //
    // Properties
    //

    String family = null
    int size = -1
    MSSFontStyle style = null

    Boolean hr = null
    Boolean getHr() { // hr must be able to be null internally, but externally return false instead of null.
        return this.hr != null ? this.hr : Boolean.FALSE
    }

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

    void updateHrIfNotSet(boolean hr) {
        if (this.hr == null) this.hr = hr
    }

    //
    // Static methods
    //

    static MSSFont createDefaultFont() {
        new MSSFont(family: "HELVETICA", size: 10, style: MSSFontStyle.NORMAL)
    }

}
