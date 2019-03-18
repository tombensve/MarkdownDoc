/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
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
import se.natusoft.doc.markdown.generator.styles.MSS.MSS_Front_Page
import se.natusoft.doc.markdown.generator.styles.MSS.MSS_Pages
import se.natusoft.doc.markdown.generator.styles.MSS.MSS_TOC
import se.natusoft.doc.markdown.generator.utils.RequiresWithSection
import se.natusoft.doc.markdown.generator.utils.Sectionizer
import se.natusoft.doc.markdown.util.NotNullTrait
import se.natusoft.doc.markdown.util.Text
import se.natusoft.doc.markdown.util.Word

import javax.imageio.ImageIO
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage

import static se.natusoft.doc.markdown.generator.utils.Sectionizer.withSection

/**
 * This wraps a few PDFBox objects and uses these to generate PDF.
 *
 * This class is for encapsulating all PDFBox functionality and to provide a
 * higher level API.
 *
 * __Note-0:__ that this API makes use of some of the MSS models, but purely as models
 * holding needed information. There is no MSS resolving of styles done by this class!!
 * That has to be done before calling this class.
 *
 * __Note-1:__ As of 2.1.0 a new feature called "freeFloating" MSS attribute was added.
 * This feature allows you to break some basic rules like standard formatting flow going
 * from top to bottom of page. It allows to reset page X & Y within a div, and page margins
 * can also be changed within a div now. So this becomes something that is completely outside
 * of the normal rendering flow. Thereby a lot of the state in the renderer are saved on entry
 * into free floating mode, and restored on exit. No problem with that in itself, the optimal
 * place to start and stop free floating is however to high up where the MSS is not available.
 * Free floating is an MSS feature and thus needs MSS. This forces the checking for freeFloating
 * MSS to be done in PDFBoxGenerator.writeParagraphContent(...), which works well ... with
 * the exception of having free floating being ended after a newPage() which can be triggered
 * by a heading paragraph (using MSS setting). When this happens the previous page X & Y gets
 * restored for the new page, where it is entirely wrong. Note that this also happens if the
 * heading triggering a new page comes directly after the end of the free floating div.
 *
 * I'm sure there is a cleaner way to resolve this, but currently the implemented solution
 * is to let newPage() check if there is a free floating active and thus set a flag in the
 * saved state to not restore X & Y. This does not exactly smell like newly baked cookies ...
 *
 * I will look at this later, will most probably require some refactoring / restructuring of
 * code. I'm not entirely satisfied with the current state anyhow.
 *
 */
@CompileStatic
@TypeChecked
class PDFBoxDocRenderer implements NotNullTrait {

    //
    // Constants
    //

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    static final String A1 = "A1"
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    static final String A2 = "A2"
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    static final String A3 = "A3"
    static final String A4 = "A4"
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    static final String A5 = "A5"
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    static final String A6 = "A6"
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    static final String LEGAL = "LEGAL"
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    static final String LETTER = "LETTER"

    /** Indicates that image should be left aligned. */
    static final float X_OFFSET_LEFT_ALIGNED = 1000000.0f

    /** Indicates that the image should be centered on the page X wise. This applies to xOffset. */
    static final float X_OFFSET_CENTER = 1000001.0f

    /** Indicates that image should be right aligned. */
    static final float X_OFFSET_RIGHT_ALIGNED = 1000002.0f

    /** Indicates that the image should be rendererd at the current position. */
    static final float X_OFFSET_CURRENT = 1000003.0f

    /** The default color pair to use if none other is provided. */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    static final MSSColorPair DEFAULT_COLOR_PAIR =
            new MSSColorPair( foreground: MSSColor.BLACK, background: MSSColor.WHITE )

    //
    // Static fields
    //

    /**
     * Maps model page sizes to PDFBox page sizes.
     */
    private static Map<String, PDRectangle> pageSizes = [
            A0    : PDRectangle.A0,
            A1    : PDRectangle.A1,
            A2    : PDRectangle.A2,
            A3    : PDRectangle.A3,
            A4    : PDRectangle.A4,
            A5    : PDRectangle.A5,
            A6    : PDRectangle.A6,
            LEGAL : PDRectangle.LEGAL,
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

        Location() {}

        Location( Location copy ) {
            this.x = copy.x
            this.y = copy.y
        }
    }

    /**
     * This represents a background box, below text.
     */
    static class Box {
        Location startLocation
        Location endLocation
        MSSColor color

        Box validate() {
            if ( this.startLocation == null ) throw new MissingPropertyException( "startLocation", Location.class )
            if ( this.color == null ) throw new MissingPropertyException( "color", MSSColor.class )
            this
        }
    }

    /**
     * This represents an internal document layer. This is not a PDFBox concept! Each layer is a PDF document.
     * They are merged at the end. There is a front layer, the main document layer, and a background layer.
     */
    class PDFDocLayer { // No, I have not missed static keyword here! This needs to be an instance member.

        /** Represents the whole document. */
        PDDocument document = new PDDocument()

        /** The current page of the document. */
        PDPage docPage

        /** The current stream of the current page of the document. */
        PDPageContentStream docStream

        /**
         * Checks if the internal content stream is available (have been created).
         */
        boolean isDocStreamAvailable() {
            return this.docStream != null
        }

