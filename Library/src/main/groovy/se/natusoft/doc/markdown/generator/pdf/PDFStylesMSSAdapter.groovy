/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.4
 *     
 *     Description
 *         Parses markdown and generates HTML and PDF.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2015-07-15: Created!
 *         
 */
package se.natusoft.doc.markdown.generator.pdf

import com.itextpdf.text.Font
import com.itextpdf.text.pdf.BaseFont
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.GeneratorContext
import se.natusoft.doc.markdown.generator.styles.MSS
import se.natusoft.doc.markdown.generator.styles.MSSColorPair
import se.natusoft.doc.markdown.generator.styles.MSSFont
import se.natusoft.doc.markdown.generator.styles.MSSExtFont

/**
 * Handles the conversion of MSS styles to iText PDF fonts.
 */
@CompileStatic
@TypeChecked
class PDFStylesMSSAdapter {

    //
    // Properties
    //

    /** The MSS to use. */
    @NotNull MSS mss

    /** The generator context. */
    @NotNull GeneratorContext generatorContext

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
    private static boolean isStandardFont(@NotNull String font) {
        try {
            return Font.FontFamily.valueOf(font) != null
        }
        catch (IllegalArgumentException ignore) {}

        false
    }

    /**
     * Best effort to resolve font. If not a standard font then an external font is expected under PDF/TTF.
     *
     * @param mssFont The font reference to resolve.
     * @param mssColorPair The color of the font.
     *
     * @return The resolved font.
     *
     * @throws GenerateException If not font is found.
     */
    private @NotNull Font resolveFont(@NotNull final MSSFont mssFont, @NotNull final MSSColorPair mssColorPair)
            throws GenerateException {

        if (!isStandardFont(mssFont.family.toUpperCase())) {
            // We don't have a standard font! Lets see if we can find a ttf font!
            MSSExtFont mssExtFont = this.mss.getPdfExternalFontPath(mssFont.family)
            if (mssExtFont == null) {
                throw new GenerateException(message: "Font '${mssFont.family}' is not a standard font and an " +
                        "external font matching this name was not found either.")
            }

            byte[] fontBytes = loadFont(mssExtFont)
            String fontName = mssFont.family
            if (!fontName.contains(".")) { fontName += ".ttf" }
            BaseFont baseFont
            try {
                baseFont = BaseFont.createFont(fontName, mssExtFont.encoding, BaseFont.EMBEDDED, BaseFont.NOT_CACHED,
                        fontBytes, new byte[0])
            }
            catch (Exception ignore) {
                fontName = mssFont.family + ".otf"
                baseFont = BaseFont.createFont(fontName, mssExtFont.encoding, BaseFont.EMBEDDED, BaseFont.NOT_CACHED,
                        fontBytes, new byte[0])
            }
            new PDFFontMSSAdapter(baseFont, mssFont, mssColorPair)
        }
        else {
            new PDFFontMSSAdapter(mssFont, mssColorPair)
        }
    }

    /**
     * Loads a font file into a byte array.
     *
     * @param mssExtFont An object containing the path to the font to read.
     *
     * @return The loaded bytes.
     *
     * @throws GenerateException on failure to load font.
     */
    private @NotNull byte[] loadFont(@NotNull final MSSExtFont mssExtFont) throws GenerateException {
        ByteArrayOutputStream fontBytes = new ByteArrayOutputStream()
        File fontPath
        try {
            fontPath = this.generatorContext.fileResource.getResourceFile(mssExtFont.fontPath)
        }
        catch (IOException ioe) {
            throw new GenerateException(message: "Font '${mssExtFont.fontPath}' was not found!", cause: ioe)
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
            throw new GenerateException(message: "Failed to load font (${mssExtFont.fontPath})!", cause: ioe)
        }
        finally {
            fontBytes.close()
            inputStream.close()
        }

        fontBytes.toByteArray()
    }

    /**
     * Returns a PDFFont for the specified document section and optional div.
     *
     * @param section The section to get font for.
     *
     * @throws GenerateException on problem with font.
     */
    @NotNull Font getFont(@NotNull final MSS.MSS_Pages section) throws GenerateException {
        validate()

        MSSFont mssFont = this.mss.forDocument.getFont(section)
        MSSColorPair mssColorPair = this.mss.forDocument.getColorPair(section)

        resolveFont(mssFont, mssColorPair)
    }

    /**
     * Returns a PDFFont for the specified TOC section.
     *
     * @param section The TOC section to get font for.
     */
    @NotNull Font getFont(@NotNull final MSS.MSS_TOC section) {
        validate()

        MSSFont mssFont = this.mss.forTOC.getFont(section)
        MSSColorPair mssColorPair = this.mss.forTOC.getColorPair(section)

        resolveFont(mssFont, mssColorPair)
    }

    /**
     * Returns a PDFFont for the specified front page section.
     *
     * @param section The front page section to get font for.
     */
    @NotNull Font getFont(@NotNull final MSS.MSS_Front_Page section) {
        validate()

        MSSFont mssFont = this.mss.forFrontPage.getFont(section)
        MSSColorPair mssColorPair = this.mss.forFrontPage.getColorPair(section)

        resolveFont(mssFont, mssColorPair)
    }
}
