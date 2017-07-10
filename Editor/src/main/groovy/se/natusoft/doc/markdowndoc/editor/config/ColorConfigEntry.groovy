/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.2
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
package se.natusoft.doc.markdowndoc.editor.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull

/**
 * This is a config entry holding a color valueComp making it available as
 * separate _red, green and blue values that can individually be changed.
 * <p/>
 * The config valueComp is stored as "_red:green:blue".
 */
@CompileStatic
@TypeChecked
class ColorConfigEntry extends ConfigEntry {
    //
    // Constructors
    //

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultRed The default _red color part.
     * @param defaultGreen The default green color part.
     * @param defaultBlue The default blue color part.
     * @param configGroup The config group this config belongs to.
     */
    ColorConfigEntry(
            @NotNull final String key,
            @NotNull final String description,
            final int defaultRed,
            final int defaultGreen,
            final int defaultBlue,
            @NotNull final String configGroup
    ) {
        super(key, description, toValue(defaultRed, defaultGreen, defaultBlue), configGroup)
    }

    //
    // Methods
    //

    /**
     * Converts _red, green, and blue int values to a config valueComp string.
     *
     * @param red The _red color part.
     * @param green The green color part.
     * @param blue The blue color part.
     */
    private static String toValue(final int red, final int green, final int blue) {
        "${red}:${green}:${blue}"
    }

    /**
     * Returns the integer valueComp of the color part specified for the current config valueComp.
     *
     * @param colorPart The color part to get.
     */
    private int valueToColor(final ColorPart colorPart) {
        Integer.valueOf(getValue().split(":")[colorPart.ordinal()])
    }

    /**
     * Returns the _red color part.
     */
    int getRed() {
        valueToColor(ColorPart.RED)
    }

    /**
     * Returns the green color part.
     */
    int getGreen() {
        valueToColor(ColorPart.GREEN)
    }

    /**
     * Returns the blue color part.
     */
    int getBlue() {
        valueToColor(ColorPart.BLUE)
    }

    /**
     * Sets the _red color part.
     *
     * @param red The _red valueComp to set.
     */
    void setRed(final int red) {
        setValue(toValue(red, getGreen(), getBlue()))
    }

    /**
     * Sets the green color part.
     * @param green The green valueComp to set.
     */
    void setGreen(final int green) {
        setValue(toValue(getRed(), green, getBlue()))
    }

    /**
     * Sets the blue color part.
     *
     * @param blue The blur valueComp to set.
     */
    void setBlue(final int blue) {
        setValue(toValue(getRed(), getGreen(), blue))
    }
}
