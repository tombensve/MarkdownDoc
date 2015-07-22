package se.natusoft.doc.markdowndoc.editor.api

import org.jetbrains.annotations.NotNull

/**
 * A factory for creating stylers.
 */
interface JTextComponentStylerFactory {

    JTextComponentStyler createStyler(@NotNull Editable stylee)
}
