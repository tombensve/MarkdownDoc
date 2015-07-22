package se.natusoft.doc.markdowndoc.editor.tools

import org.jetbrains.annotations.NotNull

/**
 * Provides misc validation methods.
 */
trait Validator {

    void notNull(@NotNull final String name, @NotNull final Object validatee) {
        if (validatee == null) {
            throw new IllegalArgumentException("${name}: Cannot be null!")
        }
    }

    void notNull(@NotNull final Object validatee) {
        notNull(validatee.class.name, validatee)
    }

}
