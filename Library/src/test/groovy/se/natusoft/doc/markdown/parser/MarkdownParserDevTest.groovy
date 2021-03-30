package se.natusoft.doc.markdown.parser

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.junit.Test
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.model.DocItem
import se.natusoft.doc.markdown.model.Table

/**
 * Test for MarkdownParser.
 */
@CompileStatic
class MarkdownParserDevTest {

    @NotNull
    private static File getTestFile(@NotNull String path) {
        // Handle both IntelliJ and Maven who runs from different roots! (I see this as a bug in IntelliJ)
        File testFile = new File(path)                      // Maven
        if (!testFile.exists()) {
            testFile = new File("Library/${path}") // IntelliJ
        }

        return testFile // Hmm ... This is groovy! Last updated object is testFile and that should be the default
                        // return value, but without the 'return' null is returned!
    }

    @Test
    void testParser() throws Exception {

        Parser parser = new MarkdownParser()

        // Handle both IntelliJ and Maven who runs from different roots! (I see this as a bug in IntelliJ)
        File testFile = getTestFile("src/test/resources/test.md")

        println(">>>>>>> " + testFile)

        Doc doc = new Doc()
        parser.parse(doc, testFile, new Properties())

        System.out.println("-------------------------------------------------------------------------------")
        for (DocItem item : doc.items) {
            System.out.println(item.toString())
        }
        System.out.println("-------------------------------------------------------------------------------")
    }

    @Test
    void tableTest() {

        File testFile = new File("src/test/resources/TableTest.md")

        Parser parser = new MarkdownParser()
        Doc doc = new Doc()

        parser.parse(doc,testFile, new Properties())
        System.out.println("-------------------------------------------------------------------------------")
        for (DocItem item : doc.items) {
            System.out.println(item.toString())
        }
        System.out.println("-------------------------------------------------------------------------------")

        assert doc.items[2].getClass() == Table.class

        Table table = doc.items[2] as Table

        assert table.header[0] == "First column"

    }

}
