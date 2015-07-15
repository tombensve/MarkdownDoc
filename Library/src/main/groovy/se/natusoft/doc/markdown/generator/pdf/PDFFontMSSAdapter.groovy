package se.natusoft.doc.markdown.generator.pdf

import com.itextpdf.text.Font
import com.itextpdf.text.pdf.BaseFont
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.generator.styles.MSSColorPair
import se.natusoft.doc.markdown.generator.styles.MSSFont
import se.natusoft.doc.markdown.generator.styles.MSSFontStyle

/**
 * Creates a PDF font from an MSSFont.
 */
@CompileStatic
@TypeChecked
class PDFFontMSSAdapter extends Font {
    //
    // Constructors
    //

    PDFFontMSSAdapter(@NotNull final BaseFont baseFont, @NotNull final MSSFont mssFont,
                      @NotNull final MSSColorPair mssColorPair) {
        super(baseFont)

        size = mssFont.size
        style = toStyle(mssFont.style)
        color = new PDFColorMSSAdapter(mssColorPair.foreground)
    }

    PDFFontMSSAdapter(@NotNull final MSSFont mssFont, @NotNull final MSSColorPair mssColorPair) {

        family = mssFont.family
        size = (float)mssFont.size
        style = toStyle(mssFont.style)
        color = new PDFColorMSSAdapter(mssColorPair.foreground)
    }

    //
    // Methods
    //

    private static final int toStyle(@NotNull final MSSFontStyle fontStyle) {
        int result
        switch (fontStyle) {
            case MSSFontStyle.NORMAL:
                result = NORMAL
                break
            case MSSFontStyle.BOLD:
                result = BOLD
                break;
            case MSSFontStyle.ITALIC:
                result = ITALIC
                break;
            case MSSFontStyle.UNDERLINE:
                result = UNDERLINE
        }

        return result
    }
}
