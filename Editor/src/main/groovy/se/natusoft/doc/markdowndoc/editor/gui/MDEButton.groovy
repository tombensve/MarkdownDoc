package se.natusoft.doc.markdowndoc.editor.gui

import javax.swing.JButton

/**
 * Button component that allows for specifying text color.
 */
class MDEButton extends JButton {

    //
    // Private Members
    //

    private String color = null

    //
    // Methods
    //

    /**
     * Sets the text of the component.
     *
     * @param text The text to set.
     */
    @Override
    void setText(final String text) {
        // The groovy compiler (2.4.3 & 2.4.4) fails compiling with a BUG! message when property access
        // is used for "super.text = ...". setText(...) however compiles fine.
        if (this.color != null) {
            super.setText("<html><font color='${this.color}'>${text}</font></html>")
        }
        else {
            super.setText(text)
        }
    }

    /**
     * Sets the color for the component.
     *
     * @param color The color to set.
     */
    void setTextColor(String color) {
        this.color = color
        if (super.text != null && !super.text.empty) {
            this.text = super.text
        }
    }
}
