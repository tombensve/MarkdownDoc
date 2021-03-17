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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-11-16: Created!
 *
 */
package se.natusoft.doc.markdown.generator

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.options.MarkdownGeneratorOptions
import se.natusoft.doc.markdown.model.*

/**
 * This is a generator that generates Markdown from a document model.
 */
@CompileStatic
class MarkdownGenerator implements Generator {

    /**
     * Extend standard context with generator specific options.
     */
    private static class MarkdownGeneratorContext extends GeneratorContext {

        /** The generator options */
        MarkdownGeneratorOptions options
    }

    //
    // Methods
    //

    /**
     * Returns the options class required for this generator.
     */
    @Override
    @NotNull public Class getOptionsClass() {
        MarkdownGeneratorOptions.class
    }

    /**
     * @return The name of this generator.
     */
    @Override
    @NotNull String getName() {
        "md"
    }

    /**
     * The main API for the generator. This does the job!
     *
     * @param document The document model to generate from.
     * @param opts The options.
     * @param rootDir The optional root to prefix configured path with.
     */
    @Override
    void generate(@NotNull final Doc document, @NotNull final Options opts, @Nullable final File rootDir)
            throws IOException, GenerateException {
        final MarkdownGeneratorContext context = new MarkdownGeneratorContext(
                options: opts as MarkdownGeneratorOptions,
                rootDir: rootDir
        )

        final def writer = rootDir != null ?
                new FileWriter(rootDir.path + File.separator + context.options.resultFile) :
                new FileWriter(context.options.resultFile)

        try {
            doGenerate(document, writer, context)
        }
        finally {
            writer.close()
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
    void generate(@NotNull final Doc document, @NotNull final Options opts, @Nullable final File rootDir,
                  @NotNull final OutputStream resultStream) throws IOException, GenerateException {
        final
        MarkdownGeneratorContext context = new MarkdownGeneratorContext(
                options: opts as MarkdownGeneratorOptions,
                rootDir: rootDir
        )

        final OutputStreamWriter resultWriter = new OutputStreamWriter(resultStream)
        doGenerate(document, resultWriter, context)
    }

    /**
     * The main API for the generator. This does the job!
     *
     * @param document The document model to generate from.
     * @param writer The Writer to write to write to.
     * @param context The generator context.
     */
    private static void doGenerate(@NotNull final Doc document, @NotNull final Writer writer,
                                   @NotNull final MarkdownGeneratorContext context)
            throws IOException, GenerateException {

        final PrintWriter pw = new PrintWriter(writer)

        document.items.each { final DocItem docItem ->
            switch (docItem.format) {
                case DocFormat.Comment:
                    pw.println("<!--")
                    pw.println("  " + (docItem as Comment).text)
                    pw.println("-->")
                    break

                case DocFormat.Paragraph:
                    writeParagraph(docItem as Paragraph, pw, context)
                    break

                case DocFormat.Header:
                    writeHeader(docItem as Header, pw)
                    break

                case DocFormat.BlockQuote:
                    writeBlockQuote(docItem as BlockQuote, pw, context)
                    break;

                case DocFormat.CodeBlock:
                    writeCodeBlock(docItem as CodeBlock, pw)
                    break

                case DocFormat.HorizontalRule:
                    writeHorizontalRule(pw)
                    break

                case DocFormat.List:
                    writeList(docItem as List, pw, context)
                    break

                case DocFormat.Div:
                    writeDiv(docItem as Div, pw)
                    break

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" +
                            docItem.getClass().getName() + "]")
            }
        }
    }

    private static void writeHeader(@NotNull final Header header, @NotNull final PrintWriter pw) {
        for (int i = 0; i < header.level.level; i++) {
            pw.print("#")
        }
        pw.print(" " + header.text)
        pw.println()
        pw.println()
    }

    private static void writeBlockQuote(@NotNull final BlockQuote blockQuote, @NotNull final PrintWriter pw,
                                        @NotNull final MarkdownGeneratorContext context) {
        pw.print("> ")
        writeParagraph(blockQuote, pw, context)
    }

    private static void writeCodeBlock(@NotNull final CodeBlock codeBlock, @NotNull final PrintWriter pw) {
        codeBlock.items.each { final DocItem item ->
            pw.print("    " + item.toString())
            pw.println()
        }
        pw.println()
    }

    private static void writeHorizontalRule(@NotNull final PrintWriter pw) {
        pw.println("----")
        pw.println()
    }

    private static void writeList(@NotNull final List list, @NotNull final PrintWriter pw,
                                  @NotNull final MarkdownGeneratorContext context) {
        writeList(list, pw, "", context)
    }

    private static void writeList(@NotNull final List list, @NotNull final PrintWriter pw, @NotNull final String indent,
                                  @NotNull final MarkdownGeneratorContext context) {

        //noinspection GroovyVariableCanBeFinal
        int itemNo = 1; //      ^^^^^ Here we go again! No, this can definitively not be final!

        list.items.each { final DocItem li ->
            if (li instanceof List) {
                writeList((List)li, pw, indent + "   ", context)
            }
            else {
                if (list.ordered) {
                    pw.print(indent)
                    pw.print("" + itemNo + ". ")
                    itemNo++
                }
                else {
                    pw.print(indent + "* ")
                }
                li.items.each { final pg ->
                    writeParagraph((Paragraph)pg, pw, context)
                }
            }
        }
    }

    private static void writeParagraph(@NotNull final Paragraph paragraph, @NotNull final PrintWriter pw,
                                       @NotNull final MarkdownGeneratorContext context)
            throws GenerateException {
        boolean first = true
        paragraph.items.each { final DocItem docItem ->
            if (docItem.renderPrefixedSpace && !first) {
                pw.print(" ")
            }
            first = false

            switch (docItem.format) {

                case DocFormat.Code:
                    writeCode((Code)docItem, pw)
                    break

                case DocFormat.Emphasis:
                    writeEmphasis((Emphasis)docItem, pw)
                    break

                case DocFormat.Strong:
                    writeStrong((Strong)docItem, pw)
                    break

                case DocFormat.Image:
                    writeImage((Image)docItem, pw, context)
                    break

                case DocFormat.Link:
                    writeLink((Link)docItem, pw)
                    break
                case DocFormat.AutoLink:
                    writeLink((AutoLink)docItem, pw)
                    break

                case DocFormat.Space:
                    pw.print("&nbsp;")
                    break;

                case DocFormat.PlainText:
                    pw.print(((PlainText)docItem).text)
                    break

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" +
                            docItem.getClass().getName() + "]")
            }
        }
        pw.println()
        pw.println()
    }

