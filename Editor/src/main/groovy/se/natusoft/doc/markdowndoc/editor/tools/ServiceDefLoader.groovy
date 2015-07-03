/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *
 *     Code Version
 *         1.3.9
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
 *         2014-02-15: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.tools

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This will load the same file as ServiceLoader but return Class object instead of instances.
 * This is basically a workaround where ServiceLoader fails to instantiate the service while a
 * Class.forName(...).newInstance() works fine!
 */
@CompileStatic
@TypeChecked
public class ServiceDefLoader {

    //
    // Private Members
    //

    private List<Class> services = new LinkedList<>()

    //
    // Constructors
    //

    /**
     * Creates a new ServiceDefLoader instance.
     *
     * @param serviceAPI The service API to load.
     */
    private ServiceDefLoader(Class serviceAPI) {
        BufferedReader svcReader = new BufferedReader(
                new InputStreamReader(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/services/" + serviceAPI.getName())
                )
        )
        try {
            String line = svcReader.readLine()
            while (line != null) {
                if (!line.trim().startsWith("#") && line.trim().length() > 0) {
                    try {
                        Class svcClass = Class.forName(line.trim())
                        services.add(svcClass)
                    }
                    catch (ClassNotFoundException  cnfe) {
                        System.err.println("Bad entry in META-INF/services/" + serviceAPI.getName() + "! [" + line + "]")
                        cnfe.printStackTrace(System.err)
                    }
                }
                line = svcReader.readLine()
            }

            svcReader.close()
        }
        catch (IOException ioe) {
            ioe.printStackTrace()
        }
    }

    //
    // Methods
    //

    /**
     * Loads the available services for the specified API.
     *
     * @param serviceAPI The service API to load services definitions for.
     */
    public static Iterator<Class> load(Class serviceAPI) {
        return new ServiceDefLoader(serviceAPI).services.iterator()
    }

    /**
     * Convenience to instantiate the specified service class.
     *
     * @param svcClass The class to instantiate.
     * @param <T> The type of the class.
     *
     * @return The instantiated instance or null on failure.
     */
    public static <T> T instantiate(Class<T> svcClass) {
        T instance = null
        try {
            instance = (T)svcClass.newInstance()
        }
        catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace(System.err)
        }
        return instance
    }
}
