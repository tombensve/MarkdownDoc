package se.natusoft.doc.markdown.io

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * Test of the Line class.
 */
@CompileStatic
class LineTest {

    @Test
    void testLine1() throws Exception {
        Line line = new Line("This is a test!", 0)

        assert line.currentWord == "This"
        assert line.nextWord == "is"
        assert line.nextWord == "a"
        assert line.nextWord == "test!"
        assert !line.hasMoreWords()
        assert line.nextWord == ""

    }

    void testLine2() throws Exception {
        Line line = new Line("This is a test!", 0)

        String result = ""
        String space = ""
        while (line.hasMoreWords()) {
            result = result + space + line.nextWord
            space = " "
        }

        assert result == "This is a test!"
    }

}
