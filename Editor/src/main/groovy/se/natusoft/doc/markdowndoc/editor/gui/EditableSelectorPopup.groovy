package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.file.Editables

import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import java.awt.AWTException
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D

/**
 * This class is responsible for selecting one of the open editables for editing.
 */
@CompileStatic
@TypeChecked
class EditableSelectorPopup extends JFrame implements MouseListeners {

    //
    // Private Members
    //

    /** Used to time mouse movement. */
    private Date mouseTime = null

    private int mouseX = 0

    private int exitLevel = 0

    //
    // Properties
    //

    Editor editor

    /** Called on close. */
    Closure closer

    //
    // Constructors
    //

    EditableSelectorPopup() {

        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Rectangle screenBounds =  gd.defaultConfiguration.bounds

        ColumnTopDownLeftRightLayout layout =
                new ColumnTopDownLeftRightLayout(hmargin: 20, vmargin: 20, hgap: 20, vgap: 0, screenSize: screenBounds)

        setLayout(new BorderLayout())
        JScrollPane scrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        scrollPane.setAutoscrolls(true)
        scrollPane.setViewportBorder(null)
        scrollPane.setBackground(Color.BLACK)

        JPanel popupContentPane = new JPanel()
        popupContentPane.setAutoscrolls(true)
        popupContentPane.setBackground(Color.BLACK)
        popupContentPane.setForeground(Color.WHITE)
        popupContentPane.setLayout(layout)


        Map<String, List<JComponent>> groups = new HashMap<>()

        // Sort files according to their directories.
        Editables.inst.files.each { File file ->
            String groupTitle = file.parentFile.absolutePath
            if (groupTitle.length() > 25) {
                groupTitle = "..." + groupTitle.substring(groupTitle.length() - 25)
            }
            List<JComponent> groupList = groups.get(groupTitle)

            if (groupList == null) {
                groupList = new LinkedList<JComponent>()
                groupList.add(new PathLabel(text: groupTitle))
                groups.put(groupTitle, groupList)
            }

            EditableFileButton editableFileButton = new EditableFileButton(file: file)
            editableFileButton.addMouseListener(this)
            groupList.add(editableFileButton)
        }

        EditableFileButton first = null

        // Then add them to the component.
        groups.keySet().each { String key ->
            List<JComponent> groupList = groups.get(key)
            groupList.each { JComponent component ->
                component.foreground = Color.WHITE
                component.background = Color.BLACK
                popupContentPane.add(component)

                if (first == null && (EditableFileButton.class.isAssignableFrom(component.class))) {
                    first = component as EditableFileButton
                }
            }
        }

        scrollPane.viewportView = popupContentPane

        add(scrollPane, BorderLayout.CENTER)

        // This fills out the outside of the shape that is not part of a square with white, so it
        // is not a true shaped window.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                EditableSelectorPopup.this.setShape(new RoundRectangle2D.Double(0.0, 0.0, width as double,
                        height as double, 10.0, 10.0))
            }
        })
        undecorated = true

        background = Color.BLACK
        if (gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
            opacity = 0.65f
        }

        setSize(1,1)
        visible = true
        size = layout.optimalSize
        setLocation(((screenBounds.width / 2) - (layout.optimalSize.width / 2)) as int,
                ((screenBounds.height / 2) - (layout.optimalSize.height / 2)) as int)

        Point p = new Point(first.x + this.x + 20, first.y + this.y + 10)
        moveMouse(p)

        popupContentPane.addMouseMotionListener(this)
    }

    //
    // Methods
    //

    /**
     * From: http://stackoverflow.com/questions/2941324/how-do-i-set-the-position-of-the-mouse-in-java
     * by Daniel.
     * <p/>
     * Groovyfied by me.
     *
     * @param p The point to move the mouse to.
     */
    static void moveMouse(Point p) {
        GraphicsEnvironment ge = GraphicsEnvironment.localGraphicsEnvironment
        GraphicsDevice[] gs = ge.screenDevices

        // Search the devices for the one that draws the specified point.
        for (GraphicsDevice device: gs) {
            GraphicsConfiguration[] configurations = device.configurations
            for (GraphicsConfiguration config: configurations) {
                Rectangle bounds = config.bounds
                if(bounds.contains(p)) {
                    // Set point to screen coordinates.
                    Point b = bounds.location
                    Point s = new Point((p.x - b.x) as int, (p.y - b.y) as int)

                    try {
                        Robot r = new Robot(device)
                        r.mouseMove(s.x as int, s.y as int)
                    } catch (AWTException e) {
                        e.printStackTrace()
                    }

                    return
                }
            }
        }

        // Couldn't move to the point, it may be off screen.
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    @Override
    void mouseClicked(MouseEvent e) {
        if (e.source instanceof  EditableFileButton) {
            EditableFileButton efb = e.source as EditableFileButton
            editor.editedFile = efb.file

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
        if (this.closer != null) {
            this.closer.call()
        }
    }

}
