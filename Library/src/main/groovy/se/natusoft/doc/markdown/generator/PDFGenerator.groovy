/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.4.2
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-11-16: Created!
 *
 */
package se.natusoft.doc.markdown.generator

import com.itextpdf.text.Anchor
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chapter
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document as PDFDocument
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image as PDFImage
import com.itextpdf.text.List as PDFList
import com.itextpdf.text.ListItem as PDFListItem
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph as PDFParagraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.Section
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.options.PDFGeneratorOptions
import se.natusoft.doc.markdown.generator.pdf.PDFColorMSSAdapter
import se.natusoft.doc.markdown.generator.pdf.PDFStylesMSSAdapter
import se.natusoft.doc.markdown.generator.styles.MSS
import se.natusoft.doc.markdown.generator.styles.MSSColor
import se.natusoft.doc.markdown.generator.styles.MSSImage
import se.natusoft.doc.markdown.io.NullOutputStream
import se.natusoft.doc.markdown.model.AutoLink
import se.natusoft.doc.markdown.model.BlockQuote
import se.natusoft.doc.markdown.model.Code
import se.natusoft.doc.markdown.model.CodeBlock
import se.natusoft.doc.markdown.model.Comment
import se.natusoft.doc.markdown.model.Div
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.model.DocFormat
import se.natusoft.doc.markdown.model.DocItem
import se.natusoft.doc.markdown.model.Emphasis
import se.natusoft.doc.markdown.model.Header
import se.natusoft.doc.markdown.model.Image
import se.natusoft.doc.markdown.model.Link
import se.natusoft.doc.markdown.model.List
import se.natusoft.doc.markdown.model.ListItem
import se.natusoft.doc.markdown.model.Paragraph
import se.natusoft.doc.markdown.model.PlainText
import se.natusoft.doc.markdown.model.Strong

import java.util.ArrayList as JArrayList
import java.util.LinkedList as JLinkedList
import java.util.List as JList

// I rename some com.itextpdf.text classes due to name conflict.
// Yeah ... should probably continue renaming for consistency ... some day ...

/**
 * This generates a PDF documentItems from the provided Doc model.
 */
@CompileStatic
@TypeChecked
class PDFGenerator implements Generator {

    //
    // Constants
    //

    /** Constant to produce a new line in list entries. */
    private static final Chunk LIST_NEWLINE = new Chunk("\n", new Font(Font.FontFamily.HELVETICA, 4))

    /** Constant to produce an underline for H2 headers. */
    private static final LineSeparator HEADING_UNDERLINE = new LineSeparator(0.01f, 100f, BaseColor.GRAY, 0, 12)

    /** Constant to produce a horizontal line.  */
    private static final LineSeparator HORIZONTAL_RULE = new LineSeparator(0.01f, 100f, BaseColor.GRAY, 5, 16)

    //
    // Inner Classes
    //

    /**
     * A context for the generation process.
     */
    private static class PDFGeneratorContext extends GeneratorContext {
        //
        // Properties
        //

        /** The options for the PDF generator. */
        PDFGeneratorOptions options

        /** Adapter between MSS and iText fonts. */
        PDFStylesMSSAdapter pdfStyles = new PDFStylesMSSAdapter()

        /**
         * This will actually be added to the real Document later on, twice: once for the fake TOC resolve render
         *  and once for the real.
         */
        JList<Section> documentItems = null

        /** Current part tracker. */
        Chapter currentChapter = null

        /** Current part tracker. */
        Section currentSection = null

        /** Current part tracker. */
        Section currentH2 = null

        /** Current part tracker. */
        Section currentH3 = null

        /** Current part tracker. */
        Section currentH4 = null

        /** Current part tracker. */
        Section currentH5 = null

        /** Keeps track of the chapter. */
        int chapterNumber

        /** The table of contents. */
        JList<TOC> toc

        /** This is to exclude the title and tables of content pages from the page numbering. */
        int pageOffset

        //
        // Methods
        //

        /**
         * Initializes the members for a running a generation pass.
         */
        private void initRun() {
            this.documentItems = new JArrayList<Section>()
            this.currentChapter = null
            this.currentSection = null
            this.currentH2 = null
            this.currentH3 = null
            this.currentH4 = null
            this.currentH5 = null
            this.chapterNumber = 1
            this.toc = new JLinkedList<TOC>()
            this.pageOffset = 0
        }

    }

    //
    // Methods
    //

    /**
     * Returns the class that handles options for this generator.
     */
    @Override
    Class getOptionsClass() {
        PDFGeneratorOptions.class
    }

    /**
     * @return The name of this generator.
     */
    @Override
    String getName() {
        "pdf"
    }


    /**
     * The main API for the generator. This does the job!
     *
     * @param doc The documentItems model to generate from.
     * @param opts The generator options.
     * @param rootDir An optional root directory to prefix output paths with. This overrides the rootDir in options!
     */
    @Override
    void generate(@NotNull final Doc doc, @NotNull final Options opts, @Nullable final File rootDir)
            throws IOException, GenerateException {

        final File resultFile = rootDir != null ? new File(rootDir, opts.resultFile) : new File(opts.resultFile)
        final FileOutputStream resultStream = new FileOutputStream(resultFile)
        try {
            generate(doc, opts, rootDir, resultStream)
        }
        finally {
            resultStream.close()
        }
    }

