package mddoc;

import junit.framework.TestCase;
import se.natusoft.doc.markdown.util.MDDocFileHandler;

import java.io.File;

/**
 */
public class MDDocTest extends TestCase {

    public void testMDDoc() throws Exception {
        MDDocFileHandler.execute("src/test/resources/mddoc/test.mddoc", true);
    }
}
