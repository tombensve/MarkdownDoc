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
 *     tommy ()
 *         Changes:
 *         2015-08-03: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.OSTrait
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.IntegerConfigEntry

import javax.swing.JFrame
import java.awt.Color
import java.awt.Window

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_TOOL

/**
 * Base class for popup window.
 */
@CompileStatic
class PopupWindow extends JFrame implements Configurable, GuiEnvToolsTrait, ColorsTrait, OSTrait {

    //
    // Private Members
    //

    private float _popupOpacity = 1.0f

    private int _topMargin = 0

    private int _bottomMargin = 0

    //
    // Properties
    //

    /**
     * The parent of the popup window. This is needed to determine popup position and size.
     * <p/>
     * Note that this is only stored here, it is subclasses that makes use of it.
     */
    Window parentWindow

    //
    // Config
    //

    public static final IntegerConfigEntry popupOpacityConfig = new IntegerConfigEntry("editor.common.popup.opacity",
            "Popup opacity.", 75, 0, 100, CONFIG_GROUP_TOOL)

    // The -1 values means not set, in which case a default will be calculated depending on platform run on.

//    public static final IntegerConfigEntry screenTopMargin =
//            new IntegerConfigEntry("editor.common.popup.top.margin", "Top margin of popup windows.",
//                    -1, -1, Integer.MAX_VALUE, CONFIG_GROUP_TOOL)
//
//    public static final IntegerConfigEntry screenBottomMargin =
//            new IntegerConfigEntry("editor.common.popup.bottom.margin", "Bottom margin of popup windows.",
//                    -1, -1, Integer.MAX_VALUE, CONFIG_GROUP_TOOL)

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
//        configProvider.registerConfig(screenTopMargin, popupWindowTopMarginConfigChanged)
//        configProvider.registerConfig(screenBottomMargin, popupWindowBottomMarginConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(popupOpacityConfig, popupOpacityChanged)
//        configProvider.unregisterConfig(screenTopMargin, popupWindowTopMarginConfigChanged)
//        configProvider.unregisterConfig(screenBottomMargin, popupWindowBottomMarginConfigChanged)
    }

    //
    // Constructors
    //

    PopupWindow() {
        initGuiEnvTools(this)
        undecorated = true
        background = Color.BLACK
        foreground = Color.WHITE
        autoRequestFocus = true
        if (alwaysOnTopSupported) {
            alwaysOnTop = true
        }
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

    // NOTE: Even though we can get screen bounds, we have no way of determining actually usable space
    //       on screen. This depends on what the desktop reserves at top or bottom. Thereby I'm forced
    //       to check which OS is we are run on to make adaptations. This gets difficult on both Linux
    //       and windows since Linux have a number of different desktops, and Windows differs in space
    //       used at bottom between pre windows 8 and windows 8. I have partly gone around this problem
    //       by making the top and bottom "margins" (for the lack of a better name) configurable in
    //       settings.
    //
    //       Maybe the use of full height popups is a bad idea.

    // NOTE: If the last 2 words of the above comment are shown in strong bright orange with red text,
    //       then you are using IDEA! Just ignore it. I have found no way to disable this horrible
    //       feature of not being able to use the word made of of b+a+d without being slapped in the
    //       face with something bright orange!

    /**
     * Called when the top margin changes.
     *
     * @param windowTopMargin The new top margin.
     */
    protected void updateWindowTopMargin(final int windowTopMargin) {
        this._topMargin = windowTopMargin
    }

    /**
     * Called when the new bottom margin changes.
     *
     * @param windowBottomMargin The new bottom margin.
     */
    @SuppressWarnings("GroovyIfStatementWithIdenticalBranches")
    protected void updateWindowBottomMargin(final int windowBottomMargin) {
        this._bottomMargin = windowBottomMargin
    }

    /**
     * Returns the opacity for the popup window.
     */
    protected float getPopupOpacity() {
        return this._popupOpacity
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    protected void setPopupOpacity(final float popupOpacity) {
        this._popupOpacity = popupOpacity
    }

    /**
     * Returns the screen top margin, where the popup should start Y wise, counted from top.
     */
    protected int getWindowTopMargin() {
        return this._topMargin
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    protected void setWindowTopMargin(final int windowTopMargin) {
        this._topMargin = windowTopMargin
    }

    /**
     * Returns the screen bottom margin, where the popup should end Y wise, counted from bottom.
     */
    protected int getWindowBottomMargin() {
        return this._bottomMargin
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    protected void setWindowBottomMargin(final int windowBottomMargin) {
        this._bottomMargin = windowBottomMargin
    }
}
