/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Code Version
 *         1.5.0
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
 *         2016-07-29: Created!
 *
 */
package se.natusoft.doc.markdown.generator.pdfbox

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.pdfbox.multipdf.Overlay
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary
import org.apache.pdfbox.util.Matrix
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.generator.pdfbox.internal.FrontPage
import se.natusoft.doc.markdown.generator.pdfbox.internal.Outline
import se.natusoft.doc.markdown.generator.pdfbox.internal.PageMargins
import se.natusoft.doc.markdown.generator.styles.*
import se.natusoft.doc.markdown.util.NotNullTrait

import javax.imageio.ImageIO
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage

/**
 * This wraps a few PDFBox objects and uses these to generate PDF.
 *
 * This class is for encapsulating all PDFBox functionality and to provide a
 * higher level API.
 *
 * __Do note__ that this API makes use of some of the MSS models, but purely as models
 * holding needed information. There is no MSS resolving of styles done by this class!!
 * That has to be done before calling this class.
 */
@CompileStatic
@TypeChecked
class PDFBoxDocRenderer implements NotNullTrait {

    //
    // Constants
    //

    static final String A1 = "A1"
    static final String A2 = "A2"
    static final String A3 = "A3"
    static final String A4 = "A4"
    static final String A5 = "A5"
    static final String A6 = "A6"
    static final String LEGAL = "LEGAL"
    static final String LETTER = "LETTER"

    static final PDFBoxFontMSSAdapter TOC_FONT = new PDFBoxFontMSSAdapter(
            new MSSFont(size: 8, family: "HELVETICA", style: MSSFontStyle.NORMAL)
    )

    /** Indicates that image should be left aligned. */
    static final float X_OFFSET_LEFT_ALIGNED = 1000000.0f

    /** Indicates that the image should be centered on the page X wise. This applies to xOffset. */
    static final float X_OFFSET_CENTER = 1000001.0f

    /** Indicates that image should be right aligned. */
    static final float X_OFFSET_RIGHT_ALIGNED = 1000002.0f

    /** The default color pair to use if none other is provided. */
    static final MSSColorPair DEFAULT_COLOR_PAIR = new MSSColorPair( foreground: MSSColor.BLACK, background: MSSColor.WHITE)

    //
    // Static fields
    //

    /**
     * Maps model page sizes to PDFBox page sizes.
     */
    private static Map<String, PDRectangle> pageSizes = [
            A0: PDRectangle.A0,
            A1: PDRectangle.A1,
            A2: PDRectangle.A2,
            A3: PDRectangle.A3,
            A4: PDRectangle.A4,
            A5: PDRectangle.A5,
            A6: PDRectangle.A6,
            LEGAL: PDRectangle.LEGAL,
            LETTER: PDRectangle.LETTER
    ]

    //
    // Inner Models
    //

    /**
     * This represents a location with an x and a y.
     */
    static class Location {
        float x = 0.0f, y = -1.0f
    }

    /**
     * This represents a background box, below text.
     */
    static class Box {
        Location startLocation
        Location endLocation
        MSSColor color

        Box validate() {
            if (this.startLocation == null) throw new MissingPropertyException("startLocation", Location.class)
            if (this.color == null) throw new MissingPropertyException("color", MSSColor.class)
            this
        }
    }

    //
    //  Inner Classes
    //

    /**
     * This holds 2 documents in parallel. One is used to render text, while the other
     * is used to render rects, etc below the text. These documents are merged on save.
     */
    private class DocMgr {
        //
        // Constants
        //

        @SuppressWarnings("GroovyMissingReturnStatement")
        final Closure<Void> DOC_FG_COLOR = { int red, int green, int blue ->
            this.docStream.setNonStrokingColor(red, green, blue) }

        @SuppressWarnings("GroovyMissingReturnStatement")
        final Closure<Void> DOC_BG_COLOR = { int red, int green, int blue ->
            this.docStream.setStrokingColor(red, green, blue) }

