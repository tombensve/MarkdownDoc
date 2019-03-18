/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.1.1
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
 *         2016-07-29: Created!
 *
 */
package se.natusoft.doc.markdown.generator.pdfbox

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.generator.styles.MSS
import se.natusoft.doc.markdown.generator.styles.MSS.MSS_Pages
import se.natusoft.doc.markdown.generator.utils.Sectionizer

/**
 * This just holds the page margins.
 */
@CompileStatic
@TypeChecked
class PageMargins {

    /** For margins. */
    MSS mss

    /** Insets for indenting on both left and right sides. */
    float leftInset = 0, rightInset = 0

    private float leftMarginTemp = 0, rightMarginTemp = 0


    float getTopMargin()  {
        mss.getTopMarginForDocument( Sectionizer.section as MSS_Pages)
    }

    float getBottomMargin() {
        mss.getBottomMarginForDocument( Sectionizer.section  as MSS_Pages)
    }

    /**
     * Overrides default left margin getter to also add left inset.
     */
    float getLeftMargin() {
        this.leftMarginTemp == 0 ? mss.getLeftMarginForDocument( Sectionizer.section  as MSS_Pages) + leftInset
                : this.leftMarginTemp
    }

    /**
     * Temporarily overrides margin.
     *
     * @param leftMargin The margin to override with.
     */
    void setLeftMargin(float leftMargin) {
        this.leftMarginTemp = leftMargin
    }

    /**
     * Overrides default right margin getter to also add right inset.
     */
    float getRightMargin() {
        this.rightMarginTemp == 0 ? mss.getRightMarginForDocument( Sectionizer.section  as MSS_Pages) + rightInset
                : this.rightMarginTemp
    }

    /**
     * Temporarily overrides margin.
     *
     * @param leftMargin The margin to override with.
     */
    void setRightMargin(float rightMargin) {
        this.rightMarginTemp = rightMargin
    }

    /**
     * Clears both overridden margins.
     */
    void clearTemps() {
        this.leftMarginTemp = 0
        this.rightMarginTemp = 0
    }
}
