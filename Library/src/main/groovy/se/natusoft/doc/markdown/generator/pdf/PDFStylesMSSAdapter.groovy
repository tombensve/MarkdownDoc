package se.natusoft.doc.markdown.generator.pdf

import com.itextpdf.text.Font
import com.itextpdf.text.pdf.BaseFont
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.GeneratorContext
import se.natusoft.doc.markdown.generator.styles.MSS
import se.natusoft.doc.markdown.generator.styles.MSSColorPair
import se.natusoft.doc.markdown.generator.styles.MSSFont
import se.natusoft.doc.markdown.generator.styles.MSSTTF

/**
 * Handles the conversion of MSS styles to iText PDF fonts.
 */
@CompileStatic
@TypeChecked
class PDFStylesMSSAdapter {

    public static final String DIV_NONE = null

    //
    // Private Members
    //

    private Map<String, Font> documentCache = new HashMap<>()
    private Map<String, Font> frontPageCache = new HashMap<>()
    private Map<String, Font> tocCache = new HashMap<>()

    //
    // Properties
    //

    /** The MSS to use. */
    MSS mss

    /** The generator context. */
    GeneratorContext generatorContext

    //
    // Methods
    //

    private void validate() {
        if (this.mss == null) throw new IllegalArgumentException("BUG: PDFStyles needs the 'mss' property set to be able to work!")
    }

    /**
     * Returns true if the specified font name is a standard font.
     *
     * @param font The name of the font to check.
     */
    private static boolean isStandardFont(String font) {
        try {
            return Font.FontFamily.valueOf(font) != null
        }
        catch (IllegalArgumentException iae) {}

        return false
    }

    /**
     * Best effort to resolve font. If not a standard font then an external font is expected under PDF/TTF.
     * @param mssFont The font reference to resolve.
     * @param mssColorPair The color of the font.
     *
     * @return The resolved font.
     *
     * @throws GenerateException If not font is found.
     */
    private Font resolveFont(MSSFont mssFont, MSSColorPair mssColorPair) throws GenerateException {
        Font resolved

        if (!isStandardFont(mssFont.family.toUpperCase())) {
            // We don't have a standard font! Lets see if we can find a ttf font!
            MSSTTF mssTtf = this.mss.getPdfTrueTypeFontPath(mssFont.family)
            if (mssTtf == null) {
                throw new GenerateException(message: "Font '${mssFont.family}' is not a standard font and a ttf font matching " +
                        "this name was not found either.")
            }

            byte[] fontBytes = loadFont(mssTtf)
            String fontName = mssFont.family
            if (!fontName.contains(".")) { fontName += ".ttf" }
            BaseFont baseFont
            try {
                baseFont = BaseFont.createFont(fontName, mssTtf.encoding, BaseFont.EMBEDDED, BaseFont.NOT_CACHED, fontBytes, new byte[0])
            }
            catch (Exception e) {
                fontName = mssFont.family + ".otf"
                baseFont = BaseFont.createFont(fontName, mssTtf.encoding, BaseFont.EMBEDDED, BaseFont.NOT_CACHED, fontBytes, new byte[0])
            }
            resolved = new PDFFontMSSAdapter(baseFont, mssFont, mssColorPair)
        }
        else {
            resolved = new PDFFontMSSAdapter(mssFont, mssColorPair)
        }

        return resolved
    }

    /**
     * Loads a font file into a byte array.
     *
     * @param mssTtf The MSSTTF object containing the path to the font to read.
     *
     * @return The loaded bytes.
     *
     * @throws GenerateException on failure to load font.
     */
    private byte[] loadFont(MSSTTF mssTtf) throws GenerateException {
        ByteArrayOutputStream fontBytes = new ByteArrayOutputStream()
        File fontPath
        try {
            fontPath = this.generatorContext.fileResource.getResourceFile(mssTtf.fontPath)
        }
        catch (IOException ioe) {
            throw new GenerateException(message: "Font '${mssTtf.fontPath}' was not found!", cause: ioe)
        }

        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fontPath))
        try {
            int b = inputStream.read()
            while (b >= 0) {
                fontBytes.write(b)
                b = inputStream.read()
            }
        }
        catch (IOException ioe) {
            throw new GenerateException(message: "Failed to load font (${mssTtf.fontPath})!", cause: ioe)
        }
        finally {
            fontBytes.close()
            inputStream.close()
        }

        return fontBytes.toByteArray()
    }

    /**
     * Returns a PDFFont for the specified document section and optional div.
     *
     * @param div The div to get font for or null if no div applies.
     * @param section The section to get font for.
     *
     * @throws GenerateException on problem with font.
     */
    @NotNull Font getFont(@Nullable String div, @NotNull MSS.MSS_Pages section) throws GenerateException {
        validate()

        String key = (div != null ? div : "") + section.name()
        Font font = this.documentCache.get(key)

        if (font == null) {
            MSSFont mssFont = this.mss.forDocument.getFont(div, section)
            MSSColorPair mssColorPair = this.mss.forDocument.getColorPair(div, section)

            font = resolveFont(mssFont, mssColorPair)

            this.documentCache.put(key, font)
        }

        return font
    }

    /**
     * Returns a PDFFont for the specified document section and optional div.
     *
     * @param section The section to get font for.
     */
    @NotNull Font getFont(@NotNull MSS.MSS_Pages section) {
        return getFont(DIV_NONE, section)
    }

    /**
     * Returns a PDFFont for the specified TOC section.
     *
     * @param section The TOC section to get font for.
     */
    @NotNull Font getFont(@NotNull MSS.MSS_TOC section) {
        validate()
        Font font = this.tocCache.get(section.name())

        if (font == null) {
            MSSFont mssFont = this.mss.forTOC.getFont(section)
            MSSColorPair mssColorPair = this.mss.forTOC.getColorPair(section)

            font = resolveFont(mssFont, mssColorPair)

            this.tocCache.put(section.name(), font)
        }

        return font
    }

    /**
     * Returns a PDFFont for the specified front page section.
     *
     * @param section The front page section to get font for.
     */
    @NotNull Font getFont(@NotNull MSS.MSS_Front_Page section) {
        validate()

        Font font = this.frontPageCache.get(section.name())

        if (font == null) {
            MSSFont mssFont = this.mss.forFrontPage.getFont(section)
            MSSColorPair mssColorPair = this.mss.forFrontPage.getColorPair(section)

            font = resolveFont(mssFont, mssColorPair)

            this.frontPageCache.put(section.name(), font)
        }

        return font
    }
}
