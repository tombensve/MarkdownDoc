/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Code Version
 *         1.3.9
 *
 *     Description
 *         Parses markdown and generates HTML and PDF.
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
 *         2012-11-16: Created!
 *
 */
package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

/**
 * This parses configured colors.
 */
@CompileStatic
@TypeChecked
@EqualsAndHashCode
@ToString
class MSSColor {
    //
    // Constants
    //

    static final MSSColor BLACK = new MSSColor(color: "0:0:0")
    static final MSSColor WHITE = new MSSColor(color: "255:255:255")
    static final MSSColor GREY = new MSSColor(color: "128:128:128")

    //
    // Properties
    //

    int red, green, blue

    void setColor(String color) {
        if (color[0] == "#") color = color[1..6]
        this.red = getRed(color)
        this.green = getGreen(color)
        this.blue = getBlue(color)
    }

    //
    // Static Methods
    //

    static final int getRed(String color) {
        if (color.contains(':')) {
            String[] rgb = color.split(":")
            Integer.valueOf(handleHex(rgb[0]))
        }
        else {
            Integer.valueOf(handleHex(color[0..1]))
        }
    }

    static final int getGreen(String color) {
        if (color.contains(':')) {
            String[] rgb = color.split(":")
            Integer.valueOf(handleHex(rgb[1]))
        }
        else {
            Integer.valueOf(handleHex(color[2..3]))
        }
    }

    static final int getBlue(String color) {
        if (color.contains(':')) {
            String[] rgb = color.split(":")
            Integer.valueOf(handleHex(rgb[2]))
        }
        else {
            Integer.valueOf(handleHex(color[4..5]))
        }
    }

    static String handleHex(String color) {
        color = color.trim().toLowerCase()
        if (color.length() == 2 && color.matches("[a-f,0-9][a-f,0-9]")) {
            char d1 = color.charAt(0)
            char d2 = color.charAt(1)
            int value = (hexValue(d1) * 16) + hexValue(d2)
            color = "" + value
        }

        color
    }

    private static int hexValue(char c) {
        int value = 0
        switch (c) {
            case '0':
                value = 0
                break
            case '1':
                value = 1
                break
            case '2':
                value = 2
                break
            case '3':
                value = 3
                break
            case '4':
                value = 4
                break
            case '5':
                value = 5
                break
            case '6':
                value = 6
                break
            case '7':
                value = 7
                break
            case '8':
                value = 8
                break
            case '9':
                value = 9
                break
            case 'a':
                value = 10
                break
            case 'b':
                value = 11
                break
            case 'c':
                value = 12
                break
            case 'd':
                value = 13
                break
            case 'e':
                value = 14
                break
            case 'f':
                value = 15
        }

        value
    }
}
