/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.0
 *     
 *     Description
 *         An editor that supports editing markdown with formatting preview.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2015-08-03: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdowndoc.editor.api.Editable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.file.Editables

import javax.swing.*
import javax.swing.border.EtchedBorder
import java.awt.*
import java.awt.event.*
import java.util.List

/**
 * This class is responsible for selecting one of the open editables for editing.
 */
@CompileStatic
@TypeChecked
class EditableSelectorLeftPopup extends PopupWindow implements MouseListenersTrait {

    //
    // Private Members
    //

    @SuppressWarnings("GroovyMissingReturnStatement") // IDEA is having a problem with Closure<Void> not returning anything ...
    private Closure<Void> cancelCallback = { close() }

    private JPanel popupContentPane = null

    /** This is kept as a member so that we can remove it again on close. */
    private ComponentListener parentMovedListener = new ComponentAdapter() {
        /**
         * Invoked when the component's position changes.
         */
        @Override
        void componentMoved(ComponentEvent e) {
            super.componentMoved(e)
            updateBounds()
        }

        /**
         * Invoked when the component's size changes.
         */
        @Override
        public void componentResized(ComponentEvent e) {
            super.componentResized(e)
            updateBounds()
        }

    }

    private ColumnTopDownLeftRightLayout contentLayout = new ColumnTopDownLeftRightLayout(
            vgap: 4,
            hgap: 4,
            leftMargin: 5,
            rightMargin: 5,
            topMargin: 10,
            bottomMargin: 30 // We need to reserve space for save and cancel button.
    )

    //
    // Properties
    //

    Editor editor
    void setEditor(final Editor editor) {
        this.editor = editor
        this.parentWindow = this.editor.GUI.windowFrame
        this.editor.addCancelCallback(cancelCallback)
        this.editor.GUI.windowFrame.addComponentListener(this.parentMovedListener)
    }

    /** Called on close. */
    Closure closer

    //
    // Constructors
    //

    /**
     * Creates a new popup window.
     */
    EditableSelectorLeftPopup() {

        setLayout(new BorderLayout())
        final JScrollPane scrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        )
        //scrollPane.setAutoscrolls(false)
        scrollPane.setViewportBorder(null)
        scrollPane.setBackground(Color.BLACK)

        this.popupContentPane = new JPanel()
        //this.popupContentPane.setAutoscrolls(false)
        this.popupContentPane.setBackground(EditableEntry.BG_COLOR)
        this.popupContentPane.setForeground(Color.WHITE)
        this.popupContentPane.layout = new VerticalFlowLayout()

        final Map<String, List<JComponent>> groups = new HashMap<>()

        // Sort files according to their directories.
        Editables.inst.files.each { final File file ->
            String groupTitle = file.parentFile.absolutePath
            if (groupTitle.length() > 36) {
                groupTitle = "..." + groupTitle.substring(groupTitle.length() - 36)
            }
            List<JComponent> groupList = groups.get(groupTitle)

            if (groupList == null) {
                groupList = new LinkedList<JComponent>()
                JPanel labelPanel = new JPanel()
                labelPanel.setBackground(Color.BLACK)
                labelPanel.layout = new BorderLayout()
                JLabel label = new JLabel("[${groupTitle}]", JLabel.RIGHT)
                label.setHorizontalTextPosition(SwingConstants.LEFT)
                label.setBackground(Color.BLACK)
                label.setForeground(Color.LIGHT_GRAY)
                labelPanel.add(label, BorderLayout.WEST)
                groupList.add(labelPanel)
                groups.put groupTitle, groupList
            }

            EditableEntry editableEntry = new EditableEntry(
                    editable: Editables.inst.getEditable(file),
                    fileName: file.name
            ).init()

            editableEntry.addMouseListener(this)
            groupList.add(editableEntry)
        }

        groups.keySet().each { final String key ->
            final List<JComponent> groupList = groups.get(key)
            groupList.each { final JComponent component ->
                popupContentPane.add(component)
            }
        }

        scrollPane.viewportView = popupContentPane

        add scrollPane, BorderLayout.WEST

        safeMakeRoundedRectangleShape()

        undecorated = true
        background = Color.BLACK

        popupContentPane.addMouseMotionListener(this)

        popupContentPane.addMouseListener(new CloseClickHandler())

