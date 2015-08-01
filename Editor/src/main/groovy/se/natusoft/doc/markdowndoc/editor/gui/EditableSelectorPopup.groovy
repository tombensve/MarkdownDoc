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

    private EditableFileButton moveToOnOpen = null

    //
    // Properties
    //

    Editor editor
    void setEditor(final Editor editor) {
        this.editor = editor
        this.editor.addCancelCallback cancelCallback
    }

    /** Called on close. */
    Closure closer

    //
    // Constructors
    //

    /**
     * Creates a new popup window.
     */
    EditableSelectorPopup() {
        initGuiGoodies(this)

        setLayout(new BorderLayout())
        final JScrollPane scrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        scrollPane.setAutoscrolls(true)
        scrollPane.setViewportBorder(null)
        scrollPane.setBackground(Color.BLACK)

        final JPanel popupContentPane = new JPanel()
        popupContentPane.setAutoscrolls true
        popupContentPane.setBackground Color.BLACK
        popupContentPane.setForeground Color.WHITE
        popupContentPane.setLayout layout


        final Map<String, List<JComponent>> groups = new HashMap<>()

        // Sort files according to their directories.
        Editables.inst.files.each { final File file ->
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

            final EditableFileButton editableFileButton =
                    new EditableFileButton(editable: Editables.inst.getEditable(file))
            editableFileButton.foreground = Color.WHITE
            editableFileButton.background = Color.BLACK
            editableFileButton.addMouseListener(this)
            groupList.add(editableFileButton)
        }

        // If the ".size()" part is red marked as a non existing method then you are using IDEA.
        // Just ignore it, Set do have a size() method.
        final int moveToOnOpenFileNo = (Editables.inst.files.size() / 2) as int
        final int mtofCount = 0

        // Then add them to the component.
        groups.keySet().each { final String key ->
            final List<JComponent> groupList = groups.get(key)
            groupList.each { final JComponent component ->
                popupContentPane.add(component)

                if (EditableFileButton.class.isAssignableFrom(component.class)) {
                    ++mtofCount

                    if (this.moveToOnOpen == null && mtofCount >= moveToOnOpenFileNo) {
                        this.moveToOnOpen = component as EditableFileButton
                    }
                }
            }
        }

        scrollPane.viewportView = popupContentPane

        add scrollPane, BorderLayout.CENTER

//        safeMakeRoundedRectangleShape()

        undecorated = true
        background = Color.BLACK

        popupContentPane.addMouseMotionListener this

        popupContentPane.addMouseListener new CloseClickHandler()
    }


    private class CloseClickHandler implements MouseListeners {
        @Override
        void mouseClicked(final MouseEvent e) {
            close()
        }
    }

    /**
     * This shows the window.
     *
     * @param _opacity The opacity to set on the window before making it visible.
     */
    void showWindow(final float _opacity) {

        updateOpacity(_opacity)

        setSize 1, 1
        visible = true

        size = layout.optimalSize

        setLocation 0, 0

        moveMouse new Point(this.moveToOnOpen.x + this.x + 20, this.moveToOnOpen.y + this.y + 10)

    }

    /**
     * Updates the opacity of the popup window. This gets called when config is changed.
     *
     * @param _opacity The new opacity.
     */
    final void updateOpacity(final float _opacity) {
        safeOpacity = _opacity
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
    void mouseMoved(final MouseEvent e) {
        // When the user moves the mouse left, then right, and then left again really fast and at least
        // 30 pixels each time then we close the window without selecting any file.
        if (this.mouseTime != null) {
            final Date now = new Date()
            final long diff = now.time - this.mouseTime.time

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