    private static void writeCode(@NotNull final Code code, @NotNull final PrintWriter pw) {
        if (!code.text.empty) {
            pw.print("`" + code.text.trim() + "`")
        }
    }

    private static void writeEmphasis(@NotNull final Emphasis emphasis, @NotNull final PrintWriter pw) {
        if (!emphasis.text.empty) {
            pw.print("_" + emphasis.text.trim() + "_")
        }
    }

    private static void writeStrong(@NotNull final Strong strong, @NotNull final PrintWriter pw) {
        if (!strong.text.empty) {
            pw.print("__" + strong.text.trim() + "__")
        }
    }

    private static void writeImage(@NotNull final Image image, @NotNull final PrintWriter pw,
                                   @NotNull final MarkdownGeneratorContext context) {
        pw.print("![" + image.text + "](" + resolveUrl(image.url, image.parseFile, context))
        if (image.title != null && image.title.trim().length() > 0) {
            pw.print(" " + image.title)
        }
        pw.print(")")
    }

    private static void writeLink(@NotNull final Link link, @NotNull final PrintWriter pw) {
        pw.print("[" + link.text + "](" + link.url)
        if (link.title != null && link.title.trim().length() > 0) {
            pw.print(" " + link.title)
        }
        pw.print(")")
    }

    private static void writeDiv(@NotNull final Div div, @NotNull final PrintWriter pw) {
        if (div.isStart()) {
            // We generate the comment version of divs which were only added due to github not
            // handling <div class="...">...</div> in markdown documents. This has been reported
            // to github.
            pw.println("<!-- @Div(\"${div.name}\") -->")
            pw.println()
        }
        else {
            pw.println("<!-- @EndDiv -->")
            pw.println()
        }
    }