        @SuppressWarnings("GroovyMissingReturnStatement")
        final Closure<Void> BG_DOC_FG_COLOR = { int red, int green, int blue ->
            this.bgDocStream.setNonStrokingColor(red, green, blue) }

        @SuppressWarnings("GroovyMissingReturnStatement")
        final Closure<Void> BG_DOC_BG_COLOR = { int red, int green, int blue ->
            this.bgDocStream.setStrokingColor(red, green, blue) }

        //
        // Properties
        //

        FrontPage frontPage


        PDDocument document = new PDDocument()
        PDPage docPage
        PDPageContentStream docStream

        PDDocument bgDocument = new PDDocument()
        PDPage bgDocPage
        PDPageContentStream bgDocStream

        /** The PDF outline. */
        Outline outline = null


        //
        // Private Members
        //

        //
        // Methods
        //

        void newPage() {
            docPage = new PDPage()
            docPage.setMediaBox(PDFBoxDocRenderer.this.pageFormat)
            document.addPage(docPage)
            if (this.docStream != null) {
                ensureTextModeOff()
                this.docStream.close()
            }
            this.docStream = new PDPageContentStream(this.document, this.docPage)

            bgDocPage = new PDPage()
            bgDocPage.setMediaBox(PDFBoxDocRenderer.this.pageFormat)
            bgDocument.addPage(bgDocPage)
            if (this.bgDocStream != null) {
                this.bgDocStream.close()
            }
            this.bgDocStream = new PDPageContentStream(this.bgDocument, this.bgDocPage)
        }
    }

    /**
     * Settings for tocEntry(...).
     */
    static class TocSettings {
        boolean useDots = true
        MSSColorPair sectionNumberColor = DEFAULT_COLOR_PAIR
        MSSColorPair sectionTitleColor = DEFAULT_COLOR_PAIR
        MSSColorPair pageNumberColor = DEFAULT_COLOR_PAIR
        MSSColorPair dotsColor = DEFAULT_COLOR_PAIR
        PDFBoxFontMSSAdapter font = PDFBoxFontMSSAdapter.DEFAULT_TOC_FONT
    }

    //
    // Private Members
    //

    /** The current font to use. */
    @NotNull
    protected PDFBoxFontMSSAdapter fontMSSAdapter

    /** A boxed area in progress if not null. */
    private Box box

    /** Holds the generated document. */
    @NotNull
    protected DocMgr docMgr = new DocMgr()

    /**
     * Keeps track of PDFBoxes text mode since I have not found a way to ask PDFBox for it.
     * ensureTextMode() and ensureTextModeOff() makes use of this.
     */
    private boolean textMode = false

    //
    // Properties
    //

    /** Indicates if page number should be rendered and incremented. */
    boolean pageNoActive = false

    /** The size of the page. Use constants on this class for valid standard sizes. */
    String pageSize = A4

    /** The current coordinates. */
    @NotNull
    private Location _pageLocation = new Location()
    public Location getPageLocation(){ this._pageLocation }
    protected void setPageLocation(Location location) { this._pageLocation = location }

    /** The page margins. */
    PageMargins margins

    /** The current page number. When the whole document is rendered this will be the last page number. */
    private int pageNo = 0
    public int getPageNo() { this.pageNo }

    /** Should be set to true when rendering pre formatted text. */
    boolean preFormatted = false

    /** The current foreground and background colors.*/
    private MSSColorPair colors
    public MSSColorPair getColors() { this.colors }

    //
    // Internal Pseudo Properties
    //

    /**
     * Gets current y coordinate with delayed init.
     */
    float getPageY() {
        if (this.pageLocation.y < 0.0f) {
            this.pageLocation.y = pageFormat.height - (this.fontMSSAdapter.size as float) - this.margins.topMargin
        }
        this._pageLocation.y
    }