    /**
     * Generates output from DocItem model.
     *
     * @param document The model to generate from.
     * @param options The generator options.
     * @param rootDir The optional root directory to prefix configured output with. Can be null. This overrides the
     *                rootDir in options!
     * @param resultStream The stream to write the result to.
     *
     * @throws IOException on I/O failures.
     * @throws GenerateException on other failures to generate target.
     */
    void generate(@NotNull final Doc doc, @NotNull final Options opts, @Nullable final File rootDir,
                  @NotNull final OutputStream resultStream) throws IOException, GenerateException {

        final PDFGeneratorContext context = new PDFGeneratorContext(
                options: opts as PDFGeneratorOptions,
                rootDir:  rootDir,
                fileResource: new FileResource(rootDir: rootDir, optsRootDir: (opts as PDFGeneratorOptions).rootDir)
        )

        context.pdfStyles.generatorContext = context

        if (context.options.mss != null && !context.options.mss.isEmpty()) {
            final File mssFile = context.fileResource.getResourceFile(context.options.mss)
            final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mssFile))
            try {
                context.pdfStyles.mss = MSS.fromInputStream(bis)
            }
            finally {
                bis.close()
            }
        }
        else {
            System.out.println("Using default MSS!")
            context.pdfStyles.mss = MSS.defaultMSS()
        }

        context.initRun()

        final LinkedList<String> divs = new LinkedList<>()

        context.pdfStyles.mss.currentDivs = divs

        doc.items.each { final DocItem docItem ->

            switch (docItem.format) {
                case DocFormat.Comment:
                    // We skip comments in general, but act on "@PB" within the comment for doing a page break.
                    final Comment comment = (Comment)docItem;
                    if (comment.text.indexOf("@PB") >= 0) {
                        context.documentItems.add(new NewPage())
                    }
                    // and also act on @PDFTitle, @PDFSubject, @PDFKeywords, @PDFAuthor, @PDFVersion, and @PDFCopyright
                    // for overriding those settings in the options. This allows the document rather than the generate
                    // config to provide this information.
                    extractCommentOptionsAnnotations(comment, context)
                    break

                case DocFormat.Paragraph:
                    writeParagraph(docItem as Paragraph, context)
                    break

                case DocFormat.Header:
                    writeHeader(docItem as Header, context)
                    break

                case DocFormat.BlockQuote:
                    writeBlockQuote(docItem as BlockQuote, context)
                    break;

                case DocFormat.CodeBlock:
                    writeCodeBlock(docItem as CodeBlock, context)
                    break

                case DocFormat.HorizontalRule:
                    writeHorizontalRule(context)
                    break

                case DocFormat.List:
                    writeList(docItem as List, context)
                    break

                case DocFormat.Div:
                    final Div div = docItem as Div
                    if (div.start) {
                        divs.offerFirst(div.name)
                    }
                    else {
                        divs.removeFirst()
                    }
                    break;

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" + docItem.class.name + "]")
            }
        }

        // Since chapters aren't added to the documentItems until the a new chapter is created we always have the
        // last chapter un-added.
        if (context.currentChapter != null) {
            context.documentItems.add(context.currentChapter)
        }

        final Rectangle pageSize = new Rectangle(PageSize.getRectangle(context.options.pageSize))
        if (context.options.backgroundColor != null) {
            pageSize.backgroundColor = new PDFColorMSSAdapter(new MSSColor(color: context.options.backgroundColor))
        }
        else {
            pageSize.backgroundColor = new PDFColorMSSAdapter(context.pdfStyles.mss.forDocument.
                    getColorPair(MSS.MSS_Pages.standard).background)
        }

        // _Please note that itext is not really compatible with groovys property access!

        PDFDocument document = null
        PdfWriter pdfWriter

        if (context.options.generateTOC) {
            // Do a fake render to generate TOC.
            document = new PDFDocument()
            document.setPageSize(pageSize)

            pdfWriter = PdfWriter.getInstance(document, new NullOutputStream())
            pdfWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_7)
            pdfWriter.setFullCompression()
            pdfWriter.setPageEvent(
                    new PageEventHandler(
                            resultFile: context.options.resultFile,
                            pageOffset: { context.pageOffset },
                            updateTOC: true,
                            toc: context.toc,
                            pdfStyles: context.pdfStyles,
                            context: context
                    )
            )

            document.open()

            // Since this.documentItems is just an ArrayList of Sections we have to add them to the real
            // documentItems now.
            context.documentItems.each { final Section section ->
                if (section instanceof NewPage) {
                    document.newPage()
                }
                else {
                    document.add(section)
                }
            }

            document.close()
        }

        // Render for real
        document = new PDFDocument()
        document.setPageSize(pageSize)

        pdfWriter = PdfWriter.getInstance(document, resultStream)
        pdfWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_7)
        pdfWriter.setFullCompression()
        pdfWriter.setPageEvent(
                new PageEventHandler(
                        resultFile: context.options.resultFile,
                        pageOffset: { context.pageOffset }, // Since this is in the form of a closure, it will always
                                                            // be up to date!
                        updateTOC: false,
                        toc: context.toc,
                        pdfStyles: context.pdfStyles,
                        context: context
                )
        )

        if (context.options.title    != null)  { document.addTitle(context.options.title)       }
        if (context.options.subject  != null)  { document.addSubject(context.options.subject)   }
        if (context.options.keywords != null)  { document.addKeywords(context.options.keywords) }
        if (context.options.author   != null)  { document.addAuthor(context.options.author)     }

        document.addCreationDate()
        document.addCreator("MarkdownDoc (https://github.com/tombensve/MarkdownDoc)")
        document.open()

        if (context.options.generateTitlePage) {
            writeTitlePage(pdfWriter, document, context)
        }

        if (context.options.generateTOC) {
            writeTOC(pdfWriter, document, context) // Modifies this.pageOffset!
        }

        // Since this.documentItems is just an ArrayList of Sections we have to add them to the real documentItems now.
        context.documentItems.each { final Section section ->
            if (section instanceof NewPage) {
                document.newPage() // This completely refuses to do anything!!!
            }
            else {
                document.add(section) // This will trigger Page events!
            }
        }

        document.close()

    }

    /**
     * Extracts a set of annotations from a comment and possibly updates context options depending on
     * found annotations.
     *
     * @param comment The comment to extract from.
     * @param context The context whose options to update.
     *
     * @return true if anything was updated, false otherwise.
     */
    private static boolean extractCommentOptionsAnnotations(@NotNull final Comment comment,
                                                            @NotNull final PDFGeneratorContext context) {
        boolean updated = false

        updated |= updateOptsFromAnnotation("@PDFTitle", comment) { final String text ->
            context.options.title = text
        }
        updated |= updateOptsFromAnnotation("@PDFSubject", comment) { final String text ->
            context.options.subject = text
        }
        updated |= updateOptsFromAnnotation("@PDFKeywords", comment) { final String text ->
            context.options.keywords = text
        }
        updated |= updateOptsFromAnnotation("@PDFAuthor", comment) { final String text ->
            context.options.author = text
        }
        updated |= updateOptsFromAnnotation("@PDFVersion", comment) { final String text ->
            context.options.version = text
        }
        updated |= updateOptsFromAnnotation("@PDFCopyright", comment) { final String text ->
            context.options.copyright = text
        }
        updated |= updateOptsFromAnnotation("@PDFAuthorLabel", comment) { final String text ->
            context.options.authorLabel = text
        }
        updated |= updateOptsFromAnnotation("@PDFVersionLabel", comment) { final String text ->
            context.options.versionLabel = text
        }
        updated |= updateOptsFromAnnotation("@PDFPageLabel", comment) { final String text ->
            context.options.pageLabel = text
        }
        updated |= updateOptsFromAnnotation("@PDFTableOfContentsLabel", comment) { final String text ->
            context.options.tableOfContentsLabel = text
        }
        updated |= updateOptsFromAnnotation("@PDFPageSize", comment) { final String text ->
            context.options.pageSize = text
        }
        updated |= updateOptsFromAnnotation("@PDFHideLinks", comment) { final String text ->
            context.options.hideLinks = Boolean.valueOf(text)
        }
        updated |= updateOptsFromAnnotation("@PDFUnorderedListItemPrefix", comment) { final String text ->
            context.options.unorderedListItemPrefix = text
        }
        updated |= updateOptsFromAnnotation("@PDFFirstLineParagraphIndent", comment) { final String text ->
            context.options.firstLineParagraphIndent = Boolean.valueOf(text)
        }
        updated |= updateOptsFromAnnotation("@PDFGenerateSectionNumbers", comment) { final String text ->
            context.options.generateSectionNumbers = Boolean.valueOf(text)
        }
        updated |= updateOptsFromAnnotation("@PDFGenerateTOC", comment) { final String text ->
            context.options.generateTOC = Boolean.valueOf(text)
        }
        updated |= updateOptsFromAnnotation("@PDFGenerateTitlePage", comment) { final String text ->
            context.options.generateTitlePage = Boolean.valueOf(text)
        }
        updated |= updateOptsFromAnnotation("@PDFTitlePageImage", comment) { final String text ->
            context.options.titlePageImage = text
        }

        updated
    }

    /**
     * Calls the extractCommentAnnotation() and if result is non null passes the result on to the update
     * annotation.
     *
     * @param ann The annotation to extract.
     * @param comment The comment to extract from.
     * @param update The closure to call on annotation value.
     *
     * @return true if and update was made, false otherwise.
     */
    private static boolean updateOptsFromAnnotation(@NotNull final String ann, @NotNull final Comment comment,
                                                 @NotNull final Closure<Object> update) {
        boolean updated = false
        final String text = extractCommentAnnotation(ann, comment)
        if (text != null) {
            update.call(text)
            updated = true
        }

        updated
    }

    /**
     * Extracts an annotation in the format of @Ann(text), @Ann("text"), or @Ann('text') from a
     * comment text.
     *
     * @param name The name of the annotation to extract. Should always start with "@".
     * @param comment The comment to extract from.
     *
     * @return The extracted annotation text or null if not found.
     */
    private static @Nullable String extractCommentAnnotation(@NotNull final String name,
                                                             @NotNull final Comment comment) {
        String result = null

        final String search = comment.text
        int ix = search.indexOf(name)
        if (ix >= 0) {
            ix = search.indexOf("(", ix)
            final int endIx = getEndParenthesis(search, ix + 1)
            result = search.substring(ix + 1, endIx).trim()
            if (result.startsWith("\"") || result.startsWith("'")) {
                result = result.substring(1)
            }
            if (result.endsWith("\"") || result.endsWith("'")) {
                result = result.substring(0, result.length() - 1)
            }
        }

        result
    }

    /**
     * Resolves the location of the annotation end parenthesis, ignoring any such within "..." or '...'.
     *
     * @param text The text to search in.
     * @param startIx The starting index.
     *
     * @return The end index.
     */
    private static int getEndParenthesis(@NotNull final String text, final int startIx) {
        boolean ignore = false
        int ix = startIx
        boolean done = false
        final int length = text.length()
        while (!done) {
            if (ix < length) {
                if (text.charAt(ix) != '\n' as char && text.charAt(ix) != '\r' as char) {
                    if (text.charAt(ix) == '"' as char || text.charAt(ix) == '\'' as char) {
                        ignore = !ignore

                    }

                    if (text.charAt(ix) == ')' as char && !ignore) {
                        done = true
                    } else {
                        ++ix
                    }
                }
                else {
                    throw new GenerateException(message:  "ERROR: A comment annotation (@Ann(...)) was not " +
                            "terminated! Could be a missing ['],[\"], or [)].")
                }
            }
            else {
                throw new GenerateException(message:  "ERROR: A comment annotation (@Ann(...)) was not " +
                        "terminated! Could be a missing ['],[\"], or [)].")
            }
        }

        ix
    }

    /**
     * Does some text replacements.
     *
     * @param text The original text.
     *
     * @return The possibly replaced text.
     */
    private static @NotNull String textReplace(@NotNull final String text) {

        text.replace("(C)", "©")
    }

    /**
     * Writes a table of content.
     *
     * @param pdfWriter The PdfWriter to write table of content on.
     * @param document The PDF documentItems being written.
     * @param context The PDF generator context.
     */
    private static void writeTOC(@NotNull final PdfWriter pdfWriter, @NotNull final PDFDocument document,
                          @NotNull final PDFGeneratorContext context) {
        final PdfContentByte cb = pdfWriter.getDirectContent()

        ++context.pageOffset

        // If you are tearing your hair over understanding this, please note that 0,0 is
        // at the bottom left of the page! I guess they just had to be different!

        float y = document.top() - document.topMargin()

        writeText(
                cb,
                Element.ALIGN_CENTER,
                context.options.tableOfContentsLabel,
                (float)(((document.right() - document.left()) / 2) + document.leftMargin()),
                (float)y,
                context.pdfStyles.getFont(MSS.MSS_TOC.toc)
        )

        y = y - 28
        context.toc.each { final TOC tocEntry ->
            writeText(
                    cb,
                    Element.ALIGN_LEFT,
                    context.options.generateSectionNumbers ? tocEntry.sectionTitle : indentTocEntry(tocEntry) ,
                    (float)(document.left() + document.leftMargin()),
                    (float)y,
                    context.pdfStyles.getFont(tocEntry.level)
            )

            writeText(
                    cb,
                    Element.ALIGN_RIGHT, "" + tocEntry.pageNumber,
                    (float)(document.right() - document.rightMargin()),
                    (float)y,
                    context.pdfStyles.getFont(MSS.MSS_TOC.toc)
            )

            y = y - 14
            if ( y < document.bottom()) {
                document.newPage()
                y = document.top()- document.topMargin()
                //noinspection GroovyResultOfIncrementOrDecrementUsed
                ++context.pageOffset // ^^^^ Its getting ridiculous, its in an if statement on true block !!!
            }
        }
    }

    private static String indentTocEntry(final TOC toc) {
        final StringBuilder sb = new StringBuilder()
        // We subtract 1 because the enum starts with "toc" and "h1" to "h6" is at ordinal 1 to 6.
        (toc.level.ordinal() - 1).times { sb.append("  ") }
        sb.append(toc.sectionTitle)

        sb.toString()
    }

    /**
     * Utility method that creates a Phrase from a String and then calls ColumnText.showTextAligned(...).
     *
     * @param cb The canvas to render on.
     * @param align The alignment to use.
     * @param text The text to render.
     * @param x The X position of the text (actually dependes on alignment).
     * @param y The Y position of the text.
     * @param font The font to use.
     */
    private static void writeText(@NotNull final PdfContentByte cb, final int align, @NotNull final String text,
                                  final float x, final float y, @NotNull final Font font) {

        final Phrase phrase = new Phrase(text, font)
        // The following unfortunately only accept NORMAL and BOLD styles. ITALIC and UNDERLINE is ignored!
        // Colors are also ignored!
        ColumnText.showTextAligned(cb, align, phrase, x, y, 0.0f)
    }

    /**
     * Writes a title page.
     *
     * @param pdfWriter The PdfWriter to write the title page on.
     * @param document The PDF documentItems being written.
     * @param context The PDF generator context.
     */
    private static void writeTitlePage(@NotNull final PdfWriter pdfWriter, @NotNull final PDFDocument document,
                                @NotNull final PDFGeneratorContext context) {

        final PdfContentByte canvas = pdfWriter.getDirectContent()

        final PDFColorMSSAdapter background = new PDFColorMSSAdapter(
                context.pdfStyles.mss.forFrontPage.getColorPair(MSS.MSS_Front_Page.title).background
        )

        final Rectangle rect = new Rectangle(document.left(), document.top(), document.right(), document.bottom());
        rect.borderWidthBottom = 0
        rect.borderColorBottom = background
        rect.borderWidthLeft = 0
        rect.borderColorLeft = background
        rect.backgroundColor = background
        canvas.rectangle(rect);

        ++context.pageOffset

        final String title = context.options.title
        final String subject = context.options.subject
        final String author = context.options.author
        final String version = context.options.version
        final String copyRight = context.options.copyright

        int topItems = 0
        int bottomItems = 0
        if (title != null) ++topItems
        if (subject != null) ++topItems
        if (version != null) ++topItems
        if (author != null) ++bottomItems
        if (copyRight != null) ++bottomItems

        final float yItemSizeTop = (float)(((document.top() - document.bottom()) / 2) / topItems)
        final float yItemSizeBottom = (float)(((document.top() - document.bottom()) / 2) / bottomItems)

        float yTop = document.top() - document.topMargin() - (float)(yItemSizeTop / 2) + 15
        float yBottom = document.bottom() + (float)(yItemSizeBottom / 2)
        final float x = (float)((document.right() - document.left()) / 2f + document.leftMargin())

        // Rendered from top of page

        if (title != null) {
            final Font font = context.pdfStyles.getFont(MSS.MSS_Front_Page.title)
            final Chunk chunk = new Chunk(textReplace(title), font)
            final Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yTop, 0.0f)
            yTop = (float)(yTop - (yItemSizeTop / 2f))
        }

        if (subject != null) {
            final Font font = context.pdfStyles.getFont(MSS.MSS_Front_Page.subject)
            final Chunk chunk = new Chunk(textReplace(subject), font)
            final Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yTop, 0.0f)
            yTop = (float)(yTop - (yItemSizeTop / 2f))
        }

        if (version != null) {
            final Font font = context.pdfStyles.getFont(MSS.MSS_Front_Page.version)
            final Chunk chunk = new Chunk("${context.options.versionLabel} " + version, font)
            final Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yTop, 0.0f)
        }

        // Rendered from bottom of page

        if (copyRight != null) {
            final Font font = context.pdfStyles.getFont(MSS.MSS_Front_Page.copyright)
            final Chunk chunk = new Chunk(textReplace(copyRight), font)
            final Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yBottom, 0.0f)
            yBottom = (float)(yBottom + (yItemSizeBottom / 2))
        }

        if (author != null) {
            final Font font = context.pdfStyles.getFont(MSS.MSS_Front_Page.author)
            final Chunk chunk = new Chunk("${context.options.authorLabel} " + author, font)
            final Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yBottom, 0.0f)
        }

        if (context.options.titlePageImage != null && context.options.titlePageImage.trim().length() != 0) {
            final MSSImage mssImage = context.pdfStyles.mss.forFrontPage.getImageData()
            final PDFImage image

            // Note that http:// does contain a ':'!
            final String imageRef = context.options.titlePageImage.replace("http:", "http§").replace("https:", "https§").
                    replace("ftp:", "ftp§")
            final String[] parts = imageRef.split(":")
            if (parts.length != 3) {
                throw new GenerateException(message: "Bad image specification! Should be <path/URL>:x:y!")
            }
            final String imagePath = parts[0].replace('§', ':')
            final float imgX = Float.valueOf(parts[1])
            final float imgY = Float.valueOf(parts[2])

            try {
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://") ||
                        imagePath.startsWith("ftp://")) {
                    image = PDFImage.getInstance(new URL(imagePath))
                } else {
                    String resourcePath = imagePath
                    if (!resourcePath.startsWith("/")) {
                        resourcePath = "/" + resourcePath
                    }
                    final URL resourceURL = System.getResource(resourcePath)
                    if (resourceURL != null) {
                        image = PDFImage.getInstance(resourceURL)
                    }
                    else {
                        final String localPath = context.fileResource.getResourceFile(imagePath)
                        image = PDFImage.getInstance(localPath)
                    }
                }
            }
            catch (final DocumentException | IOException e) {
                throw new GenerateException(message: "Failed to load front page image! [${e.message}]", cause: e)
            }

            image.scalePercent(mssImage.scalePercent)
            image.rotationDegrees = mssImage.rotateDegrees
            image.setAbsolutePosition(imgX, imgY)
            canvas.addImage(image)
        }

        document.newPage()
    }

    /**
     * Handle section options.
     *
     * @param section The section to handle.
     * @param context The PDF generator context.
     */
    private static void handleSectionOpts(@NotNull final Section section, @NotNull final PDFGeneratorContext context) {
        section.numberStyle = Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT

        if (!context.options.generateSectionNumbers) {
            section.numberDepth = 0
        }
    }

    /**
     * Writes a header text (H1 - H6).
     *
     * @param header The header model to write.
     * @param context The PDF generator context
     */
    @SuppressWarnings(["GroovyUntypedAccess", "GroovyResultOfIncrementOrDecrementUsed"])
    // The inspection fails to handle Groovys 'it' value.
    private static void writeHeader(@NotNull final Header header, @NotNull final PDFGeneratorContext context) {

        switch (header.level) {
            case { it == Header.Level.H1 } :
                // It feels like iText doesn't like it when you add a parent to its parent before
                // it has all its children ...
                if (context.currentChapter != null) {
                    context.documentItems.add(context.currentChapter)
                }
                final PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h1, context)

                final Chapter chapter = new Chapter(title, context.chapterNumber++)

                handleSectionOpts(chapter, context)

                chapter.add(Chunk.NEWLINE)

                context.currentChapter = chapter
                context.currentSection = chapter
                context.currentH2 = chapter
                context.currentH3 = chapter
                context.currentH4 = chapter
                context.currentH5 = chapter
                break

            case { it == Header.Level.H2 } :
                final PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h2, context)

                final Section section
                if (context.currentChapter != null) {
                    section = context.currentChapter.addSection(title, 2)
                }
                else {
                    // Sections can only exist in Chapters so if H1 is skipped and H2 is the first
                    // header than we have to create it as a Chapter rather than a Section.
                    if (context.currentChapter != null) {
                        context.documentItems.add(context.currentChapter)
                    }
                    section = new Chapter(title, context.chapterNumber++)
                    context.currentChapter = (Chapter)section
                }

                handleSectionOpts(section, context)

                context.currentSection = section
                context.currentH2 = section
                context.currentH3 = section
                context.currentH4 = section
                context.currentH5 = section
                break

            case { it == Header.Level.H3 } :
                final PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h3, context)

                final Section section
                if (context.currentH2 != null) {
                    section = context.currentH2.addSection(title, 3)
                }
                else {
                    // Se comment for H2
                    if (context.currentChapter != null) {
                        context.documentItems.add(context.currentChapter)
                    }
                    section = new Chapter(title, context.chapterNumber++)
                    context.currentChapter = (Chapter)section
                }

                handleSectionOpts(section, context)

                context.currentSection = section
                context.currentH3 = section
                context.currentH4 = section
                context.currentH5 = section
                break

            case { it == Header.Level.H4 } :
                final PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h4, context)

                final Section section
                if (context.currentH3 != null) {
                    section = context.currentH3.addSection(title, 4)
                }
                else {
                    // Se comment for H2
                    if (context.currentChapter != null) {
                        context.documentItems.add(context.currentChapter)
                    }
                    section = new Chapter(title, context.chapterNumber++)
                    context.currentChapter = (Chapter)section
                }

                handleSectionOpts(section, context)

                context.currentSection = section
                context.currentH4 = section
                context.currentH5 = section
                break

            case { it == Header.Level.H5 } :
                final PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h5, context)

                final Section section
                if (context.currentH4 != null) {
                    section = context.currentH4.addSection(title, 5)
                }
                else {
                    // Se comment for H2
                    if (context.currentChapter != null) {
                        context.documentItems.add(context.currentChapter)
                    }
                    section = new Chapter(title, context.chapterNumber++)
                    context.currentChapter = (Chapter)section
                }

                handleSectionOpts(section, context)

                context.currentSection = section
                context.currentH5 = section
                break

            case { it == Header.Level.H6 } :
                final PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h6, context)

                final Section section
                if (context.currentH5 != null) {
                    section = context.currentH5.addSection(title, 6)
                }
                else {
                    // Se comment for H2
                    if (context.currentChapter != null) {
                        context.documentItems.add(context.currentChapter)
                    }
                    section = new Chapter(title, context.chapterNumber++)
                    context.currentChapter = (Chapter)section
                }

                handleSectionOpts(section, context)

                context.currentSection = section
                break
        }
    }

    /**
     * Utility to create a Chunk for the header.
     *
     * @param text The header text.
     * @param level The header level to get font and colors for.
     * @param context The PDF generator context.
     *
     * @return The created Chunk.
     */
    private static @NotNull Chunk createHeaderChunk(@NotNull final String text, @NotNull final MSS.MSS_Pages level,
                                    @NotNull final PDFGeneratorContext context) {
        final Font font = context.pdfStyles.getFont(level)
        final Chunk chunk = new Chunk(textReplace(text), font)
        chunk.textRise = 2.0f
        chunk.lineHeight = (font.size + 2.0f) as float
        chunk.background = new PDFColorMSSAdapter(context.pdfStyles.mss.forDocument.getColorPair(level).background)

        chunk
    }

    /**
     * Adds header text plus an eventual horizontal ruler depending on style.
     *
     * @param header The header paragraph to add to.
     * @param text The header text to add.
     * @param level The level of the header.
     * @param context The PDF generator context.
     */
    private static void updateHeaderParagraph(@NotNull final PDFParagraph header, @NotNull final String text,
                                       @NotNull final MSS.MSS_Pages level, @NotNull final PDFGeneratorContext context) {

        header.add(createHeaderChunk(text, level, context))

        if(context.pdfStyles.mss.forDocument.getFont(level).hr) {
            header.add(Chunk.NEWLINE);
            header.add(HEADING_UNDERLINE)
            header.add(Chunk.NEWLINE)
        }
    }

    /**
     * This handles the case where there are no headings at all in the documentItems and thus no chapter nor sections.
     * In this case we create a dummy chapter and set it as both currentChapter and currentSection.
     *
     * @param context The PDF generator context.
     *
     * @return A valid Section
     */
    private static @NotNull Section getOrCreateCurrentSection(@NotNull final PDFGeneratorContext context) {
        if (context.currentSection == null) {
            context.currentChapter = new Chapter(0)
            context.currentSection = context.currentChapter
        }

        context.currentSection
    }

    /**
     * Writes block quote format text.
     *
     * @param blockQuote The block quote model to write.
     * @param context The PDF generator context.
     */
    private static void writeBlockQuote(@NotNull final BlockQuote blockQuote, @NotNull final PDFGeneratorContext context) {
        final PDFParagraph pdfParagraph = new PDFParagraph()
        pdfParagraph.setIndentationLeft(20.0f)
        final Font bqFont = new Font(context.pdfStyles.getFont(MSS.MSS_Pages.block_quote))

        // Since the MSS stylesheet always return a color, it cannot be used to override options colors.
        // It has to be the other way around.
        if (context.options.blockQuoteColor != null) {
            bqFont.setColor(
                    new PDFColorMSSAdapter(
                            new MSSColor(color:  context.options.blockQuoteColor)
                    )
            )
        }

        final PDFColorMSSAdapter background = new PDFColorMSSAdapter(
                context.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.block_quote).background
        )

        writeParagraph(pdfParagraph, blockQuote, bqFont, background, context)

        pdfParagraph.add(Chunk.NEWLINE)
        pdfParagraph.add(Chunk.NEWLINE)

        getOrCreateCurrentSection(context).add(pdfParagraph)
    }

    /**
     * Writes a code block format text.
     *
     * @param codeBlock The code block text to write.
     * @param context The PDF generator context.
     */
    private static void writeCodeBlock(@NotNull final CodeBlock codeBlock, @NotNull final PDFGeneratorContext context) {
        final Font codeFont = new Font(context.pdfStyles.getFont(MSS.MSS_Pages.code))

        final PDFParagraph pdfParagraph = new PDFParagraph(codeFont.size + 2)

        if (context.options.codeColor != null) {
            codeFont.setColor(
                    new PDFColorMSSAdapter(
                            new MSSColor(color:  context.options.codeColor)
                    )
            )
        }

        for (final DocItem item : codeBlock.items) {
            Chunk chunk = new Chunk(item.toString(), codeFont)
            chunk.setLineHeight((float)(codeFont.size))
            chunk.setTextRise(-2)
            chunk.setCharacterSpacing(0.5f)
            chunk.setBackground(
                    new PDFColorMSSAdapter(
                            context.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.code).background
                    )
            )
            pdfParagraph.add(chunk)
            pdfParagraph.add(Chunk.NEWLINE)
        }
        pdfParagraph.add(Chunk.NEWLINE)

        getOrCreateCurrentSection(context).add(pdfParagraph)
    }

    /**
     * Writes a horizontal rule.
     *
     * @param context The PDF generator context.
     */
    private static void writeHorizontalRule(@NotNull final PDFGeneratorContext context) {
        final PDFParagraph pdfParagraph = new PDFParagraph()
        pdfParagraph.add(HORIZONTAL_RULE)
        getOrCreateCurrentSection(context).add(pdfParagraph)
    }

    /**
     * Creates a com.itextpdf.text.List (PDFList) object from a se.natusoft.doc.markdown.model.List model.
     *
     * @param list The List model that determines the config of the PDFModel.
     * @param options The generator options.
     *
     * @return a configured PDFList object.
     */
    private static PDFList listToPDFList(@NotNull final List list, @NotNull final PDFGeneratorOptions options) {
        final PDFList pdfList
        if (list.ordered) {
            pdfList = new PDFList(PDFList.ORDERED)
        }
        else {
            pdfList = new PDFList(PDFList.UNORDERED)
            pdfList.setListSymbol(options.unorderedListItemPrefix)
        }
        pdfList.setAutoindent(true)

        pdfList
    }

    /**
     * Writes a list format text.
     *
     * @param list The list text to write.
     * @param context The PDF generator context.
     *
     * @throws GenerateException on failure to write this list.
     */
    private static void writeList(@NotNull final List list, @NotNull final PDFGeneratorContext context)
            throws GenerateException {

        final PDFList pdfList = listToPDFList(list, context.options)
        writeList(pdfList, list, 0f, context)
        getOrCreateCurrentSection(context).add(pdfList)
        getOrCreateCurrentSection(context).add(Chunk.NEWLINE)
    }

    /**
     * Writes a list format text with indent.
     *
     * @param pdfList The iText list object.
     * @param list The List model with the list text to write.
     * @param indent The indent value.
     * @param context The PDF generator context.
     *
     * @throws GenerateException on failure to write this list.
     */
    private static void writeList(@NotNull final PDFList pdfList, @NotNull final List list, final float indent,
                           @NotNull final PDFGeneratorContext context) throws GenerateException {

        pdfList.setIndentationLeft(indent)
        list.items.each { final DocItem item ->
            if (item instanceof ListItem) {
                final PDFListItem listItem = new PDFListItem()
                boolean first = true
                item.items.each { final pg ->
                    if (!first) {
                        // We have to fake a paragraph here since adding a (PDF)Paragraph to a (PDF)ListItem which
                        // is a (PDF)Paragraph screws it up making the list dots or numbers disappear. This
                        // unfortunately makes a little more space between paragraphs than for true paragraphs.
                        listItem.add(LIST_NEWLINE)
                        listItem.add(LIST_NEWLINE)
                    }
                    first = false

                    writeParagraph(
                            listItem,
                            pg as Paragraph,
                            context.pdfStyles.getFont(MSS.MSS_Pages.list_item),
                            new PDFColorMSSAdapter(
                                    context.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.list_item).background
                            ),
                            context
                    )
                }
                pdfList.add(listItem)
            }
            else if (item instanceof List) {
                final PDFList subList = listToPDFList(item as List, context.options)
                writeList(subList, item as List, (float)(indent + 10f), context)
                pdfList.add(subList)
            }
            else {
                throw new GenerateException(message: "Non ListItem found in List: Bad model structure!")
            }
        }
    }

    /**
     * Writes a paragraph format text.
     *
     * @param paragraph The paragraph text to write.
     * @param context The PDF generator context.
     *
     * @throws GenerateException on failure to write paragraph.
     */
    private static void writeParagraph(@NotNull final Paragraph paragraph, @NotNull final PDFGeneratorContext context)
            throws GenerateException {

        final PDFParagraph pdfParagraph = new PDFParagraph()

        pdfParagraph.setSpacingAfter(10)
        if (context.options.firstLineParagraphIndent) {
            pdfParagraph.setFirstLineIndent(10.0f)
        }
        writeParagraph(pdfParagraph, paragraph, context.pdfStyles.getFont(MSS.MSS_Pages.standard), context)

        getOrCreateCurrentSection(context).add(pdfParagraph)
    }

    /**
     * Writes a paragraph format text. This version actually does the job.
     *
     * @param pdfParagraph The iText Paragraph model.
     * @param paragraph The paragraph text to write.
     * @param font The font to use.
     * @param context The PDF generator context.
     *
     * @throws GenerateException on failure to write paragraph.
     */
    private static void writeParagraph(@NotNull final PDFParagraph pdfParagraph, @NotNull final Paragraph paragraph,
                                @NotNull final Font font, @NotNull final PDFGeneratorContext context)
            throws GenerateException {

        writeParagraph(
                pdfParagraph,
                paragraph,
                font,
                new PDFColorMSSAdapter(
                        context.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.standard).background
                ),
                context
        )
    }

    /**
     * Writes a paragraph format text. This version actually does the job.
     *
     * @param pdfParagraph The iText Paragraph model.
     * @param paragraph The paragraph text to write.
     * @param font The font to use.
     * @param context The PDF generator context.
     *
     * @throws GenerateException on failure to write paragraph.
     */
    private static void writeParagraph(@NotNull final PDFParagraph pdfParagraph, @NotNull final Paragraph paragraph,
                                @NotNull final Font font, @NotNull final PDFColorMSSAdapter background,
                                @NotNull final PDFGeneratorContext context) throws GenerateException {

        boolean first = true
        paragraph.items.each { final DocItem docItem ->
            if (docItem.renderPrefixedSpace && !first) {
                pdfParagraph.add(" ")
            }
            first = false

            switch (docItem.format) {

                case DocFormat.Code:
                    writeCode((Code)docItem, pdfParagraph, context)
                    break

                case DocFormat.Emphasis:
                    writeEmphasis((Emphasis)docItem, pdfParagraph, context)
                    break

                case DocFormat.Strong:
                    writeStrong((Strong)docItem, pdfParagraph, context)
                    break

                case DocFormat.Image:
                    writeImage((Image)docItem, pdfParagraph, context)
                    break

                case DocFormat.Link:
                    writeLink((Link)docItem, pdfParagraph, background, context)
                    break

                case DocFormat.AutoLink:
                    writeLink((AutoLink)docItem, pdfParagraph, background, context)
                    break

                case DocFormat.Space:
                    writePlainText((PlainText)docItem, pdfParagraph, font, background)
                    break;

                case DocFormat.PlainText:
                    writePlainText((PlainText)docItem, pdfParagraph, font, background)
                    break

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" +
                            docItem.getClass().getName() + "]")
            }
        }
    }

    /**
     * Writes a code formatted part within a paragraph.
     *
     * @param code The code text to write.
     * @param pdfParagraph The iText paragraph model to add to.
     * @param background The background color to use.
     * @param context The PDF generator context.
     */
    private static void writeCode(@NotNull final Code code, @NotNull final PDFParagraph pdfParagraph,
                           @NotNull final PDFGeneratorContext context) {

        final Chunk chunk = new Chunk(code.text, context.pdfStyles.getFont(MSS.MSS_Pages.code))
        chunk.setLineHeight(8)
        chunk.setCharacterSpacing(1.0f)
        pdfParagraph.add(chunk)
    }

    /**
     * Writes emphasis formatted part withing a paragraph.
     *
     * @param emphasis The emphasised text to write.
     * @param pdfParagraph The iText paragraph model to add to.
     * @param context The PDF generator context.
     */
    private static void writeEmphasis(@NotNull final Emphasis emphasis, @NotNull final PDFParagraph pdfParagraph,
                               @NotNull final PDFGeneratorContext context) {

        final Chunk chunk = new Chunk(textReplace(emphasis.text), context.pdfStyles.getFont(MSS.MSS_Pages.emphasis))

        // BUG: There seem to be a bug in iText here. For Italics iText seem to always render a white background
        //      no matter what color is in chunk.background. It works fine for Bold below.
        chunk.background = new PDFColorMSSAdapter(
                context.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.emphasis).background
        )

        pdfParagraph.add(chunk)
    }

    /**
     * Writes strong formatted part within a paragraph.
     *
     * @param strong The strong formatted text to write
     * @param pdfParagraph The iText paragraph model to add to.
     * @param context The PDF generator context
     */
    private static void writeStrong(@NotNull final Strong strong, @NotNull final PDFParagraph pdfParagraph,
                             @NotNull final PDFGeneratorContext context) {

        final Chunk chunk = new Chunk(textReplace(strong.text), context.pdfStyles.getFont(MSS.MSS_Pages.strong))

        chunk.background = new PDFColorMSSAdapter(
                context.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.strong).background
        )

        pdfParagraph.add(chunk)
    }

    /**
     * Writes an image within a paragraph.
     *
     * @param image contains url and alt text for the image.
     * @param pdfParagraph The iText paragraph model to add to.
     * @param context The PDF generator context.
     */
    private static void writeImage(@NotNull final Image image, @NotNull final PDFParagraph pdfParagraph,
                            @NotNull final PDFGeneratorContext context) {

        final PDFImage pdfImage = PDFImage.getInstance(new URL(context.fileResource.resolveUrl(image.url)))

        if (pdfImage != null) {
            final MSSImage imageInfo = context.pdfStyles.mss.forDocument.imageStyle

            if (imageInfo.scalePercent != null) pdfImage.scalePercent(imageInfo.scalePercent)
            if (imageInfo.align != null) {
                switch (imageInfo.align) {
                    case MSSImage.Align.LEFT:
                        pdfImage.setAlignment(PDFImage.LEFT)
                        break

                    case MSSImage.Align.MIDDLE:
                        pdfImage.setAlignment(PDFImage.MIDDLE)
                        break

                    case MSSImage.Align.RIGHT:
                        pdfImage.setAlignment(PDFImage.RIGHT)
                }
            }
            if (imageInfo.rotateDegrees != null) pdfImage.setRotationDegrees(imageInfo.rotateDegrees)

            pdfParagraph.add(pdfImage)

            // This sometimes helps in keeping text on the correct side of the image, but not always.
            pdfParagraph.add(Chunk.NEWLINE)
        }
        else {
            pdfParagraph.add(new Chunk("[" + image.text + "]", context.pdfStyles.getFont(MSS.MSS_Pages.standard)))
        }
    }

    /**
     * Writes a link within a paragraph.
     *
     * @param link The link url and text information.
     * @param pdfParagraph The iText paragraph model to add to.
     * @param background The background color to use.
     * @param context The PDF generator context.
     */
    private static void writeLink(@NotNull final Link link, @NotNull final PDFParagraph pdfParagraph,
                           @NotNull final PDFColorMSSAdapter background, @NotNull final PDFGeneratorContext context) {

        if (context.options.hideLinks) {
            writePlainText(link, pdfParagraph, context.pdfStyles.getFont(MSS.MSS_Pages.standard), background)
        }
        else {
            final Anchor anchor = new Anchor(link.text, context.pdfStyles.getFont(MSS.MSS_Pages.anchor))
            anchor.setReference(link.url)
            setBackgroundColorOnChunks(anchor.chunks, background)
            pdfParagraph.add(anchor)
        }
    }

    /**
     * Writes plain text within a paragraph.
     *
     * @param plainText The plain text to write.
     * @param pdfParagraph The iText paragraph to add to.
     * @param font The font to use.
     */
    private static void writePlainText(@NotNull final PlainText plainText, @NotNull final PDFParagraph pdfParagraph,
                                       @NotNull final Font font, @NotNull final PDFColorMSSAdapter background) {

        final Chunk chunk = new Chunk(textReplace(plainText.text), font)
        chunk.background = background
        pdfParagraph.add(chunk)
    }

    /**
     * Updates a list of chunks with a background color.
     *
     * @param chunks The chunks to update.
     * @param background The background color to update with.
     */
    private static void setBackgroundColorOnChunks(@NotNull final JList<Chunk> chunks,
                                                   @NotNull final PDFColorMSSAdapter background) {

        chunks.each { final Chunk chunk ->
            chunk.background = background
        }
    }

    //
    // Inner Classes
    //

    /**
     * Handles page rendering events to write header and footer and generate a table of contents.
     */
    private static class PageEventHandler extends PdfPageEventHelper {

        //
        // Properties
        //

        Closure<Integer> pageOffset
        String resultFile
        boolean updateTOC
        JList<TOC> toc
        PDFStylesMSSAdapter pdfStyles
        PDFGeneratorContext context

        @Override
        void onEndPage(@NotNull final PdfWriter writer, @NotNull final PDFDocument document) {
            if (document.pageNumber > (int)this.pageOffset.call()) {
                final PdfContentByte cb = writer.getDirectContent()

                // Write the filename centered as page header
                String fileName = this.resultFile
                final int fsIx = fileName.lastIndexOf(File.separator)
                if (fsIx >= 0) {
                    fileName = fileName.substring(fsIx + 1)
                }
                final int dotIx = fileName.lastIndexOf('.')
                fileName = fileName.substring(0, dotIx)
                final Chunk dfChunk = new Chunk(fileName, this.pdfStyles.getFont(MSS.MSS_Pages.footer))
                final Phrase documentFile = new Phrase(dfChunk)
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_CENTER,
                        documentFile,
                        (float)(((document.right() - document.left()) / 2.0f) + document.leftMargin()),
                        (float)(document.top() + 10.0),
                        0.0f
                )

                // Write the page number to the right as a page footer.
                final Chunk pageChunk = new Chunk("${context.options.pageLabel} " +
                        (document.getPageNumber() - (int)this.pageOffset.call()),
                        this.pdfStyles.getFont(MSS.MSS_Pages.footer))
                final Phrase pageNo = new Phrase(pageChunk)
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_RIGHT,
                        pageNo,
                        document.right(),
                        (float)(document.bottom() - 20.0),
                        0.0f
                )
            }
        }

        @Override
        void onChapter(@NotNull final PdfWriter writer, @NotNull final PDFDocument document,
                       final float paragraphPosition, @NotNull final PDFParagraph title) {

            if (this.updateTOC && title != null) {
                final MSS.MSS_TOC tocLevel =
                        MSS.MSS_TOC.valueOf(PdfName.decodeName(new String(title.getRole().bytes)).toLowerCase())
                this.toc.add(
                        new TOC(
                                sectionTitle: title.getContent().split("\n")[0],
                                pageNumber: document.getPageNumber(),
                                level: tocLevel
                        )
                )
            }
        }

        @Override
        void onSection(@NotNull final PdfWriter writer, @NotNull final PDFDocument document,
                       final float paragraphPosition, final int depth, @NotNull final PDFParagraph title) {

            if (this.updateTOC && title != null) {
                final MSS.MSS_TOC tocLevel =
                        MSS.MSS_TOC.valueOf(PdfName.decodeName(new String(title.getRole().bytes)).toLowerCase())
                this.toc.add(
                        new TOC(
                                sectionTitle: title.getContent().split("\n")[0],
                                pageNumber: document.getPageNumber(),
                                level: tocLevel
                        )
                )
            }
        }
    }

    /**
     * Stores a table of content entry.
     */
    private static class TOC {
        //
        // Properties
        //
        String sectionTitle
        int pageNumber
        MSS.MSS_TOC level = MSS.MSS_TOC.h1
    }

    /**
     * A rather dummy class to indicate that a new page should be generated rather than adding this section to the
     * document.
     */
    private static class NewPage extends Section {}
}