        /**
         * Returns the internal content stream. Never ever call this to check for null!
         * Use isDocStreamAvailable() for that instead!
         */
        PDPageContentStream getDocStream() {
            if ( this.docStream == null ) {
                try {
                    newPage()
                }
                catch ( Exception e ) {
                    throw new IllegalStateException( "PDFDocLayer.getDocStream() have triggered and exception! " +
                            "This should not happen! Make sure you have not called getDocStream() to check if it " +
                            "is null! Use isDocStreamAvailable() for that instead!", e )
                }
            }
            return this.docStream
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

        final Closure DOC_TEXT_AND_FILL_COLOR = { int red, int green, int blue ->
            this.middleLayer.docStream.setNonStrokingColor( red, green, blue )
            this.fgLayer.docStream.setNonStrokingColor( red, green, blue )
        }

        final Closure DOC_LINES_ETC_COLOR = { int red, int green, int blue ->
            this.middleLayer.docStream.setStrokingColor( red, green, blue )
            this.fgLayer.docStream.setStrokingColor( red, green, blue )
        }

        final Closure BG_DOC_TEXT_AND_FILL_COLOR = { int red, int green, int blue ->
            this.bgLayer.docStream.setNonStrokingColor( red, green, blue )
        }

        final Closure BG_DOC_LINES_ETC_COLOR = { int red, int green, int blue ->
            this.bgLayer.docStream.setStrokingColor( red, green, blue )
        }

        //
        // Properties
        //

        // NOTE1: If you have A and B and A is rendered before B at the same coordinates then B would be on top of A.
        //        The latest rendered is always on top of previous things. This in conjunction with that we don't
        //        know the end coordinates of a text until after rendering it makes it difficult to do things like
        //        colored boxed behind text. This could have been solved with just one document, but a bit messier.
        //        I decided to use 3 documents in parallel. One front document for text that should be above everything
        //        else. One middle document where text in general is rendered. One background document where drawing
        //        like boxes are done. These are then overlayed in correct order on save. Functions like newPage()
        //        manages all 3 layers so that they are in sync.
        //
        // NOTE2: External fonts are loaded into a specific document. An already loaded font cannot be applied
        //        to another document. It needs to be loaded separately for each document. This is currently
        //        a bit messy so the fgLayer are currently not used. I'm however leaving it in here for future
        //        use.
        //

        /** The foreground layer. Anything here will be rendered on top of the other 2 layers. */
        PDFDocLayer fgLayer = new PDFDocLayer()

        /** The middle layer. This is where the main text gets rendered. */
        PDFDocLayer middleLayer = new PDFDocLayer()

        /**
         * The "background" layer. Anything in this will be below anything in the other 2 layers. Boxes for
         * pre formatted text, for example, is rendered in this layer.
         */
        PDFDocLayer bgLayer = new PDFDocLayer()

        /**
         * This is what is used to render main text in, and will currenlty only be the middle layer.
         */
        PDFDocLayer mainLayer = middleLayer

        /** The PDF outline. */
        Outline outline = null

        /** Current page number. */
        int pageNumber = 0

        //
        // Inner Classes
        //

        /**
         * This is input to newPage(...) and is used to position the page within the document.
         */
        enum NewPagePosition {
            FIRST,
            LAST,
            AFTER_CURRENT
        }

        //
        // Methods
        //

        /**
         * Creates a new page.
         */
        void newPage() {
            newPage( NewPagePosition.LAST )
        }

        /**
         * Creates a new page.
         *
         * @param newPagePosition Where to add the new page.
         */
        void newPage( @NotNull NewPagePosition newPagePosition ) {
            ++this.pageNumber
            PDPage page = new PDPage()
            page.setMediaBox( PDFBoxDocRenderer.this.pageFormat )

            switch ( newPagePosition ) {
                case NewPagePosition.LAST:
                    this.middleLayer.document.addPage( page )
                    break

                case NewPagePosition.FIRST:
                    this.middleLayer.document.pages.insertBefore( page, this.middleLayer.document.pages.get( 0 ) )
                    break

                case NewPagePosition.AFTER_CURRENT:
                    this.middleLayer.document.pages.insertAfter( page, this.middleLayer.docPage )
                    break
            }
            this.middleLayer.docPage = page

            if ( this.middleLayer.docStreamAvailable ) {
                ensureTextModeOff()
                this.middleLayer.docStream.close()
            }
            this.middleLayer.docStream = new PDPageContentStream( this.middleLayer.document, this.middleLayer.docPage )


            PDPage bgPage = new PDPage()
            bgPage.setMediaBox( PDFBoxDocRenderer.this.pageFormat )

            switch ( newPagePosition ) {
                case NewPagePosition.LAST:
                    this.bgLayer.document.addPage( bgPage )
                    break

                case NewPagePosition.FIRST:
                    this.bgLayer.document.pages.insertBefore( bgPage, this.bgLayer.document.pages.get( 0 ) )
                    break

                case NewPagePosition.AFTER_CURRENT:
                    this.bgLayer.document.pages.insertAfter( bgPage, this.bgLayer.docPage )
                    break
            }
            this.bgLayer.docPage = bgPage

            if ( this.bgLayer.docStreamAvailable ) {
                this.bgLayer.docStream.close()
            }
            this.bgLayer.docStream = new PDPageContentStream( this.bgLayer.document, this.bgLayer.docPage )


            PDPage fgPage = new PDPage()
            fgPage.setMediaBox( PDFBoxDocRenderer.this.pageFormat )

            switch ( newPagePosition ) {
                case NewPagePosition.LAST:
                    this.fgLayer.document.addPage( fgPage )
                    break

                case NewPagePosition.FIRST:
                    this.fgLayer.document.pages.insertBefore( fgPage, this.fgLayer.document.pages.get( 0 ) )
                    break

                case NewPagePosition.AFTER_CURRENT:
                    this.fgLayer.document.pages.insertAfter( fgPage, this.fgLayer.docPage )
                    break
            }
            this.fgLayer.docPage = fgPage

            if ( this.fgLayer.docStreamAvailable ) {
                this.fgLayer.docStream.close()
            }
            this.fgLayer.docStream = new PDPageContentStream( this.fgLayer.document, this.fgLayer.docPage )
        }
    }

    /**
     * Creates a new page at the top of the document, and provides for inserting more pages under the first, but over the
     * rest of the document. This allows for rendering TOC and title page after the content is rendered.
     */
    class TopPage {
        TopPage() {
            docMgr.newPage( DocMgr.NewPagePosition.FIRST )
            PDFBoxDocRenderer.this.positionAtTopOfPage()
        }

        void newPage() {
            docMgr.newPage( DocMgr.NewPagePosition.AFTER_CURRENT )
            PDFBoxDocRenderer.this.positionAtTopOfPage()
        }
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

    /** Indicates that the current mode is free floating overlay text. */
    private boolean freeFloating = false

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

    Location getPageLocation() { this._pageLocation }

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    protected void setPageLocation( Location location ) { this._pageLocation = location }

    /** The page margins. */
    PageMargins margins

    /** The current page number. When the whole document is rendered this will be the last page number. */
    private int pageNo = 0

    int getPageNo() { this.pageNo }

    /** Should be set to true when rendering pre formatted text. */
    boolean preFormatted = false

    /** The current foreground and background colors.*/
    private MSSColorPair colors

    MSSColorPair getColors() { this.colors }

    /**
     * @return The current page number.
     */
    int getCurrentPageNumber() {
        this.docMgr.pageNumber
    }

    /** A set of holes in the text rendering area that will be avoided by normal text paragraphs. */
    List<TextHole> textHoles = new LinkedList<>()

    /** A margin between a hole edge and rendered text. A hole is usually not emtpy ... */
    float holeMargin

    /**
     * This is so that formats like small code blocks within a line/paragraph can override the font size to use for
     * newLine. Since code block can and does have a smaller font size in the default MSS when a newline occurs within
     * such being part of a paragraph the smaller font size were used before, but now this is called by paragraph code
     * renderer to set the font size of default text while rendering the code block, and then restores it to whatever
     * it was before by setting this to null again.
     */
    @Nullable
    private Integer _newLineFontSize = null

    Integer getNewLineFontSize() {
        this._newLineFontSize != null ? this._newLineFontSize : this.fontMSSAdapter.size
    }

    void setNewLineFontSize( Integer fontSize ) {
        this._newLineFontSize = fontSize
    }

    //
    // Handle saving and restoring of state for properties and members for freeFloating MSS feature.
    //

