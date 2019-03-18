/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.1.1
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
package se.natusoft.doc.markdowndoc.editor

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdowndoc.editor.api.PersistentProps

/**
 * Holds a set of persistent properties providers.
 */
@CompileStatic
@TypeChecked
class PersistentPropertiesProvider implements PersistentProps {
    //
    // Constructors
    //

    /**
     * Creates a new PersistentPropertiesProvider.
     */
    PersistentPropertiesProvider() {}

    //
    // Methods
    //

    /**
     * Sets up and returns the properties directory.
     */
    private static File getPropsDir() {
        final String userHome = System.getProperties().getProperty("user.home")
        File propsDir = new File(userHome)
        propsDir = new File(propsDir, ".markdownDoc")
        if (!propsDir.exists()) {
            propsDir.mkdirs()
        }

        propsDir
    }

    /**
     * Loads the named properties.
     *
     * @param name The name of the properties to load. Please note that this is only a name, not a path!
     *
     * @return A Properties object or null if not available (or failure to read it!).
     */
    @Override
    Properties load(final String name) {
        final File propsFile = new File(getPropsDir(), name + ".properties")
        Properties props = null

        FileReader propsReader = null
        try {
            propsReader = new FileReader(propsFile)
            props = new Properties()
            props.load(propsReader)
        }
        catch (final FileNotFoundException ignore) {}
        catch (final IOException ioe) {
            System.err.println("ERROR: " + ioe.getMessage())
        }
        finally {
            //noinspection GroovyUnusedCatchParameter
            try {if (propsReader != null) propsReader.close()} catch (final IOException unused) {}
        }

        props
    }

    /**
     * Saves the given properties with the given name.
     *
     * @param name  The name of the properties to save.
     * @param props The properties to save.
     */
    @Override
    void save(final String name, final Properties props) {
        final File propsFile = new File(getPropsDir(), name + ".properties")

        FileWriter writer = null
        try {
            writer = new FileWriter(propsFile)
            props.store(writer, "Properties for '" + name + "'.")
        }
        catch (final IOException ioe) {
            System.err.println("ERROR: " + ioe.getMessage())
        }
        finally {
            try { if (writer != null) writer.close() } catch (final IOException ignore) {}
        }
    }
}
