/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.3.5
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
        return Integer.valueOf(rgb[0])
    }

    public static final int getGreen(String color) {
        String[] rgb = color.split(":")
        return Integer.valueOf(rgb[1])
    }

    public static final int getBlue(String color) {
        String[] rgb = color.split(":")
        return Integer.valueOf(rgb[2])
    }
}
