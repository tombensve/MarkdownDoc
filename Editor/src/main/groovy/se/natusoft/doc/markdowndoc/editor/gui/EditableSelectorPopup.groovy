package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.file.Editables

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent
import java.util.List

/**
 * This class is responsible for selecting one of the open editables for editing.
 */
@CompileStatic
@TypeChecked
class EditableSelectorPopup extends JFrame implements GuiGoodies, MouseListeners {

    //
    // Private Members
    //

    /** Used to time mouse movement. */
    private Date mouseTime = null

    private int mouseX = 0

    private int exitLevel = 0

    private Closure<Void> cancelCallback = { close() }

    private ColumnTopDownLeftRightLayout layout =
            new ColumnTopDownLeftRightLayout(leftMargin: 20, topMargin: 30, hgap: 20, vgap: 4,
                    screenSize: defaultScreen_Bounds)

    private EditableFileButton first = null

    //
    // Properties
    //

    Editor editor
    void setEditor(Editor editor) {
        this.editor = editor
        this.editor.addCancelCallback cancelCallback
    }

    /** Called on close. */
    Closure closer

    //
    // Constructors
    //

    EditableSelectorPopup() {
        initGuiGoodies(this)

        setLayout(new BorderLayout())
        JScrollPane scrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        scrollPane.setAutoscrolls(true)
        scrollPane.setViewportBorder(null)
        scrollPane.setBackground(Color.BLACK)

        JPanel popupContentPane = new JPanel()
        popupContentPane.setAutoscrolls true
        popupContentPane.setBackground Color.BLACK
        popupContentPane.setForeground Color.WHITE
        popupContentPane.setLayout layout


        Map<String, List<JComponent>> groups = new HashMap<>()

        // Sort files according to their directories.
        Editables.inst.files.each { File file ->
            String groupTitle = file.parentFile.absolutePath
            if (groupTitle.length() > 25) {
                groupTitle = "..." + groupTitle.substring(groupTitle.length() - 25)
            }
            List<JComponent> groupList = groups.get groupTitle

            if (groupList == null) {
                groupList = new LinkedList<JComponent>()
                groupList.add new PathLabel(text: "[${groupTitle}]")
                groups.put groupTitle, groupList
            }

            EditableFileButton editableFileButton = new EditableFileButton(editable: Editables.inst.getEditable(file))
            editableFileButton.foreground = Color.WHITE
            editableFileButton.background = Color.BLACK
            editableFileButton.addMouseListener(this)
            groupList.add(editableFileButton)
        }

        // Then add them to the component.
        groups.keySet().each { String key ->
            List<JComponent> groupList = groups.get(key)
            groupList.each { JComponent component ->
                popupContentPane.add(component)

                if (first == null && (EditableFileButton.class.isAssignableFrom(component.class))) {
                    first = component as EditableFileButton
                }
            }
        }

        scrollPane.viewportView = popupContentPane

        add scrollPane, BorderLayout.CENTER

//        safeMakeRoundedRectangleShape()

        undecorated = true
        background = Color.BLACK
//        safeOpacity = STANDARD_OPACITY

        popupContentPane.addMouseMotionListener this

        popupContentPane.addMouseListener new CloseClickHandler()
    }

    void showWindow(final float _opacity) {

        updateOpacity(_opacity)

        setSize 1, 1
        visible = true

        size = layout.optimalSize

        setLocation 0, 0

        moveMouse new Point(first.x + this.x + 20, first.y + this.y + 10)

    }

    final void updateOpacity(final float _opacity) {
        safeOpacity = _opacity
    }

    private class CloseClickHandler implements MouseListeners {
        @Override
        void mouseClicked(MouseEvent e) {
            close()
        }
    }

    //
    // Methods
    //

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    @Override
    void mouseClicked(final MouseEvent e) {
        if (e.source instanceof  EditableFileButton) {
            final EditableFileButton efb = e.source as EditableFileButton
            editor.editable = efb.editable

            close()
        }
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    @Override
    void mouseMoved(MouseEvent e) {
        // When the user moves the mouse left, then right, and then left again really fast and at least
        // 30 pixels each time then we close the window without selecting any file.
        if (this.mouseTime != null) {
            Date now = new Date()
            long diff = now.time - this.mouseTime.time

            if (diff <= 20) {
                if (this.exitLevel == 0 || this.exitLevel == 2) {
                    if (e.x < (this.mouseX - 20)) {
                        if (this.exitLevel == 0) {
                            this.exitLevel = 1
                        }
                        else {
                            close()
                        }
                    }
                }
                else if (this.exitLevel == 1) {
                    if (e.x > (this.mouseX + 20)) {
                        this.exitLevel = 2
                    }
                }
            }
        }
        this.mouseTime = new Date()
        this.mouseX = e.x
    }

    private void close() {
        this.visible = false

        this.editor?.removeCancelCallback cancelCallback

        if (this.closer != null) {
            this.closer.call()
        }
    }

}