    /**
     * Sets current y coordinate.
     *
     * @param y The coordinate to set.
     */
    void setPageY(float y) { this.pageLocation.y = y }

    /**
     * Gets the current x coordinate.
     */
    float getPageX() {
        if (this.pageLocation.x < this.margins.leftMargin) {
            this.pageLocation.x = this.margins.leftMargin
        }
        this.pageLocation.x
    }

    /**
     * Sets the current X coordinate.
     *
     * @param x The coordinate to set.
     */
    void setPageX(float x) { this.pageLocation.x = x }

    /**
     * Converts the public size to internal size.
     *
     * @param size The size to convert.
     */
    protected PDRectangle getPageFormat() {
        return pageSizes[this.pageSize]
    }

    //
    // Methods
    //

    /**
     * @return The current page.
     */
    PDPage getCurrentPage() {
        return this.docMgr.docPage
    }

    /**
     * @return The first page.
     */
    PDPage getFirstPage() {
        this.docMgr.document.getPage(0)
    }

    /**
     * @return The current total of pages.
     */
    int getNoOfPages() {
        this.docMgr.document.getPages().count
    }

    /**
     * Returns the page at the specified index.
     *
     * @param ix The index of the page to get.
     */
    PDPage getPage(int ix) {
        return this.docMgr.document.getPage(ix)
    }

    /**
     * Adds an outline entry for the current page.
     *
     * @param number The section number of the outline entry.
     * @param title The title of the outline entry.
     */
    void addOutlineEntry(Object number, String title) {
        addOutlineEntry(number, title, currentPage)
    }

    /**
     * Adds an outline entry for the specified page.
     *
     * @param number The section number of the outline entry.
     * @param title The title of the outline entry.
     * @param page The page to point to.
     */
    void addOutlineEntry(Object number, String title, PDPage page) {
        if (this.docMgr.outline == null) {
            this.docMgr.outline = new Outline()
            this.docMgr.outline.addToDocument(this.docMgr.document)
        }
        this.docMgr.outline.addEntry(number.toString(), title, page)
    }

    /**
     * The color to render text with.
     *
     * @param foregroundColor The foreground color to set.
     */
    void applyForegroundColor(@NotNull MSSColor foregroundColor) {
        notNull("foregroundColor", foregroundColor)
        this.colors.foreground = foregroundColor
        this.colors.foreground.applyColor this.docMgr.DOC_FG_COLOR
    }

    /**
     * The background color.
     *
     * @apram backgroundColor The background color to set.
     */
    void applyBackgroundColor(@NotNull MSSColor backgroundColor) {
        notNull("backgroundColor", backgroundColor)
        this.colors.background = backgroundColor
        this.colors.background.applyColor this.docMgr.DOC_BG_COLOR
    }

    /**
     * Sets a temporary foreground color that is not saved.
     *
     * @param foregroundColor The color to set temporarily
     */
    void applyTemporaryForegroundColor(@NotNull MSSColor foregroundColor) {
        notNull("foregroundColor", foregroundColor)
        foregroundColor.applyColor this.docMgr.DOC_FG_COLOR
    }

    /**
     * This should be done after a call to applyTemporaryForegroundColor. It will restore the previous color.
     */
    void restoreForegroundColor() {
        this.colors.foreground.applyColor this.docMgr.DOC_FG_COLOR
    }

    /**
     * Sets both forground and background color.
     *
     * @param colorPair The color pair to set.
     */
    void applyColorPair(@NotNull MSSColorPair colorPair) {
        notNull("colorPair", colorPair)
        this.colors = colorPair
        applyColorsInternal()
    }

    /**
     * Applies the current colors to the current page.
     */
    private void applyColorsInternal() {
        if (this.colors != null) {
            applyForegroundColor(this.colors.foreground)
            applyBackgroundColor(this.colors.background)
        }
    }

