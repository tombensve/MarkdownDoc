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
 *     tommy ()
 *         Changes:
 *         2013-06-06: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.api;

import java.util.Properties;

/**
 * This api allows for getting and saving properties.
 */
public interface PersistentProps {

    /**
     * Loads the named properties.
     *
     * @param name The name of the properties to load. Please note that this is only a name, not a path!
     */
    Properties load(String name);

    /**
     * Saves the given properties with the given name.
     *
     * @param name The name of the properties to save.
     * @param props The properties to save.
     */
    void save(String name, Properties props);
}
