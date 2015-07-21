package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

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

    /** The file this button represents. */
    File file

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
        return this.file?.name
    }

    /**
     * Hide border when mouse is not over it.
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (this.paintButton) {
            setForeground(Color.BLACK)
            super.paintComponent(g)
        }
        else {
            g.setColor(Color.WHITE)
            g.drawString(file.name, ((width / 2) - (getFontMetrics(font).stringWidth(file.name) / 2)) as int,
                    ((height / 2) + font.size / 2) - 2 as int)
        }
    }

    /**
     * Invoked when the mouse enters a component.
     */
    @Override
    void mouseEntered(MouseEvent e) {
        this.paintButton = true
        repaint()

    }

    /**
     * Invoked when the mouse exits a component.
     */
    @Override
    void mouseExited(MouseEvent e) {
        this.paintButton = false
        repaint()
    }

    // For testing.
    static void main(String... args) throws Exception {
        JFrame jFrame = new JFrame()
        jFrame.setLayout(new BorderLayout())
        JPanel panel = new JPanel()
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
        panel.add(new PathLabel(text: "/Users/tommy/Development/projects/Tools/MDD-Dev"))
        panel.add(new EditableFileButton(file: new File("/Users/tommy/lee.tar.gz")))
        jFrame.contentPane.add(new Margin(margin: 20), BorderLayout.WEST)
        jFrame.contentPane.add(new Margin(margin: 20), BorderLayout.EAST)
        jFrame.contentPane.add(panel, BorderLayout.CENTER)
        jFrame.pack()
        jFrame.setVisible(true)
    }

}
