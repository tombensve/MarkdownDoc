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
package se.natusoft.doc.markdown.generator

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.models.TOC
import se.natusoft.doc.markdown.generator.options.PDFGeneratorOptions
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxDocRenderer
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxFontMSSAdapter
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxStylesMSSAdapter
import se.natusoft.doc.markdown.generator.pdfbox.internal.PageMargins
import se.natusoft.doc.markdown.generator.pdfbox.internal.StructuredNumber
import se.natusoft.doc.markdown.generator.styles.MSS
import se.natusoft.doc.markdown.generator.styles.MSS.MSS_Pages
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
import se.natusoft.doc.markdown.model.Paragraph
import se.natusoft.doc.markdown.model.PlainText
import se.natusoft.doc.markdown.model.Strong
import se.natusoft.doc.markdown.model.List
import se.natusoft.doc.markdown.model.ListItem

/**
 * Generates a PDF document using PDFBox to generate PDF.
 */
@CompileStatic
@TypeChecked
class PDFBoxGenerator implements Generator, BoxedTrait {
    //
    // Inner Classes
    //

    /**
     * A context for the generation process.
     */
    static class PDFGeneratorContext extends GeneratorContext {
        //
        // Properties
        //

        /** The options for the PDF generator. */
        PDFGeneratorOptions options

        /** Adapter between MSS and iText fonts. */
        PDFBoxStylesMSSAdapter pdfStyles = new PDFBoxStylesMSSAdapter()

        /** The table of contents. */
        java.util.List<TOC> toc

    }


    //
    // Private Members
    //


    //
    // Methods
    //

    /**
     * Returns the class containing OptionsManager annotated options for the generator.
     */
    @Override
    @NotNull
    Class getOptionsClass() {
        PDFGeneratorOptions.class
    }

    /**
     * @return The name of the generator. This is the name to use to specify the specific generator.
     */
    @Override
    @NotNull
    String getName() {
        "pdf"
    }

