package se.natusoft.doc.markdowndoc.editor.api;

import javax.swing.*;

/**
 * Defines a toolbar API.
 */
public interface ToolBar {
    /**
     * Adds a function to the toolbar.
     *
     * @param function The function to add.
     */
    void addFunction(EditorFunction function);

    /**
     * Creates the content of the toolbar. This cannot be done until all
     * functions have been provided.
     */
    void createToolBarContent();

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to enable.
     */
    void disableGroup(String group);

    /**
     * Disables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to disable.
     */
    void enableGroup(String group);

    /**
     * Provides the toolbar with the editor it is associated.
     *
     * @param editor The associated editor provided.
     */
    void attach(Editor editor);

    /**
     * Removes the association with the editor.
     *
     * @param editor The editor to detach from.
     */
    void detach(Editor editor);
}
