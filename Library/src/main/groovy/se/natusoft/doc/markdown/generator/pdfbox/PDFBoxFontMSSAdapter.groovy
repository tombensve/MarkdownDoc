/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.0.2
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
package se.natusoft.doc.markdown.generator.pdfbox

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.generator.styles.MSSFont
import se.natusoft.doc.markdown.generator.styles.MSSFontStyle

/**
 * Creates a PDF font from an MSSFont.
 */
@CompileStatic
@TypeChecked
class PDFBoxFontMSSAdapter {
    //
    // Constants
    //

    static final PDFBoxFontMSSAdapter PAGE_NUMBER_FONT =
            new PDFBoxFontMSSAdapter(new MSSFont(family: "HELVETICA", style: MSSFontStyle.BOLD, size: 8))

    //
    // Properties
    //

    /** The PDF font to render with. */
    PDFont font

    /** The size of the font */
    int size

    /** If underlined or not. */
    boolean underlined = false

    //
    // Constructors
    //

    PDFBoxFontMSSAdapter(@NotNull final MSSFont mssFont) {
        this.font = toStdStyle(mssFont.family, mssFont.style)
        this.size = mssFont.size
        this.underlined = mssFont.underlined
    }

    protected PDFBoxFontMSSAdapter(@NotNull PDFont font, @NotNull final MSSFont mssFont) {
        this.font = font
        this.size = mssFont.size
        this.underlined = mssFont.underlined
        setStyle(font, mssFont.style)
    }

    //
    // Methods
    //

    /**
     * Sets the font represented by this adapter on a  page content stream.
     *
     * @param contentStream The content stream to apply font to.
     */
    public void applyFont(PDPageContentStream contentStream) {
        contentStream.setFont(this.font, this.size)
    }

    /**
     * Returns true if the specified font name is a standard font.
     *
     * @param font The name of the font to check.
     */
    private static boolean isStandardFont(@NotNull final String font) {
        ["COURIER", "HELVETICA", "TIMES ROMAN", "TIMES_ROMAN"].contains(font.toUpperCase())
    }

    private static final PDFont toStdStyle(@NotNull String family, @NotNull final MSSFontStyle fontStyle) {

        PDType1Font font

        switch (fontStyle) {
            case MSSFontStyle.NORMAL:
                switch (family.toUpperCase()) {
                    case "COURIER":
                        font = PDType1Font.COURIER
                        break
                    case "HELVETICA":
                        font = PDType1Font.HELVETICA
                        break;
                    case "TIMES ROMAN":
                    case "TIMES_ROMAN":
                        font = PDType1Font.TIMES_ROMAN
                        break
                    default:
                        font = PDType1Font.HELVETICA
                }
                break
            case MSSFontStyle.BOLD:
                switch (family.toUpperCase()) {
                    case "COURIER":
                        font = PDType1Font.COURIER_BOLD
                        break
                    case "HELVETICA":
                        font = PDType1Font.HELVETICA_BOLD
                        break;
                    case "TIMES ROMAN":
                    case "TIMES_ROMAN":
                        font = PDType1Font.TIMES_BOLD
                        break
                    default:
                        font = PDType1Font.HELVETICA_BOLD
                }
                break;
            case MSSFontStyle.ITALIC:
                switch (family.toUpperCase()) {
                    case "COURIER":
                        font = PDType1Font.COURIER_OBLIQUE
                        break
                    case "HELVETICA":
                        font = PDType1Font.HELVETICA_OBLIQUE
                        break;
                    case "TIMES ROMAN":
                    case "TIMES_ROMAN":
                        font = PDType1Font.TIMES_ITALIC
                        break
                    default:
                        font = PDType1Font.HELVETICA
                }
                break;
            case MSSFontStyle.UNDERLINE:
                switch (family.toUpperCase()) {
                    case "COURIER":
                        font = PDType1Font.COURIER
                        break
                    case "HELVETICA":
                        font = PDType1Font.HELVETICA
                        break;
                    case "TIMES ROMAN":
                    case "TIMES_ROMAN":
                        font = PDType1Font.TIMES_ROMAN
                        break
                    default:
                        font = PDType1Font.HELVETICA
                }
        }

        return font
    }

    private static final void setStyle(@NotNull PDFont font, @NotNull final MSSFontStyle fontStyle) {
        switch (fontStyle) {
            case MSSFontStyle.NORMAL:
                break
            case MSSFontStyle.BOLD:
                font.fontDescriptor.forceBold = true
                break;
            case MSSFontStyle.ITALIC:
                font.fontDescriptor.italic = true
                break;
        }
    }
}
