package se.natusoft.doc.markdown.generator

import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxFontMSSAdapter
import se.natusoft.doc.markdown.generator.styles.MSSColor
import se.natusoft.doc.markdown.generator.styles.MSSColorPair

/**
 * Defines a higher level API for generating a PDF.
 */
interface DocProducer {

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

    //
    // Inner APIs
    //

    interface Location {
        void setX(float x)
        float getX()

        void setY(float y)
        float getY()
    }

    //
    // Properties
    //

    /** The size of the page. Use constants on this class for valid standard sizes. */
    void setPageSize(@NotNull String pageSize);
    String getPageSize();

    /** The current coordinates. */
    @NotNull
    public Location getPageLocation()

    void setTopMargin(float topMargin);
    float getTopMargin();

    void setBottomMargin(float bottomMargin);
    float getBottomMargin();

    void setLeftMargin(float leftMargin)
    float getLeftMargin();

    void setRightMargin(float rightMargin)
    float getRightMargin()

    /** The current page number. When the whole document is rendered this will be the last page number. */
    int getPageNo()

    /** Should be set to true when rendering pre formatted text. */
    void setPreFormatted(boolean preFormatted)
    //boolean isPreformatted()

    /** The current foreground and background colors.*/
    MSSColorPair getColors()

    //
    // Methods
    //

    /**
     * The color to render text with.
     *
     * @param foregroundColor The foreground color to set.
     */
    void applyForegroundColor(@NotNull MSSColor foregroundColor)
    /**
     * The background color.
     *
     * @apram backgroundColor The background color to set.
     */
    void applyBackgroundColor(@NotNull MSSColor backgroundColor)

    /**
     * Sets both forground and background color.
     *
     * @param colorPair The color pair to set.
     */
    void applyColorPair(@NotNull MSSColorPair colorPair)

    /**
     * Sets and applies the font represented by the adapter.
     *
     * @param fontMSSAdapter The PDFFontMSSAdapter to set.
     */
    void applyFont(@NotNull PDFBoxFontMSSAdapter fontMSSAdapter)

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param text The text to write.
     */
    void text(@NotNull String text)

    /**
     * Starts a boxed text block. Call endBox() to end it.
     *
     * @param A pair of colors to use for the box. Foreground is the non stroking color and the background is the stroking color.
     */
    void startBox(MSSColorPair boxColorPair)

    /**
     * Ends a previously started box.
     */
    void endBox()

    /**
     * Draws a horizontal ruler over the page, and moves the current line down.
     */
    void hr()

    /**
     * Draws a horizontal rules over the page, and moves the current line down.
     *
     * @param thickness The thickness of the line. Anything over half the font size is a not a good idea! 2.0 or 3.0 is suggested.
     */
    void hr(float thickness)

    /**
     * Writes a new line.
     */
    void newLine()

    /**
     * Makes an empty line for a new paragraph.
     */
    void newParagraph()

    /**
     * Creates a new page.
     */
    void newPage()

    /**
     * Forwards save to internal PDDocument.
     *
     * @param path The path of the file to save to.
     *
     * @throws IOException on failure to save
     */
    void save(String path) throws IOException

    /**
     * Forwards save to internal PDDocument.
     *
     * @param file The file to save to.
     *
     * @throws IOException on failure to save
     */
    void save(File file) throws IOException
    /**
     * Forwards save to internal PDDocument.
     *
     * @param steam The stream to save to.
     *
     * @throws IOException on failure to save
     */
    void save(OutputStream stream) throws IOException

    /**
     * Closes content stream and document.
     */
    void close()

}
