package se.natusoft.doc.markdowndoc;

import se.natusoft.doc.markdown.api.Generator;
import se.natusoft.doc.markdown.api.Options;
import se.natusoft.doc.markdown.api.Parser;
import se.natusoft.doc.markdown.exception.GenerateException;
import se.natusoft.doc.markdown.exception.ParseException;
import se.natusoft.doc.markdown.generator.HTMLGenerator;
import se.natusoft.doc.markdown.generator.PDFGenerator;
import se.natusoft.doc.markdown.model.Doc;
import se.natusoft.doc.markdown.parser.MarkdownParser;
import se.natusoft.tools.optionsmgr.CommandLineOptionsManager;
import se.natusoft.tools.optionsmgr.OptionsException;
import se.natusoft.tools.optionsmgr.OptionsModelException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

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
    public static void main(String[] args) {
        if (args.length < 2) {
            help1();
            System.exit(-1);
        }
        try {
            String selGenerator = args[0].toLowerCase();
            Generator generator = null;
            int startArg = args[1].startsWith("--") ? 1 : 2;

            if (selGenerator.equals("html")) {
                generator = new HTMLGenerator();
            }
            else if (selGenerator.equals("pdf")) {
                generator = new PDFGenerator();
            }
            else {
                System.err.println("Bad generator!");
                help1();
                System.exit(-1);
            }

            CommandLineOptionsManager<Options> optMgr = new CommandLineOptionsManager<Options>(generator.getOptionsClass());
            Options options = optMgr.loadOptions("--", args, startArg);
            if (options.isHelp()) {
                optMgr.printHelpText("--","", System.out);
            }
            else {
                String fileSpec = args[1];
                generate(generator, fileSpec, options);
            }
        }
        catch (OptionsModelException ome) {
            System.err.println("Failure: " + ome.getMessage());
        }
        catch (OptionsException oe) {
            System.err.println("Failure: " + oe.getMessage());
        }
        catch (IOException ioe) {
            System.err.println("I/O failure: " + ioe.getMessage());
        }
        catch (ParseException pe) {
            System.err.println("Failed parsing input: " + pe.getMessage());
        }
        catch (GenerateException ge) {
            System.err.println("Failed to generate: " + ge.getMessage());
        }
    }

    /**
     * Prints the help that can be given without selecting a generator.
     */
    private static void help1() {
        System.out.println("Bad arguments!");
        System.out.println("Usage: java -jar markdowndoc-cmd-line-1.0-exec.jar <generator> --help");
        System.out.println("       or");
        System.out.println("       java -jar markdowndoc-cmd-line-1.0-exec.jar <generator> <fileSpec> --<generator option> ...");
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
     * @param generator
     * @param fileSpec
     * @param options
     * @throws ParseException
     * @throws GenerateException
     */
    private static void generate(Generator generator, String fileSpec, Options options) throws ParseException, GenerateException, IOException {
        Parser parser = new MarkdownParser();
        Doc document = new Doc();

        StringTokenizer dirTokenizer = new StringTokenizer(fileSpec, ",");
        while (dirTokenizer.hasMoreTokens()) {
            SourcePath sourcePath = new SourcePath(dirTokenizer.nextToken());
            for (File file : sourcePath.getSourceFiles()) {
                parser.parse(document, file);
            }
        }

        generator.generate(document, options);
    }
}
