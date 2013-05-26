package se.natusoft.doc.markdowndoc.editor.config;

import java.awt.*;

/**
 * This takes an ConfigEntry and produces a color from that config.
 */
public class ConfigColor extends Color {
    //
    // Constructors
    //

    /**
     * Creates a new ConfigColor instance.
     *
     * @param configEntry The config entry containing the color specification. Its value
     *                    must be in the format "r:g:b".
     */
    public ConfigColor(ConfigEntry configEntry) {
        super(getRed(configEntry.getValue()), getGreen(configEntry.getValue()), getBlue(configEntry.getValue()));
    }

    //
    // Methods
    //

    /**
     * Returns the integer value of the color part specified for the current config value.
     *
     * @param colorPart The color part to get.
     */
    private static int toColor(String colorStr, ColorPart colorPart) {
        return Integer.valueOf(colorStr.split(":")[colorPart.ordinal()]);
    }

    /**
     * Returns the red part of the color spec.
     *
     * @param colorStr The complete config color spec.
     */
    private static final int getRed(String colorStr) {
        return toColor(colorStr, ColorPart.RED);
    }

    /**
     * Returns the green part of the color spec.
     *
     * @param colorStr The complete config color spec.
     */
    private static final int getGreen(String colorStr) {
        return toColor(colorStr, ColorPart.GREEN);
    }

    /**
     * Returns the blue part of the color spec.
     *
     * @param colorStr The complete config color spec.
     */
    private static final int getBlue(String colorStr) {
        return toColor(colorStr, ColorPart.BLUE);
    }
}
