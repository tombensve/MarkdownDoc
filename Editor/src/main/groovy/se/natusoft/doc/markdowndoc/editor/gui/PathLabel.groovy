package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import javax.swing.*
import java.awt.*
import java.awt.font.TextAttribute

/**
 * This is a simple label component that centers the label horizontally and renders in bold.
 */
@CompileStatic
@TypeChecked
class PathLabel extends JLabel {

    PathLabel() {
        // No , this is not "unnecessary" in any way! Groovy refuses to compile without the JLabel. qualification!
        //noinspection UnnecessaryQualifiedReference
        super("", JLabel.CENTER)

        font = font.deriveFont Font.BOLD
//        Map<TextAttribute, ?> attributes = font.getAttributes();
//        attributes.put TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON  // IDEA only error!
//        font = font.deriveFont attributes
        foreground = Color.ORANGE
    }

    @Override
    Dimension minimumSize() {
        return new Dimension(
                getFontMetrics(font).stringWidth(text),
                font.size
        )
    }

    @Override
    Dimension preferredSize() {
        return new Dimension(
                getFontMetrics(font).stringWidth(text),
                font.size
        )
    }
}
