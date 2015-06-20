package se.natusoft.doc.markdown.generator.styles

import org.jetbrains.annotations.Nullable

/**
 * Represents a pair of colors: foreground, and background.
 */
class MSSColorPair {
    //
    // Properties
    //

    MSSColor foreground = null
    MSSColor background = null

    //
    // Methods
    //

    void updateForegroundIfNotSet(@Nullable MSSColor foreground) {
        if (this.foreground == null) this.foreground = foreground
    }

    void updateBackgroundIfNotSet(@Nullable MSSColor background) {
        if (this.background == null) this.background = background
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MSSColorPair)) return false
        MSSColorPair other = obj as MSSColorPair
        return this.foreground == other.foreground && this.background == other.background
    }

    @Override
    public int hashCode() {
        Objects.hash(this.foreground, this.background)
    }

    @Override
    public String toString() {
        return "{\nforeground: \"${this.foreground}\",\nbackground: \"${this.background}\"\n}"
    }

}
