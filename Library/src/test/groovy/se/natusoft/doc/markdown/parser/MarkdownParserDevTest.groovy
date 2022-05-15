package se.natusoft.doc.markdown.parser

import groovy.transform.CompileStatic
import org.junit.Test
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.model.DocItem

/**
 * Test for MarkdownParser.
 */
@CompileStatic
class MarkdownParserDevTest {

    @Test
    void testParser() throws Exception {

        Parser parser = new MarkdownParser();

        // Handle both IntelliJ and Maven who runs from different roots! (I see this as a bug in IntelliJ)
        File testFile = new File("src/test/resources/test.md") // Maven
        if (!testFile.exists()) {
            testFile = new File("Library/src/test/resources/test.md") // IntelliJ
        }
        Doc doc = new Doc()
        parser.parse(doc, testFile, new Properties())

        System.out.println("-------------------------------------------------------------------------------")
        for (DocItem item : doc.items) {
            System.out.println(item.toString())
        }
        System.out.println("-------------------------------------------------------------------------------")

    }
}
