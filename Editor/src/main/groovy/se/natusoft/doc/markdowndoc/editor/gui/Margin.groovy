package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import javax.swing.JPanel
import java.awt.Dimension

/**
 * This is a component that only generates space.
 */
@CompileStatic
@TypeChecked
class Margin extends JPanel {

    int margin

    Dimension minimumSize() {
        return new Dimension(margin, 10)
    }

    Dimension preferredSize() {
        return new Dimension(margin, 10)
    }
}
