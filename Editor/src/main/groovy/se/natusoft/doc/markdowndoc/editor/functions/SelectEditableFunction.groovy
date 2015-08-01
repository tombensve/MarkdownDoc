package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.gui.EditableSelectorPopup

import javax.swing.*
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener

/**
 * This function pops up a list of all loaded editables and lets the user select one
 * to edit.
 */
@CompileStatic
@TypeChecked
class SelectEditableFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    private Editor editor

    private MouseMotionListener mouseMotionListener = null

    private EditableSelectorPopup popup = null

    private ConfigProvider configProvider = null

    //
    // Configs
    //

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        this.configProvider = configProvider
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        if (this.popup != null) {
            this.popup.unregisterConfigs(configProvider)
        }
    }

    //
    // Methods
    //

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    @Override
    String getGroup() {
        ToolBarGroups.FILE.name()
    }

    /**
     * Returns the name of the function.
     */
    @Override
    String getName() {
        "Select file to work with"
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    JComponent getToolBarButton() {
        null
    }

    /**
     * Returns the keyboard shortcut for triggering the function via keyboard.
     */
    @Override
    KeyboardKey getKeyboardShortcut() {
        new KeyboardKey("Ctrl+W")
    }

    /**
     * Performs the function.
     *
     * @throws FunctionException
     */
    @Override
    void perform() throws FunctionException {
        this.popup = new EditableSelectorPopup(editor: this.editor, closer: { close() } )
        this.popup.registerConfigs(this.configProvider)
        this.configProvider.refreshConfigs()
        this.popup.showWindow()
    }

    /**
     * Sets the editorPane for the component to use.
     *
     * @param editor The editorPane to set.
     */
    @Override
    void setEditor(@NotNull final Editor editor) {
        this.editor = editor

        this.mouseMotionListener = new MouseMotionListener() {
            @Override
            void mouseDragged(MouseEvent e) {}

            @Override
            void mouseMoved(MouseEvent e) {
                mouseMovedHandler(e)
            }
        }
        this.editor.addMouseMotionListener(this.mouseMotionListener)
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    void mouseMovedHandler(final MouseEvent e) {

        if (e.y > (this.editor.getGUI().editorVisibleY + 100) && e.y < (this.editor.height - 100)
                && this.popup == null) {

            if (e.x >= 0 && e.x <= 30) {
                perform()
            }
        }
    }

    /**
     * Called when instance is no longer needed.
     */
    @Override
    void close() {
        if (this.popup != null) {
            this.popup.visible = false
            this.popup.unregisterConfigs(this.configProvider)
            this.popup = null
        }
    }

}
