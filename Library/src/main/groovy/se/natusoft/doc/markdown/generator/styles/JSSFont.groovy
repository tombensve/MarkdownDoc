package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents a font.
 */
@CompileStatic
@TypeChecked
class JSSFont implements JSSStyleValue {

    //
    // Members
    //

    JSSFont parent = null
    private String _family = null
    private int _size = -1
    private JSSFontStyle _style = null

    //
    // Methods
    //

    String getFamily() { (this._family == null && this.parent != null) ? this.parent.family : this._family }
    int getSize() { (this.size == -1 && this.parent != null) ? this.parent.size : this._size }
    JSSFontStyle getStyle() { (this.style == null && this.parent != null) ? this.parent.style : this._style }

    void setFamily(String family) { this._family = family }
    void setSize(int size) { this._size = size }
    void setStyle(JSSFontStyle style) { this._style = style }

    //
    // Static methods
    //

    static JSSFont createDefaultFont() {
        return new JSSFont(family: "HELVETICA", size: 10, style: JSSFontStyle.NORMAL)
    }
}
