package se.natusoft.doc.markdown.generator.pdf

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.pdfbox.multipdf.Overlay
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.generator.styles.MSSColor
import se.natusoft.doc.markdown.generator.styles.MSSColorPair
import se.natusoft.doc.markdown.util.NotNullTrait

/**
 * This wraps an PDPageContentStream object and provide utilities to render on page.
 */
@CompileStatic
@TypeChecked
class PDFDoc implements NotNullTrait {

    //
    // Constants
    //

    public static final String A0 = "A0"
    public static final String A1 = "A1"
    public static final String A2 = "A2"
    public static final String A3 = "A3"
    public static final String A4 = "A4"
    public static final String A5 = "A5"
    public static final String A6 = "A6"
    public static final String LEGAL = "LEGAL"
    public static final String LETTER = "LETTER"

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
        String color
    }

    /**
     * This holds 2 documents in parallel. One is used to render text, while the other
     * is used to render rects, etc below the text. These documents are merged on save.
     */
    private class GenDocument {
        //
        // Properties
        //

        PDDocument document = new PDDocument()
        PDPage docPage
        PDPageContentStream docStream

        PDDocument bgDocument = new PDDocument()
        PDPage bgDocPage
        PDPageContentStream bgDocStream

        //
        // Methods
        //

        void newPage() {
            docPage = new PDPage()
            docPage.setMediaBox(PDFDoc.this.pageFormat)
            document.addPage(docPage)
            if (this.docStream != null) {
                ensureTextModeOff()
                this.docStream.close()
            }
            this.docStream = new PDPageContentStream(this.document, this.docPage)

            bgDocPage = new PDPage()
            bgDocPage.setMediaBox(PDFDoc.this.pageFormat)
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
    private PDFFontMSSAdapter fontMSSAdapter

    /** A boxed area in progress if not null. */
    private Box box

//    /** The document the page is part of. */
//    @NotNull
//    private PDDocument document = null

    /** Holds the generated document. */
    @NotNull
    private GenDocument genDoc = new GenDocument()

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

//    /** The content stream for the page. */
//    @Nullable
//    PDPageContentStream contentStream = null

    /** The page margins. */
    float topMargin, bottomMargin, leftMargin, rightMargin

    /** The current page number. When the whole document is rendered this will be the last page number. */
    private int pageNo = 1
    public int getPageNo() { this.pageNo }

    /** Should be set to true when rendering pre formatted text. */
    boolean preFormatted = false

    /** The current foreground and background colors.*/
    private MSSColorPair colors
    public MSSColorPair getColors() { this.colors }

    //
    // Methods
    //

    /**
     * Converts the public size to internal size.
     *
     * @param size The size to convert.
     */
    protected PDRectangle getPageFormat() {
        return pageSizes[this.pageSize]
    }

    /**
     * The color to render text with.
     *
     * @param foregroundColor The foreground color to set.
     */
    void applyForegroundColor(@NotNull MSSColor foregroundColor) {
        notNull("foregroundColor", foregroundColor)
        this.colors.foreground = foregroundColor
        this.genDoc.docStream.setNonStrokingColor(foregroundColor.red, foregroundColor.green, foregroundColor.blue)
    }

    /**
     * The background color.
     *
     * @apram backgroundColor The background color to set.
     */
    void applyBackgroundColor(@NotNull MSSColor backgroundColor) {
        notNull("backgroundColor", backgroundColor)
        this.colors.background = backgroundColor
        this.genDoc.docStream.setStrokingColor(backgroundColor.red, backgroundColor.green, backgroundColor.blue)
    }

    /**
     * Sets both forground and background color.
     *
     * @param colorPair The color pair to set.
     */
    void applyColorPair(@NotNull MSSColorPair colorPair) {
        notNull("colorPair", colorPair)
        this.colors = colorPair
        applyForegroundColor(colorPair.foreground)
        applyBackgroundColor(colorPair.background)
    }

    /**
     * Sets and applies the font represented by the adapter.
     *
     * @param fontMSSAdapter The PDFFontMSSAdapter to set.
     */
    void applyFont(@NotNull PDFFontMSSAdapter fontMSSAdapter) {
        notNull("fontMSSAdapter", fontMSSAdapter)
        this.fontMSSAdapter = fontMSSAdapter
        this.genDoc.docStream.leading = this.fontMSSAdapter.size + 2
        this.fontMSSAdapter.applyFont(this.genDoc.docStream)
    }

    /**
     * Sets the format/size of the page.
     *
     * @param pageFormat The format to set.
     */
    void setPageFormat(@NotNull PDRectangle pageFormat) {
        notNull("pageFormat", pageFormat)
        this.pageFormat = pageFormat
    }

    /**
     * Calculates the width of a text string.
     *
     * @param text The text to get the width of.
     *
     * @return The calculated width.
     */
    private float calcTextWidth(@NotNull String text) {
        notNull("text", text)
        (this.fontMSSAdapter.font.getStringWidth(text) / 1000.0f * (float)this.fontMSSAdapter.size) as float
    }

    /**
     * Gets current y coordinate with delayed init.
     */
    private float getPageY() {
        if (this.pageLocation.y < 0.0f) {
            this.pageLocation.y = pageFormat.height - (this.fontMSSAdapter.size as float) - this.topMargin
        }
        this._pageLocation.y
    }

    /**
     * Sets current y coordinate.
     *
     * @param y The coordinate to set.
     */
    private void setPageY(float y) { this.pageLocation.y = y }

    /**
     * Gets the current x coordinate.
     */
    private float getPageX() {
        if (this.pageLocation.x < this.leftMargin) {
            this.pageLocation.x = this.leftMargin
        }
        this.pageLocation.x
    }

    /**
     * Sets the current X coordinate.
     *
     * @param x The coordinate to set.
     */
    private void setPageX(float x) { this.pageLocation.x = x }

    /**
     * This keeps track of if the text document is in text mode or not,
     * and ensures that it is in text mode.
     */
    private void ensureTextMode() {
        if (!this.textMode) {
            this.genDoc.docStream.beginText()
            this.textMode = true
        }
    }

    /**
     * This keeps track of if the text document is in text mode or not,
     * and ensures that it isn't in text mode.
     */
    private void ensureTextModeOff() {
        if (this.textMode) {
            this.genDoc.docStream.endText()
            this.textMode = false
        }
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param text The text to write.
     */
    void text(@NotNull String text) {
        notNull("text", text)
        ensureTextMode()

        float rightMarginPos = this.pageFormat.width - this.rightMargin

        List<String> wordList = new LinkedList<>()
        String space = ""
        text.split(" ").each {String word ->
            wordList.add(space + word)
            space = " "
        }

        wordList.each { String word ->
            float wordSize = calcTextWidth(word)
            if (this.pageX + wordSize > rightMarginPos) {
                this.pageX = this.leftMargin
                this.pageY -= (this.fontMSSAdapter.size + 2)
                if (this.pageY < this.bottomMargin) {
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
    }

    /**
     * Starts a boxed text block. Call endBox() to end it.
     *
     * @param boxColor The background color of the box.
     */
    void startBox(String boxColor) {
        this.box = new Box(startLocation: new Location(x: this.leftMargin, y: pageY + (this.fontMSSAdapter.size + 2)), color: boxColor)
        this.leftMargin = this.leftMargin + 10.0f
        this.rightMargin = this.rightMargin + 10.0f
        ensureTextModeOff()

        this.pageX = this.leftMargin
        ensureTextMode()
        this.genDoc.docStream.newLineAtOffset(pageX, pageY)
    }

    /**
     * Ends a previously started box.
     */
    void endBox() {
        ensureTextModeOff()
        this.leftMargin = this.leftMargin - 10.0f
        this.rightMargin = this.rightMargin - 10.0f
        this.box.endLocation = new Location(x: this.rightMargin, y: this.pageY - 4.0f)

        MSSColor color = new MSSColor(color: this.box.color)
        this.genDoc.bgDocStream.setStrokingColor(color.red, color.green, color.blue)

        this.genDoc.bgDocStream.addRect(
                this.box.endLocation.x,
                this.box.endLocation.y,
                this.pageFormat.width - this.leftMargin - this.rightMargin,
                this.box.startLocation.y - this.box.endLocation.y
        )
        this.genDoc.bgDocStream.fillAndStroke()
        this.genDoc.bgDocStream.closePath()

        ensureTextMode()
        pageX = this.leftMargin
        this.genDoc.docStream.newLineAtOffset(pageX, pageY)
        this.box = null
    }

    private boolean isBox() {
        return this.box != null
    }

    /**
     * Writes a new line.
     */
    void newLine() {
        this.genDoc.docStream.newLine()
        this.pageX = this.leftMargin
        this.pageY -= (this.fontMSSAdapter.size + 2)
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
        String boxColor = null
        if (isBox()) {
            boxColor = this.box.color
            endBox()
        }

        this.genDoc.newPage()

        ensureTextMode()
        this.genDoc.docStream.newLineAtOffset(this.pageLocation.x, this.pageLocation.y)

        ++this.pageNo
        if (boxColor != null) {
            startBox(boxColor)
        }
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
    }

    /**
     * Closes content stream and document.
     */
    void close() {
//        if (this.contentStream != null) {
//            this.contentStream.close()
//        }
//
//        if (this.document != null) {
//            this.document.close()
//        }
    }

}
