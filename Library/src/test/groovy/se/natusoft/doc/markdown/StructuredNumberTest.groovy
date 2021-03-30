package se.natusoft.doc.markdown

import org.junit.Test
import se.natusoft.doc.markdown.util.StructuredNumber


class StructuredNumberTest {

    @Test
    void testSN() {

        StructuredNumber sn = new StructuredNumber()
        println sn
        assert sn.toString() == "0"

        println sn.increment()
        assert sn.toString() == "1"

        sn = sn.newDigit()
        println sn.root
        assert sn.root.toString() == "1.0"

        sn.increment()
        println sn.root
        assert sn.root.toString() == "1.1"

        sn.increment()
        println sn.root
        assert sn.root.toString() == "1.2"

        sn = sn.newDigit()
        println sn.root
        assert sn.root.toString() == "1.2.0"

        sn.increment()
        println sn.root
        assert sn.root.toString() == "1.2.1"

        sn = sn.deleteThisDigit()
        sn.increment()
        println sn.root
        assert sn.root.toString() == "1.3"

        sn = sn.deleteThisDigit()
        sn.increment()
        println sn.root
        assert sn.root.toString() == "2"
    }
}
