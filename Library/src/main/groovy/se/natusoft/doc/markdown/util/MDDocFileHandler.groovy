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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2013-05-05: Created!
 *
 */
package se.natusoft.doc.markdown.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.exception.ParseException
import se.natusoft.doc.markdown.generator.HTMLGenerator
import se.natusoft.doc.markdown.generator.MarkdownGenerator
import se.natusoft.doc.markdown.generator.PDFITextGenerator
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.parser.ParserProvider
import se.natusoft.tools.optionsmgr.CommandLineOptionsManager

/**
 * This handles reading and running a .mddoc file.
 */
@CompileStatic
@TypeChecked
class MDDocFileHandler {

    /**
     * Make this non instantiable.
     */
    private MDDocFileHandler() {}

    /**
     * Executes the .mddoc file.
     *
     * @param path The path to the .mdddoc file.
     * @param verbose If true verbose information is displayed to stdout.
     *
     * @throws ParseException
     */
    static execute(@NotNull final String path, final boolean verbose) throws ParseException {
        final Properties mdDocFile = new Properties()
        FileInputStream mdDocFileStream = null
        BufferedInputStream bufferedInputStream = null
        try {
            mdDocFileStream = new FileInputStream(path)
            bufferedInputStream = new BufferedInputStream(mdDocFileStream)
            mdDocFile.load(bufferedInputStream)
        }
        catch (final IOException ioe) {
            throw new ParseException(file: path, lineNo: 0, line: "", message: ioe.getMessage())
        }
        finally {
            if (bufferedInputStream != null) bufferedInputStream.close()
            if (mdDocFileStream != null) mdDocFileStream.close()
        }

        final String fileSpec = mdDocFile.get("inputPaths")

        final Properties parserOptions = new Properties();
        for (final String key : mdDocFile.stringPropertyNames()) {
            if (key.startsWith("parserOption.")) {
                parserOptions.setProperty(key.substring(13), mdDocFile.getProperty(key))
            }
        }

        if (verbose) System.out.println("Parsing input files:")
        final Doc document = new Doc();

        final SourcePaths sourcePaths = new SourcePaths(fileSpec);
        for (final File file : sourcePaths.getSourceFiles()) {
            if (verbose) System.out.println("    " + file.getPath() + " ...");
            Parser parser = ParserProvider.getParserForFile(file);
            if (parser == null) {
                throw new ParseException(file: file.getAbsolutePath(), lineNo: 0, line: "", message: "Don't know how to parse this file!")
            }
            parser.parse(document, file, parserOptions);
        }
        if (verbose) System.out.println("All parsed!");

        final boolean generatePDF = Boolean.valueOf(mdDocFile.getProperty("generate.pdf"))
        final boolean generateHTML = Boolean.valueOf(mdDocFile.getProperty("generate.html"))
        final boolean generateMarkdown = Boolean.valueOf(mdDocFile.getProperty("generate.markdown"))

        if (generatePDF) {
            final List<String> genOptsList = new LinkedList<>()
            for (final String key : mdDocFile.stringPropertyNames()) {
                if (key.startsWith("pdf.")) {
                    genOptsList.add("-" + key.substring(4))
                    genOptsList.add(mdDocFile.getProperty(key))
                }
            }
//            String[] genOpts = new String[genOptsList.size()]
//            genOpts = genOptsList.toArray(genOpts)
//            final PDFITextGenerator pdfGenerator = new PDFITextGenerator()
//            final CommandLineOptionsManager<Options> optMgr =
//                    new CommandLineOptionsManager<Options>(pdfGenerator.getOptionsClass());
//            final Options options = optMgr.loadOptions("-", genOpts, 0);
//
//            if (verbose) System.out.print("Generating " + options.getResultFile() + "...");
//            pdfGenerator.generate(document, options, null)
//            if (verbose) System.out.println("done.");
        }

        if (generateHTML) {
            final List<String> genOptsList = new LinkedList<>()
            mdDocFile.stringPropertyNames().each { final String key ->
                if (key.startsWith("html.")) {
                    genOptsList.add("-" + key.substring(5))
                    genOptsList.add(mdDocFile.getProperty(key))
                }
            }
            String[] genOpts = new String[genOptsList.size()]
            genOpts = genOptsList.toArray(genOpts)
            final HTMLGenerator htmlGenerator = new HTMLGenerator()
            final CommandLineOptionsManager<Options> optMgr =
                    new CommandLineOptionsManager<Options>(htmlGenerator.getOptionsClass());
            final Options options = optMgr.loadOptions("-", genOpts, 0);

            if (verbose) System.out.print("Generating " + options.getResultFile() + "...");
            htmlGenerator.generate(document, options, null)
            if (verbose) System.out.println("done.");
        }

        if (generateMarkdown) {
            final List<String> genOptsList = new LinkedList<>()
            mdDocFile.stringPropertyNames().each { final String key ->
                if (key.startsWith("markdown.")) {
                    genOptsList.add("-" + key.substring(9))
                    genOptsList.add(mdDocFile.getProperty(key))
                }
            }
            String[] genOpts = new String[genOptsList.size()]
            genOpts = genOptsList.toArray(genOpts)
            final MarkdownGenerator markdownGenerator = new MarkdownGenerator()
            final CommandLineOptionsManager<Options> optMgr =
                    new CommandLineOptionsManager<Options>(markdownGenerator.getOptionsClass());
            final Options options = optMgr.loadOptions("-", genOpts, 0);

            if (verbose) System.out.print("Generating " + options.getResultFile() + "...");
            markdownGenerator.generate(document, options, null)
            if (verbose) System.out.println("done.");
        }
    }
}

