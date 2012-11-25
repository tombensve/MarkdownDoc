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
class HTMLGeneratorDevTest2 extends GroovyTestCase {

    public void testParser() throws Exception {

        // Handle both IntelliJ and Maven who runs from different roots! (I see this as a bug in IntelliJ)
        File testFile = new File("src/test/resources/blogEntry.md") // Maven
        File htmlFile = new File("src/test/resources/blogEntry.html")
        if (!testFile.exists()) {
            testFile = new File("Library/src/test/resources/blogEntry.md") // IntelliJ
            htmlFile = new File("Library/src/test/resources/blogEntry.html")
        }

        Parser parser = new MarkdownParser()
        Doc doc = new Doc()
        parser.parse(doc, testFile)

        Generator generator = new HTMLGenerator()

        CommandLineOptionsManager<Options> optMgr = new CommandLineOptionsManager<Options>(generator.optionsClass)
        def args =  ["--css", "test.css", "--inlineCSS", "false", "--resultFile", htmlFile.toString()] as String[]
        Options options = optMgr.loadOptions("--", args)

        generator.generate(doc, options, null)
    }
}
