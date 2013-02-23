/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.2.4
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

// BE WARNED: This is Groovy! DO NOT LET YOUR IDE AUTOMATICALLY RESOLVE THE IMPORTS!!!!
// IF YOU DO YOU ARE GUARANTEED TO BE SCREWED!!!! This is just one of the many reasons
// I have decided that this is both my first and last Groovy code!
import java.util.List as JList
import java.util.ArrayList as JArrayList
import java.util.LinkedList as JLinkedList

import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.options.PDFGeneratorOptions
import se.natusoft.doc.markdown.generator.pdfgenerator.PDFColor
import se.natusoft.doc.markdown.model.*

// We rename some com.itextpdf.text classes due to name conflict.
import com.itextpdf.text.Paragraph as PDFParagraph
import com.itextpdf.text.List as PDFList
import com.itextpdf.text.ListItem as PDFListItem
import com.itextpdf.text.Image as PDFImage
// Yeah ... should probably continue renaming for consistency ... some day ...
import com.itextpdf.text.Font
import com.itextpdf.text.BaseColor
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.Rectangle
import com.itextpdf.text.Document
import com.itextpdf.text.Section
import com.itextpdf.text.Chapter
import com.itextpdf.text.Chunk
import com.itextpdf.text.Anchor
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.Element
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.Phrase
import se.natusoft.doc.markdown.io.NullOutputStream

/**
 * This generates a PDF documentItems from the provided Doc model.
 * <p/>
 * <b>PLEASE NOTE:</b> Each instance of this class can only be used in one thread at a time!
 * If you try to run this in multiple threads then you need an instance per thread! This is
 * due to that some data required to do the work must be class members and not method local
 * variables. This due to rendering events needing access to them.
 */
class PDFGenerator implements Generator {
    /*
     * A comment about this code and Groovy: Groovy supports "property access" for java bean properties.
     * That is:
     *
     *     documentItems.setPageSize(pageSize)
     *
     * can be written as:
     *
     *     documentItems.pageSize = pageSize
     *
     * which will compile to the same thing. The problem with this however is that there is bad IDE support for
     * Groovy. I'm using IntelliJ Idea (11.1) which has the best support, but it still sucks. It cannot handle
     * the second syntax, complaining about not being able to assign a Rectangle to a boolean. Since I don't
     * want annoying error marks in the IDE for code that still compiles and runs perfectly I'm avoiding that
     * syntax.
     *
     * I can also add, while complaining that it is impossible to do "Step Into" in the debugger when debugging
     * Groovy code. It will simply ignore the step into and to a step over instead. Thereby you have to set
     * a lot of breakpoints.
     *
     * The biggest thing I have against Groovy is that I can code a call to a non existent method and neither
     * the IDE nor the compiler will complain about that. It will happily compile and then fail runtime!!!!
     * If I thought that was OK I might as well become a JavaScript developer! I could never recommend
     * Groovy as a language to a customer. It puts higher demands on the developer. The usual setup in my
     * experience is a few experienced developers and then fill up with cheap developers. I don't say
     * that that setup is necessarily wrong, but with high demand languages like Groovy it will be bad
     * and costly IMHO.
     *
     * I just discovered that:
     *
     *     Section section = new Section(title, level)
     *
     * which I'm doing in several places really shouldn't work since the Section constructor is protected!
     * This however not only complies (have already determined that Groovy will more or less compile anything)
     * but it also works! Groovy happily lets me construct a protected class like this! This of course had
     * side effects letting me use it in way that should not be possible and it turned out at runtime it
     * wasn't.
     *
     * Yes, I'm apparently blogging in code now ...
     */

    //
    // Constants
    //

