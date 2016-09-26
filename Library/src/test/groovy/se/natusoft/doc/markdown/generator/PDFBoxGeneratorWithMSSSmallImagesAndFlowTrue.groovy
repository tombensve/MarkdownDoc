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
class PDFBoxGeneratorWithMSSSmallImagesAndFlowTrue extends GroovyTestCase {

    /*
     * Note that this test only tests that the parsing and result generation executes
     * without throwing any exception. The content of the generated result is not verified.
     */
    public void testParser() throws Exception {

        // Handle both IntelliJ and Maven who runs from different roots! (I see this as a bug in IntelliJ)
        File testFile = new File("../Editor/docs/MarkdownDoc-Editor.md") // Maven
        File pdfFile = new File("target/test-editor.pdf")
        if (!testFile.exists()) {
            testFile = new File("Editor/docs/MarkdownDoc-Editor.md") // IntelliJ
            pdfFile = new File("Library/target/test-editor.pdf")
        }

        Parser parser = new MarkdownParser()
        Doc doc = new Doc()
        parser.parse(doc, testFile, new Properties())

        Generator generator = GeneratorProvider.getGeneratorByName("pdf")

        CommandLineOptionsManager<Options> optMgr = new CommandLineOptionsManager<Options>(generator.optionsClass)
        def args = [
                "--hideLinks", "false",
                "--unorderedListItemPrefix", "• ",
                "--generateTOC", "false",
                "--generateTitlePage", "false",
                "--generateSectionNumbers", "false",
                "--mss", "./src/test/resources/test2.mss",
                "--resultFile", pdfFile.toString()
        ] as String[]
        Options options = optMgr.loadOptions("--", args)

        generator.generate(doc, options, null)
    }
}
