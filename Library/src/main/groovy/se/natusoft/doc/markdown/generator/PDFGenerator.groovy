/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Code Version
 *         1.3.9
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
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.options.PDFGeneratorOptions
import se.natusoft.doc.markdown.generator.pdf.PDFColorMSSAdapter
import se.natusoft.doc.markdown.generator.pdf.PDFHeaderLevelCache
import se.natusoft.doc.markdown.generator.pdf.PDFStylesMSSAdapter
import se.natusoft.doc.markdown.generator.styles.MSS
import se.natusoft.doc.markdown.generator.styles.MSSColor
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
 * <p/>
 * <b>PLEASE NOTE:</b> Each instance of this class can only be used in one thread at a time!
 * If you try to run this in multiple threads then you need an instance per thread! This is
 * due to that some data required to do the work must be class members and not method local
 * variables. This due to rendering events needing access to them.
 * <p/>
 * <b>Short version:</p> Not thread safe!
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
    // Private Members
    //

    /** The options for the PDF generation. This makes use of OptionsManager. */
    private PDFGeneratorOptions options = null

    /** Adapter between MSS and iText fonts. */
    private PDFStylesMSSAdapter pdfStyles = new PDFStylesMSSAdapter()

    /** This is needed for generating TOC with correct styles. */
    private PDFHeaderLevelCache headerLevelCache = new PDFHeaderLevelCache()

    /** This will actually be added to the real Document later on, twice: once for the fake TOC resolve render and once for the real. */
    private JList<Section> documentItems = null

    /** Current part tracker. */
    private Chapter currentChapter = null

    /** Current part tracker. */
    private Section currentSection = null

    /** Current part tracker. */
    private Section currentH2 = null

    /** Current part tracker. */
    private Section currentH3 = null

    /** Current part tracker. */
    private Section currentH4 = null

    /** Current part tracker. */
    private Section currentH5 = null

    /** Keeps track of the chapter. */
    private int chapterNumber

    /** The table of contents. */
    private JList<TOC> toc

    /** This is to exclude the title and tables of content pages from the page numbering. */
    private int pageOffset

    /** The generator context to pass along. */
    private GeneratorContext generatorContext = null

    //
    // Methods
    //

    /**
     * Returns the class that handles options for this generator.
     */
    @Override
    public Class getOptionsClass() {
        return PDFGeneratorOptions.class
    }

    /**
     * @return The name of this generator.
     */
    @Override
    String getName() {
        return "pdf"
    }

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

    /**
     * The main API for the generator. This does the job!
     *
     * @param doc The documentItems model to generate from.
     * @param opts The generator options.
     * @param rootDir An optional root directory to prefix output paths with. This overrides the rootDir in options!
     */
    @Override
    public void generate(Doc doc, Options opts, File rootDir) throws IOException, GenerateException {
        File resultFile = rootDir != null ? new File(rootDir, opts.resultFile) : new File(opts.resultFile)
        FileOutputStream resultStream = new FileOutputStream(resultFile)
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
     * @param rootDir The optional root directory to prefix configured output with. Can be null. This overrides the rootDir in options!
     * @param resultStream The stream to write the result to.
     *
     * @throws IOException on I/O failures.
     * @throws GenerateException on other failures to generate target.
     */
    public void generate(Doc doc, Options opts, File rootDir, OutputStream resultStream) throws IOException, GenerateException {
        this.options = opts as PDFGeneratorOptions

        this.generatorContext = new GeneratorContext(fileResource: new FileResource(rootDir: rootDir, optsRootDir: this.options.rootDir))

        this.pdfStyles.generatorContext = this.generatorContext

        if (this.options.mss != null && !this.options.mss.isEmpty()) {
            File mssFile = this.generatorContext.fileResource.getResourceFile(this.options.mss)
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mssFile))
            try {
                this.pdfStyles.mss = MSS.fromInputStream(bis)
            }
            finally {
                bis.close()
            }
        }
        else {
            System.out.println("Using default MSS!")
            this.pdfStyles.mss = MSS.defaultMSS()
        }

        initRun()

        LinkedList<String> divs = new LinkedList<>()

        this.pdfStyles.mss.currentDivs = divs

        doc.items.each { DocItem docItem ->

            switch (docItem.format) {
                case DocFormat.Comment:
                    // We skip comments, but act on "@PB" within the comment for doing a page break.
                    Comment comment = (Comment)docItem;
                    if (comment.text.indexOf("@PB") >= 0) {
                        this.documentItems.add(new NewPage())
                    }
                    break

                case DocFormat.Paragraph:
                    writeParagraph(docItem as Paragraph)
                    break

                case DocFormat.Header:
                    writeHeader(docItem as Header)
                    break

                case DocFormat.BlockQuote:
                    writeBlockQuote(docItem as BlockQuote)
                    break;

                case DocFormat.CodeBlock:
                    writeCodeBlock(docItem as CodeBlock)
                    break

                case DocFormat.HorizontalRule:
                    writeHorizontalRule()
                    break

                case DocFormat.List:
                    writeList(docItem as List)
                    break

                case DocFormat.Div:
                    Div div = docItem as Div
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
        if (this.currentChapter != null) {
            this.documentItems.add(this.currentChapter)
        }

        Rectangle pageSize = new Rectangle(PageSize.getRectangle(this.options.pageSize))
        if (this.options.backgroundColor != null) {
            pageSize.backgroundColor = new PDFColorMSSAdapter(new MSSColor(color: this.options.backgroundColor))
        }
        else {
            pageSize.backgroundColor = new PDFColorMSSAdapter(this.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.standard).background)
        }

        // Please note that itext is not really compatible with groovys property access!

        PDFDocument document = null
        PdfWriter pdfWriter

        if (this.options.generateTOC) {
            // Do a fake render to generate TOC.
            document = new PDFDocument()
            document.setPageSize(pageSize)

            pdfWriter = PdfWriter.getInstance(document, new NullOutputStream())
            pdfWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_7)
            pdfWriter.setFullCompression()
            pdfWriter.setPageEvent(
                    new PageEventHandler(
                            resultFile: this.options.resultFile,
                            pageOffset: { this.pageOffset },
                            updateTOC: true,
                            toc: this.toc,
                            pdfStyles: this.pdfStyles,
                            headerLevelCache: this.headerLevelCache
                    )
            )

            document.open()

            // Since this.documentItems is just an ArrayList of Sections we have to add them to the real documentItems now.
            this.documentItems.each {Section section ->
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
                        resultFile: this.options.resultFile,
                        pageOffset: { this.pageOffset }, // Since this is in the form of a closure, it will always be up to date!
                        updateTOC: false,
                        toc: this.toc,
                        pdfStyles: this.pdfStyles,
                        headerLevelCache: this.headerLevelCache
                )
        )

        if (this.options.title != null)      { document.addTitle(this.options.title)       }
        if (this.options.subject != null)    { document.addSubject(this.options.subject)   }
        if (this.options.keywords != null)   { document.addKeywords(this.options.keywords) }
        if (this.options.author != null)     { document.addAuthor(this.options.author)     }

        document.addCreationDate()
        document.addCreator("MarkdownDoc (https://github.com/tombensve/MarkdownDoc)")
        document.open()

        if (this.options.generateTitlePage) {
            writeTitlePage(pdfWriter, document)
        }

        if (this.options.generateTOC) {
            writeTOC(pdfWriter, document) // Modifies this.pageOffset!
        }

        // Since this.documentItems is just an ArrayList of Sections we have to add them to the real documentItems now.
        this.documentItems.each { Section section ->
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
     * Does some text replacements.
     *
     * @param text The original text.
     *
     * @return The possibly replaced text.
     */
    private static String textReplace(String text) {
        String replaced = text

        replaced = replaced.replace("(C)", "©")

        return replaced
    }

    /**
     * Writes a table of content.
     *
     * @param pdfWriter The PdfWriter to write table of content on.
     * @param document The PDF documentItems being written.
     */
    private void writeTOC(PdfWriter pdfWriter, final PDFDocument document) {
        PdfContentByte cb = pdfWriter.getDirectContent()

        ++this.pageOffset

        // If you are tearing your hair over understanding this, please note that 0,0 is
        // at the bottom left of the page! I guess they just had to be different!

        float y = document.top() - document.topMargin()

        writeText(cb, Element.ALIGN_CENTER, "Table of Contents", (float)(((document.right() - document.left()) / 2) + document.leftMargin()),
                (float)y, this.pdfStyles.getFont(MSS.MSS_TOC.toc))

        y = y - 28
        this.toc.each { TOC tocEntry ->
            writeText(cb, Element.ALIGN_LEFT, tocEntry.sectionTitle, (float)(document.left() + document.leftMargin()), (float)y,
                    this.pdfStyles.getFont(tocEntry.level))
            writeText(cb, Element.ALIGN_RIGHT, "" + tocEntry.pageNumber, (float)(document.right() - document.rightMargin()), (float)y,
                    this.pdfStyles.getFont(MSS.MSS_TOC.toc))
            y = y - 14
            if ( y < document.bottom()) {
                document.newPage()
                y = document.top()- document.topMargin()
                ++this.pageOffset
            }
        }
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
    private static void writeText(PdfContentByte cb, int align, String text, float x, float y, Font font) {
        Phrase phrase = new Phrase(text, font)
        // The following unfortunately only accept NORMAL and BOLD styles. ITALIC and UNDERLINE is ignored!
        // Colors are also ignored!
        ColumnText.showTextAligned(cb, align, phrase, x, y, 0.0f)
    }

    /**
     * Writes a title page.
     *
     * @param pdfWriter The PdfWriter to write the title page on.
     * @param document The PDF documentItems being written.
     */
    private void writeTitlePage(PdfWriter pdfWriter, PDFDocument document) {
        PdfContentByte canvas = pdfWriter.getDirectContent()

        PDFColorMSSAdapter background = new PDFColorMSSAdapter(this.pdfStyles.mss.forFrontPage.getColorPair(MSS.MSS_Front_Page.title).background)
        Rectangle rect = new Rectangle(document.left(), document.top(), document.right(), document.bottom());
        rect.borderWidthBottom = 0
        rect.borderColorBottom = background
        rect.borderWidthLeft = 0
        rect.borderColorLeft = background
        rect.backgroundColor = background
        canvas.rectangle(rect);

        ++this.pageOffset

        String title = this.options.title
        String subject = this.options.subject
        String author = this.options.author
        String version = this.options.version
        String copyRight = this.options.copyright

        int topItems = 0
        int bottomItems = 0
        if (title != null) ++topItems
        if (subject != null) ++topItems
        if (version != null) ++topItems
        if (author != null) ++bottomItems
        if (copyRight != null) ++bottomItems

        float yItemSizeTop = (float)(((document.top() - document.bottom()) / 2) / topItems)
        float yItemSizeBottom = (float)(((document.top() - document.bottom()) / 2) / bottomItems)

        float yTop = document.top() - document.topMargin() - (float)(yItemSizeTop / 2) + 15
        float yBottom = document.bottom() + (float)(yItemSizeBottom / 2)
        float x = (float)(document.right() - document.left()) / 2f + document.leftMargin()

        // Rendered from top of page

        if (title != null) {
            Font font = this.pdfStyles.getFont(MSS.MSS_Front_Page.title)
            Chunk chunk = new Chunk(textReplace(title), font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yTop, 0.0f)
            yTop = (float)(yTop - (yItemSizeTop / 2f))
        }

        if (subject != null) {
            Font font = this.pdfStyles.getFont(MSS.MSS_Front_Page.subject)
            Chunk chunk = new Chunk(textReplace(subject), font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yTop, 0.0f)
            yTop = (float)(yTop - (yItemSizeTop / 2f))
        }

        if (version != null) {
            Font font = this.pdfStyles.getFont(MSS.MSS_Front_Page.version)
            Chunk chunk = new Chunk("${this.pdfStyles.mss.forFrontPage.getVersionLabel("Version:")} " + version, font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yTop, 0.0f)
        }

        // Rendered from bottom of page

        if (copyRight != null) {
            Font font = this.pdfStyles.getFont(MSS.MSS_Front_Page.copyright)
            Chunk chunk = new Chunk(textReplace(copyRight), font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yBottom, 0.0f)
            yBottom = (float)(yBottom + (yItemSizeBottom / 2))
        }

        if (author != null) {
            Font font = this.pdfStyles.getFont(MSS.MSS_Front_Page.author)
            Chunk chunk = new Chunk("${this.pdfStyles.mss.forFrontPage.getAuthorLabel("Author:")} " + author, font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, yBottom, 0.0f)
        }

        document.newPage()
    }

    /**
     * Handle section options.
     *
     * @param section The section to handle.
     */
    private void handleSectionOpts(Section section) {
        section.numberStyle = Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT

        if (!this.options.generateSectionNumbers) {
            section.numberDepth = 0
        }
    }

    /**
     * Writes a header text (H1 - H6).
     *
     * @param header The header model to write.
     */
    private void writeHeader(Header header) {

        switch (header.level) {
            case { it == Header.Level.H1 } :
                this.headerLevelCache.put(header.text, MSS.MSS_TOC.h1)
                // It feels like iText doesn't like it when you add a parent to its parent before
                // it has all its children ...
                if (this.currentChapter != null) {
                    this.documentItems.add(this.currentChapter)
                }
                PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h1)

                Chapter chapter = new Chapter(title, this.chapterNumber ++)

                handleSectionOpts(chapter)

                chapter.add(Chunk.NEWLINE)

                this.currentChapter = chapter
                this.currentSection = chapter
                this.currentH2 = chapter
                this.currentH3 = chapter
                this.currentH4 = chapter
                this.currentH5 = chapter
                break

            case { it == Header.Level.H2 } :
                this.headerLevelCache.put(header.text, MSS.MSS_TOC.h2)
                PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h2)

                Section section
                if (this.currentChapter != null) {
                    section = this.currentChapter.addSection(title, 2)
                }
                else {
                    // Sections can only exist in Chapters so if H1 is skipped and H2 is the first
                    // header than we have to create it as a Chapter rather than a Section.
                    if (this.currentChapter != null) {
                        this.documentItems.add(this.currentChapter)
                    }
                    section = new Chapter(title, this.chapterNumber++)
                    this.currentChapter = (Chapter)section
                }

                handleSectionOpts(section)

                this.currentSection = section
                this.currentH2 = section
                this.currentH3 = section
                this.currentH4 = section
                this.currentH5 = section
                break

            case { it == Header.Level.H3 } :
                this.headerLevelCache.put(header.text, MSS.MSS_TOC.h3)
                PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h3)

                Section section
                if (this.currentH2 != null) {
                    section = this.currentH2.addSection(title, 3)
                }
                else {
                    // Se comment for H2
                    if (this.currentChapter != null) {
                        this.documentItems.add(this.currentChapter)
                    }
                    section = new Chapter(title, this.chapterNumber++)
                    this.currentChapter = (Chapter)section
                }

                handleSectionOpts(section)

                this.currentSection = section
                this.currentH3 = section
                this.currentH4 = section
                this.currentH5 = section
                break

            case { it == Header.Level.H4 } :
                this.headerLevelCache.put(header.text, MSS.MSS_TOC.h4)
                PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h4)

                Section section
                if (this.currentH3 != null) {
                    section = this.currentH3.addSection(title, 4)
                }
                else {
                    // Se comment for H2
                    if (this.currentChapter != null) {
                        this.documentItems.add(this.currentChapter)
                    }
                    section = new Chapter(title, this.chapterNumber++)
                    this.currentChapter = (Chapter)section
                }

                handleSectionOpts(section)

                this.currentSection = section
                this.currentH4 = section
                this.currentH5 = section
                break

            case { it == Header.Level.H5 } :
                this.headerLevelCache.put(header.text, MSS.MSS_TOC.h5)
                PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h5)

                Section section
                if (this.currentH4 != null) {
                    section = this.currentH4.addSection(title, 5)
                }
                else {
                    // Se comment for H2
                    if (this.currentChapter != null) {
                        this.documentItems.add(this.currentChapter)
                    }
                    section = new Chapter(title, this.chapterNumber++)
                    this.currentChapter = (Chapter)section
                }

                handleSectionOpts(section)

                this.currentSection = section
                this.currentH5 = section
                break

            case { it == Header.Level.H6 } :
                this.headerLevelCache.put(header.text, MSS.MSS_TOC.h6)
                PDFParagraph title = new PDFParagraph()
                updateHeaderParagraph(title, header.text, MSS.MSS_Pages.h6)

                Section section
                if (this.currentH5 != null) {
                    section = this.currentH5.addSection(title, 6)
                }
                else {
                    // Se comment for H2
                    if (this.currentChapter != null) {
                        this.documentItems.add(this.currentChapter)
                    }
                    section = new Chapter(title, this.chapterNumber++)
                    this.currentChapter = (Chapter)section
                }

                handleSectionOpts(section)

                this.currentSection = section
                break
        }
    }

    /**
     * Utility to create a Chunk for the header.
     *
     * @param text The header text.
     * @param level The header level to get font and colors for.
     *
     * @return The created Chunk.
     */
    private Chunk createHeaderChunk(String text, MSS.MSS_Pages level) {
        Font font = this.pdfStyles.getFont(level)
        Chunk chunk = new Chunk(textReplace(text), font)
        chunk.setTextRise(2f)
        chunk.setLineHeight((float)(font.size + 2.0f))
        chunk.background = new PDFColorMSSAdapter(this.pdfStyles.mss.forDocument.getColorPair(level).background)
        return chunk
    }

    /**
     * Adds header text plus an eventual horizontal ruler depending on style.
     *
     * @param header The header paragraph to add to.
     * @param text The header text to add.
     * @param level The level of the header.
     */
    private void updateHeaderParagraph(PDFParagraph header, String text, MSS.MSS_Pages level) {
        header.add(createHeaderChunk(text, level))
        if(this.pdfStyles.mss.forDocument.getFont(level).hr) {
            header.add(Chunk.NEWLINE);
            header.add(HEADING_UNDERLINE)
            header.add(Chunk.NEWLINE)
        }
    }

    /**
     * This handles the case where there are no headings at all in the documentItems and thus no chapter nor sections.
     * In this case we create a dummy chapter and set it as both currentChapter and currentSection.
     *
     * @return A valid Section
     */
    private Section getOrCreateCurrentSection() {
        if (this.currentSection == null) {
            this.currentChapter = new Chapter(0)
            this.currentSection = this.currentChapter
        }
        return this.currentSection
    }

    /**
     * Writes block quote format text.
     *
     * @param blockQuote The block quote model to write.
     */
    private void writeBlockQuote(BlockQuote blockQuote) {
        PDFParagraph pdfParagraph = new PDFParagraph()
        pdfParagraph.setIndentationLeft(20.0f)
        Font bqFont = new Font(this.pdfStyles.getFont(MSS.MSS_Pages.block_quote))
        // Since the MSS stylesheet always return a color, it cannot be used to override options colors.
        // It has to be the other way around.
        if (this.options.blockQuoteColor != null) {
            bqFont.setColor(new PDFColorMSSAdapter(new MSSColor(color:  this.options.blockQuoteColor)))
        }
        PDFColorMSSAdapter background = new PDFColorMSSAdapter(this.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.block_quote).background)
        writeParagraph(pdfParagraph, blockQuote, bqFont, background)
        pdfParagraph.add(Chunk.NEWLINE)
        pdfParagraph.add(Chunk.NEWLINE)
        getOrCreateCurrentSection().add(pdfParagraph)
    }

    /**
     * Writes a code block format text.
     *
     * @param codeBlock The code block text to write.
     */
    private void writeCodeBlock(CodeBlock codeBlock) {
        PDFParagraph pdfParagraph = new PDFParagraph()
        pdfParagraph.setKeepTogether(true)

        Font codeFont = new Font(this.pdfStyles.getFont(MSS.MSS_Pages.code))
        if (this.options.codeColor != null) {
            codeFont.setColor(new PDFColorMSSAdapter(new MSSColor(color:  this.options.codeColor)))
        }

//        paragraph.add(Chunk.NEWLINE)
        for (DocItem item : codeBlock.items) {
            Chunk chunk = new Chunk(item.toString(), codeFont)
            chunk.setLineHeight((float)(codeFont.size + 1.0))
            chunk.setTextRise(-2)
            chunk.setCharacterSpacing(0.5f)
            chunk.setBackground(new PDFColorMSSAdapter(this.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.code).background))
            pdfParagraph.add(chunk)
            pdfParagraph.add(Chunk.NEWLINE)
        }
        pdfParagraph.add(Chunk.NEWLINE)

        getOrCreateCurrentSection().add(pdfParagraph)
    }

    /**
     * Writes a horizontal rule.
     */
    private void writeHorizontalRule() {
        PDFParagraph pdfParagraph = new PDFParagraph()
        pdfParagraph.add(HORIZONTAL_RULE)
        getOrCreateCurrentSection().add(pdfParagraph)
    }

    /**
     * Creates a com.itextpdf.text.List (PDFList) object from a se.natusoft.doc.markdown.model.List model.
     *
     * @param list The List model that determines the config of the PDFModel.
     * @param options The generator options.
     *
     * @return a configured PDFList object.
     */
    private static PDFList listToPDFList(List list, PDFGeneratorOptions options) {
        PDFList pdfList
        if (list.ordered) {
            pdfList = new PDFList(PDFList.ORDERED)
        }
        else {
            pdfList = new PDFList(PDFList.UNORDERED)
            pdfList.setListSymbol(options.unorderedListItemPrefix)
        }
        pdfList.setAutoindent(true)
        return pdfList
    }

    /**
     * Writes a list format text.
     *
     * @param list The list text to write.
     *
     * @throws GenerateException on failure to write this list.
     */
    private void writeList(List list) throws GenerateException {
        PDFList pdfList = listToPDFList(list, this.options)
        writeList(pdfList, list, 0f)
        getOrCreateCurrentSection().add(pdfList)
        getOrCreateCurrentSection().add(Chunk.NEWLINE)
    }

    /**
     * Writes a list format text with indent.
     *
     * @param pdfList The iText list object.
     * @param list The List model with the list text to write.
     * @param indent The indent value.
     *
     * @throws GenerateException on failure to write this list.
     */
    private void writeList(PDFList pdfList, List list, float indent) throws GenerateException {
        pdfList.setIndentationLeft(indent)
        for (DocItem item : list.items) {
            if (item instanceof ListItem) {
                PDFListItem listItem = new PDFListItem()
                boolean first = true
                item.items.each { pg ->
                    if (!first) {
                        // We have to fake a paragraph here since adding a (PDF)Paragraph to a (PDF)ListItem which
                        // is a (PDF)Paragraph screws it up making the list dots or numbers disappear. This unfortunately
                        // makes a little more space between paragraphs than for true paragraphs.
                        listItem.add(LIST_NEWLINE)
                        listItem.add(LIST_NEWLINE)
                    }
                    first = false
                    writeParagraph(listItem, (Paragraph)pg, this.pdfStyles.getFont(MSS.MSS_Pages.list_item),
                        new PDFColorMSSAdapter(this.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.list_item).background))
                }
                pdfList.add(listItem)
            }
            else if (item instanceof List) {
                PDFList subList = listToPDFList(item as List, this.options)
                writeList(subList, item as List, (float)(indent + 10f))
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
     *
     * @throws GenerateException on failure to write paragraph.
     */
    private void writeParagraph(Paragraph paragraph) throws GenerateException {
        PDFParagraph pdfParagraph = new PDFParagraph()

        pdfParagraph.setSpacingAfter(10)
        if (this.options.firstLineParagraphIndent) {
            pdfParagraph.setFirstLineIndent(10.0f)
        }
        writeParagraph(pdfParagraph, paragraph, this.pdfStyles.getFont(MSS.MSS_Pages.standard))

        getOrCreateCurrentSection().add(pdfParagraph)
    }

    /**
     * Writes a paragraph format text. This version actually does the job.
     *
     * @param pdfParagraph The iText Paragraph model.
     * @param paragraph The paragraph text to write.
     * @param font The font to use.
     *
     * @throws GenerateException on failure to write paragraph.
     */
    private void writeParagraph(PDFParagraph pdfParagraph, Paragraph paragraph, Font font) throws GenerateException {
        writeParagraph(pdfParagraph, paragraph, font,
                new PDFColorMSSAdapter(this.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.standard).background))
    }

    /**
     * Writes a paragraph format text. This version actually does the job.
     *
     * @param pdfParagraph The iText Paragraph model.
     * @param paragraph The paragraph text to write.
     * @param font The font to use.
     *
     * @throws GenerateException on failure to write paragraph.
     */
    private void writeParagraph(PDFParagraph pdfParagraph, Paragraph paragraph, Font font, PDFColorMSSAdapter background) throws GenerateException {

        boolean first = true
        for (DocItem docItem : paragraph.items) {
            if (docItem.renderPrefixedSpace && !first) {
                pdfParagraph.add(" ")
            }
            first = false

            switch (docItem.format) {

                case DocFormat.Code:
                    writeCode((Code)docItem, pdfParagraph)
                    break

                case DocFormat.Emphasis:
                    writeEmphasis((Emphasis)docItem, pdfParagraph)
                    break

                case DocFormat.Strong:
                    writeStrong((Strong)docItem, pdfParagraph)
                    break

                case DocFormat.Image:
                    writeImage((Image)docItem, pdfParagraph)
                    break

                case DocFormat.Link:
                    writeLink((Link)docItem, pdfParagraph, background)
                    break

                case DocFormat.AutoLink:
                    writeLink((AutoLink)docItem, pdfParagraph, background)
                    break

                case DocFormat.Space:
                    writePlainText((PlainText)docItem, pdfParagraph, font, background)
                    break;

                case DocFormat.PlainText:
                    writePlainText((PlainText)docItem, pdfParagraph, font, background)
                    break

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" + docItem.getClass().getName() + "]")
            }
        }
    }

    /**
     * Writes a code formatted part within a paragraph.
     *
     * @param code The code text to write.
     * @param pdfParagraph The iText paragraph model to add to.
     * @param background The background color to use.
     */
    private void writeCode(Code code, PDFParagraph pdfParagraph) {
        Chunk chunk = new Chunk(code.text, this.pdfStyles.getFont(MSS.MSS_Pages.code))
        chunk.setLineHeight(8)
        chunk.setCharacterSpacing(1.0f)
        pdfParagraph.add(chunk)
    }

    /**
     * Writes emphasis formatted part withing a paragraph.
     *
     * @param emphasis The emphasised text to write.
     * @param pdfParagraph The iText paragraph model to add to.
     */
    private void writeEmphasis(Emphasis emphasis, PDFParagraph pdfParagraph) {
        Chunk chunk = new Chunk(textReplace(emphasis.text), this.pdfStyles.getFont(MSS.MSS_Pages.emphasis))
        // BUG: There seem to be a bug in iText here. For Italics iText seem to always render a white background
        //      no matter what color is in chunk.background. It works fine for Bold below.
        chunk.background = new PDFColorMSSAdapter(this.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.emphasis).background)
        pdfParagraph.add(chunk)
    }

    /**
     * Writes strong formatted part within a paragraph.
     *
     * @param strong The strong formatted text to write
     * @param pdfParagraph The iText paragraph model to add to.
     */
    private void writeStrong(Strong strong, PDFParagraph pdfParagraph) {
        Chunk chunk = new Chunk(textReplace(strong.text), this.pdfStyles.getFont(MSS.MSS_Pages.strong))
        chunk.background = new PDFColorMSSAdapter(this.pdfStyles.mss.forDocument.getColorPair(MSS.MSS_Pages.strong).background)
        pdfParagraph.add(chunk)
    }

    /**
     * Writes an image within a paragraph.
     *
     * @param image contains url and alt text for the image.
     * @param pdfParagraph The iText paragraph model to add to.
     */
    private void writeImage(Image image, PDFParagraph pdfParagraph) {
        PDFImage pdfImage = PDFImage.getInstance(new URL(this.generatorContext.fileResource.resolveUrl(image.url, image.parseFile, this.options.resultFile)))
        pdfImage.scalePercent(60.0f)
        if (pdfImage != null) {
            pdfParagraph.add(pdfImage)
            // This sometimes helps in keeping text on the correct side of the image, but not always.
            pdfParagraph.add(Chunk.NEWLINE)
        }
        else {
            pdfParagraph.add(new Chunk("[" + image.text + "]", this.pdfStyles.getFont(MSS.MSS_Pages.standard)))
        }
    }

    /**
     * Writes a link within a paragraph.
     *
     * @param link The link url and text information.
     * @param pdfParagraph The iText paragraph model to add to.
     * @param background The background color to use.
     */
    private void writeLink(Link link, PDFParagraph pdfParagraph, PDFColorMSSAdapter background) {
        if (this.options.hideLinks) {
            writePlainText(link, pdfParagraph, this.pdfStyles.getFont(MSS.MSS_Pages.standard), background)
        }
        else {
            Anchor anchor = new Anchor(link.text, this.pdfStyles.getFont(MSS.MSS_Pages.anchor))
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
    private static void writePlainText(PlainText plainText, PDFParagraph pdfParagraph, Font font, PDFColorMSSAdapter background) {
        Chunk chunk = new Chunk(textReplace(plainText.text), font)
        chunk.background = background
        pdfParagraph.add(chunk)
    }

    /**
     * Updates a list of chunks with a background color.
     *
     * @param chunks The chunks to update.
     * @param background The background color to update with.
     */
    private static void setBackgroundColorOnChunks(JList<Chunk> chunks, PDFColorMSSAdapter background) {
        chunks.each { Chunk chunk ->
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

        Closure<Integer> pageOffset
        String resultFile
        boolean updateTOC
        JList<TOC> toc
        PDFStylesMSSAdapter pdfStyles
        PDFHeaderLevelCache headerLevelCache

        @Override
        public void onEndPage(PdfWriter writer, PDFDocument document) {
            if (document.pageNumber > (int)this.pageOffset.call()) {
                PdfContentByte cb = writer.getDirectContent()

                // Write the filename centered as page header
                String fileName = this.resultFile
                int fsIx = fileName.lastIndexOf(File.separator)
                if (fsIx >= 0) {
                    fileName = fileName.substring(fsIx + 1)
                }
                int dotIx = fileName.lastIndexOf('.')
                fileName = fileName.substring(0, dotIx)
                Chunk dfChunk = new Chunk(fileName, this.pdfStyles.getFont(MSS.MSS_Pages.footer))
                Phrase documentFile = new Phrase(dfChunk)
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_CENTER,
                        documentFile,
                        (float)(((document.right() - document.left()) / 2.0f) + document.leftMargin()),
                        (float)(document.top() + 10.0),
                        0.0f
                )

                // Write the page number to the right as a page footer.
                Chunk pageChunk = new Chunk("Page " + (document.getPageNumber() - (int)this.pageOffset.call()),
                        this.pdfStyles.getFont(MSS.MSS_Pages.footer))
                Phrase pageNo = new Phrase(pageChunk)
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
        public void onChapter(PdfWriter writer, PDFDocument document, float paragraphPosition, PDFParagraph title) {
            if (this.updateTOC && title != null) {
                String content = title.getContent()
                this.toc.add(new TOC(sectionTitle: title.getContent().split("\n")[0], pageNumber: document.getPageNumber(),
                        level: this.headerLevelCache.getLevel(content)))
            }
        }

        @Override
        public void onSection(PdfWriter writer, PDFDocument document, float paragraphPosition, int depth, PDFParagraph title) {
            if (this.updateTOC && title != null) {
                this.toc.add(new TOC(sectionTitle: title.getContent().split("\n")[0], pageNumber: document.getPageNumber(),
                        level: this.headerLevelCache.getLevel(title.getContent())))
            }
        }
    }

    /**
     * Stores a table of content entry.
     */
    private static class TOC {
        // Properties
        String sectionTitle
        int pageNumber
        MSS.MSS_TOC level = MSS.MSS_TOC.h1
    }

    /**
     * A rather dummy class to indicate that a new page should be generated rather than adding this section to the document.
     */
    private static class NewPage extends Section {}
}
