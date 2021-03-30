package se.natusoft.doc.markdown.generator

import groovy.transform.CompileStatic
import org.junit.Test
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
class MarkdownGeneratorDevTest {

    @Test
    /*
     * Note that this test only tests that the parsing and result generation executes
     * without throwing any exception. The content of the generated result is not verified.
     */
    void testParser() throws Exception {

        // Handle both IntelliJ and Maven who runs from different roots! (I see this as a bug in IntelliJ)
        File testFile = new File("src/test/resources/test.md") // Maven
        File mdFile = new File("src/test/resources/test-generated.md")
        if (!testFile.exists()) {
            testFile = new File("Library/src/test/resources/test.md") // IntelliJ
            mdFile = new File("Library/src/test/resources/test-generated.md")
        }

        Parser parser = new MarkdownParser()
        Doc doc = new Doc()
        parser.parse(doc, testFile, new Properties())

        Generator generator = new MarkdownGenerator()

        CommandLineOptionsManager<Options> optMgr = new CommandLineOptionsManager<Options>(generator.optionsClass)
        def args =  ["--resultFile", mdFile.toString()] as String[]
        Options options = optMgr.loadOptions("--", args)

        generator.generate(doc, options, null)
    }
}
