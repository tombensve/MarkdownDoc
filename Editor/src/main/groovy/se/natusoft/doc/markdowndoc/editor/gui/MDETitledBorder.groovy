package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

/**
 *
 */
@CompileStatic
@TypeChecked
class MDETitledBorder extends TitledBorder {

    /**
     * Creates a TitledBorder instance.
     *
     * @param title the title the border should display
     */
    MDETitledBorder() {
        super(new EmptyBorder(0, 0, 0, 0), "")
    }


}
