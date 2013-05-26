package se.natusoft.doc.markdowndoc.editor.api;

import javax.swing.*;

/**
 * This interface provides an API towards the editor GUI.
 */
public interface GUI {

    /**
     * Needed for popping upp dialogs.
     */
    JFrame getWindowFrame();

    /**
     * Returns the panel above the editor and toolbar.
     */
    JPanel getTopPanel();

    /**
     * Returns the panel below the editor and toolbar.
     */
    JPanel getBottomPanel();

    /**
     * Returns the panel to the left of the editor and toolbar.
     */
    JPanel getLeftPanel();

    /**
     * Returns the panel to the right of the editor and toolbar.
     */
    JPanel getRightPanel();
}
