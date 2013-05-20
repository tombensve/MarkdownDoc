package se.natusoft.doc.markdowndoc.editor;

import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;

import javax.swing.*;
import java.util.*;

/**
 * This is the editor toolbar.
 */
class MDEToolBar extends JToolBar {
    //
    // Private Members
    //

    private List<String> toolBarGroups = new LinkedList<String>();

    private Map<String, List<EditorFunction>> functions = new HashMap<String, List<EditorFunction>>();

    //
    // Constructors
    //

    public MDEToolBar() {}

    //
    // Methods
    //

    /**
     * Adds a function to the toolbar.
     *
     * @param function The function to add.
     */
    public void addFunction(EditorFunction function) {
        if (!this.toolBarGroups.contains(function.getToolBarGroup())) {
            this.toolBarGroups.add(function.getToolBarGroup());
        }

        List<EditorFunction> groupFunctions = this.functions.get(function.getToolBarGroup());
        if (groupFunctions == null) {
            groupFunctions = new LinkedList<EditorFunction>();
            this.functions.put(function.getToolBarGroup(), groupFunctions);
        }

        groupFunctions.add(function);
    }

    /**
     * Creates the content of the toolbar. This cannot be done until all
     * functions have been provided.
     */
    public void createToolBarContent() {
        Iterator<String> groupIterator = this.toolBarGroups.iterator();
        while (groupIterator.hasNext()) {
            String group = groupIterator.next();

            List<EditorFunction> functions = this.functions.get(group);
            for (EditorFunction function : functions) {
                add(function.getToolBarButton());
            }

            if (groupIterator.hasNext()) {
                add(new JToolBar.Separator());
            }
        }
    }

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to enable.
     */
    public void disableGroup(String group) {
        List<EditorFunction> functions = this.functions.get(group);
        if (functions != null) {
            for (EditorFunction function : functions) {
                function.getToolBarButton().setEnabled(false);
            }
        }
        else {
            throw new RuntimeException("Cannot disable non existent group '" + group + "'!");
        }
    }

    /**
     * Disables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to disable.
     */
    public void enableGroup(String group) {
        List<EditorFunction> functions = this.functions.get(group);
        if (functions != null) {
            for (EditorFunction function : functions) {
                function.getToolBarButton().setEnabled(true);
            }
        }
        else {
            throw new RuntimeException("Cannot enable non existent group '" + group + "'!");
        }
    }
}
