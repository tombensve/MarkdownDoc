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
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.generator.pdfbox.internal.FrontPage
import se.natusoft.doc.markdown.generator.pdfbox.internal.Outline
import se.natusoft.doc.markdown.generator.pdfbox.internal.PageMargins
import se.natusoft.doc.markdown.generator.styles.MSSColor
import se.natusoft.doc.markdown.generator.styles.MSSColorPair
import se.natusoft.doc.markdown.generator.styles.MSSFont
import se.natusoft.doc.markdown.generator.styles.MSSFontStyle
import se.natusoft.doc.markdown.util.NotNullTrait

/**
 * This wraps a few PDFBox objects and uses these to generate PDF.
 *
 * This class is for encapsulating all PDFBox functionality and to provide a
 * higher level API.
 */
@CompileStatic
@TypeChecked
class PDFBoxDocRenderer implements NotNullTrait {

    //
    // Constants
    //

    @SuppressWarnings("GroovyUnusedDeclaration")
    public static final String A0 = "A0"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public static final String A1 = "A1"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public static final String A2 = "A2"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public static final String A3 = "A3"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public static final String A4 = "A4"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public static final String A5 = "A5"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public static final String A6 = "A6"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public static final String LEGAL = "LEGAL"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public static final String LETTER = "LETTER"

    public static final PDFBoxFontMSSAdapter TOC_FONT = new PDFBoxFontMSSAdapter(
            new MSSFont(size: 8, family: "HELVETICA", style: MSSFontStyle.NORMAL)
    )

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
        MSSColorPair colorPair

        Box validate() {
            if (this.startLocation == null) throw new MissingPropertyException("startLocation", Location.class)
            if (this.colorPair == null) throw new MissingPropertyException("colorPair", MSSColorPair.class)
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
    protected DocMgr genDoc = new DocMgr()

    /** Is set to true on beginText. */
    private boolean textMode = false

    //
    // Properties
    //

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
    private int pageNo = 1
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
        return this.genDoc.docPage
    }

    /**
     * @return The first page.
     */
    PDPage getFirstPage() {
        this.genDoc.document.getPage(0)
    }

    /**
     * @return The current total of pages.
     */
    int getNoOfPages() {
        this.genDoc.document.getPages().count
    }

    /**
     * Returns the page at the specified index.
     *
     * @param ix The index of the page to get.
     */
    PDPage getPage(int ix) {
        return this.genDoc.document.getPage(ix)
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
        if (this.genDoc.outline == null) {
            this.genDoc.outline = new Outline()
            this.genDoc.outline.addToDocument(this.genDoc.document)
        }
        this.genDoc.outline.addEntry(number.toString(), title, page)
    }

    /**
     * The color to render text with.
     *
     * @param foregroundColor The foreground color to set.
     */
    void applyForegroundColor(@NotNull MSSColor foregroundColor) {
        notNull("foregroundColor", foregroundColor)
        this.colors.foreground = foregroundColor
        this.colors.foreground.applyColor this.genDoc.DOC_FG_COLOR
    }

    /**
     * The background color.
     *
     * @apram backgroundColor The background color to set.
     */
    void applyBackgroundColor(@NotNull MSSColor backgroundColor) {
        notNull("backgroundColor", backgroundColor)
        this.colors.background = backgroundColor
        this.colors.background.applyColor this.genDoc.DOC_BG_COLOR
    }

    /**
     * Sets a temporary foreground color that is not saved.
     *
     * @param foregroundColor The color to set temporarily
     */
    void applyTemporaryForegroundColor(@NotNull MSSColor foregroundColor) {
        notNull("foregroundColor", foregroundColor)
        foregroundColor.applyColor this.genDoc.DOC_FG_COLOR
    }

