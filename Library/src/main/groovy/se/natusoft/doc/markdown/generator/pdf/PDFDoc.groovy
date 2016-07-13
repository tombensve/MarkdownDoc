package se.natusoft.doc.markdown.generator.pdf

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.generator.styles.MSSColor
import se.natusoft.doc.markdown.generator.styles.MSSColorPair
import se.natusoft.doc.markdown.io.Line

/**
 * This wraps an PDPageContentStream object and provide utilities to render on page.
 */
@CompileStatic
@TypeChecked
class PDFDoc {
    //
    // Inner Models
    //

    static class Location {
        float x = 0.0f, y = -1.0f
    }

    //
    // Private Members
    //

    /** The format to use for pages. */
    private PDRectangle pageFormat = PDRectangle.A4

    /** The current font to use. */
    @NotNull
    private PDFFontMSSAdapter fontMSSAdapter

    //
    // Properties
    //

    /** The current coordinates. */
    @NotNull
    private Location pageLocation = new Location()
    Location getPageLocation() { this.pageLocation }

    /** The PDFBox page representative. */
    @Nullable
    PDPage currentPage = null

    /** The content stream for the page. */
    @Nullable
    PDPageContentStream contentStream = null

    /** The document the page is part of. */
    @NotNull
    PDDocument document = null

    /** The page margins. */
    float topMargin, bottomMargin, leftMargin, rightMargin

    /** The current page number. When the whole document is rendered this will be the last page number. */
    private int pageNo = 1
    int getPageNo() { this.pageNo }

    //
    // Methods
    //

    /**
     * The color to render text with.
     *
     * @param foregroundColor The foreground color to set.
     */
    void applyForegroundColor(@NotNull MSSColor foregroundColor) {
        ensureInitialized()
        this.contentStream.setNonStrokingColor(foregroundColor.red, foregroundColor.green, foregroundColor.blue)
    }

    /**
     * The background color.
     *
     * @apram backgroundColor The background color to set.
     */
    void applyBackgroundColor(@NotNull MSSColor backgroundColor) {
        ensureInitialized()
        this.contentStream.setStrokingColor(backgroundColor.red, backgroundColor.green, backgroundColor.blue)
    }

    /**
     * Sets both forground and background color.
     *
     * @param colorPair The color pair to set.
     */
    void applyColorPair(@NotNull MSSColorPair colorPair) {
        applyForegroundColor(colorPair.foreground)
        applyBackgroundColor(colorPair.background)
    }

    /**
     * Sets and applies the font represented by the adapter.
     *
     * @param fontMSSAdapter The PDFFontMSSAdapter to set.
     */
    void applyFont(@NotNull PDFFontMSSAdapter fontMSSAdapter) {
        this.fontMSSAdapter = fontMSSAdapter
        this.contentStream.leading = this.fontMSSAdapter.size + 2
        this.fontMSSAdapter.applyFont(this.contentStream)
    }

    /**
     * Sets the format/size of the page.
     *
     * @param pageFormat The format to set.
     */
    void setPageFormat(@NotNull PDRectangle pageFormat) {
        this.pageFormat = pageFormat
    }

    /**
     * @return The size of the page.
     */
    @NotNull
    public PDRectangle getPageFormat() {
        return this.pageFormat
    }

    /**
     * Calculates the width of a text string.
     *
     * @param text The text to get the width of.
     *
     * @return The calculated width.
     */
    float calcTextWidth(@NotNull String text) {
        (this.fontMSSAdapter.font.getStringWidth(text) / 1000.0f * (float)this.fontMSSAdapter.size) as float
    }

    /**
     * Gets current y coordinate with delayed init.
     */
    private float getY() {
        if (this.pageLocation.y < 0.0f) {
            this.pageLocation.y = pageFormat.height - (this.fontMSSAdapter.size as float) - this.topMargin
        }
        this.y
    }

    /**
     * Gets the current x coordinate.
     */
    private float getX() {
        if (this.pageLocation.x < this.leftMargin) {
            this.pageLocation.x = this.leftMargin
        }
        this.pageLocation.x
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param text The text to write.
     */
    void writeText(@NotNull String text) {
        writeText(new Line(text))
    }

    /**
     * This ensures all vital fields/properties are initialized.
     */
    private void ensureInitialized() {
        if (this.document == null || this.currentPage == null || this.contentStream == null) {
            newPage()
        }
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param text The text to write.
     */
    void writeText(@NotNull Line text) {
        ensureInitialized()

        if (text == null) {
            // The @NotNull does not inhibit from actually passing null!
            this.contentStream.showText("[NULL]")
            return
        }

        float rightMarginPos = this.pageFormat.width - this.rightMargin
        float lineSize = calcTextWidth(text.toString())
        if ((this.x + lineSize) > rightMarginPos) {
            int words = text.numberOfWords
            while ((this.x + lineSize) > rightMarginPos && words > 0) {
                lineSize = calcTextWidth(text.getTextUpToWord(--words))
            }
            this.contentStream.showText(text.getTextUpToWord(words))
            this.contentStream.newLine()
            this.pageLocation.y -= (this.fontMSSAdapter.size + 2)

            if (this.pageLocation.y < this.bottomMargin) {
                newPage()
            }

            String nextLinePart = text.getTextFromWord(words)
            this.contentStream.showText(nextLinePart)
            this.pageLocation.x += calcTextWidth(nextLinePart)

        }
        else {
            EL buggadero: This of course does not handle that the rest of the text might be too long!
                          This needs to loop under the text left will fit on one line.
            this.contentStream.showText(text.toString())
            this.pageLocation.x += lineSize
        }
    }

    /**
     * Writes a new line.
     */
    void writeNL() {
        ensureInitialized()
        this.contentStream.newLine()
    }

    /**
     * Creates a new page.
     */
    void newPage() {
        if (this.document == null) {
            this.document = new PDDocument()
        }
        this.currentPage = new PDPage()
        this.currentPage.setMediaBox(this.pageFormat)
        this.document.addPage(this.currentPage)
        if (this.contentStream != null) {
            this.contentStream.endText()
            this.contentStream.close()
        }
        this.contentStream = new PDPageContentStream(this.document, this.currentPage)
        this.pageLocation = new Location(x: this.leftMargin, y: this.pageFormat.height - this.topMargin)
        this.contentStream.beginText()
        this.contentStream.newLineAtOffset(this.pageLocation.x, this.pageLocation.y)
    }

    /**
     * Forwards save to internal PDDocument.
     *
     * @param path The path of the file to save to.
     *
     * @throws IOException on failure to save
     */
    void save(String path) throws IOException {
        this.contentStream.close()
        this.document.save(path)
    }

    /**
     * Forwards save to internal PDDocument.
     *
     * @param file The file to save to.
     *
     * @throws IOException on failure to save
     */
    void save(File file) throws IOException {
        this.contentStream.close()
        this.document.save(file)
    }

    /**
     * Forwards save to internal PDDocument.
     *
     * @param steam The stream to save to.
     *
     * @throws IOException on failure to save
     */
    void save(OutputStream stream) throws IOException {
        this.contentStream.close()
        this.document.save(stream)
    }

    /**
     * Closes content stream and document.
     */
    void close() {
        if (this.contentStream != null) {
            this.contentStream.close()
        }

        if (this.document != null) {
            this.document.close()
        }
    }
}
