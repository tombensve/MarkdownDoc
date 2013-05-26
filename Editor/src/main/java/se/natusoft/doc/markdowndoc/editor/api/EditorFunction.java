package se.natusoft.doc.markdowndoc.editor.api;

import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;

/**
 * This represents an editor function.
 */
public interface EditorFunction extends EditorComponent {

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    String getGroup();

    /**
     * Returns the name of the function.
     */
    String getName();

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    JComponent getToolBarButton();

    /**
     * Keyboard trigger for the "down" key (shit, ctrl, alt, ...)
     */
    int getDownKeyMask();

    /**
     * The keyboard trigger key code.
     */
    int getKeyCode();

    /**
     * Performs the function.
     *
     * @throws FunctionException
     */
    void perform() throws FunctionException;

}