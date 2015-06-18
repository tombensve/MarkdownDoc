package se.natusoft.doc.markdown.generator.pdf

import com.itextpdf.text.Font
import se.natusoft.doc.markdown.generator.styles.MSSFont
import se.natusoft.doc.markdown.generator.styles.MSSFontStyle

/**
 * Creates a PDF font from an MSSFont.
 */
class PDFFont extends Font {

    PDFFont(MSSFont mssFont) {
        super(toFontFamily(mssFont.family), mssFont.size, toStyle(mssFont.style))
    }

    private static final FontFamily toFontFamily(String family) {
        FontFamily ffam = FontFamily.valueOf(family.toUpperCase())
        if (ffam == null) {
            ffam = FontFamily.HELVETICA
        }

        return ffam
    }

    private static final int toStyle(MSSFontStyle fontStyle) {
        int result = NORMAL
        switch (fontStyle) {
            case NORMAL:
                result = NORMAL
                break
            case BOLD:
                result = BOLD
                break;
            case ITALIC:
                result = ITALIC
                break;
            case UNDERLINE:
                result = UNDERLINE
        }

        return result
    }
}