    /**
     * Generates output from DocItem model.
     *
     * @param document The model to generate from.
     * @param options The generator options.
     * @param rootDir The optional root directory to prefix configured output with. Can be null.
     *
     * @throws IOException on I/O failures.
     * @throws GenerateException on other failures to generate target.
     */
    @Override
    void generate(@NotNull Doc document, @NotNull Options options, @Nullable File rootDir) throws IOException, GenerateException {
        final File resultFile = rootDir != null ? new File(rootDir, options.resultFile) : new File(options.resultFile)
        final FileOutputStream resultStream = new FileOutputStream(resultFile)
        try {
            generate(document, options, rootDir, resultStream)
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
     * @param rootDir The optional root directory to prefix configured output with. Can be null.
     * @param resultStream The stream to write the result to.
     *
     * @throws IOException on I/O failures.
     * @throws GenerateException on other failures to generate target.
     */
    @Override
    void generate(
            @NotNull Doc document,
            @NotNull Options options,
            @Nullable File rootDir,
            @NotNull OutputStream resultStream
    ) throws IOException, GenerateException {

        final PDFGeneratorContext context = new PDFGeneratorContext(
                options: options as PDFGeneratorOptions,
                rootDir:  rootDir,
                fileResource: new FileResource(rootDir: rootDir, optsRootDir: (options as PDFGeneratorOptions).rootDir)
        )

        context.pdfStyles.generatorContext = context

        // Load MSS file if specified
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

        PDFBoxDocRenderer doc = new PDFBoxDocRenderer(
                margins: new PageMargins(
                        topMargin:    context.pdfStyles.mss.forDocument.topMargin,
                        bottomMargin: context.pdfStyles.mss.forDocument.bottomMargin,
                        leftMargin:   context.pdfStyles.mss.forDocument.leftMargin,
                        rightMargin:  context.pdfStyles.mss.forDocument.rightMargin,
                ),
                pageSize: context.options.pageSize
        )

        final LinkedList<String> divs = new LinkedList<>()

        context.pdfStyles.mss.currentDivs = divs

        document.items.each { final DocItem docItem ->

            switch (docItem.format) {
                case DocFormat.Comment:
                    // We skip comments in general, but act on "@PB" within the comment for doing a page break.
                    final Comment comment = (Comment)docItem;
                    if (comment.text.indexOf("@PB") >= 0) {
                        doc.newPage()
                    }
                    // and also act on @PDFTitle, @PDFSubject, @PDFKeywords, @PDFAuthor, @PDFVersion, and @PDFCopyright
                    // for overriding those settings in the options. This allows the document rather than the generate
                    // config to provide this information.
                    extractCommentOptionsAnnotations(comment, context)
                    break

                case DocFormat.Paragraph:
                    writeParagraph(docItem as Paragraph, doc, context)
                    break

                case DocFormat.Header:
                    writeHeader(docItem as Header, doc, context)
                    break

                case DocFormat.BlockQuote:
                    writeBlockQuote(docItem as BlockQuote, doc, context)
                    break;

                case DocFormat.CodeBlock:
                    writeCodeBlock(docItem as CodeBlock, doc, context)
                    break

                case DocFormat.HorizontalRule:
                    doc.hr()
                    break

                case DocFormat.List:
                    writeList(docItem as List, doc, context)
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
     * Handles writing of paragraphs taking care of in paragraph formatting.
     *
     * @param paragraph The paragraph to write.
     * @param doc The PDF document renderer.
     * @param context The generator context.
     */
    static void writeParagraph(@NotNull Paragraph paragraph, @NotNull PDFBoxDocRenderer doc, @NotNull PDFGeneratorContext context) {
        doc.newSection()
        writeParagraphContent(paragraph, doc, context)
    }

    /**
     * Handles writing of paragraphs taking care of in paragraph formatting.
     *
     * @param paragraph The paragraph to write.
     * @param doc The PDF document renderer.
     * @param context The generator context.
     */
    static void writeParagraphContent(@NotNull Paragraph paragraph, @NotNull PDFBoxDocRenderer doc, @NotNull PDFGeneratorContext context) {

        ParagraphWriter pw = new ParagraphWriter(doc: doc, context: context)
        pw.doc = doc
        pw.context = context

        boolean first = true
        paragraph.items.each { final DocItem docItem ->
            if (docItem.renderPrefixedSpace && !first) {
                doc.text("  ")
            }
            first = false

            switch (docItem.format) {

                case DocFormat.Code:
                    pw.writeCode(docItem as Code)
                    break

                case DocFormat.Emphasis:
                    pw.writeEmphasis(docItem as Emphasis)
                    break

                case DocFormat.Strong:
                    pw.writeStrong(docItem as Strong)
                    break

                case DocFormat.Image:
                    pw.writeImage(docItem as Image)
                    break

                case DocFormat.Link:
                    pw.writeLink(docItem as Link)
                    break

                case DocFormat.AutoLink:
                    pw.writeAutoLink(docItem as AutoLink)
                    break

                case DocFormat.Space:
                    pw.writePlainText(docItem as PlainText)
                    break;

                case DocFormat.PlainText:
                    pw.writePlainText(docItem as PlainText)
                    break

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" +
                            docItem.getClass().getName() + "]")
            }
        }

    }

    /**
     * Writes a header.
     *
     * @param header The header text to write and the header level.
     * @param doc The PDF document renderer
     * @param context The generator context.
     */
    static void writeHeader(@NotNull Header header, @NotNull PDFBoxDocRenderer doc, @NotNull PDFGeneratorContext context) {
        MSS_Pages section = MSS_Pages.valueOf("h" + header.level.level)

        doc.applyFont(new PDFBoxFontMSSAdapter(context.pdfStyles.mss.forDocument.getFont(section)))
        doc.applyColorPair(context.pdfStyles.mss.forDocument.getColorPair(section))

        doc.newSection()
        doc.text(header.text)
        doc.newLine()
    }

    /**
     * Writes a block quote.
     *
     * @param blockQuote The bock quote text to write.
     * @param doc The PDF document renderer.
     * @param context The generator context.
     */
    static void writeBlockQuote(@NotNull BlockQuote blockQuote, @NotNull PDFBoxDocRenderer doc, @NotNull PDFGeneratorContext context) {
        checkBoxed(MSS_Pages.block_quote, doc, context.pdfStyles.mss)

        doc.applyFont(new PDFBoxFontMSSAdapter(context.pdfStyles.mss.forDocument.getFont(MSS_Pages.block_quote)))
        doc.applyColorPair(context.pdfStyles.mss.forDocument.getColorPair(MSS_Pages.block_quote))

        doc.newSection()
        blockQuote.items.each { final DocItem docItem ->
            // There should only be plain texts here, but to be sure …
            if (PlainText.class.isAssignableFrom(docItem.class)) {
                doc.text((docItem as PlainText).text)
            }
        }

        clearBoxed(doc)
    }

    /**
     * Writes a code block.
     *
     * @param codeBlock The code block text to write.
     * @param doc The PDF document renderer.
     * @param context The generator context.
     */
    static void writeCodeBlock(@NotNull CodeBlock codeBlock, @NotNull PDFBoxDocRenderer doc, @NotNull PDFGeneratorContext context) {
        checkBoxed(MSS_Pages.code, doc, context.pdfStyles.mss)

        doc.applyFont(new PDFBoxFontMSSAdapter(context.pdfStyles.mss.forDocument.getFont(MSS_Pages.code)))
        doc.applyColorPair(context.pdfStyles.mss.forDocument.getColorPair(MSS_Pages.code))

        doc.newSection()
        codeBlock.items.each { final DocItem docItem ->
            // There should only be plain texts here, but to be sure …
            if (PlainText.class.isAssignableFrom(docItem.class)) {
                doc.text((docItem as PlainText).text)
            }
        }

        clearBoxed(doc)
    }

    /**
     * Writes a list.
     *
     * @param list The list to write.
     * @param doc The PDF document renderer
     * @param context The generator context.
     */
    static void writeList(@NotNull List list, @NotNull PDFBoxDocRenderer doc, @NotNull PDFGeneratorContext context) {
        StructuredNumber num
        if (list.isOrdered()) {
            num = new StructuredNumber(30)
        }
        writeListInt(list, "", num, doc, context)
    }

    Note that we need to support paragraph insets both left and right, to be able to handle list entries correctly when they
    are longer than one line!

    /**
     * The internal version of writing a list. This is called recursively for each sublist.
     *
     * @param list The list to write.
     * @param indent The indentation level.
     * @param num The current number if ordered, null otherwise.
     * @param doc The PDF document renderer
     * @param context The generator context.
     */
    private static void writeListInt(
            @NotNull List list,
            @NotNull String indent,
            @Nullable StructuredNumber num,
            @NotNull PDFBoxDocRenderer doc,
            @NotNull PDFGeneratorContext context
    ) {
        list.items.each { final DocItem item ->
            if (item instanceof ListItem) {
                doc.newLine()
                doc.text(indent)
                if (num != null) {
                    doc.text("${num} ")
                }
                else {
                    doc.text("• ")
                }

                item.items.each { final DocItem pg ->
                    if (Paragraph.class.isAssignableFrom(pg.class)) {
                        writeParagraphContent(pg as Paragraph, doc, context)
                    }
                }
            }
            else if (item instanceof List) {
                StructuredNumber newNum = null
                if (num != null) {
                    newNum = new StructuredNumber(num)
                    newNum.downLevel()
                }
                writeListInt(item as List, indent + "  ", newNum, doc, context)
            }
            else {
                throw new GenerateException(message: "Non ListItem found in List: Bad model structure!")
            }
        }

    }
}

/**
 * Support for boxing sections.
 */
@CompileStatic
@TypeChecked
trait BoxedTrait {

    //
    // Private Members
    //

    /** To be thread safe we hold this value in a ThreadLocal. */
    private static final ThreadLocal<Boolean> boxed = new ThreadLocal<>()

    //
    // Methods
    //

    /**
     * Checks if a section should be boxed or not according to the MSS. If true
     * then it starts the box tracking so the clearBoxed(…) method can render the box.
     *
     * @param section The section to check for if it is boxed.
     * @param doc The PDF document renderer.
     * @param mss Supplies style information.
     */
    static void checkBoxed(@NotNull MSS_Pages section, @NotNull PDFBoxDocRenderer doc, @NotNull MSS mss) {
        boxed.set(mss.forDocument.isBoxed(section))
        if (boxed.get()) {
            doc.startBox(mss.forDocument.getBoxColor(section))
        }

    }

    /**
     * This terminates and renders the box if boxed is true.
     *
     * @param doc The PDF document renderer.
     */
    static void clearBoxed(@NotNull PDFBoxDocRenderer doc) {
        if (boxed.get()) {
            doc.endBox()
        }

        boxed.set(false)
    }
}

/**
 * Handles writing paragraph formats.
 */
@CompileStatic
@TypeChecked
class ParagraphWriter implements BoxedTrait {
    //
    // Properties
    //

    /** The PDF document renderer. */
    @NotNull PDFBoxDocRenderer doc

    /** The generator context. */
    @NotNull PDFBoxGenerator.PDFGeneratorContext context

    //
    // Methods
    //

    /**
     * Utility method to make path shorter for reference in other methods.
s     */
    private @NotNull MSS.ForDocument getForDocument() {
        return this.context.pdfStyles.mss.forDocument
    }

    /**
     * Writes pre formatted code.
     *
     * @param code The code to write.
     */
    void writeCode(@NotNull Code code) {
        checkBoxed(MSS_Pages.code, this.doc, this.context.pdfStyles.mss)

        doc.applyFont(new PDFBoxFontMSSAdapter(this.forDocument.getFont(MSS_Pages.code)))
        doc.applyColorPair(this.forDocument.getColorPair(MSS_Pages.code))
        doc.text(code.text)

        clearBoxed(this.doc)
    }

    /**
     * Writes emphasised text.
     *
     * @param emphasis The text to write.
     */
    void writeEmphasis(@NotNull Emphasis emphasis) {
        checkBoxed(MSS_Pages.emphasis, this.doc, this.context.pdfStyles.mss)

        doc.applyFont(new PDFBoxFontMSSAdapter(this.forDocument.getFont(MSS_Pages.emphasis)))
        doc.applyColorPair(this.forDocument.getColorPair(MSS_Pages.emphasis))
        doc.text(emphasis.text)

        clearBoxed(this.doc)
    }

    /**
     * Writes strong/bold text.
     *
     * @param strong The text to write.
     */
    void writeStrong(@NotNull Strong strong) {
        checkBoxed(MSS_Pages.strong, this.doc, this.context.pdfStyles.mss)

        doc.applyFont(new PDFBoxFontMSSAdapter(this.forDocument.getFont(MSS_Pages.strong)))
        doc.applyColorPair(this.forDocument.getColorPair(MSS_Pages.strong))
        doc.text(strong.text)

        clearBoxed(this.doc)
    }

    /**
     * Writes plain text.
     *
     * @param text The text to write.
     */
    void writePlainText(@NotNull PlainText text) {
        checkBoxed(MSS_Pages.standard, this.doc, this.context.pdfStyles.mss)

        doc.applyFont(new PDFBoxFontMSSAdapter(this.forDocument.getFont(MSS_Pages.standard)))
        doc.applyColorPair(this.forDocument.getColorPair(MSS_Pages.standard))
        doc.text(text.text)

        clearBoxed(this.doc)
    }

    /**
     * Writes an image.
     *
     * @param image The image to write.
     */
    void writeImage(@NotNull Image image) {
        checkBoxed(MSS_Pages.image, this.doc, this.context.pdfStyles.mss)

        doc.image(image.url, this.forDocument.imageStyle)

        clearBoxed(this.doc)
    }

    /**
     * Writes a link.
     *
     * @param link The link to write.
     */
    void writeLink(@NotNull Link link) {
        checkBoxed(MSS_Pages.anchor, this.doc, this.context.pdfStyles.mss)

        doc.link(link.title, link.url)

        clearBoxed(this.doc)
    }

    /**
     * Writes an auto link with text same as url.
     *
     * @param autoLink The link to write.
     */
    void writeAutoLink(@NotNull AutoLink autoLink) {
        checkBoxed(MSS_Pages.anchor, this.doc, this.context.pdfStyles.mss)

        doc.link(autoLink.url, autoLink.url)

        clearBoxed(this.doc)
    }

}
