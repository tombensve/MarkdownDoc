/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Maven Plugin
 *     
 *     Code Version
 *         1.2.4
 *     
 *     Description
 *         A maven plugin for generating documentation from markdown.
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
 *         2012-11-18: Created!
 *         
 */
package se.natusoft.doc.markdowndoc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import se.natusoft.doc.markdown.api.Generator;
import se.natusoft.doc.markdown.api.Options;
import se.natusoft.doc.markdown.api.Parser;
import se.natusoft.doc.markdown.exception.GenerateException;
import se.natusoft.doc.markdown.exception.ParseException;
import se.natusoft.doc.markdown.generator.GeneratorProvider;
import se.natusoft.doc.markdown.generator.HTMLGenerator;
import se.natusoft.doc.markdown.generator.PDFGenerator;
import se.natusoft.doc.markdown.generator.options.GeneratorOptions;
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions;
import se.natusoft.doc.markdown.generator.options.MarkdownGeneratorOptions;
import se.natusoft.doc.markdown.generator.options.PDFGeneratorOptions;
import se.natusoft.doc.markdown.model.Doc;
import se.natusoft.doc.markdown.parser.MarkdownParser;
import se.natusoft.doc.markdown.parser.ParserProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Goal which touches a timestamp file.
 *
 * @goal doc
 * 
 * @phase generate-sources
 */
public class MarkdownDocMavenPlugin extends AbstractMojo {

    /**
     * Provides the options for which generator to run.
     *
     * @parameter
     * @optional
     */
    private GeneratorOptions generatorOptions = new GeneratorOptions();

    /**
     * Provides the options for the HTMLGenerator. These are only relevant if
     * generatorOptions.generator == "html".
     *
     * @parameter
     * @optional
     */
    private HTMLGeneratorOptions htmlGeneratorOptions = new HTMLGeneratorOptions();

    /**
     * Provides the options for the MarkdownGenerator. These are only relevant if
     * generatorOptions.generator == "md".
     *
     * @parameter
     * @optional
     */
    private MarkdownGeneratorOptions mdGeneratorOptions = new MarkdownGeneratorOptions();

    /**
     * Provides the options for the PDFGenerator. These are only relevant if
     * generatorOptions.generator == "pdf".
     *
     * @parameter
     * @optional
     */
    private PDFGeneratorOptions pdfGeneratorOptions = new PDFGeneratorOptions();

    /**
     * The projects base directory.
     *
     * @parameter expression="${basedir}"
     */
    private String baseDir;

    /**
     * Executes this mojo.
     *
     * @throws MojoExecutionException on bad config and other failures.
     */
    public void execute() throws MojoExecutionException {
        Parser parser = null;
        String selParser = this.generatorOptions.getParser().toLowerCase();
        if (selParser.equals("markdown")) {
            parser = new MarkdownParser();
        }
        else if (selParser.startsWith("byext")) {
            // leave parser null!
        }
        else {
            throw new MojoExecutionException("Unknown parser specified: '" + selParser + "'!");
        }

        Generator generator = null;
        Options options = null;
        String selGenerator = generatorOptions.getGenerator().toLowerCase();
        generator = GeneratorProvider.getGeneratorByName(selGenerator);
        if (generator == null) {
            throw new MojoExecutionException("Unknown generator: '" + selGenerator + "'!");
        }

        // Hmm ... We can't get around having an options model for a generator declared as a private member and
        // plugin parameter. But with this reflection match we only need to add it above.
        for (Field field : getClass().getDeclaredFields()) {
            if (field.getType().equals(generator.getOptionsClass())) {
                try {
                    options = (Options)field.get(this);
                    break;
                }
                catch (IllegalAccessException iae) {}
            }
        }

        // Parse

        File projRoot = getRootDir();
        Doc document = new Doc();
        SourcePaths sourcePaths = new SourcePaths(projRoot, generatorOptions.getInputPaths());
        String parserOptsStr = generatorOptions.getParserOptions();
        Properties parserOptions = new Properties();
        if (parserOptsStr != null) {
            for (String parserOpt : parserOptsStr.split(",")) {
                String[] nameValue = parserOpt.split("=");
                parserOptions.put(nameValue[0], nameValue[1]);
            }
        }
        if (sourcePaths.hasSourceFiles()) {
            try {
                System.out.println("Parsing the following files:");
                for (File sourceFile : sourcePaths.getSourceFiles()) {
                    System.out.println("    " + sourceFile);
                    Parser fileParser = parser;
                    if (fileParser == null) {
                        fileParser = ParserProvider.getParserForFile(sourceFile);
                    }
                    if (fileParser == null) {
                        throw new MojoExecutionException("Don't know how to parse '" + sourceFile.getName() + "'!");
                    }
                    fileParser.parse(document, sourceFile, parserOptions);
                }
                System.out.println("All parsed!");
            }
            catch (ParseException pe) {
                throw new MojoExecutionException("Parse failure!", pe);
            }
            catch (IOException ioe) {
                throw new MojoExecutionException("I/O failure while parsing input!", ioe);
            }

            // Generate

            try {
                if (options.getResultFile().startsWith(projRoot.getAbsolutePath())) {
                    System.out.println("Generating: " + options.getResultFile());
                    generator.generate(document, options, null);
                }
                else {
                    System.out.println("Generating: " + projRoot.getAbsolutePath() + File.separator + options.getResultFile());
                    generator.generate(document, options, projRoot);
                }
            }
            catch (GenerateException ge) {
                throw new MojoExecutionException("Failed to generate html!", ge);
            }
            catch (IOException ioe) {
                throw new MojoExecutionException("I/O problems when generating!", ioe);
            }
        }
        else {
            System.out.println("No document source files were specified, and thus nothing were generated!");
        }
    }

    /**
     * Returns a File representing the directory whose parent directory does not have a pom.xml.
     * In other words, the root of a multi-module build.
     */
    private File getRootDir() {
        File root = new File(this.baseDir);
        while (havePOM(root.getParentFile().listFiles())) {
            root = root.getParentFile();
        }

        return root;
    }

    /**
     * Checks if any of the passed files is a pom.xml.
     *
     * @param files The files to check.
     *
     * @return true if found, false otherwise.
     */
    private boolean havePOM(File[] files) {
        for (File file : files) {
            if (file.getName().toLowerCase().equals("pom.xml")) {
                return true;
            }
        }

        return false;
    }

}
