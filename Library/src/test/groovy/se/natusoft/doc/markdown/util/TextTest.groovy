package se.natusoft.doc.markdown.util

import org.junit.Test

class TextTest {

    @Test
    void testTestStd() {
        String content = "This is a test  of a   string to parse."
        Text text = new Text(content: content)
        text.words.each {Word word ->
            println ">>${word.toString(true)}<<"
        }

        assert text.words[0].toString(true) == "This "
        assert text.words[1].toString(true) == "is "
        assert text.words[2].toString(true) == "a "
        assert text.words[3].toString(true) == "test  "
        assert text.words[4].toString(true) == "of "
        assert text.words[5].toString(true) == "a   "
        assert text.words[6].toString(true) == "string "
        assert text.words[7].toString(true) == "to "
        assert text.words[8].toString(true) == "parse."
    }
}
