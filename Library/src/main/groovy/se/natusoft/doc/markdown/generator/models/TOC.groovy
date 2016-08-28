package se.natusoft.doc.markdown.generator.models

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.generator.styles.MSS

/**
 * Stores a table of content entry.
 */
class TOC {
    //
    // Properties
    //

    @Nullable String sectionNumber
    @NotNull String sectionTitle
    int pageNumber = 0
    MSS.MSS_TOC section = MSS.MSS_TOC.toc
}
