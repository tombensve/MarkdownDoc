/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.0.2
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
 *     tommy ()
 *         Changes:
 *         2015-07-15: Created!
 *
 */
package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.jetbrains.annotations.Nullable

/**
 * This represents a font.
 */
@CompileStatic
@TypeChecked
@EqualsAndHashCode
@ToString(includeNames = true)
class MSSFont {

    //
    // Properties
    //

    String family = null
    int size = -1
    MSSFontStyle style = null
    boolean underlined = false

    //
    // Methods
    //

    void updateFamilyIfNotSet(@Nullable final String family) {
        if (this.family == null) this.family = family
    }

    void updateSizeIfNotSet(final int size) {
        if (this.size == -1) this.size = size
    }

    void updateStyleIfNotSet(@Nullable final String styles) {
        if (this.style == null) {
            styles.split(",").each { String strStyle ->
                MSSFontStyle fontStyle = MSSFontStyle.valueOf(strStyle)
                if (fontStyle == null) { throw new MSSException(message: "'${strStyle}' is not a valid font style!") }
                if (fontStyle == MSSFontStyle.UNDERLINE) {
                    this.underlined = true
                }
                else {
                    this.style = fontStyle
                }
            }
        }
    }


}
