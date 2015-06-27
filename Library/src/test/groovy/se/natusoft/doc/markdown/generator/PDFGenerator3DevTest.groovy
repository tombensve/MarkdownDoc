package se.natusoft.doc.markdown.generator

import groovy.transform.CompileStatic
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.parser.MarkdownParser
import se.natusoft.tools.optionsmgr.CommandLineOptionsManager

/**
 * Test for MarkdownParser.
 */
@CompileStatic
class PDFGenerator3DevTest extends GroovyTestCase {

    /*
     * Note that this test only tests that the parsing and result generation executes
     * without throwing any exception. The content of the generated result is not verified.
     */
    public void testParser() throws Exception {

        // Handle both IntelliJ and Maven who runs from different roots! (I see this as a bug in IntelliJ)
        File testFile = new File("src/test/resources/Apache-2.0.md") // Maven
        File pdfFile = new File("src/test/resources/Apache-2.0.pdf")
        if (!testFile.exists()) {
            testFile = new File("Library/src/test/resources/Apache-2.0.md") // IntelliJ
            pdfFile = new File("Library/src/test/resources/Apache-2.0.pdf")
        }

        Parser parser = new MarkdownParser()
        Doc doc = new Doc()
        parser.parse(doc, testFile, new Properties())

        Generator generator = new PDFGenerator()

        CommandLineOptionsManager<Options> optMgr = new CommandLineOptionsManager<Options>(generator.optionsClass)
        def args = [
                "--title", "Test of generating a larger text",
                "--author", "Tommy Svensson",
                "--subject", "PDF generation",
                "--version", "1.0",
                "--copyright", "Copyright © 2012 Natusoft AB",
                "--hideLinks", "false",
                "--unorderedListItemPrefix", "• ",
                "--firstLineParagraphIndent", "false",
                "--resultFile", pdfFile.toString()
        ] as String[]
        Options options = optMgr.loadOptions("--", args)

        generator.generate(doc, options, null)
    }
}
