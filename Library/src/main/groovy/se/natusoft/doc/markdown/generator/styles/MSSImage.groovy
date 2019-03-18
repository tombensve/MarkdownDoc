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
 *         2015-07-15: Created!
 *
 */
package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.jetbrains.annotations.Nullable
import se.natusoft.json.JSONBoolean
import se.natusoft.json.JSONNumber
import se.natusoft.json.JSONString

/**
 * This holds image information.
 */
@CompileStatic
@TypeChecked
@EqualsAndHashCode
@ToString(includeNames = true)
class MSSImage {
    @CompileStatic
    static enum Align {
        LEFT,
        MIDDLE,
        RIGHT,
        CURRENT
    }

    //
    // Properties
    //

    /** The percent to scale images. */
    Float scalePercent

    /** The alignment of images. */
    Align align

    /** The degrees to rotate image. */
    Float rotateDegrees

    /** If true then text will flow around image. If false text will continue below image. */
    Boolean imgFlow

    /** The margin to use between image and text when imgFlow is true. */
    Float imgFlowMargin

    /** Overridden X coordinate of image. */
    Float imgX

    /** Overridden Y coordinate of image. */
    Float imgY

    //
    // Methods
    //

    void updateScaleIfNotSet(@Nullable final JSONNumber scale) {
        if (scale != null && this.scalePercent == null) {
            this.scalePercent = scale.toFloat()
        }
    }

    void updateAlignIfNotSet(@Nullable final JSONString align) {
        if (align != null && this.align == null) {
            this.align = Align.valueOf(align.toString().toUpperCase())
            if (this.align == null) {
                throw new MSSException(message: "Bad alignment value! Must be one of LEFT, MIDDLE or RIGHT.")
            }
        }
    }

    void updateRotateIfNotSet(@Nullable final JSONNumber degrees) {
        if (degrees != null && this.rotateDegrees == null) {
            this.rotateDegrees = degrees.toFloat()
        }
    }

    void updateImgFlowIfNotSet(@Nullable final JSONBoolean imgFlow) {
        if (imgFlow != null && this.imgFlow == null) {
            this.imgFlow = imgFlow.asBoolean
        }
    }

    void updateImgFlowMarginIfNotSet(@Nullable final JSONNumber flowMargin) {
        if (flowMargin != null && this.imgFlowMargin == null) {
            this.imgFlowMargin = flowMargin.toFloat()
        }
    }

    void updateImgXIfNotSet(@Nullable final JSONNumber imgX) {
        if (imgX != null && this.imgX == null) {
            this.imgX = imgX.toFloat()
        }
    }

    void updateImgYIfNotSet(@Nullable final JSONNumber imgY) {
        if (imgY != null && this.imgY == null) {
            this.imgY = imgY.toFloat()
        }
    }

}
