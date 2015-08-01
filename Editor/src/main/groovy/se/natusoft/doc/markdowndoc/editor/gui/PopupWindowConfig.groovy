package se.natusoft.doc.markdowndoc.editor.gui

import se.natusoft.doc.markdowndoc.editor.config.IntegerConfigEntry

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_TOOL

/**
 * Common configs for popup windows.
 */
class PopupWindowConfig {

    public static final IntegerConfigEntry popupOpacityConfig = new IntegerConfigEntry("editor.common.popup.opacity",
            "Popup opacity.", 75, 0, 100, CONFIG_GROUP_TOOL)

    // The -1 values means not set, in which case a default will be calculated depending on platform run on.

    public static final IntegerConfigEntry screenTopMargin =
            new IntegerConfigEntry("editor.common.popup.top.margin", "Top margin of popup windows.",
                    -1, -1, Integer.MAX_VALUE, CONFIG_GROUP_TOOL)

    public static final IntegerConfigEntry screenBottomMargin =
            new IntegerConfigEntry("editor.common.popup.bottom.margin", "Bottom margin of popup windows.",
                    -1, -1, Integer.MAX_VALUE, CONFIG_GROUP_TOOL)

}
