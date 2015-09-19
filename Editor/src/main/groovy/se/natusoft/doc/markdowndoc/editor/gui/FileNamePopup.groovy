package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry

import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.*

/**
 * Pops up an small window at the bottom of editor window showing a file name.
 */
@CompileStatic
@TypeChecked
class FileNamePopup implements GuiEnvToolsTrait, ColorsTrait {

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
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(PopupWindow.popupOpacityConfig, opacityChanged)
    }

    //
    // Properties
    //

    JFrame parent

    //
    // Private Members
    //

    private JWindow nameDisplayPopup = null

    private JLabel nameLabel = null

    private boolean showing = false

    //
    // Methods
    //

    /**
     * Applies the opacity.
     */
    protected void updateOpacity() {
        if (this.nameDisplayPopup != null) {
            safeOpacity = this.opacity
        }
    }

    /**
     * Sets the filename to display. This is called by displayName(...), but should also be called
     * to change the displayed name if isShowing() is true.
     * </p>
     * Put in other words: If isShowing() is true then do setFileName() otherwise do displayName().
     *
     * @param fileName The filename to set.
     */
    void setFileName(final String fileName) {
        if (this.nameLabel != null) {
            this.nameLabel.text = fileName

            this.nameDisplayPopup.validate()
            this.nameDisplayPopup.size = this.nameDisplayPopup.preferredSize

            final Container contentPane = this.parent.contentPane
            final int x = (parent.x + (parent.width / 2) - (this.nameDisplayPopup.width / 2)) as int
            final int y = (parent.y + contentPane.y + contentPane.height - this.nameDisplayPopup.height - 10) as int
            this.nameDisplayPopup.location = new Point(x, y as int)

        }
    }

    /**
     * Shows the filename popup.
     */
    synchronized void displayName(final String fileName) {
        if (this.nameDisplayPopup == null) {
            this.nameDisplayPopup = new JWindow(this.parent)
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

        setFileName(fileName)

        this.nameDisplayPopup.visible = true

        this.showing = true
    }

    /**
     * Hides the filename popup.
     */
    synchronized void hideName() {
        if (this.nameDisplayPopup != null) {
            this.nameDisplayPopup.visible = false
        }
        this.showing = false
    }

    /**
     * Returns true if the dialog is showing.
     */
    synchronized boolean isShowing() {
        return this.showing
    }
}