    private static class SaveState {
        boolean restoreLocation = true
        Location pageLocation
        int pageNumber, pageNo
        MSSColorPair colors
        boolean preFormatted
        Closure<Void> stylesApplicator
        List<TextHole> textHoles = []
    }
    private SaveState savedState = null

    void saveState() {
        this.savedState = new SaveState(
                pageLocation: new Location( this._pageLocation ),
                pageNumber: this.docMgr.pageNumber,
                pageNo: this.pageNo,
                colors: new MSSColorPair(
                        foreground: this.colors.foreground,
                        background: this.colors.background
                ),
                preFormatted: this.preFormatted,
                stylesApplicator: this.stylesApplicator
        )
        this.savedState.textHoles.addAll( this.textHoles )
    }

    void restoreState() {
        if ( this.savedState.restoreLocation ) {
            this._pageLocation = this.savedState.pageLocation
        }
        this.docMgr.pageNumber = this.savedState.pageNumber
        this.pageNo = this.savedState.pageNo
        this.colors = this.savedState.colors
        this.preFormatted = this.savedState.preFormatted
        this.stylesApplicator = this.savedState.stylesApplicator
        this.textHoles.clear()
        this.textHoles.addAll( this.savedState.textHoles )
        this.savedState = null
    }

    //
    // Internal Pseudo Properties
    //

    /**
     * Returns the standard text font size.
     */
    int getStandardTextFontSize() {
        return this.stylesMSSAdapter.mss.forDocument.getFont( MSS_Pages.standard ).size
    }

    /**
     * Gets current y coordinate with delayed init.
     */
    float getPageY() {
        if ( this.pageLocation.y < 0.0f ) {
            this.pageLocation.y = pageFormat.height - ( this.fontMSSAdapter.size as float ) -
                    this.margins.topMargin
        }
        this.pageLocation.y
    }

    /**
     * Sets current y coordinate.
     *
     * @param y The coordinate to set.
     */
    void setPageY( float y ) { this.pageLocation.y = y }

    /**
     * Gets the current x coordinate.
     */
    float getPageX() {
        this.pageLocation.x
    }

    /**
     * Sets the current X coordinate.
     *
     * @param x The coordinate to set.
     */
    void setPageX( float x ) {
        this.pageLocation.x = x
    }

    /**
     * Converts the public size to internal size.
     *
     * @param size The size to convert.
     */
    PDRectangle getPageFormat() {
        return pageSizes[ this.pageSize ]
    }

    //
    // Methods
    //

    /**
     * Checks for "freeFloating" MSS setting and enables / disables free floating mode.
     */
    void handleFreeFloating() {
        boolean currentFreeFloating = this.stylesMSSAdapter.mss.isFreeFloatingForDocument( this.section as MSS_Pages )
        if ( currentFreeFloating && !this.freeFloating ) {
            this.freeFloating = true
            saveState()
            this.pageX = this.stylesMSSAdapter.mss.getPageXForDocument( this.section as MSS_Pages )
            this.pageY = this.stylesMSSAdapter.mss.getPageYForDocument( this.section as MSS_Pages )
        }
        else if ( !currentFreeFloating && this.freeFloating ) {
            this.freeFloating = false
            restoreState()
        }
    }

    /**
     * Resets page X at left margin for new paragraph
     */
    void resetXForNewParagraph() {
        this.pageX = this.margins.leftMargin
    }

    void applyStyles() {
        if ( this.stylesApplicator != null ) {
            this.stylesApplicator.call()
        }
    }

    /**
     * @return The current page.
     */
    PDPage getCurrentPage() {
        return this.docMgr.mainLayer.docPage
    }

