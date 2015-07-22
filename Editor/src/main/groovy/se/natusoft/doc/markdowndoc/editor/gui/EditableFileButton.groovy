package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdowndoc.editor.api.Editable

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent

/**
 * This is a button that represents an editable file. It can be clicked on to select that file for editing
 * in the main window.
 */
@CompileStatic
@TypeChecked
class EditableFileButton extends JButton implements MouseListeners {

    //
    // Private Members
    //

    private boolean paintButton = false

    //
    // Properties
    //

    /** The editable this button represents. */
    Editable editable

    //
    // Constructors
    //

    public EditableFileButton() {
        addMouseListener(this)
    }

    //
    // Methods
    //

    @Override
    String getText() {
        return this.editable?.file?.name
    }

    /**
     * Hide border when mouse is not over it.
     * @param g
     */
    @Override
    protected void paintComponent(final Graphics g) {
        if (this.paintButton) {
            setForeground(Color.BLACK)
            super.paintComponent(g)
        }
        else {
            g.setColor(Color.WHITE)
            g.drawString(
                    this.editable.file.name, ((width / 2) -
                    (getFontMetrics(font).stringWidth(this.editable.file.name) / 2)) as int,
                    ((height / 2) + font.size / 2) - 2 as int)
        }
    }

    /**
     * Invoked when the mouse enters a component.
     */
    @Override
    void mouseEntered(final MouseEvent ignored) {
        this.paintButton = true
        repaint()

    }

    /**
     * Invoked when the mouse exits a component.
     */
    @Override
    void mouseExited(final MouseEvent ignored) {
        this.paintButton = false
        repaint()
    }

}
