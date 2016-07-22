package se.natusoft.doc.markdown.generator

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.options.PDFGeneratorOptions
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxDocRenderer
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxStylesMSSAdapter
import se.natusoft.doc.markdown.generator.styles.MSS
import se.natusoft.doc.markdown.model.Comment
import se.natusoft.doc.markdown.model.Div
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.model.DocFormat
import se.natusoft.doc.markdown.model.DocItem
import se.natusoft.doc.markdown.model.Paragraph

/**
 *
 */
abstract class PDFBoxGenerator implements Generator {
    //
    // Inner Classes
    //

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
     * A context for the generation process.
     */
    private static class PDFGeneratorContext extends GeneratorContext {
        //
        // Properties
        //

        /** The options for the PDF generator. */
        PDFGeneratorOptions options

        /** Adapter between MSS and iText fonts. */
        PDFBoxStylesMSSAdapter pdfStyles = new PDFBoxStylesMSSAdapter()

        /** The table of contents. */
        java.util.List<TOC> toc

        /** The current page number. */
        int pageNumber

    }


    //
    // Private Members
    //

    private PDFBoxDocRenderer doc

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
                fileResource: new FileResource(rootDir: rootDir, optsRootDir: (opts as PDFGeneratorOptions).rootDir)
        )

        // TODO: Add to MSS instead of hardcoding.
        this.doc = new PDFBoxDocRenderer(
                topMargin: 50,
                bottomMargin: 50,
                leftMargin: 50,
                rightMargin: 50,
                pageSize: context.options.pageSize
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

        final LinkedList<String> divs = new LinkedList<>()

        context.pdfStyles.mss.currentDivs = divs

        document.items.each { final DocItem docItem ->

            switch (docItem.format) {
                case DocFormat.Comment:
                    // We skip comments in general, but act on "@PB" within the comment for doing a page break.
                    final Comment comment = (Comment)docItem;
                    if (comment.text.indexOf("@PB") >= 0) {
                        this.doc.newPage()
                    }
                    // and also act on @PDFTitle, @PDFSubject, @PDFKeywords, @PDFAuthor, @PDFVersion, and @PDFCopyright
                    // for overriding those settings in the options. This allows the document rather than the generate
                    // config to provide this information.
                    extractCommentOptionsAnnotations(comment, context)
                    break

                case DocFormat.Paragraph:
                    writeParagraph(doc, docItem as Paragraph, context)
                    break

                case DocFormat.Header:
//                    writeHeader(docItem as Header, context)
                    break

                case DocFormat.BlockQuote:
//                    writeBlockQuote(docItem as BlockQuote, context)
                    break;

                case DocFormat.CodeBlock:
//                    writeCodeBlock(docItem as CodeBlock, context)
                    break

                case DocFormat.HorizontalRule:
//                    writeHorizontalRule(context)
                    break

                case DocFormat.List:
//                    writeList(docItem as List, context)
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


//        final Rectangle pageSize = new Rectangle(PageSize.getRectangle(context.options.pageSize))
//        if (context.options.backgroundColor != null) {
//            pageSize.backgroundColor = new PDFColorMSSAdapter(new MSSColor(color: context.options.backgroundColor))
//        }
//        else {
//            pageSize.backgroundColor = new PDFColorMSSAdapter(context.pdfStyles.mss.forDocument.
//                    getColorPair(MSS.MSS_Pages.standard).background)
//        }

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

    void writeParagraph(PDFBoxDocRenderer docProducer, Paragraph paragraph, PDFGeneratorContext context) {

        boolean first = true
        paragraph.items.each { final DocItem docItem ->
            if (docItem.renderPrefixedSpace && !first) {
                docProducer.text("  ")
            }
            first = false

            switch (docItem.format) {

                case DocFormat.Code:
//                    writeCode(docItem as Code, docProducer, context)
                    break

                case DocFormat.Emphasis:
//                    writeEmphasis(docItem as Emphasis, docProducer, context)
                    break

                case DocFormat.Strong:
//                    writeStrong(docItem as Strong, docProducer, context)
                    break

                case DocFormat.Image:
//                    writeImage(docItem as Image, docProducer, context)
                    break

                case DocFormat.Link:
//                    writeLink(docItem as Link, docProducer, context)
                    break

                case DocFormat.AutoLink:
//                    writeLink(docItem as AutoLink, docProducer, context)
                    break

                case DocFormat.Space:
//                    writePlainText(docItem as PlainText, docProducer, context)
                    break;

                case DocFormat.PlainText:
//                    writePlainText(docItem as PlainText, docProducer, context)
                    break

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" +
                            docItem.getClass().getName() + "]")
            }
        }

    }
}
