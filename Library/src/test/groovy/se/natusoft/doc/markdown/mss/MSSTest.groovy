package se.natusoft.doc.markdown.mss

import se.natusoft.doc.markdown.generator.styles.MSS

/**
 *
 */
class MSSTest extends GroovyTestCase {

    void testMSS1() throws Exception {
        File testFile = new File("src/test/resources/test.mss") // Maven
        if (!testFile.exists()) {
            testFile = new File("Library/src/test/resources/test.mss") // IntelliJ
        }

        MSS mss = MSS.fromInputStream(new FileInputStream(testFile))

        assert mss.getColorPairForDocument(null, MSS.MSS_Pages.h1).foreground.blue == 0
        assert mss.getFontForDocument(null, MSS.MSS_Pages.h1).size == 20
        assert mss.getFontForDocument(null, MSS.MSS_Pages.h6).size == 11
    }

}
