package se.natusoft.doc.markdown.generator.pdf

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.generator.styles.MSS

/**
 * Keeps track of header level for headers. This because when we generate the TOC,
 * we really have no idea at which level each TOC entry is at, so we cannot apply
 * the appropriate styling. Therefore we save the heading level with the heading
 * text to be able to look it up later. This will of course fail if you have several
 * heading with identical text content. I can however currently not come upp with
 * any other solution to the problem.
 * <p/>
 * My first attempt was to subclass iTexts Paragraph adding the header level,
 * but iText messed that upp by not keeping my instance passed to it.
 */
@CompileStatic
@TypeChecked
class PDFHeaderLevelCache {

    //
    // Private Members
    //

    private Map<String, MSS.MSS_TOC> cache = new HashMap<>()

    //
    // Methods
    //

    void put(@NotNull String text, @NotNull MSS.MSS_TOC level) {
        this.cache.put(text, level)
    }

    MSS.MSS_TOC getLevel(@NotNull String text) {
        // Unfortunately we cannot just use the get() method since the passed text can
        // contain a bit more than what was stored, i.e section numbers, which iText adds.

        MSS.MSS_TOC level = this.cache.get(this.cache.keySet().find { String key ->
            text.matches("[0-9,.]*" + key.replace("*", "\\*").replace("\\", "\\\\").replace(".", "\\."))
        })

        level != null ? level : MSS.MSS_TOC.h1
    }

}