        this.addKeyListener(new KeyHandler())
    }


    /**
     * This shows the window.
     */
    void showWindow() {

        updateOpacity(popupOpacity)

        // Wait with setting size until the popup window have been completely shown so
        // that we get the correct sizes of components being laid out.
        addComponentListener(new ComponentAdapter() {

            @Override
            void componentShown(final ComponentEvent e) {
                updatePopupSize()
            }

        })

        visible = true
        setSize(10, 10)

    }

    /**
     * Upodates the bounds of this window.
     */
    private void updateBounds() {
        final boolean fullScreen = isFullScreenWindow(this.editor.GUI.windowFrame)

        this.bounds = new Rectangle(
                this.parentWindow.x - (!fullScreen ? 321 : 0),
                this.parentWindow.y,
                320,
                this.parentWindow.height
        )
    }

    /**
     * Updates the size of the window after being opened.
     */
    private void updatePopupSize() {
        updateBounds()
        moveMouse(new Point(this.parentWindow.x - 100, this.parentWindow.y + 10))
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
        if (e.source instanceof EditableEntry) {
            final EditableEntry ee = e.source as EditableEntry
            editor.editable = ee.editable
        }
    }

    private void close() {
        PopupLock.instance.locked = false

        this.visible = false

        this.editor?.removeCancelCallback cancelCallback

        if (this.closer != null) {
            this.closer.call()
        }
    }

    //
    // Inner Classes
    //

    @CompileStatic
    @TypeChecked
    private class CloseClickHandler implements MouseListenersTrait {
        @Override
        void mouseClicked(final MouseEvent e) {
            close()
        }
    }

    @CompileStatic
    @TypeChecked
    private class KeyHandler extends KeyAdapter implements KeyListener {

        /**
         * Invoked when a key has been typed.
         * See the class description for {@link KeyEvent} for a definition of
         * a key typed event.
         */
        @Override
        void keyPressed(final KeyEvent e) {
            if (e.keyCode == KeyEvent.VK_ESCAPE) {
                close()
            }
        }
    }

    /**
     * This represents one file entry in the editor when selecting which file to work with.
     */
    @CompileStatic
    @TypeChecked
    private static class EditableEntry extends JPanel implements MouseListenersTrait {

        public static final Color TEXT_COLOR = new Color(20,20,20)
        public static final Color BG_COLOR = new Color(240, 240, 230)

        //
        // Properties
        //

        private String fileName
        private Editable editable

        //
        // Private Members
        //

        private JLabel fileLabel
        private JLabel startTextLabel1
        private JLabel startTextLabel2

        //
        // Post Constructor
        //

        final EditableEntry init() {
            this.layout = new BoxLayout(this, BoxLayout.Y_AXIS)
            this.setBackground(BG_COLOR)
            this.setBorder(new EtchedBorder(EtchedBorder.LOWERED))

            this.fileLabel = new JLabel(this.fileName, JLabel.LEFT)
            this.fileLabel.setHorizontalTextPosition(SwingConstants.LEFT)
            this.fileLabel.font = fileLabel.font.deriveFont(Font.BOLD)
            this.fileLabel.foreground = TEXT_COLOR
            this.add(fileLabel)

            String startText = this.editable.editorPane.text.length() < 50 ? this.editable.editorPane.text :
                    this.editable.editorPane.text.substring(0, 50)

            this.startTextLabel1 = new JLabel(startText, JLabel.LEFT)
            this.startTextLabel1.setHorizontalTextPosition(SwingConstants.LEFT)
            this.startTextLabel1.foreground = TEXT_COLOR
            this.add(startTextLabel1)

            if (this.editable.editorPane.text.length() > 50) {
                startText = this.editable.editorPane.text.length() < 100 ? this.editable.editorPane.text.substring(50) :
                        this.editable.editorPane.text.substring(50, 100)
                this.startTextLabel2 = new JLabel(startText, JLabel.LEFT)
                this.startTextLabel2.setHorizontalTextPosition(SwingConstants.LEFT)
                this.startTextLabel2.foreground = TEXT_COLOR
                this.add(startTextLabel2)
            }

            this
        }

        //
        // Methods
        //

        @Override
        Dimension getMinimumSize() {
            return new Dimension(300, this.fileLabel.font.size * 2 + 4)
        }

        @Override
        void mouseClicked(final MouseEvent e) {
            forwardMouseClicked(e, this)
        }

        @Override
        public synchronized void addMouseListener(MouseListener mouseListener) {
            mltAddMouseListener(mouseListener)
            super.addMouseListener(this)
            this.fileLabel.addMouseListener(this)
            this.startTextLabel1.addMouseListener(this)
            if (this.startTextLabel2 != null) {
                this.startTextLabel2.addMouseListener(this)
            }
        }

        @Override
        public synchronized void removeMouseListener(MouseListener mouseListener) {
            mltRemoveMouseListener(mouseListener)
            super.removeMouseListener(this)
            this.fileLabel.removeMouseListener(this)
            this.startTextLabel1.removeMouseListener(this)
            if (this.startTextLabel2 != null) {
                this.startTextLabel2.removeMouseListener(this)
            }
        }
    }

}