    private static final FONT = new Font(Font.FontFamily.HELVETICA, 10)
    private static final FONT_BLOCKQUOTE = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY)
    private static final FONT_H1 = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD)
    private static final FONT_H2 = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD)
    private static final FONT_H3 = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)
    private static final FONT_H4 = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)
    private static final FONT_H5 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)
    private static final FONT_H6 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)
    private static final FONT_EMPHASIS = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)
    private static final FONT_STRONG = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)
    private static final FONT_CODE = new Font(Font.FontFamily.COURIER, 9, Font.NORMAL, BaseColor.DARK_GRAY)
    private static final FONT_ANCHOR = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY)
    private static final FONT_LIST_ITEM = new Font(Font.FontFamily.HELVETICA, 10)
    private static final FONT_FOOTER = new Font(Font.FontFamily.HELVETICA, 8)
    private static final FONT_TOC = new Font(Font.FontFamily.HELVETICA, 9)

    private static final Chunk LIST_NEWLINE = new Chunk("\n", new Font(Font.FontFamily.HELVETICA, 4))

    private static final LineSeparator H2_UNDERLINE = new LineSeparator(0.01f, 100f, BaseColor.GRAY, 0, 12)
    private static final LineSeparator HORIZONTAL_RULE = new LineSeparator(0.01f, 100f, BaseColor.GRAY, 5, 16)

    //
    // Private Methods
    //

    // Note: Since we need to put some variables here due to event handler access there is
    // no point in passing other stuff around in the previous this inner class so I
    // put it here instead.

    private PDFGeneratorOptions options = null

    /** This will actually be added to the real Document later on, twice: once for the fake render and once for the real. */
    private JList<Section> documentItems = null

    private Chapter currentChapter = null

    private Section currentSection = null

    private Section currentH2 = null

    private Section currentH3 = null

    private Section currentH4 = null

    private Section currentH5 = null

    private int chapterNumber

    private Font currentParagraphFont = null

    private JList<TOC> toc

    /** This is to exclude the title and tables of content pages from the page numbering. */
    private int pageOffset

    /** This is only true for the fake render that is only done to generate the TOC. */
    private boolean updateTOC = true

    /** The root dir to prefix paths with. */
    private File rootDir = null

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
        this.currentParagraphFont = FONT
        this.toc = new JLinkedList<TOC>()
        this.pageOffset = 0
    }

    /**
     * The main API for the generator. This does the job!
     *
     * @param doc The documentItems model to generate from.
     * @param opts The generator options.
     * @param rootDir An optional root directory to prefix output paths with.
     */
    @Override
    public void generate(Doc doc, Options opts, File rootDir) throws IOException, GenerateException {
        initRun()
        this.options = (PDFGeneratorOptions)opts
        this.rootDir = rootDir

        for (DocItem docItem : doc.items) {

            switch (docItem.format) {
                case DocFormat.Comment:
                    // We skip comments, but act on "@PB" within the comment for doing a page break.
                    Comment comment = (Comment)docItem;
                    if (comment.text.indexOf("@PB") >= 0) {
                        this.documentItems.add(new NewPage())
                    }
                    break

                case DocFormat.Paragraph:
                    writeParagraph((Paragraph)docItem)
                    break

                case DocFormat.Header:
                    writeHeader((Header)docItem)
                    break

                case DocFormat.BlockQuote:
                    writeBlockQuote((BlockQuote)docItem)
                    break;

                case DocFormat.CodeBlock:
                    writeCodeBlock((CodeBlock)docItem)
                    break

                case DocFormat.HorizontalRule:
                    writeHorizontalRule((HorizontalRule)docItem)
                    break

                case DocFormat.List:
                    writeList((List)docItem)
                    break

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
            pageSize.backgroundColor = new PDFColor(this.options.backgroundColor)
        }

        // Please note that itext is not really compatible with groovys property access!

        Document document = null
        PdfWriter pdfWriter = null

        if (this.options.generateTOC) {
            // Do a fake render to generate TOC.
            this.updateTOC = true
            document = new Document()
            document.setPageSize(pageSize)

            pdfWriter = PdfWriter.getInstance(document, new NullOutputStream())
            pdfWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_7)
            pdfWriter.setFullCompression()
            pdfWriter.setPageEvent(new PageEventHandler())
            document.open()

            // Since this.documentItems is just an ArrayList of Sections we have to add them to the real documentItems now.
            for (Section section : this.documentItems) {
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
        this.updateTOC = false // Don't generate a new TOC on the second pass.
        document = new Document()
        document.setPageSize(pageSize)

        File resultFile = this.rootDir != null ? new File(this.rootDir, this.options.resultFile) : new File(this.options.resultFile)
        pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(resultFile))
        pdfWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_7)
        pdfWriter.setFullCompression()
        pdfWriter.setPageEvent(new PageEventHandler())

        if (this.options.title != null)      { document.addTitle(this.options.title) }
        if (this.options.subject != null)    { document.addSubject(this.options.subject) }
        if (this.options.keywords != null)   { document.addKeywords(this.options.keywords) }
        if (this.options.author != null)     { document.addAuthor(this.options.author) }

        document.addCreationDate()
        document.addCreator("MarkdownDoc (https://github.com/tombensve/MarkdownDoc)")
        document.open()

        if (this.options.generateTitlePage) {
            writeTitlePage(pdfWriter, document)
        }

        if (this.options.generateTOC) {
            writeTOC(pdfWriter, document)
        }

        // Since this.documentItems is just an ArrayList of Sections we have to add them to the real documentItems now.
        for (Section section : this.documentItems) {
            if (section instanceof NewPage) {
                document.newPage() // This completely refuses to do anything!!!
            }
            else {
                document.add(section)
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
    private String textReplace(String text) {
        String replaced = text

        replaced = replaced.replace("&lt;", "<")
        replaced = replaced.replace("&gt;", ">")
        replaced = replaced.replace("(C)", "©")

        return replaced
    }

    /**
     * Writes a table of content.
     *
     * @param pdfWriter The PdfWriter to write table of content on.
     * @param document The PDF documentItems being written.
     */
    private void writeTOC(PdfWriter pdfWriter, Document document) {
        PdfContentByte cb = pdfWriter.getDirectContent()

        ++this.pageOffset

        // If you are tearing your hair over understanding this, please note that 0,0 is
        // at the bottom left of the page! I guess they just had to be different!

        float y = document.top() - document.topMargin()

        writeText(cb, Element.ALIGN_CENTER, "Table of Contents", (float)(((document.right() - document.left()) / 2) + document.leftMargin()), y)
        y = y - 28
        for (TOC tocEntry : this.toc) {
            writeText(cb, Element.ALIGN_LEFT, tocEntry.sectionTitle, (float)(document.left() + document.leftMargin()), y)
            writeText(cb, Element.ALIGN_RIGHT, "" + tocEntry.pageNumber, (float)(document.right() - document.rightMargin()), y)
            y = y - 14
            if ( y < document.bottom()) {
                document.newPage()
                y = document.top()- document.topMargin()
                ++this.pageOffset
            }
        }
    }

    /**
     * Writes a title page.
     *
     * @param pdfWriter The PdfWriter to write the title page on.
     * @param document The PDF documentItems being written.
     */
    private void writeTitlePage(PdfWriter pdfWriter, Document document) {
        PdfContentByte cb = pdfWriter.getDirectContent()

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
        float x = (document.right() - document.left()) / 2 + document.leftMargin()

        // Rendered from top of page

        if (title != null) {
            Font font = new Font(Font.FontFamily.HELVETICA, 25)
            Chunk chunk = new Chunk(textReplace(title), font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, phrase, x, yTop, 0.0f)
            yTop = yTop - (yItemSizeTop / 2)
        }

        if (subject != null) {
            Font font = new Font(Font.FontFamily.HELVETICA, 15)
            Chunk chunk = new Chunk(textReplace(subject), font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, phrase, x, yTop, 0.0f)
            yTop = yTop - (yItemSizeTop / 2)
        }

        if (version != null) {
            Font font = new Font(Font.FontFamily.HELVETICA, 12)
            Chunk chunk = new Chunk("Version: " + version, font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, phrase, x, yTop, 0.0f)
        }

        // Rendered from bottom of page

        if (copyRight != null) {
            Font font = new Font(Font.FontFamily.HELVETICA, 12)
            Chunk chunk = new Chunk(textReplace(copyRight), font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, phrase, x, yBottom, 0.0f)
            yBottom = yBottom + (yItemSizeBottom / 2)
        }

        if (author != null) {
            Font font = new Font(Font.FontFamily.HELVETICA, 12)
            Chunk chunk = new Chunk("Author: " + author, font)
            Phrase phrase = new Phrase(chunk)
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, phrase, x, yBottom, 0.0f)
        }


        document.newPage()
    }

    /**
     * Utility method that creates a Phrase from a String and then calls ColumntText.showTextAligned(...).
     *
     * @param cb The convar to render on.
     * @param align The alignment to use.
     * @param text The text to render.
     * @param x The X position of the text (actually dependes on alignment).
     * @param y The Y position of the text.
     */
    private void writeText(PdfContentByte cb, int align, String text, float x, float y) {
        Phrase phrase = new Phrase()
        Chunk chunk = new Chunk(text, FONT_TOC)
        phrase.add(chunk)
        ColumnText.showTextAligned(cb, align, phrase, x, y, 0.0f)
    }

    /**
     * Writes a header text (H1 - H6).
     *
     * @param header The header model to write.
     */
    private void writeHeader(Header header) {

        switch (header.level) {
            case { it == Header.Level.H1 } :
                // It feels like iText doesn't like it when you add a parent to its parent before
                // it has all its children ...
                if (this.currentChapter != null) {
                    this.documentItems.add(this.currentChapter)
                }
                PDFParagraph title = new PDFParagraph()
                title.add(createHeaderChunk(header.text, FONT_H1))
                Chapter chapter = new Chapter(title, this.chapterNumber ++)
                chapter.setNumberStyle(Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT)
                chapter.add(Chunk.NEWLINE)
                this.currentChapter = chapter
                this.currentSection = chapter
                this.currentH2 = chapter
                this.currentH3 = chapter
                this.currentH4 = chapter
                this.currentH5 = chapter
                break

            case { it == Header.Level.H2 } :
                PDFParagraph title = new PDFParagraph()
                title.add(createHeaderChunk(header.text, FONT_H2))
                title.add(Chunk.NEWLINE);
                title.add(H2_UNDERLINE)
                title.add(Chunk.NEWLINE)
                Section section = null
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
                section.setNumberStyle(Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT)
                this.currentSection = section
                this.currentH2 = section
                this.currentH3 = section
                this.currentH4 = section
                this.currentH5 = section
                break

            case { it == Header.Level.H3 } :
                PDFParagraph title = new PDFParagraph()
                title.add(createHeaderChunk(header.text, FONT_H3))
                Section section = null
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
                section.setNumberStyle(Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT)
                this.currentSection = section
                this.currentH3 = section
                this.currentH4 = section
                this.currentH5 = section
                break

            case { it == Header.Level.H4 } :
                PDFParagraph title = new PDFParagraph()
                title.add(createHeaderChunk(header.text, FONT_H4))
                Section section = null
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
                section.setNumberStyle(Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT)
                this.currentSection = section
                this.currentH4 = section
                this.currentH5 = section
                break

            case { it == Header.Level.H5 } :
                PDFParagraph title = new PDFParagraph()
                title.add(createHeaderChunk(header.text, FONT_H5))
                Section section = null
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
                section.setNumberStyle(Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT)
                this.currentSection = section
                this.currentH5 = section
                break

            case { it == Header.Level.H6 } :
                PDFParagraph title = new PDFParagraph()
                title.add(createHeaderChunk(header.text, FONT_H6))
                Section section = null
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
                section.setNumberStyle(Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT)
                this.currentSection = section
                break
        }
    }

    /**
     * Utility to create a Chunk for the header.
     *
     * @param text The header text.
     * @param font The font to use for the header.
     *
     * @return The created Chunk.
     */
    private Chunk createHeaderChunk(String text, Font font) {
        Chunk chunk = new Chunk(textReplace(text), font)
        chunk.setTextRise(2f)
        chunk.setLineHeight((float)(font.size + 2.0f))
        return chunk
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
        Font bqFont = new Font(FONT_BLOCKQUOTE)
        if (this.options.blockQuoteColor != null) {
            bqFont.setColor(new PDFColor(this.options.blockQuoteColor))
        }
        writeParagraph(pdfParagraph, blockQuote, bqFont)
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

        Font codeFont = new Font(FONT_CODE)
        if (this.options.codeColor != null) {
            codeFont.setColor(new PDFColor(this.options.codeColor))
        }

//        paragraph.add(Chunk.NEWLINE)
        for (DocItem item : codeBlock.items) {
            Chunk chunk = new Chunk(item.toString(), codeFont)
            chunk.setLineHeight((float)(codeFont.size + 1.0))
            chunk.setTextRise(-2)
            chunk.setCharacterSpacing(0.5f)
            pdfParagraph.add(chunk)
            pdfParagraph.add(Chunk.NEWLINE)
        }
        pdfParagraph.add(Chunk.NEWLINE)

        getOrCreateCurrentSection().add(pdfParagraph)
    }

    /**
     * Writes a horizontal rule.
     *
     * @param horizontalRule The model representing the horizontal rule. This is just a marker it contains no text.
     */
    private void writeHorizontalRule(HorizontalRule horizontalRule) {
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
    private PDFList listToPDFList(List list, PDFGeneratorOptions options) {
        PDFList pdfList = null
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
                    writeParagraph(listItem, (Paragraph)pg, FONT_LIST_ITEM)
                }
                pdfList.add(listItem)
            }
            else if (item instanceof List) {
                PDFList subList = listToPDFList((List)item, this.options)
                writeList(subList, (List)item, (float)(indent + 10f))
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
        writeParagraph(pdfParagraph, paragraph, FONT)

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
                    writeLink((Link)docItem, pdfParagraph)
                    break

                case DocFormat.AutoLink:
                    writeLink((AutoLink)docItem, pdfParagraph)
                    break

                case DocFormat.Space:
                    writePlainText((PlainText)docItem, pdfParagraph, font)
                    break;

                case DocFormat.PlainText:
                    writePlainText((PlainText)docItem, pdfParagraph, font)
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
     */
    private void writeCode(Code code, PDFParagraph pdfParagraph) {
        Chunk chunk = new Chunk(code.text, FONT_CODE)
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
        pdfParagraph.add(new Chunk(textReplace(emphasis.text), FONT_EMPHASIS))
    }

    /**
     * Writes strong formatted part within a paragraph.
     *
     * @param strong The strong formatted text to write
     * @param pdfParagraph The iText paragraph model to add to.
     */
    private void writeStrong(Strong strong, PDFParagraph pdfParagraph) {
        pdfParagraph.add(new Chunk(textReplace(strong.text), FONT_STRONG))
    }

    /**
     * - Adds file: if no protocol is specified.
     * - If file: then resolved to full path if not found with relative path.
     *
     * @param url The DocItem item provided url.
     * @param parseFile The source file of the DocItem item.
     */
    private String resolveUrl(String url, File parseFile) {
        String resolvedUrl = url
        if (!resolvedUrl.startsWith("file:") && !resolvedUrl.startsWith("http:")) {
            resolvedUrl = "file:" + resolvedUrl
        }
        if (resolvedUrl.startsWith("file:")) {
            String path = resolvedUrl.substring(5)
            File testFile = new File(path)

            if (!testFile.exists()) {
                // Try relative to parseFile first.
                int ix = parseFile.canonicalPath.lastIndexOf(File.separator)
                if (ix >= 0) {
                    String path1 = parseFile.canonicalPath.substring(0, ix + 1) + path
                    if (this.rootDir != null) {
                        // The result file is relative to the root dir!
                        resolvedUrl = "file:" + this.rootDir.canonicalPath + File.separator + path1
                        testFile = new File(this.rootDir.canonicalPath + File.separator + path1)
                    }
                    else {
                        resolvedUrl = "file:" + path1
                        testFile = new File(path1)
                    }
                }
                if (!testFile.exists()) {
                    // Try relative to result file.
                    ix = this.options.resultFile.lastIndexOf(File.separator)
                    if (ix >= 0) {
                        String path2 = this.options.resultFile.substring(0, ix + 1) + path
                        if (this.rootDir != null) {
                            // The result file is relative to the root dir!
                            resolvedUrl = "file:" + this.rootDir.canonicalPath + File.separator + path2
                        }
                        else {
                            resolvedUrl = "file:" + path2
                        }
                    }
                }
            }
        }
        return resolvedUrl
    }

    /**
     * Writes an image within a paragraph.
     *
     * @param image contains url and alt text for the image.
     * @param pdfParagraph The iText paragraph model to add to.
     */
    private void writeImage(Image image, PDFParagraph pdfParagraph) {
        PDFImage pdfImage = PDFImage.getInstance(new URL(resolveUrl(image.url, image.parseFile)))
        pdfImage.scalePercent(60.0f)
        if (pdfImage != null) {
            pdfParagraph.add(pdfImage)
            // This sometimes helps in keeping text on the correct side of the image, but not always.
            pdfParagraph.add(Chunk.NEWLINE)
        }
        else {
            pdfParagraph.add(new Chunk("[" + image.text + "]", FONT))
        }
    }

    /**
     * Writes a link within a paragraph.
     *
     * @param link The link url and text information.
     * @param pdfParagraph The iText paragraph model to add to.
     */
    private void writeLink(Link link, PDFParagraph pdfParagraph) {
        if (this.options.hideLinks) {
            writePlainText(link, pdfParagraph, FONT)
        }
        else {
            Anchor anchor = new Anchor(link.text, FONT_ANCHOR)
            anchor.setReference(link.url)
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
    private void writePlainText(PlainText plainText, PDFParagraph pdfParagraph, Font font) {
        pdfParagraph.add(new Chunk(textReplace(plainText.text), font))
    }

    //
    // Inner Classes
    //

    /**
     * Handles page rendering events to write header and footer and generate a table of contents.
     */
    private class PageEventHandler extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            if (document.pageNumber > PDFGenerator.this.pageOffset) {
                PdfContentByte cb = writer.getDirectContent()

                // Write the filename centered as page header
                String fileName = PDFGenerator.this.options.resultFile
                int fsIx = fileName.lastIndexOf(File.separator)
                if (fsIx >= 0) {
                    fileName = fileName.substring(fsIx + 1)
                }
                int dotIx = fileName.lastIndexOf('.')
                fileName = fileName.substring(0, dotIx)
                Chunk dfChunk = new Chunk(fileName, FONT_FOOTER)
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
                Chunk pageChunk = new Chunk("Page " + (document.getPageNumber() - PDFGenerator.this.pageOffset), FONT_FOOTER)
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
        public void onChapter(PdfWriter writer, Document document, float paragraphPosition, PDFParagraph title) {
            if (PDFGenerator.this.updateTOC && title != null) {
                PDFGenerator.this.toc.add(new TOC(sectionTitle: title.getContent().split("\n")[0], pageNumber: document.getPageNumber()))
            }
        }

        @Override
        public void onSection(PdfWriter writer, Document document, float paragraphPosition, int depth, PDFParagraph title) {
            if (PDFGenerator.this.updateTOC && title != null) {
                PDFGenerator.this.toc.add(new TOC(sectionTitle: title.getContent().split("\n")[0], pageNumber: document.getPageNumber()))
            }
        }
    }

    /**
     * Stores a table of content entry.
     */
    private static class TOC {

        String sectionTitle

        int pageNumber

    }

    /**
     * A rather dummy class to indicate that a new page should be generated rather than adding this section to the document.
     */
    private static class NewPage extends Section {}
}
