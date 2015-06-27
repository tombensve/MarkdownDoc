package se.natusoft.doc.markdown.generator.pdf

import com.itextpdf.text.BaseColor
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.generator.styles.MSSColor

/**
 * This represents a PDFColor
 */
@CompileStatic
@TypeChecked
class PDFColorMSSAdapter extends BaseColor {

    PDFColorMSSAdapter(final MSSColor color) {
        super(color.red, color.green, color.blue)
    }
}