    /**
     * Temporarily changes the colors to the specified color pair and then executes the closure with those colors,
     * restoring them again after the closure has run.
     *
     * @param colorPair The color pair to apply during the closure.
     * @param withColorsCall The closure to call.
     */
    void withColors(MSSColorPair colorPair, Closure<Void> withColorsCall) {
        MSSColorPair origColors = this.colors
        applyColorPair(colorPair)
        withColorsCall.call()
        applyColorPair(origColors)
    }

    /**
     * Loads an external font and wraps it as an PDFBoxFontMSSAdapter.
     *
     * @param url The url to the font.
     * @param mssFont An MSSFont defining size and style.
     * @return
     */
    PDFBoxFontMSSAdapter loadExternalFont(String url, MSSFont mssFont) {
        URL fontURL = new URL(url)
        PDFont font = PDType0Font.load(this.docMgr.document, fontURL.openStream())
        return new PDFBoxFontMSSAdapter(font, mssFont)
    }

    /**
     * Sets and applies the font represented by the adapter.
     *
     * @param fontMSSAdapter The PDFFontMSSAdapter to set.
     */
    void applyFont(@NotNull PDFBoxFontMSSAdapter fontMSSAdapter) {
        notNull("fontMSSAdapter", fontMSSAdapter)
        this.fontMSSAdapter = fontMSSAdapter
        applyFontInternal()
    }

    /**
     * Applies the current font to the current page.
     */
    protected void applyFontInternal() {
        if (this.fontMSSAdapter != null) {
            this.docMgr.docStream.leading = this.fontMSSAdapter.size + 2
            this.fontMSSAdapter.applyFont(this.docMgr.docStream, this.docMgr.docPage)
        }
    }

    /**
     * Temporarily changes font, executes a closure, and then restores the current font again.
     *
     * @param font The temp font to use.
     * @param withFontCall The closure to call while font is active.
     */
    protected void withFont(@NotNull PDFBoxFontMSSAdapter font, Closure withFontCall) {
        PDFBoxFontMSSAdapter origFont = this.fontMSSAdapter
        applyFont( font)
        withFontCall.call()
        if (origFont != null) applyFont(origFont)
    }

    /**
     * Calculates the width of a text string.
     *
     * @param text The text to get the width of.
     *
     * @return The calculated width.
     */
    protected float calcTextWidth(@NotNull String text) {
        calcTextWidth(this.fontMSSAdapter, text)
    }

    /**
     * Calculates the width of a text string.
     *
     * @param fontAdapter The fontAdapter to get font from.
     * @param text The text to get the width of.
     *
     * @return The calculated width.
     */
    protected float calcTextWidth(@NotNull PDFBoxFontMSSAdapter fontAdapter, @NotNull String text) {
        notNull("text", text)
        (fontAdapter.font.getStringWidth(text) / 1000.0f * (float)fontAdapter.size) as float
    }

    /**
     * This keeps track of if the text document is in text mode or not,
     * and ensures that it is in text mode.
     *
     * This is needed because you can only render text while in text mode, and it
     * is only possible to set a position on the page directly after enabling text
     * mode.
     */
    protected void ensureTextMode() {
        if (!this.textMode) {
            this.docMgr.docStream.beginText()
            this.textMode = true
        }
    }

    /**
     * The same as ensureTextMode() but also positions rendering at x, y.
     *
     * Do note that this does not change the values of pageX and pageY!
     *
     * @param x The x coordinate to render text at.
     * @param y The y coordinate to render text at.
     */
    protected void ensureTextMode(float x, float y) {
        ensureTextMode()
        this.docMgr.docStream.newLineAtOffset(x, y)
    }

    /**
     * This keeps track of if the text document is in text mode or not,
     * and ensures that it isn't in text mode.
     */
    protected void ensureTextModeOff() {
        if (this.textMode) {
            this.docMgr.docStream.endText()
            this.textMode = false
        }
    }

    //
    // Document Content Methods
    //

