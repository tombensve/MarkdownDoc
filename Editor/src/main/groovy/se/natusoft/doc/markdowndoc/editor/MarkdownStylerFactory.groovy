package se.natusoft.doc.markdowndoc.editor

import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editable
import se.natusoft.doc.markdowndoc.editor.api.JTextComponentStyler
import se.natusoft.doc.markdowndoc.editor.api.JTextComponentStylerFactory

/**
 * A factory for creating markdown styler.
 */
class MarkdownStylerFactory implements JTextComponentStylerFactory {

    @Override
    JTextComponentStyler createStyler(@NotNull final Editable editable) {

        MarkdownStyler styler = new MarkdownStyler(editable.editorPane)
        if (styler instanceof Configurable) {
            (styler as Configurable).registerConfigs(Services.configs)
            Services.configurables.add(styler as Configurable)
        }

        styler
    }
}
