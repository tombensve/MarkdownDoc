/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-02-15: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.api.ToolBar
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.util.*
import java.util.List

/**
 * A toolbar that will open a java.awt.Window for each toolbar button. It will open
 * when the mouse is at the top of the editorPane window and close if it leaves that area.
 */
@CompileStatic
class SinglePopupToolbar implements GuiEnvToolsTrait, ToolBar, Configurable {

    //
    // Private Members
    //

    private Editor editor

    private List<String> toolBarGroups = new LinkedList<>()

    private Map<String, List<EditorFunction>> functions = new HashMap<>()

    private JWindow toolBarWindow = null

    private boolean open = false

    private int calculatedWidth = 0

    private MouseMotionListener mouseMotionListener = null

    private float toolbarOpacity = 1.0f

    //
    // Config
    //

    private Closure opacityChanged = { final ConfigEntry ce ->
        final int ival = Integer.valueOf(ce.value)
        this.toolbarOpacity = (((float)ival) / 100.0f) as float
        safeOpacity = this.toolbarOpacity
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        // We reuse the PopupWindow configuration for opacity.
        configProvider.registerConfig(PopupWindow.popupOpacityConfig, opacityChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        // We reuse the PopupWindow configuration for opacity.
        configProvider.unregisterConfig(PopupWindow.popupOpacityConfig, opacityChanged)
    }

    //
    // Constructors
    //

    /**
     * Creates a new MultiPopupToolbar.
     */
    SinglePopupToolbar() {}

    //
    // Methods
    //

    protected void mouseMovedHandler(@NotNull final MouseEvent e) {
        if (this.editor == null) return

        final int y = e.y - this.editor.GUI.editorVisibleY
        if (y <= topMargin && e.x >= 0 && e.x <= editorWidth) {
            if (!isOpen() && !PopupLock.instance.locked) {
                final int toolbarWidth = calculateWidth()
                final int x = (int)(parentFrame.x + (parentFrame.width / 2) - (toolbarWidth / 2))

                final int titleBarHeight =
                        (int)(parentFrame.bounds.height - parentFrame.contentPane.bounds.height)
                open(parentFrame, x, parentFrame.y + titleBarHeight + 2)
            }
        }
        else {
            if (isOpen()) {
                close()
            }
        }
    }

    /**
     * Provides the toolbar with the editorPane it is associated.
     *
     * @param editor The associated editorPane provided.
     */
    void attach(@NotNull final Editor editor) {
        this.editor = editor

        this.mouseMotionListener = new MouseMotionListener() {
            @Override
            void mouseDragged(final MouseEvent e) {}

            @Override
            void mouseMoved(final MouseEvent e) {
                mouseMovedHandler(e)
            }
        }
        this.editor.addMouseMotionListener(this.mouseMotionListener)
    }

    /**
     * Removes the association with the editorPane.
     *
     * @param editor The editorPane to detach from.
     */
    @Override
    void detach(@NotNull final Editor editor) {
        close()
        editor.removeMouseMotionListener(this.mouseMotionListener)
        this.editor = null
        this.toolBarWindow = null
    }


    /**
     * Convenience method to get information from associated editorPane.
     */
    private @NotNull JFrame getParentFrame() {
        this.editor.getGUI().getWindowFrame()
    }

    /**
     * Convenience method to get information from associated editorPane.
     */
    private int getTopMargin() {
        this.editor.getTopMargin()
    }

    /**
     *
     * Convenience method to get information from associated editorPane.
     */
    private int getEditorWidth() {
        this.editor.getWidth()
    }

    /**
     * Opens the toolbar.
     *
     * @param parent The parent window.
     * @param x The X coordinate to open at.
     * @param y The Y coordinate to open at.
     */
    private void open(@NotNull final JFrame parent, final int x, final int y) {
        PopupLock.instance.locked = true

        final boolean create = (this.toolBarWindow == null)

        if (create) {
            this.toolBarWindow = new JWindow(parent)
            initGuiEnvTools(this.toolBarWindow)
            safeOpacity = this.toolbarOpacity
            safeMakeRoundedRectangleShape()

            this.toolBarWindow.setBackground(Color.BLACK)

            this.toolBarWindow.setLayout(new BorderLayout())

            final JPanel panel = new JPanel()
            panel.setBackground(Color.BLACK)
            this.toolBarWindow.add(panel, BorderLayout.CENTER)
            panel.layout = new FlowLayout(FlowLayout.LEFT)
            boolean separator = false
            this.toolBarGroups.each { final String group ->
                if (separator) {
                    panel.add(new JToolBar.Separator())
                }
                this.functions.get(group)?.each { final EditorFunction editorFunction ->
                    final JComponent toolbarButton = editorFunction.getToolBarButton()
                    panel.add(toolbarButton)
                }
                separator = true
            }
            this.toolBarWindow.validate()
            this.toolBarWindow.size = this.toolBarWindow.preferredSize
            this.calculatedWidth = this.toolBarWindow.width
        }

        this.toolBarWindow.location = new Point(x, y)
        this.toolBarWindow.visible = true

        this.open = true
    }

    /**
     * Closes the toolbar.
     */
    private void close() {
        PopupLock.instance.locked = false

        if (this.toolBarWindow != null) {
            this.toolBarWindow.setVisible(false)
        }
        this.open = false
    }

    /**
     * Calculates an estimated width until a real width is provided. When a real width is available it is returned instead.
     */
    private int calculateWidth() {
        int width = this.calculatedWidth
        if (width == 0) {
            this.toolBarGroups.each { final String group ->
                this.functions.get(group)?.each { final EditorFunction editorFunction ->
                    width = (width + editorFunction.getToolBarButton().getPreferredSize().width) as int
                }
                width = (width + 10) as int
            }
        }

        width
    }

    /**
     * @return true if toolbar is open.
     */
    boolean isOpen() {
        this.open
    }

    /**
     * Adds a function to the toolbar.
     *
     * @param function The function to add.
     */
    void addFunction(@NotNull final EditorFunction function) {
        if (!this.toolBarGroups.contains(function.getGroup())) {
            this.toolBarGroups.add(function.getGroup())
        }

        List<EditorFunction> groupFunctions = this.functions.get(function.getGroup())
        if (groupFunctions == null) {
            groupFunctions = new LinkedList<>()
            this.functions.put(function.getGroup(), groupFunctions)
        }

        groupFunctions.add(function)
    }

    /**
     * Creates the content of the toolbar. This cannot be done until all
     * functions have been provided.
     */
    @Override
    void createToolBarContent() {
        // This is done by open() and thus nothing needs to be done here.
    }

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to enable.
     */
    void disableGroup(@NotNull final String group) {
        final List<EditorFunction> functions = this.functions.get(group)
        if (functions != null) {
            functions.each { final EditorFunction function ->
                function.getToolBarButton().setEnabled(false)
            }
        }
        else {
            throw new RuntimeException("Cannot disable non existent group '" + group + "'!")
        }
    }

    /**
     * Disables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to disable.
     */
    void enableGroup(@NotNull final String group) {
        final List<EditorFunction> functions = this.functions.get(group)
        if (functions != null) {
            functions.each { final EditorFunction function ->
                function.getToolBarButton().setEnabled(true)
            }
        }
        else {
            throw new RuntimeException("Cannot enable non existent group '" + group + "'!")
        }
    }

}