    /**
     * Writes a TOC entry.
     *
     * @param tocEntry The toc entry to render plus rendering info
     */
    @SuppressWarnings("UnnecessaryQualifiedReference")
    void tocEntry(
            @Nullable String sectionNumber,
            @NotNull String sectionTitle,
            int pageNumber,
            @NotNull TocSettings tocSettings = new TocSettings() // Groovy is groovy :-)
    ) {
        MSSColorPair origColors = this.colors
        PDFBoxFontMSSAdapter origFont = this.fontMSSAdapter

        if (this.textMode) {
            ensureTextModeOff()
        }

        this.pageX = this.margins.leftMargin

        applyFont(tocSettings.font)

        // Section number if any
        if (sectionNumber != null) {
            applyColorPair(tocSettings.sectionNumberColor)
            ensureTextMode(this.pageX, this.pageY)
            this.text(sectionNumber)
            ensureTextModeOff()
            this.pageX = this.margins.leftMargin + 60.0f
        }

        // Section title
        ensureTextMode(this.pageX, this.pageY)
        float titleEnd = (this.pageX + calcTextWidth(sectionTitle) - 20.0f) as float
        applyColorPair(tocSettings.sectionTitleColor)
        this.text(sectionTitle)
        ensureTextModeOff()

        // Page number
        float pagePos = (this.pageFormat.width - this.margins.rightMargin - calcTextWidth(TOC_FONT, "${pageNo}")) as float
        ensureTextMode(pagePos, this.pageY)
        applyColorPair(tocSettings.pageNumberColor)
        this.text("${pageNumber}")
        ensureTextModeOff()

        // Dots between title and page number.
        if (tocSettings.useDots) {
            applyColorPair(tocSettings.dotsColor)
            float dotsSize = pagePos - titleEnd
            // The magic number 50.0 was arrived at by playing with a calculator and testing :-) This works for font sizes 8 - 13.
            // This code does not support larger font sizes since then the columns start overwriting each other.
            float dotSize = calcTextWidth(TOC_FONT, ".") + (TOC_FONT.size / 50.0f as float)
            int noDots = (dotsSize / dotSize) as int
            StringBuilder sb = new StringBuilder()
            noDots.times {
                sb.append('.')
            }
            ensureTextMode((pagePos - calcTextWidth(TOC_FONT, sb.toString()) - 6.0f) as float, this.pageY)
            this.docMgr.docStream.showText(sb.toString())
            ensureTextModeOff()
        }

        applyColorPair(origColors)
        applyFont(origFont)

        this.pageY -= TOC_FONT.size + 2
        if (this.pageY <= this.margins.bottomMargin) {
            newPage()
        }
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param text The text to write.
     *
     * @return a PDRectangle enclosing the text just written. Useful when adding (PDF) annotations.
     */
    PDRectangle text(@NotNull Object text) {
        notNull("text", text)
        ensureTextMode()
        String _text = text.toString()

        float rightMarginPos = this.pageFormat.width - this.margins.rightMargin

        // Find trailing spaces
        StringBuilder trailingSpaces = new StringBuilder()
        int pos = _text.size() - 1
        while (_text.charAt(pos) == ' ' as char && pos >= 0) {
            trailingSpaces.append(" ")
            --pos
        }

        // This will loose trailing spaces!
        List<String> wordList = new LinkedList<>()
        String space = ""
        _text.split(" ").each {String word ->
            wordList.add(space + word)
            space = " "
        }

        // This will add trailing spaces again.
        if (trailingSpaces.size() > 0) {
            wordList.add(trailingSpaces.toString())
        }

        PDRectangle textArea = new PDRectangle(lowerLeftX: this.pageX, lowerLeftY: this.pageY)

        wordList.each { String word ->
            float wordSize = calcTextWidth(word)
            if (this.pageX + wordSize > rightMarginPos) {
                this.pageX = this.margins.leftMargin
                this.pageY -= (this.fontMSSAdapter.size + 2)
                if (this.pageY < this.margins.bottomMargin) {
                    newPage()
                }
                else {
                    // Note: newLineAtOffset(...) does not work here!
                    this.docMgr.docStream.newLine()
                    if (!this.preFormatted) {
                        word = word.trim()
                    }
                }
            }
            this.docMgr.docStream.showText(word)
            this.pageX += wordSize
        }

        textArea.upperRightX = this.pageX
        textArea.upperRightY = this.pageY + this.fontMSSAdapter.size

        textArea
    }

    /**
     * Renders a text centered on the line and then moves to the next line.
     *
     * @param text The text to center.
     */
    void center(String text) {
        ensureTextModeOff()
        ensureTextMode() // Must do this for newLineAt(...) to work

        float x = ((this.pageFormat.width / 2.0f) - (calcTextWidth(text) / 2.0f)) as float
        this.docMgr.docStream.newLineAtOffset(x, this.pageY)
        this.docMgr.docStream.showText(text)
        this.pageX = this.margins.leftMargin
        this.pageY -= (this.fontMSSAdapter.size + 2.0f)
        if (this.pageY < this.margins.bottomMargin) {
            newPage()
        }
    }

    /**
     * Starts a boxed text block. Call endBox() to end it.
     *
     * @param boxColor The color to render the box in.
     */
    void startBox(MSSColor boxColor) {
        this.box = new Box(
                startLocation: new Location(
                        x: this.margins.leftMargin,
                        y: pageY + (this.fontMSSAdapter.size + 2)
                ),
                color: boxColor
        ).validate()
        this.margins.leftMargin = this.margins.leftMargin + 10.0f
        this.margins.rightMargin = this.margins.rightMargin + 10.0f
        ensureTextModeOff()

        this.pageX = this.margins.leftMargin
        ensureTextMode()
        this.docMgr.docStream.newLineAtOffset(pageX, pageY)
    }

    /**
     * Ends a previously started box.
     */
    void endBox() {
        ensureTextModeOff()
        this.margins.leftMargin = this.margins.leftMargin - 10.0f
        this.margins.rightMargin = this.margins.rightMargin - 10.0f
        this.box.endLocation = new Location(x: this.margins.rightMargin, y: this.pageY - 4.0f)

        this.box.color.applyColor this.docMgr.BG_DOC_FG_COLOR
        this.box.color.applyColor this.docMgr.BG_DOC_BG_COLOR

        this.docMgr.bgDocStream.addRect(
                this.box.endLocation.x,
                this.box.endLocation.y,
                this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin,
                this.box.startLocation.y - this.box.endLocation.y
        )
        this.docMgr.bgDocStream.fillAndStroke()
        this.docMgr.bgDocStream.closePath()

        ensureTextMode()
        pageX = this.margins.leftMargin
        this.docMgr.docStream.newLineAtOffset(pageX, pageY)
        this.box = null
    }

    /**
     * @return true if there is an active box.
     */
    private boolean isBox() {
        return this.box != null
    }

    /**
     * Draws a horizontal ruler over the page, and moves the current line down.
     */
    void hr() { hr(1.5f) }

    /**
     * Draws a horizontal rules over the page, and moves the current line down.
     *
     * @param thickness The thickness of the line. Anything over half the font size is a not a good idea! 2.0 or 3.0 is suggested.
     */
    void hr(float thickness) {
        if (this.pageY - (this.fontMSSAdapter.size * 2) < this.margins.bottomMargin) {
            newPage()
            // We intentionally do not draw the hr if it is on a page break.
        }
        else {
            newLine()
            ensureTextModeOff()
            float hrY = this.pageY + 2.0f
            this.docMgr.docStream.addRect(
                    this.margins.leftMargin, hrY, this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin, thickness
            )
            this.docMgr.docStream.fillAndStroke()
            this.docMgr.docStream.closePath()
            ensureTextMode()
            newLineAtPageLocation()
        }
    }

    /**
     * Writes a new line.
     */
    void newLine() {
        this.docMgr.docStream.newLine()
        this.pageX = this.margins.leftMargin
        this.pageY -= (this.fontMSSAdapter.size + 2)
        if (this.pageY < this.margins.bottomMargin) {
            newPage()
        }
    }

    /**
     * Writes a new line if not already at a new line and then writes one more new line to create an empty line.
     */
    void newSection() {
        if (this.pageX != this.margins.leftMargin) {
            newLine()
        }
        newLine()
    }

    /**
     * Creates a new line at page coordinates.
     */
    protected void newLineAtPageLocation() {
        this.pageX = this.margins.leftMargin
        this.pageY -= (this.fontMSSAdapter.size + 2)
        if (this.pageY < this.margins.bottomMargin) {
            newPage()
        }
        else {
            this.docMgr.docStream.newLineAtOffset(this.pageX, this.pageY)
        }
    }

    /**
     * Makes an empty line for a new paragraph.
     */
    void newParagraph() {
        newLine()
        newLine()
    }

    /**
     * Creates a new page.
     */
    void newPage() {
        MSSColor boxColor = null
        if (isBox()) {
            boxColor = this.box.color
            endBox()
        }

        this.docMgr.newPage()

        this.pageX = this.margins.leftMargin
        this.pageY = this.pageFormat.height - this.margins.topMargin

        ensureTextMode()
        this.docMgr.docStream.newLineAtOffset(this.pageLocation.x, this.pageLocation.y)

        applyFontInternal()
        applyColorsInternal()

        if (this.pageNoActive) {
            ++this.pageNo
            pageNumber(this.pageNo)
        }

        if (boxColor != null) {
            startBox(boxColor)
        }
    }

    /**
     * Adds a link to a page.
     *
     * @param linkText The text of the link.
     * @param uri The uri of the link
     */
    void link(@NotNull String linkText, @NotNull String uri) {
        applyTemporaryForegroundColor(MSSColor.LINK_BLUE)

        // Ain't Groovy cool! :-)
        this.docMgr.docPage.annotations.add(
            new PDAnnotationLink(
                    borderStyle: new PDBorderStyleDictionary(style: PDBorderStyleDictionary.STYLE_UNDERLINE, width: 0),
                    rectangle: this.text(linkText),
                    action: new PDActionURI(URI: uri)
            )
        )

        restoreForegroundColor()
    }

    /**
     * Renders an image on the page.
     *
     * @param imageUrl The url to the image.
     * @param scale the scale factor to apply to image size.
     */
    void image(String imageUrl, float scale) {
        image(imageUrl, 0.0f, 0.0f, 0.0f, scale, 0.0f)
    }

    /**
     * Renders an image on the page.
     *
     * @param imageUrl The url to the image.
     * @param xOffset The x offset to render at or 0 for left page margin.
     * @param scale the scale factor to apply to image size.
     */
    void image(String imageUrl, float xOffset, float scale) {
        image(imageUrl, xOffset, 0.0f, 0.0f, scale, 0.0f)
    }

    /**
     * Renders an image on the page using information from an MSSImage instance.
     *
     * @param imageUrl The image url.
     * @param mssImage The MSSImage instance to use for placement and scaling.
     */
    void image(String imageUrl, MSSImage mssImage) {
        float xOffset = X_OFFSET_LEFT_ALIGNED
        switch (mssImage.align) {
            case MSSImage.Align.LEFT:
                break
            case MSSImage.Align.MIDDLE:
                xOffset = X_OFFSET_CENTER
                break
            case MSSImage.Align.RIGHT:
                xOffset = X_OFFSET_RIGHT_ALIGNED
                break
        }
        image(imageUrl, xOffset, 0.0f, 0.0f, mssImage.scalePercent, mssImage.rotateDegrees)
    }

    /**
     * Renders an image on the page.
     *
     * @param imageUrl The url to the image.
     * @param xOffset The x offset to render at or 0 for left page margin. The X_OFFSET_* constants can also be used.
     * @param scale the scale factor to apply to image size.
     * @param rotate The number of degrees to rotate image. Note that image can become larger on all sides!!
     */
    void image(String imageUrl, float xOffset, float yOffset, float bottomAdd, float scale, float rotate) {
        PDImageXObject image
        URL url = new URL(imageUrl)

        if (scale > 1.0f) {
            scale = scale / 100.0f as float
        }

        // The dumb PDImageXObject API only allows loading from local file!! Thereby we have to go a little lower
        // than that. Since the TIFF support only loads from local file, TIFFs are not supported!
        if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg")) {
            image = JPEGFactory.createFromStream(this.docMgr.document, url.openStream())
        }
        else {
            BufferedImage bufferedImage = ImageIO.read(url.openStream())
            image = LosslessFactory.createFromImage(this.docMgr.document, bufferedImage)
        }

        float scaledWidth = image.width * scale
        float scaledHeight = image.height * scale

        if (xOffset == X_OFFSET_CENTER) {
            xOffset = ((this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin) / 2.0f) - (scaledWidth / 2.0f) as float
        }
        else if (xOffset == X_OFFSET_RIGHT_ALIGNED) {
            xOffset = this.pageFormat.width - this.margins.rightMargin - scaledWidth
        }

        if (pageY - yOffset - scaledHeight - bottomAdd - 8.0f < this.margins.bottomMargin) {
            newPage()
        }
        ensureTextModeOff()

        AffineTransform at = new AffineTransform(
                scaledWidth,
                0, 0,
                scaledHeight,
                (xOffset + this.margins.leftMargin) as float,
                this.pageY - yOffset - scaledHeight - 2.0f as float
        );
        at.rotate(Math.toRadians(rotate));
        this.docMgr.docStream.drawImage(image, new Matrix(at));

        this.pageX = this.margins.leftMargin
        this.pageY = this.pageY - yOffset - scaledHeight - bottomAdd - 8.0f
        ensureTextMode(this.pageX, this.pageY)
    }

