/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Code Version
 *         1.4
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
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions
import se.natusoft.doc.markdown.model.*

/**
 * This is a generator that generates HTML from a document model.
 */
@CompileStatic
@TypeChecked
class HTMLGenerator implements Generator {

    private static class HTMLGeneratorContext extends GeneratorContext {

        /** The HTML generator options. */
        HTMLGeneratorOptions options
    }

    //
    // Methods
    //

    /**
     * Returns the options class required for this generator.
     */
    @Override
    @NotNull Class getOptionsClass() {
        HTMLGeneratorOptions.class
    }

    /**
     * @return The name of this generator.
     */
    @Override
    @NotNull String getName() {
        "html"
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
        final HTMLGeneratorContext context = new HTMLGeneratorContext(
                options: opts as HTMLGeneratorOptions,
                rootDir: rootDir
        )

        final def writer = rootDir != null ?
                new FileWriter(rootDir.path + File.separator + context.options.resultFile) :
                new FileWriter(context.options.resultFile)

        try {
            doGenerate(document, context.options, writer, context)
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
     * @param context The HTML genrator context.
     *
     * @throws IOException on I/O failures.
     * @throws GenerateException on other failures to generate target.
     */
    @Override
    void generate(@NotNull final Doc document, @NotNull final Options opts, @Nullable final File rootDir,
                  @NotNull final OutputStream resultStream)
            throws IOException, GenerateException {
        final HTMLGeneratorContext context = new HTMLGeneratorContext(
                options: opts as HTMLGeneratorOptions,
                rootDir: rootDir
        )

        final OutputStreamWriter resultWriter = new OutputStreamWriter(resultStream)
        doGenerate(document, context.options, resultWriter, context)
        resultWriter.close()
    }


    /**
     * The main API for the generator. This does the job!
     *
     * @param document The document model to generate from.
     * @param options The options.
     */
    private static void doGenerate(@NotNull final Doc document, @NotNull final HTMLGeneratorOptions options,
                            @NotNull final Writer writer, @NotNull final HTMLGeneratorContext context)
            throws IOException, GenerateException {

        final def printWriter = new PrintWriter(writer)
        final def html = new HTMLOutput(pw: printWriter)

        if (!options.primitiveHTML) {
            printWriter.println("<!DOCTYPE html>")
        }
        html.tagln("html")
        html.tagln("head")
        if (!options.primitiveHTML) {
            html.tage("meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"")
            html.ln()
            html.tage("meta name=\"generated-by\" content=\"MarkdownDoc\"")
            html.ln()
        }
        if (options.css != null && options.css.trim().length() > 0) {
            if (options.inlineCSS) {
                html.tagln("style type=\"text/css\"")
                final BufferedReader reader
                if (options.css.startsWith("classpath:")) {
                    final String css = options.css.substring(10)
                    final InputStream inStream = ClassLoader.getSystemResourceAsStream(css)
                    reader = new BufferedReader(new InputStreamReader(inStream))
                }
                else {
                    reader = new BufferedReader(new FileReader(options.css))
                }
                String line = reader.readLine()
                while (line != null) {
                    html.doIndent()
                    html.outputln(line) // We want no <>& translations here.
                    line = reader.readLine()
                }
                reader.close()
                html.etagln("style")
            }
            else {
                html.tage("link href=\"" + options.css + "\" type=\"text/css\" rel=\"stylesheet\"")
                html.ln()
            }
        }
        html.etagln("head")
        html.tagln("body")

        document.items.each { final DocItem docItem ->
            switch (docItem.format) {
                case DocFormat.Comment:
                    html.doIndent()
                    html.outputln("<!--")
                    html.doIndent()
                    html.outputln("  " + ((Comment)docItem).text)
                    html.doIndent()
                    html.outputln("-->")
                    break

                case DocFormat.Paragraph:
                    writeParagraph((Paragraph)docItem, html, context)
                    break

                case DocFormat.Header:
                    writeHeader((Header)docItem, html)
                    break

                case DocFormat.BlockQuote:
                    writeBlockQuote((BlockQuote)docItem, html, context)
                    break;

                case DocFormat.CodeBlock:
                    writeCodeBlock((CodeBlock)docItem, html)
                    break

                case DocFormat.HorizontalRule:
                    writeHorizontalRule(html)
                    break

                case DocFormat.List:
                    writeList((List)docItem, html, context)
                    break

                case DocFormat.Div:
                    writeDiv((Div)docItem, html)
                    break

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" + docItem.class.name + "]")
            }
        }

        html.etagln("body")
        html.etagln("html")
    }

    private static void writeHeader(@NotNull final Header header, @NotNull final HTMLOutput html) {
        html.tagln(header.level.name(), header.text)
    }

    private static void writeBlockQuote(@NotNull final BlockQuote blockQuote, @NotNull final HTMLOutput html,
                                 @NotNull final HTMLGeneratorContext context) {
        html.tagln("blockquote")
        html.tagln("p")
        html.doIndent()
        writeParagraphContent(blockQuote, html, context)
        html.etagln("p")
        html.etagln("blockquote")
    }

    private static void writeCodeBlock(@NotNull final CodeBlock codeBlock, @NotNull final HTMLOutput html) {
        html.tagln("pre")
        html.tagln("code")
        codeBlock.items.each { final DocItem item ->
            html.content(item.toString())
            html.outputln("")
        }
        html.etagln("code")
        html.etagln("pre")
    }

    private static void writeHorizontalRule(@NotNull final HTMLOutput html) {
        html.tage("hr")
    }

    private static void writeList(@NotNull final List list, @NotNull final HTMLOutput html,
                           @NotNull final HTMLGeneratorContext context) {
        if (list.ordered) {
            html.tagln("ol")
        }
        else {
            html.tagln("ul")
        }
        list.items.each { final DocItem li ->
            if (li instanceof List) {
                writeList((List)li, html, context)
            }
            else {
                html.tagln("li")
                li.items.each { final pg ->
                    writeParagraph((Paragraph)pg, html, context)
                }
                html.etagln("li")
            }
        }
        if (list.ordered) {
            html.etagln("ol")
        }
        else {
            html.etagln("ul")
        }
    }

    private static void writeParagraph(@NotNull final Paragraph paragraph, @NotNull  final HTMLOutput html,
                                @NotNull final HTMLGeneratorContext context)
            throws GenerateException {
        html.tagln("p")
        html.doIndent()
        writeParagraphContent(paragraph, html, context)
        html.etagln("p")
    }

    private static void writeParagraphContent(@NotNull final Paragraph paragraph, @NotNull final HTMLOutput html,
                                       @NotNull final HTMLGeneratorContext context)
            throws GenerateException {
        boolean first = true
        paragraph.items.each { final DocItem docItem ->
            if (docItem.renderPrefixedSpace && !first) {
                html.content(" ")
            }
            first = false

            switch (docItem.format) {

                case DocFormat.Code:
                    writeCode((Code)docItem, html)
                    break

                case DocFormat.Emphasis:
                    writeEmphasis((Emphasis)docItem, html)
                    break

                case DocFormat.Strong:
                    writeStrong((Strong)docItem, html)
                    break

                case DocFormat.Image:
                    writeImage((Image)docItem, html, context)
                    break

                case DocFormat.Link:
                    writeLink((Link)docItem, html)
                    break
                case DocFormat.AutoLink:
                    writeLink((AutoLink)docItem, html)
                    break

                case DocFormat.Space:
                    html.output("&nbsp;")
                    break;

                case DocFormat.PlainText:
                    html.content(((PlainText)docItem).text)
                    break

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" + docItem.class.name + "]")
            }
        }
        html.contentln("")
    }

    private static void writeCode(@NotNull final Code code, @NotNull final HTMLOutput html) {
        html.tag("code", code.text)
    }

    private static void writeEmphasis(@NotNull  final Emphasis emphasis, @NotNull final HTMLOutput html) {
        html.tag("em", emphasis.text)
    }

    private static void writeStrong(@NotNull final Strong strong, @NotNull final HTMLOutput html) {
        html.tag("strong", strong.text)
    }

    private static void writeImage(@NotNull final Image image, @NotNull  final HTMLOutput html,
                            @NotNull final HTMLGeneratorContext context) {
        html.tage("img src='" + resolveUrl(image.url, image.parseFile, context) + "' title='" + image.title +
                "' alt='" + image.text + "'")
    }

    private static void writeLink(@NotNull final Link link, @NotNull final HTMLOutput html) {
        html.tag("a href='" + link.url + "' title='" + link.title + "'")
        html.content(link.text)
        html.etag("a")
    }

    private static writeDiv(@NotNull final Div div, @NotNull final HTMLOutput html) {
        if (div.isStart()) {
            html.doIndent()
            html.outputln("<div class=\"${div.name}\">")
            html.incrementIndent()
        }
        else {
            html.decrementIndent()
            html.doIndent()
            html.outputln("</div>")
        }
    }

    // TODO: Break out resolveUrl & possiblyMakeRelative to a common base class or a Trait.

    /**
     * - Adds file: if no protocol is specified.
     * - If file: then resolved to full path if not found with relative path.
     *
     * @param url The DocItem item provided url.
     * @param parseFile The source file of the DocItem item.
     */
    private static @NotNull String resolveUrl(@NotNull final String url, @NotNull final File parseFile,
                                       @NotNull final HTMLGeneratorContext context) {
        String resolvedUrl = url
        if (!resolvedUrl.startsWith("file:") && !resolvedUrl.startsWith("http")) {
            resolvedUrl = "file:" + resolvedUrl
        }
        if (resolvedUrl.startsWith("file:")) {
            final String path = resolvedUrl.substring(5)
            File testFile = new File(path)

            if (!testFile.exists()) {
                // Try relative to parseFile first.
                int ix = parseFile != null ? parseFile.canonicalPath.lastIndexOf(File.separator) : -1
                if (ix >= 0) {
                    final String path1 = parseFile.canonicalPath.substring(0, ix + 1) + path
                    if (context.rootDir != null) {
                        // The result file is relative to the root dir!
                        resolvedUrl = "file:" + possiblyMakeRelative(context.rootDir.canonicalPath +
                                File.separator + path1, context)
                        testFile = new File(context.rootDir.canonicalPath + File.separator + path1)
                    }
                    else {
                        resolvedUrl = "file:" + possiblyMakeRelative(path1, context)
                        testFile = new File(path1)
                    }
                }
                if (!testFile.exists()) {
                    // Try relative to result file.
                    ix = context.options.resultFile != null ? context.options.resultFile.lastIndexOf(File.separator) : -1
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
     *
     * @return A possibly relative path.
     */
    private static @NotNull String possiblyMakeRelative(@NotNull final String path,
                                                        @NotNull final HTMLGeneratorContext context) {
        String resultPath = path

        if (context.options.makeFileLinksRelativeTo != null && context.options.makeFileLinksRelativeTo.trim().length() > 0) {
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

    //
    // Inner Classes
    //

    /**
     * A small convenience class for writing HTML will auto indentation.
     */
    private static class HTMLOutput {
        //
        // Private Members
        //

        /** The writer to output on. */
        PrintWriter pw

        /** The indentation level */
        private int indent = 0

        //
        // Methods
        //

        /**
         * Provides character replacements.
         *
         * @param content The content to replace in.
         *
         * @return A new string with replacements.
         */
        private static @NotNull String replace(@NotNull final String content) {
            return content.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
        }

        void incrementIndent() {
            this.indent += 2
        }

        void decrementIndent() {
            this.indent -= 2
        }

        /**
         * Output as a start tag.
         *
         * @param tag The name of the tag to output.
         */
        void tag(@NotNull final String tag) {
            pw.print("<" + tag + ">")
        }

        /**
         * Outputs the tag and content and ends the tag.
         *
         * @param tag The name of the tag to output.
         * @param content The content of the tag.
         */
        void tag(@NotNull final String tag, @NotNull final String content) {
            pw.print("<" + tag + ">" + replace(content) + "</" + tag + ">")
        }

        /**
         * Outputs a content-less tag that is both started and ended.
         *
         * @param tag The tag to output.
         */
        void tage(@NotNull final String tag) {
            doIndent()
            pw.print("<" + tag + "/>")
        }

        /**
         * Does the same as tag(tag) but adds a newline also.
         *
         * @param tag The tag to output.
         */
        void tagln(@NotNull final String tag) {
            doIndent()
            pw.println("<" + tag + ">")
            incrementIndent()
        }

        /**
         * Does the same as tag(tag, content) but adds a newline also.
         *
         * @param tag The tag to output.
         * @param content The content of the tag.
         */
        void tagln(@NotNull final String tag, @NotNull final String content) {
            doIndent()
            pw.println("<" + tag + ">" + replace(content) + "</" + tag + ">")
        }

        /**
         * Outputs the content of a tag. This also translates &lt;, &gt;, & into their entities.
         *
         * @param content The content to output.
         */
        void content(@NotNull final String content) {
            pw.print(replace(content))
        }

        /**
         * Does the same as content(content) but also adds a newline.
         *
         * @param cont The content to output.
         */
        void contentln(@NotNull final String cont) {
            doIndent()
            content(cont)
            pw.println()
        }

        /**
         * Outputs a line of text as is.
         * <p/>
         * <b>Do note that this method used to be called print()! Due to the Groovy giga-insanity of overriding print() and
         * println() for *ALL* objects globally, just to make it possible to write println("...") without the System.out in
         * front of it! Any method in your code called print() or println() will never ever be called! This is totally
         * insane! I really like Groovy in general, but this ... </b>
         *
         * @param text The text to output.
         */
        void output(@NotNull final String text) {
            pw.print(text)
        }

        /**
         * Outputs a line of text ending with a newline.
         * <p/>
         * <b>Do note that this method used to be called println()! Due to the Groovy giga-insanity of overriding print() and
         * println() for *ALL* objects globally, just to make it possible to write println("...") without the System.out in
         * front of it! Any method in your code called print() or println() will never ever be called! This is totally
         * insane! I really like Groovy in general, but this ... </b>
         *
         * @param text The text to output.
         */
        void outputln(@NotNull final String text) {
            pw.println(text)
        }

        /**
         * Outputs an end tag.
         *
         * @param tag The tag to end.
         */
        void etag(@NotNull final String tag) {
            pw.print("</" + tag + ">")
        }

        /**
         * Outputs an end tag plus a newline.
         *
         * @param tag The tag to end.
         */
        void etagln(@NotNull final String tag) {
            decrementIndent()
            doIndent()
            pw.println("</" + tag + ">")
        }

        /**
         * Outputs a newline.
         */
        void ln() {
            pw.println()
        }

        /**
         * Outputs indentation at current indent level.
         */
        void doIndent() {
            // If the ".times" part is error marked in red then you are using IDEA. It is perfectly valid to
            // do a .times on an int. This compiles without any problems. If it were Eclipse I would not
            // be surprised, but I do put the JetBrains to a higher standard.
            this.indent.times {
                pw.print(" ")
            }
        }
    }
}
