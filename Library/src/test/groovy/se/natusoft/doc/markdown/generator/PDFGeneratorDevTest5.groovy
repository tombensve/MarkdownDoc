package se.natusoft.doc.markdown.generator

import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.parser.MarkdownParser
import se.natusoft.tools.optionsmgr.CommandLineOptionsManager

/**
 * Test for MarkdownParser.
 */
class PDFGeneratorDevTest5 extends GroovyTestCase {

    public void testParser() throws Exception {

        // Handle both IntelliJ and Maven who runs from different roots! (I see this as a bug in IntelliJ)
        File testFile = new File("src/test/resources/CDDL-1.0-License.md") // Maven
        File pdfFile = new File("src/test/resources/CDDL-1.0-License.pdf")
        if (!testFile.exists()) {
            testFile = new File("Library/src/test/resources/CDDL-1.0-License.md") // IntelliJ
            pdfFile = new File("Library/src/test/resources/CDDL-1.0-License.pdf")
        }

        Parser parser = new MarkdownParser()
        Doc doc = new Doc()
        parser.parse(doc, testFile)

        Generator generator = new PDFGenerator()

        CommandLineOptionsManager<Options> optMgr = new CommandLineOptionsManager<Options>(generator.optionsClass)
        def args = [
                "--hideLinks", "false",
                "--unorderedListItemPrefix", "• ",
                "--firstLineParagraphIndent", "false",
                "--resultFile", pdfFile.toString()
        ] as String[]
        Options options = optMgr.loadOptions("--", args)

        generator.generate(doc, options, null)
    }
}
