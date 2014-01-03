/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.2.10
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
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions
import se.natusoft.doc.markdown.model.*

/**
 * This is a generator that generates HTML from a document model.
 */
@CompileStatic
class HTMLGenerator implements Generator {

    //
    // Private Members
    //

    private HTMLGeneratorOptions options

    private File rootDir

    //
    // Methods
    //

    /**
     * Returns the options class required for this generator.
     */
    @Override
    public Class getOptionsClass() {
        return HTMLGeneratorOptions.class
    }

    /**
     * @return The name of this generator.
     */
    @Override
    String getName() {
        return "html"
    }

    /**
     * The main API for the generator. This does the job!
     *
     * @param document The document model to generate from.
     * @param opts The options.
     * @param rootDir The optional root to prefix configured path with.
     */
    @Override
    public void generate(Doc document, Options opts, File rootDir) throws IOException, GenerateException {
        this.options = (HTMLGeneratorOptions)opts
        this.rootDir = rootDir

        def writer //= new FileWriter(rootDir != null ? (rootDir.getPath() + File.separator + options.resultFile) : options.resultFile)
        if (rootDir != null) {
            writer = new FileWriter(rootDir.path + File.separator + this.options.resultFile)
        }
        else {
            writer = new FileWriter(this.options.resultFile)
        }
        try {
            doGenerate(document, this.options, writer)
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
    public void generate(Doc document, Options opts, File rootDir, OutputStream resultStream) throws IOException, GenerateException {
        this.options = (HTMLGeneratorOptions)opts
        this.rootDir = rootDir
        OutputStreamWriter resultWriter = new OutputStreamWriter(resultStream)
        doGenerate(document, this.options, resultWriter)
        resultWriter.close()
    }


    /**
     * The main API for the generator. This does the job!
     *
     * @param document The document model to generate from.
     * @param options The options.
     */
    private void doGenerate(Doc document, HTMLGeneratorOptions options, Writer writer) throws IOException, GenerateException {

        PrintWriter printWriter = new PrintWriter(writer)
        def html = new HTMLOutput(pw: printWriter)

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
                BufferedReader reader = null;
                if (options.css.startsWith("classpath:")) {
                    String css = options.css.substring(10)
                    InputStream inStream = ClassLoader.getSystemResourceAsStream(css)
                    reader = new BufferedReader(new InputStreamReader(inStream))
                }
                else {
                    reader = new BufferedReader(new FileReader(options.css))
                }
                String line = reader.readLine()
                while (line != null) {
                    html.doIndent()
                    html.println(line) // We want no <>& translations here.
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

        for (DocItem docItem : document.items) {
            switch (docItem.format) {
                case DocFormat.Comment:
                    html.doIndent()
                    html.println("<!--")
                    html.doIndent()
                    html.println("  " + ((Comment)docItem).text)
                    html.doIndent()
                    html.println("-->")
                    break

                case DocFormat.Paragraph:
                    writeParagraph((Paragraph)docItem, html)
                    break

                case DocFormat.Header:
                    writeHeader((Header)docItem, html)
                    break

                case DocFormat.BlockQuote:
                    writeBlockQuote((BlockQuote)docItem, html)
                    break;

                case DocFormat.CodeBlock:
                    writeCodeBlock((CodeBlock)docItem, html)
                    break

                case DocFormat.HorizontalRule:
                    writeHorizontalRule(html)
                    break

                case DocFormat.List:
                    writeList((List)docItem, html)
                    break

                default:
                    throw new GenerateException(message: "Unknown format model in Doc! [" + docItem.class.name + "]")
            }
        }

        html.etagln("body")
        html.etagln("html")
    }

    private void writeHeader(Header header, HTMLOutput html) {
        html.tagln(header.level.name(), header.text)
    }

    private void writeBlockQuote(BlockQuote blockQuote, HTMLOutput html) {
        html.tagln("blockquote")
        html.tagln("p")
        html.doIndent()
        writeParagraphContent(blockQuote, html)
        html.etagln("p")
        html.etagln("blockquote")
    }

    private void writeCodeBlock(CodeBlock codeBlock, HTMLOutput html) {
        html.tagln("pre")
        html.tagln("code")
        for (DocItem item : codeBlock.items) {
            html.content(item.toString())
            html.println("")
        }
        html.etagln("code")
        html.etagln("pre")
    }

    private void writeHorizontalRule(HTMLOutput html) {
        html.tage("hr")
    }

    private void writeList(List list, HTMLOutput html) {
        if (list.ordered) {
            html.tagln("ol")
        }
        else {
            html.tagln("ul")
        }
        list.items.each { DocItem li ->
            if (li instanceof List) {
                writeList((List)li, html)
            }
            else {
                html.tagln("li")
//                html.doIndent()
                li.items.each { pg ->
                    writeParagraph((Paragraph)pg, html)
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

    private void writeParagraph(Paragraph paragraph, HTMLOutput html) throws GenerateException {
        html.tagln("p")
        html.doIndent()
        writeParagraphContent(paragraph, html)
        html.etagln("p")
    }

    private void writeParagraphContent(Paragraph paragraph, HTMLOutput html) throws GenerateException {
        boolean first = true
        for (DocItem docItem : paragraph.items) {
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
                    writeImage((Image)docItem, html)
                    break

                case DocFormat.Link:
                    writeLink((Link)docItem, html)
                    break
                case DocFormat.AutoLink:
                    writeLink((AutoLink)docItem, html)
                    break

                case DocFormat.Space:
                    html.print("&nbsp;")
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

    private void writeCode(Code code, HTMLOutput html) {
        html.tag("code", code.text)
    }

    private void writeEmphasis(Emphasis emphasis, HTMLOutput html) {
        html.tag("em", emphasis.text)
    }

    private void writeStrong(Strong strong, HTMLOutput html) {
        html.tag("strong", strong.text)
    }

    private void writeImage(Image image, HTMLOutput html) {
        html.tage("img src='" + resolveUrl(image.url, image.parseFile) + "' title='" + image.title + "' alt='" + image.text + "'")
    }

    private void writeLink(Link link, HTMLOutput html) {
        html.tag("a href='" + link.url + "' title='" + link.title + "'")
        html.content(link.text)
        html.etag("a")
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
                int ix = parseFile != null ? parseFile.canonicalPath.lastIndexOf(File.separator) : -1
                if (ix >= 0) {
                    String path1 = parseFile.canonicalPath.substring(0, ix + 1) + path
                    if (this.rootDir != null) {
                        // The result file is relative to the root dir!
                        resolvedUrl = "file:" + possiblyMakeRelative(this.rootDir.canonicalPath + File.separator + path1)
                        testFile = new File(this.rootDir.canonicalPath + File.separator + path1)
                    }
                    else {
                        resolvedUrl = "file:" + possiblyMakeRelative(path1)
                        testFile = new File(path1)
                    }
                }
                if (!testFile.exists()) {
                    // Try relative to result file.
                    ix = this.options.resultFile != null ? this.options.resultFile.lastIndexOf(File.separator) : -1
                    if (ix >= 0) {
                        String path2 = this.options.resultFile.substring(0, ix + 1) + path
                        if (this.rootDir != null) {
                            // The result file is relative to the root dir!
                            resolvedUrl = "file:" + possiblyMakeRelative(this.rootDir.canonicalPath + File.separator + path2)
                        }
                        else {
                            resolvedUrl = "file:" + possiblyMakeRelative(path2)
                        }
                    }
                }
            }
        }

        return resolvedUrl
    }

    /**
     * Checks of a relative path is wanted and if so checks if the specified path can be made relative to the configured
     * relative path. If so it is made relative.
     *
     * @param path The original path to check and convert.
     *
     * @return A possibly relative path.
     */
    private String possiblyMakeRelative(String path) {
        String resultPath = path

        if (this.options.makeFileLinksRelativeTo != null && this.options.makeFileLinksRelativeTo.trim().length() > 0) {
            String[] relativeToParts = this.options.makeFileLinksRelativeTo.split("\\+")
            File relFilePath = new File(relativeToParts[0])
            String expandedRelativePath = relFilePath.canonicalPath
            if (resultPath.startsWith(expandedRelativePath)) {
                resultPath = resultPath.substring(expandedRelativePath.length() + 1)
                if (relativeToParts.length > 1) {
                    resultPath = relativeToParts[1] + resultPath
                }
            }
        }

        return resultPath
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
        private static String replace(String content) {
            return content.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
        }

        /**
         * Output as a start tag.
         *
         * @param tag The name of the tag to output.
         */
        public void tag(String tag) {
            pw.print("<" + tag + ">")
        }

        /**
         * Outputs the tag and content and ends the tag.
         *
         * @param tag The name of the tag to output.
         * @param content The content of the tag.
         */
        public void tag(String tag, String content) {
            pw.print("<" + tag + ">" + replace(content) + "</" + tag + ">")
        }

        /**
         * Outputs a content-less tag that is both started and ended.
         *
         * @param tag The tag to output.
         */
        public void tage(String tag) {
            doIndent()
            pw.print("<" + tag + "/>")
        }

        /**
         * Does the same as tag(tag) but adds a newline also.
         *
         * @param tag The tag to output.
         */
        public void tagln(String tag) {
            doIndent()
            pw.println("<" + tag + ">")
            indent = indent + 2
        }

        /**
         * Does the same as tag(tag, content) but adds a newline also.
         *
         * @param tag The tag to output.
         * @param content The content of the tag.
         */
        public void tagln(String tag, String content) {
            doIndent()
            pw.println("<" + tag + ">" + replace(content) + "</" + tag + ">")
        }

        /**
         * Outputs the content of a tag. This also translates &lt;, &gt;, & into their entities.
         *
         * @param content The content to output.
         */
        public void content(String content) {
            pw.print(replace(content))
        }

        /**
         * Does the same as content(content) but also adds a newline.
         *
         * @param cont The content to output.
         */
        public void contentln(String cont) {
            doIndent()
            content(cont)
            pw.println()
        }

        /**
         * Outputs a line of text as is.
         *
         * @param text The text to output.
         */
        public void print(String text) {
            pw.print(text)
        }

        /**
         * Outputs a line of text ending with a newline.
         *
         * @param text The text to output.
         */
        public void println(String text) {
            pw.println(text)
        }

        /**
         * Outputs an end tag.
         *
         * @param tag The tag to end.
         */
        public void etag(String tag) {
            pw.print("</" + tag + ">")
        }

        /**
         * Outputs an end tag plus a newline.
         *
         * @param tag The tag to end.
         */
        public void etagln(String tag) {
            indent = indent - 2
            doIndent()
            pw.println("</" + tag + ">")
        }

        /**
         * Outputs a newline.
         */
        public void ln() {
            pw.println()
        }

        /**
         * Outputs indentation at current indent level.
         */
        public void doIndent() {
            this.indent.times {
                pw.print(" ")
            }
        }
    }
}
