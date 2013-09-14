/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.2.9
 *     
 *     Description
 *         An editor that supports editing markdown with formatting preview.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2013-05-27: Created!
 *         
 */
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

    public MDEToolBar() {
        setRollover(true);
    }

    //
    // Methods
    //

    /**
     * Adds a function to the toolbar.
     *
     * @param function The function to add.
     */
    public void addFunction(EditorFunction function) {
        if (!this.toolBarGroups.contains(function.getGroup())) {
            this.toolBarGroups.add(function.getGroup());
        }

        List<EditorFunction> groupFunctions = this.functions.get(function.getGroup());
        if (groupFunctions == null) {
            groupFunctions = new LinkedList<EditorFunction>();
            this.functions.put(function.getGroup(), groupFunctions);
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
