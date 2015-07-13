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
        RIGHT
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

    void updateScaleIfNotSet(@Nullable JSONNumber scale) {
        if (this.scalePercent == null) {
            this.scalePercent = scale?.toFloat()
        }
    }

    void updateAlignIfNotSet(@Nullable JSONString align) {
        if (this.align == null) {
            if (align != null) {
                this.align = Align.valueOf(align.toString().toUpperCase())
                if (this.align == null) {
                    throw new MSSException(message: "Bad alignment value! Must be one of LEFT, MIDDLE or RIGHT.")
                }
            }
        }
    }

    void updateRotateIfNotSet(@Nullable JSONNumber degrees) {
        if (this.rotateDegrees == null) {
            this.rotateDegrees = degrees?.toFloat()
        }
    }

}
