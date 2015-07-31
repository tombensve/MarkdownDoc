package se.natusoft.doc.markdowndoc.editor.gui

import javax.swing.JComponent
import java.awt.Color

/**
 * A trait for color setting support.
 */
trait Colors {

    static void updateColors(JComponent component) {
        component.background = Color.BLACK
        component.foreground = Color.WHITE
    }
}
