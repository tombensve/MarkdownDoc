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
import se.natusoft.doc.markdown.generator.models.TOC
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

    @SuppressWarnings("GroovyUnusedDeclaration") static final String A1 = "A1"
    @SuppressWarnings("GroovyUnusedDeclaration") static final String A2 = "A2"
    @SuppressWarnings("GroovyUnusedDeclaration") static final String A3 = "A3"
    @SuppressWarnings("GroovyUnusedDeclaration") static final String A4 = "A4"
    @SuppressWarnings("GroovyUnusedDeclaration") static final String A5 = "A5"
    @SuppressWarnings("GroovyUnusedDeclaration") static final String A6 = "A6"
    @SuppressWarnings("GroovyUnusedDeclaration") static final String LEGAL = "LEGAL"
    @SuppressWarnings("GroovyUnusedDeclaration") static final String LETTER = "LETTER"

    static final PDFBoxFontMSSAdapter TOC_FONT = new PDFBoxFontMSSAdapter(
            new MSSFont(size: 8, family: "HELVETICA", style: MSSFontStyle.NORMAL)
    )

    /** Indicates that image should be left aligned. */
    static final float X_OFFSET_LEFT_ALIGNED = 1000000.0f

    /** Indicates that the image should be centered on the page X wise. This applies to xOffset. */
    static final float X_OFFSET_CENTER = 1000001.0f

    /** Indicates that image should be right aligned. */
    static final float X_OFFSET_RIGHT_ALIGNED = 1000002.0f

    /** Indicates that the image should be rendererd at the current position. */
    static final float X_OFFSET_CURRENT = 1000003.0f

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
        final Closure<Void> DOC_TEXT_AND_FILL_COLOR = { int red, int green, int blue ->
            this.docStream.setNonStrokingColor(red, green, blue) }

        @SuppressWarnings("GroovyMissingReturnStatement")
        final Closure<Void> DOC_LINES_ETC_COLOR = { int red, int green, int blue ->
            this.docStream. setStrokingColor(red, green, blue) }

        @SuppressWarnings("GroovyMissingReturnStatement")
        final Closure<Void> BG_DOC_TEXT_AND_FILL_COLOR = { int red, int green, int blue ->
            this.bgDocStream.setNonStrokingColor(red, green, blue) }

        @SuppressWarnings("GroovyMissingReturnStatement")
        final Closure<Void> BG_DOC_LINES_ETC_COLOR = { int red, int green, int blue ->
            this.bgDocStream.setStrokingColor(red, green, blue) }

        //
        // Properties
        //

        FrontPage frontPage

        // The main "front" document.
        PDDocument document = new PDDocument()
        PDPage docPage
        PDPageContentStream docStream
        PDPageContentStream getDocStream() { if (this.docStream == null) { newPage() }; return this.docStream }

        // The "background" document. This and the "front" document will be merged with the "front" document in front of this
        // document on save. Thereby any  background rendering should be done in this.
        PDDocument bgDocument = new PDDocument()
        PDPage bgDocPage
        PDPageContentStream bgDocStream

        /** The PDF outline. */
        Outline outline = null

        int pageNumber = 0

        //
        // Private Members
        //

        //
        // Methods
        //

        void newPage() {
            ++this.pageNumber
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
        PDFBoxStylesMSSAdapter styles = null
    }

    //
    // Private Members
    //

    /** The current font to use. */
    @NotNull
    protected PDFBoxFontMSSAdapter fontMSSAdapter

    /** The current style to use. */
    @NotNull
    protected PDFBoxStylesMSSAdapter stylesMSSAdapter

    /** The current section. */
    @NotNull
    protected MSS.Section section

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

    /** This is a cache of the last applied styles so that newPage() can apply the styles on the new page. */
    private Closure<Void> stylesApplicator = null

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

    /**
     * @return The current page number.
     */
    public int getCurrentPageNumber() {
        this.docMgr.pageNumber
    }

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
        this.pageLocation.y
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
    void setPageX(float x) {
        this.pageLocation.x = x
    }

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

    void applyStyles() {
        if (this.stylesApplicator != null) {
            this.stylesApplicator.call()
        }
    }

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
     *  Sets the left inset. Set to 0 to remove inset.
     *
     * @param inset The inset to set.
     */
    void setLeftInset(float inset) {
        this.margins.leftInset = inset
    }

    /**
     * @return The left inset.
     */
    float getLeftInset() {
        return this.margins.leftInset
    }

    /**
     * Sets the right inset. Set to 0 to remove inset.
     * @param inset
     */
    void setRightInset(float inset) {
        this.margins.rightInset = inset
    }

    /**
     * @return The right inset.
     */
    float getRightInset() {
        return this.margins.rightInset
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
     * @param textAndLinesColor The color for text and lines to set.
     */
    void setTextAndLinesColor(@NotNull MSSColor textAndLinesColor) {
        notNull("foregroundColor", textAndLinesColor)
        this.colors.foreground = textAndLinesColor
        this.colors.foreground.applyColor this.docMgr.DOC_TEXT_AND_FILL_COLOR
    }

    /**
     * The color for background fills.
     *
     * @apram backgroundColor The background color to set.
     */
    void setBackgroundFillColor(@NotNull MSSColor backgroundColor) {
        notNull("backgroundColor", backgroundColor)
        this.colors.background = backgroundColor
        this.colors.background.applyColor this.docMgr.BG_DOC_TEXT_AND_FILL_COLOR
    }

    /**
     * Sets a temporary foreground color that is not saved.
     *
     * @param foregroundColor The color to set temporarily
     */
    void setTemporaryForegroundColor(@NotNull MSSColor foregroundColor) {
        notNull("foregroundColor", foregroundColor)
        foregroundColor.applyColor this.docMgr.DOC_TEXT_AND_FILL_COLOR
    }

    /**
     * This should be done after a call to applyTemporaryForegroundColor. It will restore the previous color.
     */
    void restoreForegroundColor() {
        this.colors.foreground.applyColor this.docMgr.DOC_TEXT_AND_FILL_COLOR
    }

    /**
     * Sets both forground and background color.
     *
     * @param colorPair The color pair to set.
     */
    void setColorPair(@NotNull MSSColorPair colorPair) {
        notNull("colorPair", colorPair)
        this.colors = colorPair
        this.colors.foreground.applyColor this.docMgr.DOC_TEXT_AND_FILL_COLOR
        this.colors.background.applyColor this.docMgr.BG_DOC_LINES_ETC_COLOR
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
        setColorPair(colorPair)
        withColorsCall.call()
        setColorPair(origColors)
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
    void setStyle(@NotNull PDFBoxStylesMSSAdapter stylesMSSAdapter, MSS.Section section) {
        notNull("stylesMSSAdapter", stylesMSSAdapter)
        this.stylesMSSAdapter = stylesMSSAdapter
        this.section = section

        setFont(getFontAdapter(stylesMSSAdapter, section))
    }

    /**
     * Applies only a font.
     *
     * @param fontMSSAdapter The adapter for the font to apply.
     */
    void setFont(PDFBoxFontMSSAdapter fontMSSAdapter) {
        this.fontMSSAdapter = fontMSSAdapter
        applyFontInternal()
    }

    /**
     * Gets a Font adapter from a styles adapter and a section.
     *
     * @param stylesMSSAdapter The styles adapter to use.
     * @param section The section to get font adapter for. All of MSS.MSS_Pages.*, MSS.MSS_Front_Page.*, and MSS.MSS_TOC.*
     *                are valid. These all implements MSS.Section.
     *
     * @return
     */
    protected PDFBoxFontMSSAdapter getFontAdapter(PDFBoxStylesMSSAdapter stylesMSSAdapter, MSS.Section section) {
        PDFBoxFontMSSAdapter fontMSSAdapter = null

        switch (section.class) {
            case MSS.MSS_Pages.class:
                fontMSSAdapter = stylesMSSAdapter.getFont(this.docMgr.document, section as MSS.MSS_Pages)
                break

            case MSS.MSS_Front_Page:
                fontMSSAdapter = stylesMSSAdapter.getFont(this.docMgr.document, section as MSS.MSS_Front_Page)
                break

            case MSS.MSS_TOC:
                fontMSSAdapter = stylesMSSAdapter.getFont(this.docMgr.document, section as MSS.MSS_TOC)
                break

            default:
                throw new IllegalStateException("BUG: unknown MSS.Section!")
        }

        fontMSSAdapter
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
     * Temporarily changes style, executes a closure, and then restores the current style again.
     *
     * @param style The temp style to use.
     * @param section The style should be for this section.
     * @param withFontCall The closure to call while font is active.
     */
    protected void withStyle(@NotNull PDFBoxStylesMSSAdapter style, MSS.Section section, Closure withStyleCall) {
        PDFBoxStylesMSSAdapter origStyle = this.stylesMSSAdapter
        MSS.Section origSection = this.section
        setStyle(style, section)
        withStyleCall.call()
        if (origStyle != null) setStyle(origStyle, origSection)
    }

    /**
     * Temporarily changes font, executes a closure, and then restores the current font again.
     *
     * @param style The temp style to use.
     * @param withFontCall The closure to call while font is active.
     */
    protected void withFont(@NotNull PDFBoxFontMSSAdapter font, Closure withFontCall) {
        PDFBoxFontMSSAdapter origFont = this.fontMSSAdapter
        setFont(font)
        withFontCall.call()
        if (origFont != null) setFont(origFont)
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
    void ensureTextMode() {
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
    void ensureTextMode(float x, float y) {
        ensureTextMode()
        this.docMgr.docStream.newLineAtOffset(x, y)
    }

    /**
     * This keeps track of if the text document is in text mode or not,
     * and ensures that it isn't in text mode.
     */
    void ensureTextModeOff() {
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
     * @param tocSettings Style settings for TOC.
     */
    @SuppressWarnings("UnnecessaryQualifiedReference")
    void tocEntry(
            @NotNull TOC tocEntry,
            @NotNull TocSettings tocSettings = new TocSettings()
    ) {
        MSSColorPair origColors = this.colors
        PDFBoxStylesMSSAdapter origStyles = this.stylesMSSAdapter
        MSS.Section origSection = this.section
        ensureTextModeOff()

        this.pageX = this.margins.leftMargin

        setStyle(tocSettings.styles, tocEntry.section)

        // Section number if any
        if (tocEntry.sectionNumber != null) {
            setColorPair(tocSettings.sectionNumberColor)
            ensureTextMode(this.pageX, this.pageY)
            this.text(tocEntry.sectionNumber)
            ensureTextModeOff()
            this.pageX = this.margins.leftMargin + 60.0f
        }

        // Section title
        ensureTextMode(this.pageX, this.pageY)
        float titleEnd = (this.pageX + calcTextWidth(tocEntry.sectionTitle) - 20.0f) as float
        setColorPair(tocSettings.sectionTitleColor)
        this.text(tocEntry.sectionTitle)
        ensureTextModeOff()

        // Page number
        float pagePos = (this.pageFormat.width - this.margins.rightMargin - calcTextWidth(TOC_FONT, "${pageNo}")) as float
        ensureTextMode(pagePos, this.pageY)
        setColorPair(tocSettings.pageNumberColor)
        this.text("${tocEntry.pageNumber}")
        ensureTextModeOff()

        // Dots between title and page number.
        if (tocSettings.useDots) {
            setColorPair(tocSettings.dotsColor)
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

        setColorPair(origColors)
        setStyle(origStyles, origSection)

        this.pageY -= TOC_FONT.size + 2
        if (this.pageY <= this.margins.bottomMargin) {
            newPage()
        }
    }

    /**
     * Writes pre formatted text as is.
     *
     * @param text The pre formatted text
     * @param styleText A closure that will be called to style text when needed.
     */
    PDRectangle preFormattedText(@NotNull Object text, @Nullable Closure<Void> styleText) {
        notNull("text", text)

        ensureTextMode()

        this.stylesApplicator = styleText
        applyStyles()

        String[] lines = text.toString().split("\n|\r")
        PDRectangle textArea = new PDRectangle(lowerLeftX: this.pageX, lowerLeftY: this.pageY)

        lines.each { String line ->
            this.docMgr.docStream.showText(line)
            this.pageX = this.margins.leftMargin
            this.pageY -= (this.fontMSSAdapter.size + 2)
            if (this.pageY < this.margins.bottomMargin) {
                newPage()
                if (styleText != null) { styleText.call() }
            }
            else {
                // Note: newLineAtOffset(...) does not work here!
                this.docMgr.docStream.newLine()
            }
        }

        textArea.upperRightX = this.pageX
        textArea.upperRightY = this.pageY + this.fontMSSAdapter.size

        textArea
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param txt The text to write.
     * @param styleText A closure that will be called to style text when needed.
     *
     * @return a PDRectangle enclosing the text just written. Useful when adding (PDF) annotations.
     */
    PDRectangle text(@NotNull Object txt,  @Nullable Closure<Void> styleText) {
        text(txt, styleText, false)
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param txt The text to write.
     *
     * @return a PDRectangle enclosing the text just written. Useful when adding (PDF) annotations.
     */
    PDRectangle text(@NotNull Object txt) {
        text(txt, null, false)
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param txt The txt to write.
     * @param styleText A closure that will be called to style text when needed.
     * @param pgBoxed If true then the text will be rendered with a background of the set background color.
     *
     * @return a PDRectangle enclosing the txt just written. Useful when adding (PDF) annotations.
     */
    PDRectangle text(@NotNull Object txt, @Nullable Closure<Void> styleText, boolean pgBoxed) {
        notNull("txt", txt)

        if (pgBoxed) {
            startParagraphBox()
        }

        ensureTextMode()

        // styleText will be null every now and then, and apparently though not obviously clear why, the stylesApplicator
        // needs to be set to null these times, or save will fail later on!
        this.stylesApplicator = styleText
        applyStyles()

        String _text = txt.toString()

        float rightMarginPos = this.pageFormat.width - this.margins.rightMargin

        // Find trailing spaces
        StringBuilder trailingSpaces = new StringBuilder()
        int pos = _text.size() - 1
        while (pos >= 0 && _text.charAt(pos) == ' ' as char) {
            trailingSpaces.append(" ")
            --pos
        }

        // This will loose trailing spaces, but keep spaces between words.
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
        PDRectangle boxedTextArea = new PDRectangle(lowerLeftX: this.pageX - 1, lowerLeftY: this.pageY)

        wordList.each { String word ->
            float wordSize = calcTextWidth(word)
            if (this.pageX + wordSize > rightMarginPos) {
                if (pgBoxed) { endParagraphBox(boxedTextArea) }

                this.pageX = this.margins.leftMargin
                this.pageY -= (this.fontMSSAdapter.size + 2)
                if (this.pageY < this.margins.bottomMargin) {
                    newPage()
//                    if (styleText != null) { styleText.call() }
                }
                else {
                    // Note: newLineAtOffset(...) does not work here!
                    this.docMgr.docStream.newLine()
                }
                if (!this.preFormatted) { word = word.trim() }

                if (pgBoxed) { boxedTextArea = new PDRectangle(lowerLeftX: this.pageX - 1, lowerLeftY: this.pageY) }
            }
            this.docMgr.docStream.showText(word)
            this.pageX += wordSize
        }
        if (pgBoxed) {
            endParagraphBox(boxedTextArea)
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

        applyStyles()

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
     * Starts a paragraph boxing.
     */
    void startParagraphBox() {
        // Placeholder for now.
    }

    /**
     * Ends a paragraph boxing.
     *
     * @param textArea The beginning of the box to render.
     */
    void endParagraphBox(PDRectangle textArea) {
        textArea.upperRightX = this.pageX + 1
        textArea.upperRightY = this.pageY + this.fontMSSAdapter.size

        // Since it does not seem possible to have other background color than white, the only way I found to have another
        // background color on text is to render a rect behind the text of the desired background color. This is really
        // annoying, but a google on setting background color for text in PDFBox does not give an answer. Maybe PDF as format
        // does not support it.
        this.docMgr.bgDocStream.addRect(
                textArea.lowerLeftX,
                textArea.lowerLeftY -2,
                textArea.upperRightX - textArea.lowerLeftX,
                this.fontMSSAdapter.size + 2
        )
        this.docMgr.bgDocStream.closeAndFillAndStroke()
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
        this.margins.leftMargin = this.margins.leftMargin - 10.0f
        this.margins.rightMargin = this.margins.rightMargin - 10.0f
        this.box.endLocation = new Location(x: this.margins.rightMargin, y: this.pageY)

        this.box.color.applyColor this.docMgr.BG_DOC_TEXT_AND_FILL_COLOR
        this.box.color.applyColor this.docMgr.BG_DOC_LINES_ETC_COLOR

        this.docMgr.bgDocStream.addRect(
                this.box.endLocation.x,
                this.box.endLocation.y,
                this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin,
                this.box.startLocation.y - this.box.endLocation.y
        )
        this.docMgr.bgDocStream.closeAndFillAndStroke()

        ensureTextModeOff()
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
     * Draws a horizontal rules over the page, and moves the current line down.
     *
     * @param thickness The thickness of the line. Anything over half the font size is a not a good idea! 2.0 or 3.0 is suggested.
     * @param color The color to draw the hr in.
     */
    void hr(float thickness, @Nullable MSSColor color) {
        if (this.pageY - 8 < this.margins.bottomMargin) {
            newPage()
            // We intentionally do not draw the hr if it is on a page break.
        }
        else {
            newLine()
            ensureTextModeOff()
            float hrY = this.pageY + 1

            if (color!= null) {
                color.applyColor this.docMgr.DOC_TEXT_AND_FILL_COLOR
                color.applyColor this.docMgr.DOC_LINES_ETC_COLOR
            }

            this.docMgr.docStream.addRect(
                    this.margins.leftMargin, hrY, this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin, thickness
            )
            this.docMgr.docStream.closeAndFillAndStroke()
            ensureTextMode()
            //newLineAtPageLocation()
        }
    }

    /**
     * Writes a new line.
     */
    void newLine() {
        ensureTextModeOff()
        ensureTextMode()

        this.pageX = this.margins.leftMargin
        this.pageY -= (this.fontMSSAdapter.size + 2)
        this.docMgr.docStream.newLineAtOffset(this.pageX, this.pageY)
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

        if (this.pageNoActive) {
            ++this.pageNo
            pageNumber(this.pageNo)
        }

        if (boxColor != null) {
            startBox(boxColor)
        }

        applyStyles()
    }

    /**
     * Adds a link to a page.
     *
     * @param linkText The text of the link.
     * @param uri The uri of the link
     */
    void link(@NotNull String linkText, @NotNull String uri) {
        setTemporaryForegroundColor(MSSColor.LINK_BLUE)

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
            case MSSImage.Align.CURRENT:
                xOffset = X_OFFSET_CURRENT
                break
        }
        image(imageUrl, xOffset, 0.0f, 0.0f, mssImage.scalePercent, mssImage.rotateDegrees)
    }

    /**
     * Renders an image on the page.
     *
     * @param imageUrl The url to the image.
     * @param xOffset The x offset to render at or 0 for left page margin. The X_OFFSET_* constants can also be used.
     * @param yOffset The y offset to render at. Usually 0.
     * @param bottomAdd How much to add under image. Usually 0.
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

        float imageX = this.pageX, imageY = this.pageY
        float moveX = 0.0f, moveY = 0.0f

        if (xOffset == X_OFFSET_LEFT_ALIGNED) {
            imageX = this.margins.leftMargin
            moveX = scaledWidth + 2
        }
        if (xOffset == X_OFFSET_CENTER) {
            imageX = this.margins.leftMargin + ((this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin) / 2.0f) -
                    (scaledWidth / 2.0f) as float
            moveY = yOffset + scaledHeight + bottomAdd
        }
        else if (xOffset == X_OFFSET_RIGHT_ALIGNED) {
            imageX = this.pageFormat.width - (this.margins.rightMargin + scaledWidth)
            moveY = yOffset + scaledHeight + bottomAdd
        }

        if (pageY - yOffset - scaledHeight - bottomAdd - 8.0f < this.margins.bottomMargin) {
            newPage()
        }
        ensureTextModeOff()

        AffineTransform at = new AffineTransform(
                scaledWidth,
                0, 0,
                scaledHeight,
                imageX,
                imageY
        );
        at.rotate(Math.toRadians(rotate));
        this.docMgr.docStream.drawImage(image, new Matrix(at));

        if (moveX == 0.0f) {
            this.pageX = this.margins.leftMargin
        }
        else {
            this.pageX += moveX
        }
        this.pageY = this.pageY - moveY//(yOffset + scaledHeight + bottomAdd)
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
