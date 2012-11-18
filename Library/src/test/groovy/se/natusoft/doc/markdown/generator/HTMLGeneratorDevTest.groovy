package se.natusoft.doc.markdown.generator

import se.natusoft.doc.markdown.api.Parser

import se.natusoft.doc.markdown.parser.markdown.MarkdownParser
import se.natusoft.doc.markdown.model.Doc

import se.natusoft.doc.markdown.api.Generator

import se.natusoft.tools.optionsmgr.CommandLineOptionsManager
import se.natusoft.doc.markdown.api.Options

/**
 * Test for MarkdownParser.
 */
class HTMLGeneratorDevTest extends GroovyTestCase {

    public void testParser() throws Exception {

        // Handle both IntelliJ and Maven who runs from different roots! (I see this as a bug in IntelliJ)
        File testFile = new File("src/test/resources/test.md") // Maven
        File htmlFile = new File("src/test/resources/test.html")
        if (!testFile.exists()) {
            testFile = new File("Library/src/test/resources/test.md") // IntelliJ
            htmlFile = new File("Library/src/test/resources/test.html")
        }

        Parser parser = new MarkdownParser()
        Doc doc = new Doc()
        parser.parse(doc, testFile)

        Generator generator = new HTMLGenerator()

        CommandLineOptionsManager<Options> optMgr = new CommandLineOptionsManager<Options>(generator.optionsClass)
        def args =  ["--css", "test.css", "--inlineCSS", "false", "--resultFile", htmlFile.toString()] as String[]
        Options options = optMgr.loadOptions("--", args)

        generator.generate(doc, options)
    }
}
