/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Command Line
 *     
 *     Code Version
 *         2.1.1
 *     
 *     Description
 *         Parses markdown and generates HTML, PDF, and markdown.
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
 *         2012-11-21: Created!
 *
 */
package se.natusoft.doc.markdowndoc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.natusoft.doc.markdown.api.Generator;
import se.natusoft.doc.markdown.api.Options;
import se.natusoft.doc.markdown.api.Parser;
import se.natusoft.doc.markdown.exception.GenerateException;
import se.natusoft.doc.markdown.exception.ParseException;
import se.natusoft.doc.markdown.generator.GeneratorProvider;
import se.natusoft.doc.markdown.model.Doc;
import se.natusoft.doc.markdown.parser.ParserProvider;
import se.natusoft.doc.markdown.util.MDDocFileHandler;
import se.natusoft.doc.markdown.util.SourcePaths;
import se.natusoft.tools.optionsmgr.CommandLineOptionsManager;
import se.natusoft.tools.optionsmgr.OptionsException;
import se.natusoft.tools.optionsmgr.OptionsModelException;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * This is the main for running from command line.
 */
public class Main {

    // Non instantiable.
    private Main() {}

    /**
     * Executes this tool from the command line.
     *
     * @param args The arguments.
     */
    public static void main(final String[] args) {
        try {
            if (args.length == 1 && args[0].endsWith(".mddoc")) {
                MDDocFileHandler.execute(args[0], true);
            }
            else {
                if (args.length < 2) {
                    help1();
                    System.exit(-1);
                }

                final Properties parserOptions = new Properties();
                final String selGenerator = args[0].toLowerCase();
                Generator generator = null;
                int startArg = 1;
                if (args.length >= 3 && args[2].startsWith("parserOptions:")) {
                    startArg = 3;
                    final String parserOptsStr = args[2].substring(14);
                    for (String parserOpt : parserOptsStr.split(",")) {
                        final String[] nameValue = parserOpt.split("=");
                        parserOptions.put(nameValue[0], nameValue[1]);
                    }
                }
                else {
                    startArg = args[1].startsWith("--") ? 1 : 2;
                }

                generator = GeneratorProvider.getGeneratorByName(selGenerator);
                if (generator == null) {
                    System.err.println("Unknown generator: " + selGenerator + "!");
                    help1();
                    System.exit(-1);
                }

                final CommandLineOptionsManager<Options> optMgr =
                        new CommandLineOptionsManager<Options>(generator.getOptionsClass());
                final Options options = optMgr.loadOptions("--", args, startArg);
                if (options.isHelp()) {
                    optMgr.printHelpText("--","", System.out);
                }
                else {
                    String fileSpec = args[1];
                    generate(generator, fileSpec, options, parserOptions);
                }
            }
        }
        catch (final OptionsModelException ome) {
            System.err.println("Failure: " + ome.getMessage());
        }
        catch (final OptionsException oe) {
            System.err.println("Failure: " + oe.getMessage());
        }
        catch (final IOException ioe) {
            System.err.println("I/O failure: " + ioe.getMessage());
        }
        catch (final ParseException pe) {
            System.err.println("Failed parsing input: " + pe.getMessage());
        }
        catch (final GenerateException ge) {
            System.err.println("Failed to generate: " + ge.getMessage());
        }
    }

    /**
     * Prints the help that can be given without selecting a generator.
     */
    private static void help1() {
        System.out.println("Bad arguments!");
        System.out.println("Usage: java -jar markdowndoc-cmd-line-n.n[.n]-exec.jar <generator> --help");
        System.out.println("       or");
        System.out.println("       java -jar markdowndoc-cmd-line-n.n[.n]-exec.jar <generator> <fileSpec> --<generator option> ...");
        System.out.println("       or");
        System.out.println("       java -jar markdowndoc-cmd-line-n.n[.n]-exec.jar <generator> <fileSpec> parserOptions:<parserOptions> —<generator option> ...");
        System.out.println("       or");
        System.out.println("       java -jar markdowndoc-cmd-line-n.n[.n]-exec.jar <path to a .mddoc file>");
        System.out.println("");
        System.out.println("Where <generator> is one of 'html' or 'pdf'. Specify generator and --help to se generator specific options.");
        System.out.println("and <fileSpec> is a comma separated set of paths to files to parse as input. Wildcards like /**/ and ");
        System.out.println("regular expressions can be used. No space on either side of the commas!");
        System.out.println("Example: src/main/docs/**/.*\\.md,...");
        System.out.println("");
    }

    /**
     * Parses the input and generates output.
     *
     * @param generator The generator to run.
     * @param fileSpec The source files to parse.
     * @param options The generator options.
     * @param parserOptions The parser options.
     *
     * @throws ParseException
     * @throws GenerateException
     */
    private static void generate(@NotNull final Generator generator, @NotNull final String fileSpec,
                                 @NotNull final Options options, @Nullable final Properties parserOptions)
            throws ParseException, GenerateException, IOException {

        final Doc document = new Doc();

        final SourcePaths sourcePaths = new SourcePaths(fileSpec);
        for (File file : sourcePaths.getSourceFiles()) {
            final Parser parser = ParserProvider.getParserForFile(file);
            if (parser == null) {
                ParseException parseException = new ParseException();
                parseException.setFile(file.getAbsolutePath());
                parseException.setLineNo(0);
                parseException.setLine("");
                parseException.setMessage("Don't know how to parse this file!");
                throw parseException;
            }
            parser.parse(document, file, parserOptions);
        }

        generator.generate(document, options, null);
    }
}
