package mddoc;

import junit.framework.TestCase
import org.junit.Test;
import se.natusoft.doc.markdown.util.MDDocFileHandler;

import java.io.File;

class MDDocTest extends TestCase {

    @Test
    void testMDDoc() throws Exception {
        MDDocFileHandler.execute("src/test/resources/mddoc/test.mddoc", true)
    }
}