    // TODO: Break out resolveUrl & possiblyMakeRelative to a common base class or a Trait.

    /**
     * - Adds file: if no protocol is specified.
     * - If file: then resolved to full path if not found with relative path.
     *
     * @param url The DocItem item provided url.
     * @param parseFile The source file of the DocItem item.
     * @param context The generator run context.
     */
    private static String resolveUrl(@NotNull final String url, @NotNull final File parseFile,
                                     @NotNull final MarkdownGeneratorContext context) {
        String resolvedUrl = url
        if (!resolvedUrl.startsWith("file:") && !resolvedUrl.startsWith("http:") && !resolvedUrl.startsWith("https:")) {
            resolvedUrl = "file:" + resolvedUrl
        }
        if (resolvedUrl.startsWith("file:")) {
            final String path = resolvedUrl.substring(5)
            File testFile = new File(path)

            if (!testFile.exists()) {
                // Try relative to parseFile first.
                int ix = parseFile.canonicalPath.lastIndexOf(File.separator)
                if (ix >= 0) {
                    final String path1 = parseFile.canonicalPath.substring(0, ix + 1) + path
                    if (context.rootDir != null) {
                        // The result file is relative to the root dir!
                        resolvedUrl = "file:" + possiblyMakeRelative(context.rootDir.canonicalPath + File.separator +
                                path1, context)
                        testFile = new File(context.rootDir.canonicalPath + File.separator + path1)
                    }
                    else {
                        resolvedUrl = "file:" + possiblyMakeRelative(path1, context)
                        testFile = new File(path1)
                    }
                }
                if (!testFile.exists()) {
                    // Try relative to result file.
                    ix = context.options.resultFile.lastIndexOf(File.separator)
                    if (ix >= 0) {
                        final String path2 = context.options.resultFile.substring(0, ix + 1) + path
                        if (context.rootDir != null) {
                            // The result file is relative to the root dir!
                            resolvedUrl = "file:" + possiblyMakeRelative(context.rootDir.canonicalPath +
                                    File.separator + path2, context)
                        }
                        else {
                            resolvedUrl = "file:" + possiblyMakeRelative(path2, context)
                        }
                    }
                }
            }
        }

        resolvedUrl
    }

    /**
     * Checks of a relative path is wanted and if so checks if the specified path can be made relative to the configured
     * relative path. If so it is made relative.
     *
     * @param path The original path to check and convert.
     * @param context The Markdown generator context.
     *
     * @return A possibly relative path.
     */
    private static @NotNull String possiblyMakeRelative(@NotNull final String path,
                                                        @NotNull final MarkdownGeneratorContext context) {
        String resultPath = path

        if (context.options.makeFileLinksRelativeTo != null &&
                context.options.makeFileLinksRelativeTo.trim().length() > 0) {
            final String[] relativeToParts = context.options.makeFileLinksRelativeTo.split("\\+")
            final File relFilePath = new File(relativeToParts[0])
            final String expandedRelativePath = relFilePath.canonicalPath
            if (resultPath.startsWith(expandedRelativePath)) {
                resultPath = resultPath.substring(expandedRelativePath.length() + 1)
                if (relativeToParts.length > 1) {
                    resultPath = relativeToParts[1] + resultPath
                }
            }
        }

        resultPath
    }

}
