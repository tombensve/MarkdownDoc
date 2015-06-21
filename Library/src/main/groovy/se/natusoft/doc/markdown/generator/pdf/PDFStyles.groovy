package se.natusoft.doc.markdown.generator.pdf

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.generator.styles.MSS
import se.natusoft.doc.markdown.generator.styles.MSSColorPair
import se.natusoft.doc.markdown.generator.styles.MSSFont

/**
 * Handles the conversion of MSS styles to iText PDF fonts.
 */
@CompileStatic
@TypeChecked
class PDFStyles {

    public static final String DIV_NONE = null

    //
    // Private Members
    //

    private Map<String, PDFFont> documentCache = new HashMap<>()
    private Map<String, PDFFont> frontPageCache = new HashMap<>()
    private Map<String, PDFFont> tocCache = new HashMap<>()

    //
    // Properties
    //

    /** The MSS to use. */
    MSS mss

    //
    // Methods
    //

    private void validate() {
        if (this.mss == null) throw new IllegalArgumentException("BUG: PDFStyles needs the 'mss' property set to be able to work!")
    }

    /**
     * Returns a PDFFont for the specified document section and optional div.
     *
     * @param div The div to get font for or null if no div applies.
     * @param section The section to get font for.
     */
    @NotNull PDFFont getFont(@Nullable String div, @NotNull MSS.MSS_Pages section) {
        validate()

        String key = (div != null ? div : "") + section.name()
        PDFFont font = this.documentCache.get(key)

        if (font == null) {
            MSSFont mssFont = this.mss.forDocument.getFont(div, section)
            MSSColorPair mssColorPair = this.mss.forDocument.getColorPair(div, section)
            font = new PDFFont(mssFont, mssColorPair)
            this.documentCache.put(key, font)
        }

        return font
    }

    /**
     * Returns a PDFFont for the specified document section and optional div.
     *
     * @param section The section to get font for.
     */
    @NotNull PDFFont getFont(@NotNull MSS.MSS_Pages section) {
        return getFont(DIV_NONE, section)
    }

    /**
     * Returns a PDFFont for the specified TOC section.
     *
     * @param section The TOC section to get font for.
     */
    @NotNull PDFFont getFont(@NotNull MSS.MSS_TOC section) {
        validate()
        PDFFont font = this.tocCache.get(section.name())

        if (font == null) {
            MSSFont mssFont = this.mss.forTOC.getFont(section)
            MSSColorPair mssColorPair = this.mss.forTOC.getColorPair(section)
            font = new PDFFont(mssFont, mssColorPair)
            this.tocCache.put(section.name(), font)
        }

        return font
    }

    /**
     * Returns a PDFFont for the specified front page section.
     *
     * @param section The front page section to get font for.
     */
    @NotNull PDFFont getFont(@NotNull MSS.MSS_Front_Page section) {
        validate()

        PDFFont font = this.frontPageCache.get(section.name())

        if (font == null) {
            MSSFont mssFont = this.mss.forFrontPage.getFont(section)
            MSSColorPair mssColorPair = this.mss.forFrontPage.getColorPair(section)
            font = new PDFFont(mssFont, mssColorPair)
            this.frontPageCache.put(section.name(), font)
        }

        return font
    }
}
