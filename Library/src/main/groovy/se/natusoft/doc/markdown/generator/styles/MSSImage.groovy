/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Code Version
 *         1.5.0
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

import org.jetbrains.annotations.Nullable
import se.natusoft.json.JSONNumber
import se.natusoft.json.JSONString

/**
 * This holds image information.
 */
class MSSImage {
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

    //
    // Methods
    //

    void updateScaleIfNotSet(@Nullable final JSONNumber scale) {
        if (this.scalePercent == null) {
            this.scalePercent = scale?.toFloat()
        }
    }

    void updateAlignIfNotSet(@Nullable final JSONString align) {
        if (this.align == null) {
            if (align != null) {
                this.align = Align.valueOf(align.toString().toUpperCase())
                if (this.align == null) {
                    throw new MSSException(message: "Bad alignment value! Must be one of LEFT, MIDDLE or RIGHT.")
                }
            }
        }
    }

    void updateRotateIfNotSet(@Nullable final JSONNumber degrees) {
        if (this.rotateDegrees == null) {
            this.rotateDegrees = degrees?.toFloat()
        }
    }

}
