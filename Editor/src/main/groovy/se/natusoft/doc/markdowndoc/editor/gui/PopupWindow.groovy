package se.natusoft.doc.markdowndoc.editor.gui

import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.OSTrait
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.IntegerConfigEntry

import javax.swing.JFrame
import java.awt.Color

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_TOOL

/**
 * Base class for popup window.
 */
class PopupWindow extends JFrame implements Configurable, GuiGoodiesTrait, Colors, OSTrait {

    //
    // Private Members
    //

    private float _popupOpacity = 1.0f

    private int _topMargin = 0

    private int _bottomMargin = 0

    //
    // Config
    //

    public static final IntegerConfigEntry popupOpacityConfig = new IntegerConfigEntry("editor.common.popup.opacity",
            "Popup opacity.", 75, 0, 100, CONFIG_GROUP_TOOL)

    // The -1 values means not set, in which case a default will be calculated depending on platform run on.

    public static final IntegerConfigEntry screenTopMargin =
            new IntegerConfigEntry("editor.common.popup.top.margin", "Top margin of popup windows.",
                    -1, -1, Integer.MAX_VALUE, CONFIG_GROUP_TOOL)

    public static final IntegerConfigEntry screenBottomMargin =
            new IntegerConfigEntry("editor.common.popup.bottom.margin", "Bottom margin of popup windows.",
                    -1, -1, Integer.MAX_VALUE, CONFIG_GROUP_TOOL)

    protected Closure popupOpacityChanged = { @NotNull final ConfigEntry ce ->
        final int ival = Integer.valueOf(ce.value)
        updateOpacity(((ival as float) / 100.0f) as float)
    }

    protected Closure popupWindowTopMarginConfigChanged = { @NotNull final ConfigEntry ce ->
        updateWindowTopMargin(Integer.valueOf(ce.value))
    }

    protected Closure popupWindowBottomMarginConfigChanged = { @NotNull final ConfigEntry ce ->
        updateWindowBottomMargin(Integer.valueOf(ce.value))
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.registerConfig(popupOpacityConfig, popupOpacityChanged)
        configProvider.registerConfig(screenTopMargin, popupWindowTopMarginConfigChanged)
        configProvider.registerConfig(screenBottomMargin, popupWindowBottomMarginConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(popupOpacityConfig, popupOpacityChanged)
        configProvider.unregisterConfig(screenTopMargin, popupWindowTopMarginConfigChanged)
        configProvider.unregisterConfig(screenBottomMargin, popupWindowBottomMarginConfigChanged)
    }

    //
    // Constructors
    //

    PopupWindow() {
        initGuiGoodies(this)
        undecorated = true
        background = Color.BLACK
        foreground = Color.WHITE
    }

    //
    // Methods
    //

    /**
     * Called when the opacity changes.
     *
     * @param opacity The new opacity.
     */
    protected void updateOpacity(final float opacity) {
        this._popupOpacity = opacity
        safeOpacity = opacity
    }

    /**
     * Called when the top margin changes.
     *
     * @param windowTopMargin The new top margin.
     */
    protected void updateWindowTopMargin(final int windowTopMargin) {
        this._topMargin = windowTopMargin
        if (this._topMargin == -1) {
            if (macOSXOS) {
                this._topMargin = 23
            }
            else if (linuxOS) {
                this._topMargin = 4
            }
            else {
                this._topMargin = 0
            }
        }
    }

    /**
     * Called when the new bottom margin changes.
     *
     * @param windowBottomMargin The new bottom margin.
     */
    protected void updateWindowBottomMargin(final int windowBottomMargin) {
        this._bottomMargin = windowBottomMargin
        if (this._bottomMargin == -1) {
            if (windowsOS) {
                this._bottomMargin = 70
            }
            else if (linuxOS) {
                this._bottomMargin = 40
            }
            else if (macOSXOS) {
                this._bottomMargin = 0
            }
            else {
                this._bottomMargin = 0
            }
        }
    }

    /**
     * This filters the actual content bottom margin, not the screen bottom margin.
     *
     * @param bottomMargin The original bottom margin.
     *
     * @return A possibly updated bottom margin.
     */
    protected int filterBottomMargin(int bottomMargin) {
        if (linuxOS) {
            bottomMargin += 20
        }
        else if (windowsOS) {
            bottomMargin += 10
        }

        return bottomMargin
    }

    /**
     * Returns the opacity for the popup window.
     */
    protected float getPopupOpacity() {
        return this._popupOpacity
    }

    protected void setPopupOpacity(final float popupOpacity) {
        this._popupOpacity = popupOpacity
    }

    /**
     * Returns the screen top margin, where the popup should start Y wise, counted from top.
     */
    protected int getWindowTopMargin() {
        return this._topMargin
    }

    protected void setWindowTopMargin(final int windowTopMargin) {
        this._topMargin = windowTopMargin
    }

    /**
     * Returns the screen bottom margin, where the popup should end Y wise, counted from bottom.
     */
    protected int getWindowBottomMargin() {
        return this._bottomMargin
    }

    protected void setWindowBottomMargin(final int windowBottomMargin) {
        this._bottomMargin = windowBottomMargin
    }
}