    /**
     * This should be done after a call to applyTemporaryForegroundColor. It will restore the previous color.
     */
    void restoreForegroundColor() {
        this.colors.foreground.applyColor this.genDoc.DOC_FG_COLOR
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
            this.genDoc.docStream.leading = this.fontMSSAdapter.size + 2
            this.fontMSSAdapter.applyFont(this.genDoc.docStream, this.genDoc.docPage)
        }
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
            this.genDoc.docStream.beginText()
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
        this.genDoc.docStream.newLineAtOffset(x, y)
    }

    /**
     * This keeps track of if the text document is in text mode or not,
     * and ensures that it isn't in text mode.
     */
    protected void ensureTextModeOff() {
        if (this.textMode) {
            this.genDoc.docStream.endText()
            this.textMode = false
        }
    }

    //
    // Document Content Methods
    //

    /**
     * Writes a TOC entry.
     *
     * @param number The number of the entry if any. Can be null.
     * @param title The title of the entry.
     * @param page The page number of the entry.
     */
    void tocEntry(@Nullable String number, @NotNull String title, int page) {
        tocEntry(number, title, page, true)
    }

    /**
     * Writes a TOC entry.
     *
     * @param number The number of the entry if any. Can be null.
     * @param title The title of the entry.
     * @param pageNo The page number of the entry.
     * @param useDots If true then dots will be drawn between title and page number.
     */
    void tocEntry(@Nullable String number, @NotNull String title, int pageNo, boolean useDots) {
        if (this.textMode) {
            ensureTextModeOff()
        }

        TOC_FONT.applyFont(this.genDoc.docStream, null)
        applyTemporaryForegroundColor(MSSColor.BLACK)

        this.pageX = this.margins.leftMargin

        // Section number if any
        if (number != null) {
            ensureTextMode(this.pageX, this.pageY)
            text(number)
            ensureTextModeOff()
            this.pageX = this.margins.leftMargin + 60.0f
        }

        // Section title
        ensureTextMode(this.pageX, this.pageY)
        float titleEnd = (this.pageX + calcTextWidth(TOC_FONT, title) - 20.0f) as float
        text(title)
        ensureTextModeOff()

        // Page number
        float pagePos = (this.pageFormat.width - this.margins.rightMargin - calcTextWidth(TOC_FONT, "${pageNo}")) as float
        ensureTextMode(pagePos, this.pageY)
        text("${pageNo}")
        ensureTextModeOff()

        // Dots between title and page number.
        if (useDots) {
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
            this.genDoc.docStream.showText(sb.toString())
            ensureTextModeOff()
        }

        this.fontMSSAdapter.applyFont(this.genDoc.docStream, null)
        restoreForegroundColor()

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
    PDRectangle text(@NotNull String text) {
        notNull("text", text)
        ensureTextMode()

        float rightMarginPos = this.pageFormat.width - this.margins.rightMargin

        // Find trailing spaces
        StringBuilder trailingSpaces = new StringBuilder()
        int pos = text.size() - 1
        while (text.charAt(pos) == ' ' as char && pos >= 0) {
            trailingSpaces.append(" ")
            --pos
        }

        // This will loose trailing spaces!
        List<String> wordList = new LinkedList<>()
        String space = ""
        text.split(" ").each {String word ->
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
                    this.genDoc.docStream.newLine()
                    if (!this.preFormatted) {
                        word = word.trim()
                    }
                }
            }
            this.genDoc.docStream.showText(word)
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
        this.genDoc.docStream.newLineAtOffset(x, this.pageY)
        this.genDoc.docStream.showText(text)
        this.pageX = this.margins.leftMargin
        this.pageY -= (this.fontMSSAdapter.size + 2.0f)
        if (this.pageY < this.margins.bottomMargin) {
            newPage()
        }
    }

    /**
     * Starts a boxed text block. Call endBox() to end it.
     *
     * @param A pair of colors to use for the box. Foreground is the non stroking color and the background is the stroking color.
     */
    void startBox(MSSColorPair boxColorPair) {
        this.box = new Box(
                startLocation: new Location(
                        x: this.margins.leftMargin,
                        y: pageY + (this.fontMSSAdapter.size + 2)
                ),
                colorPair: boxColorPair
        ).validate()
        this.margins.leftMargin = this.margins.leftMargin + 10.0f
        this.margins.rightMargin = this.margins.rightMargin + 10.0f
        ensureTextModeOff()

        this.pageX = this.margins.leftMargin
        ensureTextMode()
        this.genDoc.docStream.newLineAtOffset(pageX, pageY)
    }

    /**
     * Ends a previously started box.
     */
    void endBox() {
        ensureTextModeOff()
        this.margins.leftMargin = this.margins.leftMargin - 10.0f
        this.margins.rightMargin = this.margins.rightMargin - 10.0f
        this.box.endLocation = new Location(x: this.margins.rightMargin, y: this.pageY - 4.0f)

        this.box.colorPair.foreground.applyColor this.genDoc.BG_DOC_FG_COLOR
        this.box.colorPair.background.applyColor this.genDoc.BG_DOC_BG_COLOR

        this.genDoc.bgDocStream.addRect(
                this.box.endLocation.x,
                this.box.endLocation.y,
                this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin,
                this.box.startLocation.y - this.box.endLocation.y
        )
        this.genDoc.bgDocStream.fillAndStroke()
        this.genDoc.bgDocStream.closePath()

        ensureTextMode()
        pageX = this.margins.leftMargin
        this.genDoc.docStream.newLineAtOffset(pageX, pageY)
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
            this.genDoc.docStream.addRect(
                    this.margins.leftMargin, hrY, this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin, thickness
            )
            this.genDoc.docStream.fillAndStroke()
            this.genDoc.docStream.closePath()
            ensureTextMode()
            newLineAtPageLocation()
        }
    }

    /**
     * Writes a new line.
     */
    void newLine() {
        this.genDoc.docStream.newLine()
        this.pageX = this.margins.leftMargin
        this.pageY -= (this.fontMSSAdapter.size + 2)
        if (this.pageY < this.margins.bottomMargin) {
            newPage()
        }
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
            this.genDoc.docStream.newLineAtOffset(this.pageX, this.pageY)
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
        MSSColorPair boxColor = null
        if (isBox()) {
            boxColor = this.box.colorPair
            endBox()
        }

        this.genDoc.newPage()

        this.pageX = this.margins.leftMargin
        this.pageY = this.pageFormat.height - this.margins.topMargin

        ensureTextMode()
        this.genDoc.docStream.newLineAtOffset(this.pageLocation.x, this.pageLocation.y)

        applyFontInternal()
        applyColorsInternal()

        ++this.pageNo
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
        this.genDoc.docPage.getAnnotations().add(
            new PDAnnotationLink(
                    borderStyle: new PDBorderStyleDictionary(style: PDBorderStyleDictionary.STYLE_UNDERLINE, width: 0),
                    rectangle: text(linkText),
                    action: new PDActionURI(URI: uri)
            )
        )

        restoreForegroundColor()
    }

    void image(String imageUrl, float xOffset) {
        // todo
    }

    /**
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

        this.genDoc.docStream.close()
        this.genDoc.bgDocStream.close()

        Overlay overlay = new Overlay(
                inputPDF: this.genDoc.document,
                allPagesOverlayPDF: this.genDoc.bgDocument,
                overlayPosition: Overlay.Position.BACKGROUND
        )
        PDDocument finalDoc = overlay.overlay(new HashMap<Integer, String>())
        overlay.close()

        finalDoc.save(stream)

        PDFMergerUtility pmu = new PDFMergerUtility()
    }

    /**
     * Closes content stream and document.
     */
    void close() {
        this.genDoc.document.close()
        this.genDoc.bgDocument.close()
    }

}
