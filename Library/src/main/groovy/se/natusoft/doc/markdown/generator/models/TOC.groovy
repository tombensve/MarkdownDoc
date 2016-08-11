package se.natusoft.doc.markdown.generator.models

import se.natusoft.doc.markdown.generator.styles.MSS

/**
 * Stores a table of content entry.
 */
class TOC {
    //
    // Properties
    //

    String sectionNumber
    String sectionTitle
    int pageNumber = 0
    MSS.MSS_TOC section = MSS.MSS_TOC.toc
}