    void pageNumber(int pageNumber) {
        String pgnStr = "${pageNumber}"
        ensureTextModeOff()
        withFont PDFBoxFontMSSAdapter.PAGE_NUMBER_FONT, {
            float width = calcTextWidth(pgnStr)
            ensureTextMode(this.pageFormat.width - this.margins.rightMargin - width as float, this.margins.bottomMargin - 10 as float)
            this.text(pgnStr)
        }
        ensureTextModeOff()
        ensureTextMode(this.pageX, this.pageY)
    }

    /**
     *
     * Forwards save to internal PDDocument.
     *
     * @param path The path of the file to save to.
     *
     * @throws IOException on failure to save
     */
    void save(String path) throws IOException {
        save(new File(path))
    }

    /**
     * Forwards save to internal PDDocument.
     *
     * @param file The file to save to.
     *
     * @throws IOException on failure to save
     */
    void save(File file) throws IOException {
        OutputStream saveStream = file.newOutputStream()
        try {
            save(saveStream)
        }
        finally {
            saveStream.close()
        }
    }

    /**
     * Forwards save to internal PDDocument.
     *
     * @param steam The stream to save to.
     *
     * @throws IOException on failure to save
     */
    void save(OutputStream stream) throws IOException {

        this.docMgr.docStream.close()
        this.docMgr.bgDocStream.close()

        Overlay overlay = new Overlay(
                inputPDF: this.docMgr.document,
                allPagesOverlayPDF: this.docMgr.bgDocument,
                overlayPosition: Overlay.Position.BACKGROUND
        )
        PDDocument finalDoc = overlay.overlay(new HashMap<Integer, String>())
        overlay.close()

        finalDoc.save(stream)
    }

    /**
     * Closes content stream and document.
     */
    void close() {
        this.docMgr.document.close()
        this.docMgr.bgDocument.close()
    }

}
