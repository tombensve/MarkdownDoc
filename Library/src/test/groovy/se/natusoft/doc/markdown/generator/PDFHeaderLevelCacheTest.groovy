package se.natusoft.doc.markdown.generator

import se.natusoft.doc.markdown.generator.pdf.PDFHeaderLevelCache
import se.natusoft.doc.markdown.generator.styles.MSS

class PDFHeaderLevelCacheTest extends GroovyTestCase {

    public void testPHLCache() {
        PDFHeaderLevelCache cache = new PDFHeaderLevelCache()

        cache.put("qwerty", MSS.MSS_TOC.h1)
        cache.put("qaz-qwerty", MSS.MSS_TOC.h2)
        cache.put("tommy", MSS.MSS_TOC.h3)

        assert cache.getLevel("qwerty") == MSS.MSS_TOC.h1
        assert cache.getLevel("1.2.3 qwerty") == MSS.MSS_TOC.h1
        assert cache.getLevel("qaz-qwerty") == MSS.MSS_TOC.h2
        assert cache.getLevel("tommy") == MSS.MSS_TOC.h3
    }
}
