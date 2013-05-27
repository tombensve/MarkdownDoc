/* 
 * 
 * PROJECT
 *     Name
 *         Editor
 *     
 *     Code Version
 *         1.2.6
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
 *         2013-05-27: Created!
 *         
 */
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
