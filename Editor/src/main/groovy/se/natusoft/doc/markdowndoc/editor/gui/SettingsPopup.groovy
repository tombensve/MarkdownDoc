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
 *         2014-02-01: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.OSTrait
import se.natusoft.doc.markdowndoc.editor.adapters.WindowListenerAdapter
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry

import javax.swing.border.EmptyBorder
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.WindowListener
import java.util.List
import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowEvent

/**
 * The settings window.
 */
@CompileStatic
class SettingsPopup extends PopupWindow implements OSTrait {
    //
    // Private Members
    //

    /** The tabbed pane containing the group panels. */
    private JPanel groupPane = null

    /** The group panels for easy lookup using group name. */
    private Map<String, List<JComponent>> groupPanels = new HashMap<>()

    // This closure have to be defined at this level to be able to call the setWindowsVisibility() method.
    // If it is defined within a method it has access to method details, but not other class methods.
    @SuppressWarnings("GroovyMissingReturnStatement")
    private Closure<Void> closeWindow = {
        setWindowVisibility(false)
    }

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

    @NotNull Closure<Void> saveSettingsProvider

    @NotNull Closure<Void> cancelSettingsProvider

    @NotNull boolean fullScreenMode

    //
    // Methods
    //

    private void saveSettings() {
        this.saveSettingsProvider?.call()
    }

    private void cancelSettings() {
        this.cancelSettingsProvider?.call()
    }

    /**
     * Called when the top margin changes.
     *
     * @param _windowTopMargin The new top margin.
     */
    @Override
    protected void updateWindowTopMargin(final int _windowTopMargin) {
        super.updateWindowTopMargin(_windowTopMargin)
        updateBounds()
    }

    /**
     * Called when the new bottom margin changes.
     *
     * @param _windowBottomMargin The new bottom margin.
     */
    @Override
    protected void updateWindowBottomMargin(final int _windowBottomMargin) {
        super.updateWindowBottomMargin(_windowBottomMargin)
        updateBounds()
    }


    private void setupWindow() {
        super.title = "MarkdownDoc Editor Settings"

        layout = new BorderLayout()

        addWindowListener(new WindowListenerAdapter() {
            @Override
            void windowClosing(final WindowEvent ignored) {
                cancelSettings()
            }
        })

        this.parentWindow.addComponentListener(this.parentMovedListener)

        this.groupPane = new JPanel(this.contentLayout)
        this.groupPane.border = null
        updateColors(this.groupPane)

        final JScrollPane scrollPane = new JScrollPane(
                this.groupPane,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        scrollPane.border = new EmptyBorder(0, 0, 0, 0)
        scrollPane.viewportBorder = new EmptyBorder(0, 0, 0, 0)
        add(scrollPane, BorderLayout.CENTER)

        final JPanel buttons = new JPanel(new FlowLayout())
        updateColors(buttons)

        final JButton saveButton = new JButton(text: "Save")
        //updateColors(saveButton)
        saveButton.addActionListener { final ActionEvent ignored ->
                saveSettings()
                fadeOutWindow(closeWindow)
        }

        buttons.add(saveButton)

        final JButton cancelButton = new JButton(text: "Cancel")
        //updateColors(cancelButton)
        cancelButton.addActionListener { final ActionEvent ignored ->
                cancelSettings()
                fadeOutWindow(closeWindow)
        }

        buttons.add(cancelButton)

        add(buttons, BorderLayout.SOUTH)

        safeMakeRoundedRectangleShape()
    }

    void updateBounds() {
        int width = ((int)minimumSize.width + 125) * 2
        if (fullScreenMode) {
            this.bounds = new Rectangle(
                    this.parentWindow.x + this.parentWindow.width - width,
                    this.parentWindow.y,
                    width,
                    this.parentWindow.height
            )
        }
        else {
            this.bounds = new Rectangle (
                    this.parentWindow.x + this.parentWindow.width,
                    this.parentWindow.y,
                    width,
                    this.parentWindow.height
            )
        }
    }

    void setWindowVisibility(final boolean state) {
        if (state) {
            PopupLock.instance.locked = true

            setupWindow()

            // Add config components to window.
            if (this.groupPane.componentCount == 0) {
                this.groupPanels.keySet().each { final String key ->
                    final List<JComponent> comps = this.groupPanels[key]
                    comps.each { final JComponent comp ->
                        this.groupPane.add(comp)
                    }
                }
            }

            safeOpacity = 0.0f
            visible = true
            updateBounds()
            moveMouse(new Point((this.bounds.x + 20) as int, (this.bounds.y + 20) as int))

            fadeInWindow(this.popupOpacity)
        }
        else {
            PopupLock.instance.locked = false
            PopupLock.instance.transferLock = false
            visible = false
            this.parentWindow.removeComponentListener(this.parentMovedListener)
        }
    }

    /**
     * Adds a configuration to the settings window.
     *
     * @param configEntry The config entry to add.
     */
    void addConfig(@NotNull final ConfigEntry configEntry) {
        List<JComponent> groupList = this.groupPanels.get(configEntry.getConfigGroup())
        if (groupList == null) {
            groupList = new LinkedList<JComponent>()

            final JLabel groupTitle = new JLabel("---===:{ ${configEntry.configGroup} }:===---",
                    JLabel.CENTER)
            groupTitle.font = groupTitle.font.deriveFont(Font.BOLD)
            updateColors(groupTitle)
            groupList.add(groupTitle)

            this.groupPanels.put(configEntry.configGroup, groupList)
        }

        final ConfigValueEditor configEditPanel = new ConfigValueEditor(configEntry, this)
        updateColors(configEditPanel)
        groupList.add(configEditPanel)
    }

}
