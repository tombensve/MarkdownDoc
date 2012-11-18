package se.natusoft.doc.markdown.io

/**
 * Test of the Line class.
 */
class LineTest extends GroovyTestCase {

    public void testLine1() throws Exception {
        Line line = new Line("This is a test!", 0)

        assert line.currentWord == "This"
        assert line.nextWord == "is"
        assert line.nextWord == "a"
        assert line.nextWord == "test!"
        assert !line.hasMoreWords()
        assert line.nextWord == ""

    }

    public void testLine2() throws Exception {
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
