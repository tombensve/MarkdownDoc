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
package se.natusoft.doc.markdown.generator.pdfgenerator

import com.itextpdf.text.BaseColor
import groovy.transform.CompileStatic

/**
 * This extends BaseColor and parses configured colors.
 */
@CompileStatic
class PDFColor extends BaseColor {

    public PDFColor(String color) {
        super(getRed(color), getGreen(color), getBlue(color))
    }

    public static final int getRed(String color) {
        String[] rgb = color.split(":")
        return Integer.valueOf(handleHex(rgb[0]))
    }

    public static final int getGreen(String color) {
        String[] rgb = color.split(":")
        return Integer.valueOf(handleHex(rgb[1]))
    }

    public static final int getBlue(String color) {
        String[] rgb = color.split(":")
        return Integer.valueOf(handleHex(rgb[2]))
    }

    private static String handleHex(String color) {
        if (color.trim().length() == 2 && color.matches("[a-f,A-F,0-9][a-f,A-F,0-9]")) {
            char d1 = color.trim().charAt(0)
            char d2 = color.trim().charAt(1)
            int value = (hexValue(d1) * 16) + hexValue(d2)
            color = "" + value
        }

        return color
    }

    private static int hexValue(char c) {
        int value
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
            case 'A':
            case 'a':
                value = 10
                break
            case 'B':
            case 'b':
                value = 11
                break
            case 'C':
            case 'c':
                value = 12
                break
            case 'D':
            case 'd':
                value = 13
                break
            case 'E':
            case 'e':
                value = 14
                break
            case 'F':
            case 'f':
                value = 15
        }

        return value
    }
}
