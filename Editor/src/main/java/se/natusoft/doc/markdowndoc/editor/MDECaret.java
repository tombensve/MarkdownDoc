package se.natusoft.doc.markdowndoc.editor;

import javax.swing.text.DefaultCaret;
import java.awt.Rectangle;

/**
 * This provides a Caret variant that does not scroll to the bottom of the file on
 * setText(...).
 */
public class MDECaret extends DefaultCaret {

    @Override
    protected void adjustVisibility(Rectangle nloc) {

    }

}
