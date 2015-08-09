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
        final int result
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
