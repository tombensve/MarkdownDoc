/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.4.2
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
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.file.Editables

import javax.swing.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.util.List

/**
 * This class is responsible for selecting one of the open editables for editing.
 */
@CompileStatic
@TypeChecked
class EditableSelectorPopup extends PopupWindow implements MouseListenersTrait {

    //
    // Private Members
    //

    /** Used to time mouse movement for a  mouse shake close. */
    private Date mouseTime = null

    /** Used in conjunction with mouse shake close. */
    private int mouseX = 0

    /** Used in conjunction with mouse shake close. */
    private int exitLevel = 0

    @SuppressWarnings("GroovyMissingReturnStatement") // IDEA is having a problem with Closure<Void> not returning anything.
    private Closure<Void> cancelCallback = { close() }

    private ColumnTopDownLeftRightLayout layout =
            new ColumnTopDownLeftRightLayout(
                    leftMargin: 20,
                    rightMargin: 30,
                    topMargin: 10,
                    bottomMargin: 40,
                    hgap: 20,
                    vgap: 4
            )

    private EditableFileButton moveToOnOpen = null

    private JPanel popupContentPane = null

    //
    // Properties
    //

    Editor editor
    void setEditor(final Editor editor) {
        this.editor = editor
        this.parentWindow = this.editor.GUI.windowFrame
        this.editor.addCancelCallback(cancelCallback)
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

        setLayout(new BorderLayout())
        final JScrollPane scrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        scrollPane.setAutoscrolls(true)
        scrollPane.setViewportBorder(null)
        scrollPane.setBackground(Color.BLACK)

        this.popupContentPane = new JPanel()
        this.popupContentPane.setAutoscrolls(true)
        this.popupContentPane.setBackground(Color.BLACK)
        this.popupContentPane.setForeground(Color.WHITE)
        this.popupContentPane.setLayout(layout)


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

        safeMakeRoundedRectangleShape()

        undecorated = true
        background = Color.BLACK

        popupContentPane.addMouseMotionListener(this)

        popupContentPane.addMouseListener(new CloseClickHandler())

        this.addKeyListener(new KeyHandler())
    }


    private class CloseClickHandler implements MouseListenersTrait {
        @Override
        void mouseClicked(final MouseEvent e) {
            close()
        }
    }

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

        this.layout.doLayout(this.popupContentPane, false)
    }

    /**
     * Updates the size of the window after being opened.
     */
    private void updatePopupSize() {
        final boolean fullScreen = isFullScreenWindow(this.editor.GUI.windowFrame)

        this.bounds = new Rectangle(
                this.parentWindow.x + 50,
                this.parentWindow.y + 80,
                this.parentWindow.width - 100,
                this.parentWindow.height - 160
        )

        moveMouse(new Point(this.moveToOnOpen.x + this.x + 20, this.moveToOnOpen.y + this.y + 10))
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
            final JFrame wf = this.editor.GUI.windowFrame
            moveMouse(new Point(wf.x + (wf.width / 2) as int, wf.y + (wf.height / 2) as int))
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
        PopupLock.instance.locked = false

        this.visible = false

        this.editor?.removeCancelCallback cancelCallback

        if (this.closer != null) {
            this.closer.call()
        }
    }

}
