/* 
 * 
 * PROJECT
 *     Name
 *         Editor
 *     
 *     Code Version
 *         1.2.6
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
