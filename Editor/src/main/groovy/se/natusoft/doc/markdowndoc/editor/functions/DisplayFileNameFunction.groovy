package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.gui.ColorsTrait
import se.natusoft.doc.markdowndoc.editor.gui.GuiEnvToolsTrait
import se.natusoft.doc.markdowndoc.editor.gui.PopupLock
import se.natusoft.doc.markdowndoc.editor.gui.PopupWindow

import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener

/**
 * This function displays the name of the currently edited file at the same time as the toolbar is visible.
 */
@CompileStatic
@TypeChecked
class DisplayFileNameFunction implements EditorFunction, Configurable, GuiEnvToolsTrait, ColorsTrait {

    //
    // Config
    //

    private float opacity = 1.0f

    private Closure opacityChanged = { final ConfigEntry ce ->
        final int ival = Integer.valueOf(ce.value)
        this.opacity = (ival / 100) as float
        updateOpacity()
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        // We reuse the PopupWindows opacity config constant so that we do not get multiple configs for opacity in
        // the settings.
        configProvider.registerConfig(PopupWindow.popupOpacityConfig, opacityChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(PopupWindow.popupOpacityConfig, opacityChanged)
    }

    //
    // Private Members
    //

    private MouseMotionListener mouseMotionListener

    private JWindow nameDisplayPopup = null

    private JLabel nameLabel = null

    //
    // Properties
    //

    Editor editor

    //
    // Methods
    //

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    @Override
    String getGroup() {
        return null
    }

    /**
     * Returns the name of the function.
     */
    @Override
    String getName() {
        return "DisplayFileName"
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    JComponent getToolBarButton() {
        return null
    }

    /**
     * Returns the keyboard shortcut for triggering the function via keyboard.
     */
    @Override
    KeyboardKey getKeyboardShortcut() {
        return null
    }

    /**
     * Performs the function.
     *
     * @throws FunctionException
     */
    @Override
    void perform() throws FunctionException {

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
            void mouseDragged(final MouseEvent e) {}

            @Override
            void mouseMoved(final MouseEvent e) {
                mouseMovedHandler(e)
            }
        }
        this.editor.addMouseMotionListener(this.mouseMotionListener)

    }

    /**
     * Called when instance is no longer needed.
     */
    @Override
    void close() {
        this.editor.removeMouseMotionListener(this.mouseMotionListener)
    }

    protected void mouseMovedHandler(@NotNull final MouseEvent e) {
        if (this.editor == null) return
        final int y = e.y - this.editor.GUI.editorVisibleY
        if (y <= this.editor.topMargin && e.x >= 0 && e.x <= this.editor.width) {
            if (!PopupLock.instance.locked) {
                displayName()
            }
        }
        else {
            hideName()
        }
    }

    protected void updateOpacity() {
        if (this.nameDisplayPopup != null) {
            safeOpacity = this.opacity
        }
    }

    public void setFileName(final String fileName) {
        if (this.nameLabel != null) {
            this.nameLabel.text = fileName
            this.nameDisplayPopup.validate()
            this.nameDisplayPopup.size = this.nameDisplayPopup.preferredSize
        }
    }

    protected void displayName() {
        if (this.nameDisplayPopup == null) {
            this.nameDisplayPopup = new JWindow(this.editor.GUI.windowFrame)
            initGuiEnvTools(this.nameDisplayPopup)
            updateOpacity()
            safeMakeRoundedRectangleShape()

            updateColors(this.nameDisplayPopup)

            this.nameDisplayPopup.layout = new BorderLayout()

            final JPanel panel = new JPanel()
            updateColors(panel)
            this.nameDisplayPopup.add(panel, BorderLayout.CENTER)
            panel.layout = new BorderLayout()

            panel.setBorder(new EmptyBorder(5, 5, 5, 5))

            this.nameLabel = new JLabel()
            updateColors(this.nameLabel)
            this.nameLabel.font = this.nameLabel.font.deriveFont(Font.BOLD)
            this.nameLabel.font = this.nameLabel.font.deriveFont(40.0f)

            panel.add(this.nameLabel, BorderLayout.CENTER)
        }

        fileName = this.editor.editable.file.name

        final JFrame parent = this.editor.GUI.windowFrame
        final Container contentPane = parent.contentPane
        final int x = (parent.x + (parent.width / 2) - (this.nameDisplayPopup.width / 2)) as int
        final int y = (parent.y + contentPane.y + contentPane.height - this.nameDisplayPopup.height - 10) as int

        this.nameDisplayPopup.location = new Point(x, y)
        this.nameDisplayPopup.visible = true
    }


    protected void hideName() {
        if (this.nameDisplayPopup != null) {
            this.nameDisplayPopup.visible = false
        }
    }

}