    /**
     * @return The first page.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    PDPage getFirstPage() {
        this.docMgr.mainLayer.document.getPage( 0 )
    }

    /**
     * @return The current total of pages.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    int getNoOfPages() {
        this.docMgr.mainLayer.document.getPages().count
    }

    /**
     * Returns the page at the specified index.
     *
     * @param ix The index of the page to get.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    PDPage getPage( int ix ) {
        return this.docMgr.mainLayer.document.getPage( ix )
    }

    /**
     *  Sets the left inset. Set to 0 to remove inset.
     *
     * @param inset The inset to set.
     */
    void setLeftInset( float inset ) {
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
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    void setRightInset( float inset ) {
        this.margins.rightInset = inset
    }

    /**
     * @return The right inset.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    float getRightInset() {
        return this.margins.rightInset
    }

    /**
     * Adds an outline entry for the current page.
     *
     * @param headerLevel The header level of the entry.
     * @param title The title of the outline entry.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    void addOutlineEntry( int headerLevel, @NotNull String title ) {
        addOutlineEntry( headerLevel, title, currentPage )
    }

    /**
     * Adds an outline entry for the specified page.
     *
     * @param headerLevel The header level of the entry.
     * @param title The title of the outline entry.
     * @param page The page to point to.
     */
    void addOutlineEntry( int headerLevel, @NotNull String title, @NotNull PDPage page ) {
        if ( this.docMgr.outline == null ) {
            this.docMgr.outline = new Outline()
            this.docMgr.outline.addToDocument( this.docMgr.mainLayer.document )
        }
        this.docMgr.outline.addEntry( headerLevel, title, page )
    }

    /**
     * The color to render text with.
     *
     * @param textAndLinesColor The color for text and lines to set.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    void setTextAndLinesColor( @NotNull MSSColor textAndLinesColor ) {
        notNull( "foregroundColor", textAndLinesColor )
        this.colors.foreground = textAndLinesColor
        this.colors.foreground.applyColor this.docMgr.DOC_TEXT_AND_FILL_COLOR
    }

    /**
     * Sets the color to use for drawing lines, etc.
     *
     * @param color The color to set.
     */
    void setLinesEtcColor( @NotNull MSSColor color ) {
        color.applyColor this.docMgr.DOC_LINES_ETC_COLOR
    }

    /**
     * The color for background fills.
     *
     * @apram backgroundColor The background color to set.
     */
    void setBackgroundFillColor( @NotNull MSSColor backgroundColor ) {
        notNull( "backgroundColor", backgroundColor )
        this.colors.background = backgroundColor
        this.colors.background.applyColor this.docMgr.BG_DOC_TEXT_AND_FILL_COLOR
    }

    /**
     * Sets a temporary foreground color that is not saved.
     *
     * @param foregroundColor The color to set temporarily
     */
    void setTemporaryForegroundColor( @NotNull MSSColor foregroundColor ) {
        notNull( "foregroundColor", foregroundColor )
        foregroundColor.applyColor this.docMgr.DOC_TEXT_AND_FILL_COLOR
    }

    /**
     * This should be done after a call to applyTemporaryForegroundColor. It will restore the previous color.
     */
    void restoreForegroundColor() {
        this.colors.foreground.applyColor this.docMgr.DOC_TEXT_AND_FILL_COLOR
    }

    /**
     * Sets the color pair by styles adapter and section.
     *
     * @param stylesMSSAdapter The adapter to use for looking up style.
     * @param section Lookup styles for this section.
     */
    void setColorPair( @NotNull PDFBoxStylesMSSAdapter stylesMSSAdapter, MSS_Pages section ) {
        setColorPair( stylesMSSAdapter.mss.forDocument.getColorPair( section ) )
    }

    /**
     * Sets both forground and background color.
     *
     * @param colorPair The color pair to set.
     */
    void setColorPair( @NotNull MSSColorPair colorPair ) {
        notNull( "colorPair", colorPair )
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
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    void withColors( @NotNull MSSColorPair colorPair, @NotNull Closure withColorsCall ) {
        MSSColorPair origColors = this.colors
        setColorPair( colorPair )
        withColorsCall.call()
        setColorPair( origColors )
    }

    /**
     * Loads an external font and wraps it as an PDFBoxFontMSSAdapter.
     *
     * @param url The url to the font.
     * @param mssFont An MSSFont defining size and style.
     * @return
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    PDFBoxFontMSSAdapter loadExternalFont( @NotNull String url, @NotNull MSSFont mssFont ) {
        URL fontURL = new URL( url )
        // Note that this is only done for the middle layer!
        PDFont font = PDType0Font.load( this.docMgr.middleLayer.document, fontURL.openStream() )
        return new PDFBoxFontMSSAdapter( font, mssFont )
    }

    /**
     * Sets and applies the font represented by the adapter. This will also resolve external fonts
     * specified in the MSS file.
     *
     * @param fontMSSAdapter The PDFFontMSSAdapter to set.
     */
    void setStyle( @NotNull PDFBoxStylesMSSAdapter stylesMSSAdapter, @NotNull MSS.Section section ) {
        notNull( "stylesMSSAdapter", stylesMSSAdapter )
        this.stylesMSSAdapter = stylesMSSAdapter
        this.section = section

        setFont( getFontAdapter( stylesMSSAdapter, section ) )
    }

    /**
     * Returns the current styles MSS adapter.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    PDFBoxStylesMSSAdapter getStylesMSSAdapter() {
        return this.stylesMSSAdapter
    }

    /**
     * Applies only a font.
     *
     * @param fontMSSAdapter The adapter for the font to apply.
     */
    private void setFont( @NotNull PDFBoxFontMSSAdapter fontMSSAdapter ) {
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
    @NotNull
    PDFBoxFontMSSAdapter getFontAdapter( @NotNull PDFBoxStylesMSSAdapter stylesMSSAdapter,
                                         @NotNull MSS.Section section ) {
        PDFBoxFontMSSAdapter fontMSSAdapter

        switch ( section.class ) {
            case MSS_Pages.class:
                fontMSSAdapter = stylesMSSAdapter.getFont( this.docMgr.mainLayer.document, section as MSS_Pages )
                break

            case MSS_Front_Page:
                fontMSSAdapter = stylesMSSAdapter.getFont( this.docMgr.mainLayer.document, section as MSS_Front_Page )
                break

            case MSS_TOC:
                fontMSSAdapter = stylesMSSAdapter.getFont( this.docMgr.mainLayer.document, section as MSS_TOC )
                break

            default:
                throw new IllegalStateException( "BUG: unknown MSS.Section!" )
        }

        fontMSSAdapter
    }

    /**
     * Applies the current font to the current page.
     */
    protected void applyFontInternal() {
        if ( this.fontMSSAdapter != null ) {
            this.docMgr.middleLayer.docStream.leading = this.fontMSSAdapter.size + 2
            this.fontMSSAdapter.applyFont( this.docMgr.middleLayer.docStream )
        }
    }

    /**
     * Temporarily changes style, executes a closure, and then restores the current style again.
     *
     * @param style The temp style to use.
     * @param section The style should be for this section.
     * @param withStyleCall The closure to call while font is active.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    protected void withStyle( @NotNull PDFBoxStylesMSSAdapter style, @NotNull MSS.Section section,
                              @NotNull Closure withStyleCall ) {
        PDFBoxStylesMSSAdapter origStyle = this.stylesMSSAdapter
        MSS.Section origSection = this.section
        setStyle( style, section )
        withStyleCall.call()
        if ( origStyle != null ) setStyle( origStyle, origSection )
    }

    /**
     * Temporarily changes font, executes a closure, and then restores the current font again.
     *
     * @param style The temp style to use.
     * @param withFontCall The closure to call while font is active.
     */
    protected void withFont( @NotNull PDFBoxFontMSSAdapter font, @NotNull Closure withFontCall ) {
        PDFBoxFontMSSAdapter origFont = this.fontMSSAdapter
        setFont( font )
        withFontCall.call()
        if ( origFont != null ) setFont( origFont )
    }

    /**
     * Calculates the width of a text string.
     *
     * @param text The text to get the width of.
     *
     * @return The calculated width.
     */
    float calcTextWidth( @NotNull String text ) {
        calcTextWidth( this.fontMSSAdapter, text )
    }

    /**
     * Calculates the width of a text string.
     *
     * @param fontAdapter The fontAdapter to get font from.
     * @param text The text to get the width of.
     *
     * @return The calculated width.
     */
    static protected float calcTextWidth( @NotNull PDFBoxFontMSSAdapter fontAdapter, @NotNull String text ) {
        notNull( "text", text )
        float width
        try {
            width = ( fontAdapter.font.getStringWidth( text ) / 1000.0f * ( float ) fontAdapter.size ) as float
        }
        catch ( IllegalArgumentException ignore ) { // PDFBox have problems with certain characters.
            StringBuilder sb = new StringBuilder()
            text.length().times {
                sb.append( 'X' )
            }
            width = ( fontAdapter.font.getStringWidth( sb.toString() ) / 1000.0f * ( float ) fontAdapter.size ) as float
        }

        width
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
        if ( !this.textMode ) {
            this.docMgr.mainLayer.docStream.beginText()
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
    void ensureTextMode( float x, float y ) {
        ensureTextMode()
        this.docMgr.mainLayer.docStream.newLineAtOffset( x, y )
    }

    /**
     * This keeps track of if the text document is in text mode or not,
     * and ensures that it isn't in text mode.
     */
    void ensureTextModeOff() {
        if ( this.textMode ) {
            this.docMgr.mainLayer.docStream.endText()
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
     * @param useDots If true dots will be rendered between section title and page number.
     * @param dotFactor The font size is divided by this value and the result is appended to the calculated
     *                  width of a '.' char. What value to use depends on the font and also partly on the
     *                  size of the font.
     * @param newPage A closure to call to create a new page.
     */
    @SuppressWarnings( "UnnecessaryQualifiedReference" )
    void tocEntry(
            @NotNull TOC tocEntry,
            boolean useDots = false,
            //float dotFactor,
            Closure<Void> newPage
    ) {
        withSection( MSS_Pages.standard ) {
            ensureTextModeOff()

            this.pageX = this.margins.leftMargin

            // Section number if any
            if ( tocEntry.sectionNumber != null ) {
                positionTextAtPageLocation()
                rawText( tocEntry.sectionNumber )
                this.pageX = this.margins.leftMargin + calcTextWidth( "0.0.0.0.0.0   " )
            }

            // Section title
            positionTextAtPageLocation()
            float titleEnd = ( this.pageX + calcTextWidth( tocEntry.sectionTitle ) ) as float
            rawText( tocEntry.sectionTitle )

            // Page number
            this.pageX = ( ( this.pageFormat.width - this.margins.rightMargin ) -
                    calcTextWidth( "${ tocEntry.pageNumber }" ) ) as float

            positionTextAtPageLocation()
            rawText( "${ tocEntry.pageNumber }" )

            // Dots between title and page number.
            if ( useDots ) {
                // Note that this.pageX is now the position of the page number at the right of the page! That minus
                // titleEnd gives us the space between the end of the title to the beginning of the page number.

                // Note that in the result there will be more non dotted space before the 1 digit page numbers than
                // the 2 digit page numbers, and the dots are more or less aligned to the right! This should not
                // be the result, and I fail to comprehend why it is!!
                float dotsSize = ( ( this.pageX - titleEnd ) - calcTextWidth( "..0000" ) ) as float

                StringBuilder sb = new StringBuilder()
                while ( calcTextWidth( sb.toString() ) < dotsSize ) {
                    sb.append( '.' )
                }
                this.pageX = ( titleEnd + calcTextWidth( ".." ) ) as float
                positionTextAtPageLocation()
                rawText( sb.toString() )
            }

            this.pageY -= this.fontMSSAdapter.size + 2
            if ( this.getPageY() <= this.margins.bottomMargin ) {
                newPage.call()
            }
        }
    }

    /**
     * @return a new TocPage.
     */
    TopPage createTopPage() {
        new TopPage()
    }

    /**
     * Writes pre formatted text as is.
     *
     * @param text The pre formatted text
     * @param stylesApplicator A closure that will be called to style text when needed.
     */
    @RequiresWithSection
    @NotNull
    PDRectangle preFormattedText( @NotNull Object text, @Nullable Closure stylesApplicator ) {
        notNull( "text", text )

        ensureTextMode()

        this.stylesApplicator = stylesApplicator
        applyStyles()

        float s_pageY = this.pageY
        float s_bottomMargin = this.margins.bottomMargin

        String[] lines = text.toString().split( "[\n\r]" )

        PDRectangle textArea = new PDRectangle( lowerLeftX: this.pageX, lowerLeftY: s_pageY )

        lines.each { String line ->
            this.docMgr.mainLayer.docStream.showText( line )
            this.pageX = this.margins.leftMargin
            this.pageY -= ( this.fontMSSAdapter.size + 2 )
            if ( this.pageY < s_bottomMargin ) {
                newPage()
                if ( stylesApplicator != null ) {
                    stylesApplicator.call()
                }
            }
            else {
                // Note: newLineAtOffset(...) does not work here!
                this.docMgr.mainLayer.docStream.newLine()
            }
        }

        textArea.upperRightX = this.pageX
        textArea.upperRightY = s_pageY + this.fontMSSAdapter.size

        textArea
    }

    /**
     * This word wraps a pre formatted text. If a line is too long it will be broken at a word boundary
     * and a new line inserted. __Note__ that this is done only once for each line!! If new new line is
     * also too long, nothing will be done with it! We are still dealing with pre formatted text! This
     * is just a little convenience help if it gets a little bit too long to fit within margins. This
     * is also only called if "preformattedWordWrap" is set to true in MSS!
     *
     * @param text The pre formatted text to word wrap slightly.
     *
     * @return A possibly wrapped text.
     */
    @NotNull
    String wordWrapPreformattedText( @NotNull String text ) {
        String result = ""
        String cr = ""

        text.split( "[\n\r]" ).each { String origLine ->
            String line = origLine
            boolean needWrap = false
            float width = calcTextWidth( line )
            while ( ( this.margins.leftMargin + this.leftInset + width ) > ( this.pageFormat.width - this.margins.rightMargin ) ) {
                needWrap = true
                line = line[ 0..( line.size() - 2 ) ]
                width = calcTextWidth( line )
            }
            result += cr
            if ( needWrap ) {
                while ( ![' ', '\t'].contains( line[ line.size() - 1 ] ) && line.size() > 0 ) {
                    line = line[ 0..( line.size() - 2 ) ]
                }
                line = line[ 0..( line.size() - 2 ) ]

                result += line
                result += "\n       "
                result += origLine[ line.size()..origLine.size() - 1 ]
            }
            else {
                result += line
            }
            cr = "\n"
        }

        result
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param txt The text to write.
     * @param stylesApplicator A closure that will be called to style text when needed.
     *
     * @return a PDRectangle enclosing the text just written. Useful when adding (PDF) annotations.
     */
    @RequiresWithSection
    @NotNull
    PDRectangle text( @NotNull Object txt, @Nullable Closure stylesApplicator ) {
        text( txt, stylesApplicator, false )
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param txt The text to write.
     *
     * @return a PDRectangle enclosing the text just written. Useful when adding (PDF) annotations.
     */
    @RequiresWithSection
    @NotNull
    PDRectangle text( @NotNull Object txt ) {
        text( txt, null, false )
    }

    /**
     * Returns the hole the specified coordinates are in if any, otherwise null is returned.
     *
     * @param x The x coordinate of the hole to check for.
     * @param y The y coordinate of the hole to check for.
     *
     * @return A TextHole or null.
     */
    @Nullable
    private TextHole checkForHole( float x, float y ) {
        this.textHoles.find { TextHole hole ->
            x >= hole.x && x <= ( hole.x + hole.width ) && y >= hole.y && y <= ( hole.y + hole.height )
        }
    }

    /**
     * Checks if pageX is within a hole in the text and if so moves pageX to after the hole.
     *
     * @param adaptParams holeMargin and wordSize parameters.
     */
    private void adaptToTextHoles( @NotNull AdaptParams adaptParams ) {
        float x = this.pageX
        float y = this.pageY + ( this.fontMSSAdapter.size * 2 )
        TextHole hole = checkForHole( x, y )
        if ( hole == null ) {
            x = this.pageX + adaptParams.holeMargin + adaptParams.wordSize
            hole = checkForHole( x, y )
        }
        if ( hole != null ) {
            this.pageX = hole.x + hole.width + adaptParams.holeMargin
        }
    }

    private static class AdaptParams {
        float holeMargin, wordSize
    }

    /**
     * Writes text to the document using the current font, colors, etc.
     *
     * @param txt The txt to write
     * @param stylesApplicator A closure that will be called to style text when needed.
     * @param pgBoxed If true then the text will be rendered with a background of the set background color.
     *
     * @return a PDRectangle enclosing the txt just written. Useful when adding (PDF) annotations.
     */
    @RequiresWithSection
    @NotNull
    PDRectangle text( @NotNull Object txt, @Nullable Closure stylesApplicator, boolean pgBoxed ) {

        notNull( "txt", txt )

        this.stylesApplicator = stylesApplicator
        applyStyles()


        Text text = new Text( content: txt.toString() )

        if ( pgBoxed ) {
            startParagraphBox()
        }

        ensureTextMode()

        float rightMarginPos = this.pageFormat.width - this.margins.rightMargin

        PDRectangle textArea = new PDRectangle( lowerLeftX: this.pageX, lowerLeftY: this.pageY )
        PDRectangle boxedTextArea = new PDRectangle( lowerLeftX: this.pageX - 1, lowerLeftY: this.pageY )

        text.words.each { Word word ->
            float wordSize = calcTextWidth( word.toString( this.preFormatted ) )

            adaptToTextHoles( new AdaptParams( holeMargin: this.holeMargin, wordSize: wordSize ) )

            if ( this.pageX + wordSize > rightMarginPos ) {
                if ( pgBoxed ) {
                    endParagraphBox( boxedTextArea )
                }

                this.pageX = this.margins.leftMargin
                this.pageY -= ( this.newLineFontSize + 2 )
                if ( this.pageY < this.margins.bottomMargin ) {
                    newPage()
                }

                adaptToTextHoles( new AdaptParams( holeMargin: this.holeMargin, wordSize: wordSize ) )

                if ( pgBoxed ) {
                    boxedTextArea = new PDRectangle( lowerLeftX: this.pageX - 1, lowerLeftY: this.pageY )
                }
            }

            positionTextAtPageLocation()

            try {
                this.docMgr.mainLayer.docStream.showText( word.toString( this.preFormatted ) )
                if ( this.fontMSSAdapter.underlined ) {
                    // I did spend some hours on trying to do this the officially correct way, but
                    // finally gave up since nothing seemed to work.
                    ensureTextModeOff()
                    applyStyles()
                    this.docMgr.mainLayer.docStream.setLineWidth( 0.001f )
                    this.docMgr.mainLayer.docStream.addRect( this.pageX, this.pageY - 3 as float, wordSize, 0.5f )
                    this.docMgr.mainLayer.docStream.closeAndFillAndStroke()
                    ensureTextMode()
                }
            }
            catch ( IllegalArgumentException iae ) {
                System.err.println( "PDFBox failed to render word '${ word }' due to \"${ iae.message }\"" )
            }
            this.pageX += wordSize
        }
        if ( pgBoxed ) {
            endParagraphBox( boxedTextArea )
        }

        // This is important! If not cleared, this text style could later be applied to a non text object, and PDFBox
        // will not fail on this until save, and it fails with a very cryptic message!
        this.stylesApplicator = null

        textArea.upperRightX = this.pageX
        textArea.upperRightY = this.pageY + this.fontMSSAdapter.size

        textArea
    }

    /**
     * This draws the specified text as is at the current pageX and pageY location.
     *
     * This does not handle new line nor page breaks!
     *
     * @param text The text to draw.
     */
    void rawText( @NotNull String text ) {
        rawText( text, null )
    }

    /**
     * This draws the specified text as is at the current pageX and pageY location.
     *
     * This does not handle new line nor page breaks!
     *
     * @param text The text to draw.
     * @param stylesApplicator This gets executed to apply styles when needed.
     */
    void rawText( @NotNull String text, @NotNull Closure stylesApplicator ) {
        this.stylesApplicator = stylesApplicator
        applyStyles()

        positionTextAtPageLocation()
        this.docMgr.mainLayer.docStream.showText( text )
        this.pageX += calcTextWidth( text )

        this.stylesApplicator = null
    }

    /**
     * Renders a text centered on the line and then moves to the next line.
     *
     * @param text The text to center.
     */
    void center( @NotNull String text ) {
        center( text, null )
    }

    /**
     * Renders a text centered on the line and then moves to the next line.
     *
     * __Note__: This is a much simpler rendering than text(...) does! For example, if the passed text here
     * is wider than the page, it will not be split into multiple lines! It will break margin and page bounds!
     *
     * @param text The text to center.
     * @param stylesApplicator Callback that applies styles.
     */
    void center( @NotNull String text, @NotNull Closure stylesApplicator ) {
        ensureTextModeOff()
        ensureTextMode() // Must do this for newLineAt(...) to work

        this.stylesApplicator = stylesApplicator
        applyStyles()

        float x = ( ( this.pageFormat.width / 2.0f ) - ( calcTextWidth( text ) / 2.0f ) ) as float
        this.docMgr.mainLayer.docStream.newLineAtOffset( x, this.pageY )
        this.docMgr.mainLayer.docStream.showText( text )
        this.pageX = this.margins.leftMargin
        this.pageY -= ( this.fontMSSAdapter.size + 2.0f )
        if ( this.pageY < this.margins.bottomMargin ) {
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
    @RequiresWithSection
    void endParagraphBox( @NotNull PDRectangle textArea ) {
        textArea.upperRightX = this.pageX + 1
        textArea.upperRightY = this.pageY + this.fontMSSAdapter.size

        // Since it does not seem possible to have other background color than white, the only way I found to have another
        // background color on text is to render a rect behind the text of the desired background color. This is really
        // annoying, but a google on setting background color for text in PDFBox does not give an answer. Maybe PDF as format
        // does not support it.
        this.docMgr.bgLayer.docStream.addRect(
                textArea.lowerLeftX,
                textArea.lowerLeftY - 2.0f as float,
                textArea.upperRightX - textArea.lowerLeftX as float,
                this.fontMSSAdapter.size + 2.0f as float
        )
        this.docMgr.bgLayer.docStream.closeAndFillAndStroke()
    }

    /**
     * Starts a boxed text block. Call endBox() to end it.
     *
     * @param boxColor The color to render the box in.
     */
    @RequiresWithSection
    void startBox( @NotNull MSSColor boxColor ) {
        this.box = new Box(
                startLocation: new Location(
                        x: this.margins.leftMargin,
                        y: pageY + ( this.fontMSSAdapter.size + 2 )
                ),
                color: boxColor
        ).validate()
        this.margins.leftMargin = this.margins.leftMargin + 10.0f
        this.margins.rightMargin = this.margins.rightMargin + 10.0f
        ensureTextModeOff()

        this.pageX = this.margins.leftMargin
        ensureTextMode()
        this.docMgr.mainLayer.docStream.newLineAtOffset( pageX, pageY )
    }

    /**
     * Ends a previously started box.
     */
    void endBox() {
        this.margins.clearTemps() // Needs to be done first!
        this.box.endLocation = new Location( x: this.margins.rightMargin, y: this.pageY )

        this.box.color.applyColor this.docMgr.BG_DOC_TEXT_AND_FILL_COLOR
        this.box.color.applyColor this.docMgr.BG_DOC_LINES_ETC_COLOR

        this.docMgr.bgLayer.docStream.addRect(
                this.box.endLocation.x,
                this.box.endLocation.y,
                this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin as float,
                this.box.startLocation.y - this.box.endLocation.y as float
        )
        this.docMgr.bgLayer.docStream.closeAndFillAndStroke()

        ensureTextModeOff()
        ensureTextMode()
        pageX = this.margins.leftMargin
        this.docMgr.mainLayer.docStream.newLineAtOffset( pageX, pageY )
        this.box = null
    }

    /**
     * @return true if there is an active box.
     */
    private boolean isBox() {
        return this.box != null
    }

    /**
     * Draws a underline left to right under the current text.
     */
    @RequiresWithSection
    void underlineText( float underlineOffset ) {
        ensureTextModeOff()
        this.docMgr.mainLayer.docStream.setLineWidth( 0.001f )
        this.docMgr.mainLayer.docStream.addRect( this.margins.leftMargin, ( this.pageY - underlineOffset ) as float,
                this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin as float, 0.001f )
        this.docMgr.mainLayer.docStream.closeAndFillAndStroke()
        ensureTextMode()
    }

    /**
     * Draws a horizontal rules over the page, and moves the current line down.
     *
     * @param thickness The thickness of the line. Anything over half the font size is a not a good idea! 2.0 or 3.0 is suggested.
     * @param color The color to draw the hr in.
     */
    @RequiresWithSection
    void hr( float thickness, @Nullable MSSColor color ) {
        if ( this.pageY - this.fontMSSAdapter.size < this.margins.bottomMargin ) {
            newPage()
            // We intentionally do not draw the hr if it is on a page break.
        }
        else {
            //newLine()
            ensureTextModeOff()
            float hrY = this.pageY + ( this.fontMSSAdapter.size / 2 ) + 4

            if ( color != null ) {
                color.applyColor this.docMgr.DOC_TEXT_AND_FILL_COLOR
                color.applyColor this.docMgr.DOC_LINES_ETC_COLOR
            }

            this.docMgr.mainLayer.docStream.setLineWidth( thickness )
            this.docMgr.mainLayer.docStream.moveTo( this.margins.leftMargin, hrY )
            this.docMgr.mainLayer.docStream.lineTo( this.pageFormat.width - this.margins.rightMargin as float, hrY )
            this.docMgr.mainLayer.docStream.closeAndFillAndStroke()

            ensureTextMode( this.pageX, this.pageY )
            //newLine()
        }
    }

    /**
     * Sets text at exact pageX and pageY.
     */
    void positionTextAtPageLocation() {
        ensureTextModeOff()
        ensureTextMode()
        this.docMgr.mainLayer.docStream.newLineAtOffset( this.pageX, this.pageY )
    }

    /**
     * Writes a new line.
     */
    void newLine() {
        newLine( 0.0f )
    }

    /**
     * Writes a new line.
     *
     * @paam yOffset an additional offset to add.
     */
    @RequiresWithSection
    void newLine( float yOffset ) {
        ensureTextModeOff()
        ensureTextMode()

        this.pageX = this.margins.leftMargin
        this.pageY -= ( yOffset + this.newLineFontSize + 2 )
        this.docMgr.mainLayer.docStream.newLineAtOffset( this.pageX, this.pageY )
        if ( this.pageY < this.margins.bottomMargin ) {
            newPage()
        }
    }

    /**
     * Writes a new line if not already at a new line and then writes one more new line to create an empty line.
     */
    void newSection() {
        if ( this.pageX != this.margins.leftMargin ) {
            newLine()
        }
        ensureTextModeOff()
        ensureTextMode()

        this.pageX = this.margins.leftMargin
        this.pageY -= this.stylesMSSAdapter.mss.getParagraphSpaceForDocument( Sectionizer.section as MSS_Pages )
    }

    /**
     * Positions rendering at the top of the page.
     */
    @RequiresWithSection
    void positionAtTopOfPage() {
        this.pageX = this.margins.leftMargin
        this.pageY = this.pageFormat.height - this.margins.topMargin

        // See Note-1 at top of file!
        if ( this.savedState != null ) {
            this.savedState.restoreLocation = false
        }

        ensureTextMode()
        this.docMgr.mainLayer.docStream.newLineAtOffset( this.pageLocation.x, this.pageLocation.y )
    }

    /**
     * Creates a new page.
     */
    @RequiresWithSection
    void newPage() {
        this.textHoles.clear()

        MSSColor boxColor = null
        if ( isBox() ) {
            boxColor = this.box.color
            endBox()
        }

        this.docMgr.newPage()

        positionAtTopOfPage()

        applyFontInternal()

        if ( this.pageNoActive ) {
            ++this.pageNo
            pageNumber( this.pageNo )
        }

        if ( boxColor != null ) {
            startBox( boxColor )
        }

        applyStyles()
    }

    /**
     * Adds a link to a page.
     *
     * @param linkText The text of the link.
     * @param uri The uri of the link
     */
    void link( @NotNull String linkText, @NotNull String uri ) {
        setTemporaryForegroundColor( MSSColor.LINK_BLUE )

        // Ain't Groovy cool! :-)
        this.docMgr.mainLayer.docPage.annotations.add(
                new PDAnnotationLink(
                        borderStyle: new PDBorderStyleDictionary( style: PDBorderStyleDictionary.STYLE_UNDERLINE, width: 0 ),
                        rectangle: this.text( linkText ),
                        action: new PDActionURI( URI: uri )
                )
        )

        restoreForegroundColor()
    }

    static final class ImageParam {
        /** If non null, the url to the image. */
        @NotNull
        InputStream imageStream

        /** Set to true if image stream contans a JPEG. */
        boolean jpeg = false

        /** The x offset to render at or 0 for left page margin. The X_OFFSET_* constants can also be used. */
        float xOffset = 0

        /** The y offset to render at. Usually 0. */
        float yOffset = 0

        /** How much to add under image. Usually 0. */
        float bottomAdd = 0

        /** The scale factor to apply to image size. */
        float scale = 0.60f

        /** The number of degrees to rotate image. Note that image can become larger on all sides!! */
        float rotate = 0.0f

        /** If true then a hole is created for the image which will mean that text will flow around it */
        boolean createHole = false

        /** The margin around holes. */
        float holeMargin = 4.0f

        /** Overrides the x coordinate if non null. */
        Float xOverride = null

        /** Overrides the y coordinate if non null. */
        Float yOverride = null
    }

    /**
     * Renders an image on the page.
     *
     * @param param The named parameters to this method. @See ImageParam.
     */
    void image( @NotNull ImageParam param ) {

        this.holeMargin = param.holeMargin

        PDImageXObject image

        if ( param.scale > 1.0f ) {
            param.scale = param.scale / 100.0f as float
        }

        // The dumb PDImageXObject API only allows loading from local file!! Thereby we have to go a little lower
        // than that. Since the TIFF support only loads from local file, TIFFs are not supported!

        if ( param.jpeg ) {
            image = JPEGFactory.createFromStream( this.docMgr.mainLayer.document, param.imageStream )
        }
        else {
            BufferedImage bufferedImage = ImageIO.read( param.imageStream )
            image = LosslessFactory.createFromImage( this.docMgr.mainLayer.document, bufferedImage )
        }

        float scaledWidth = image.width * param.scale
        float scaledHeight = image.height * param.scale

        float imageX = this.pageX, imageY = ( this.pageY - scaledHeight ) + this.fontMSSAdapter.size

        // TODO: A better formula for calculating how many font sizes to add to Y and how many for the hole.

        if ( Math.abs( scaledHeight - this.fontMSSAdapter.size ) < 20 ) {
            imageY += this.fontMSSAdapter.size
        }

        if ( param.xOverride != null ) {
            imageX = param.xOverride
        }
        if ( param.yOverride != null ) {
            imageY = param.yOverride
        }

        if ( param.xOverride == null ) {
            if ( param.xOffset == X_OFFSET_LEFT_ALIGNED ) {
                imageX = this.margins.leftMargin
                if ( this.pageX <= ( this.margins.leftMargin + scaledWidth ) ) {
                    imageY -= ( this.fontMSSAdapter.size + 2.0f )
                }
            }
            else if ( param.xOffset == X_OFFSET_CENTER ) {
                imageX = this.margins.leftMargin + ( ( this.pageFormat.width - this.margins.leftMargin - this.margins.rightMargin ) / 2.0f ) -
                        ( scaledWidth / 2.0f ) as float
                if ( this.pageX >= imageX ) {
                    imageY -= ( this.fontMSSAdapter.size + 2.0f )
                }
            }
            else if ( param.xOffset == X_OFFSET_RIGHT_ALIGNED ) {
                imageX = this.pageFormat.width - ( this.margins.rightMargin + scaledWidth )
                if ( this.pageX >= ( this.pageFormat.width - this.margins.rightMargin - scaledWidth ) ) {
                    imageY -= ( this.fontMSSAdapter.size + 2.0f )
                }
            }
            else {
                imageX += param.xOffset
            }
        }

        if ( imageY - ( param.yOffset + param.bottomAdd + 8.0f ) < this.margins.bottomMargin ) {
            newPage()
            imageY = this.pageY - scaledHeight
        }
        ensureTextModeOff()

        // Image X & Y seem to be left, top while pdf page have 0,0 at left, bottom ...
        AffineTransform at = new AffineTransform(
                scaledWidth,
                0.0f, 0.0f,
                scaledHeight,
                imageX,
                imageY
        )
        at.rotate( Math.toRadians( param.rotate ) )
        this.docMgr.mainLayer.docStream.drawImage( image, new Matrix( at ) )

        if ( param.createHole ) {
            this.textHoles << new TextHole(
                    x: imageX - holeMargin,
                    // Adding (font-size * 2) was achieved by trial and error, but I'm failing to understand why this is needed!
                    // It feels like an offset between image and text coordinates!
                    y: imageY + holeMargin + this.fontMSSAdapter.size,
                    width: scaledWidth + ( holeMargin * 2.0f ),
                    height: scaledHeight + ( ( holeMargin * 2.0f ) as float ) + ( this.fontMSSAdapter.size as float )
            )
        }
        else {
            newLine( scaledHeight + 2.0f as float )
        }

        ensureTextMode( this.pageX, this.pageY )
    }

    /**
     * Renders a page number.
     *
     * @param pageNumber The page number to render.
     */
    void pageNumber( int pageNumber ) {
        String pgnStr = "${ pageNumber }"
        ensureTextModeOff()

        PDFBoxFontMSSAdapter pageNoFont = PDFBoxFontMSSAdapter.PAGE_NUMBER_FONT

        withFont( pageNoFont ) {
            float width = calcTextWidth( pgnStr )
            ensureTextModeOff()
            ensureTextMode( this.pageFormat.width - this.margins.rightMargin - width as float, this.margins.bottomMargin -
                    ( ( pageNoFont.size * 2 ) + 6 ) as float )
            this.docMgr.mainLayer.docStream.showText( pgnStr )
        }
        ensureTextModeOff()
        ensureTextMode( this.pageX, this.pageY )
    }

    /**
     *
     * Forwards save to internal PDDocument.
     *
     * @param path The path of the file to save to.
     *
     * @throws IOException on failure to save
     */
    void save( @NotNull String path ) throws IOException {
        save( new File( path ) )
    }

    /**
     * Forwards save to internal PDDocument.
     *
     * @param file The file to save to.
     *
     * @throws IOException on failure to save
     */
    void save( @NotNull File file ) throws IOException {
        OutputStream saveStream = file.newOutputStream()
        try {
            save( saveStream )
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
    void save( @NotNull OutputStream stream ) throws IOException {
        PDDocument finalDoc

        if ( this.docMgr.middleLayer.docStreamAvailable ) {
            this.docMgr.middleLayer.docStream.close()

            if ( this.docMgr.bgLayer.docStreamAvailable ) {
                this.docMgr.bgLayer.docStream.close()

                Overlay overlay = new Overlay(
                        inputPDF: this.docMgr.middleLayer.document,
                        allPagesOverlayPDF: this.docMgr.bgLayer.document,
                        overlayPosition: Overlay.Position.BACKGROUND
                )
                finalDoc = overlay.overlay( new HashMap<Integer, String>() )
                overlay.close()
            }

            if ( this.docMgr.fgLayer.docStreamAvailable && finalDoc != null ) {
                this.docMgr.fgLayer.docStream.close()

                Overlay overlay = new Overlay(
                        inputPDF: finalDoc,
                        allPagesOverlayPDF: this.docMgr.fgLayer.document,
                        overlayPosition: Overlay.Position.FOREGROUND
                )
                finalDoc = overlay.overlay( new HashMap<Integer, String>() )
                overlay.close()

            }
        }

        if ( finalDoc != null ) {
            finalDoc.save( stream )
            finalDoc.close()
            this.docMgr.middleLayer.document.close()
            this.docMgr.bgLayer.document.close()
            this.docMgr.fgLayer.document.close()
        }
        else {
            throw new IOException( "There was nothing to save!" )
        }
    }

    /**
     * Closes content stream and document.
     */
    void close() {
        this.docMgr.fgLayer.document.close()
        this.docMgr.middleLayer.document.close()
        this.docMgr.bgLayer.document.close()
    }

}
