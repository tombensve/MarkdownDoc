/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.1.2
 *     
 *     Description
 *         Parses markdown and generates HTML and PDF.
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
 *         2013-02-02: Created!
 *         
 */
package se.natusoft.doc.markdown.generator

import se.natusoft.doc.markdown.api.Generator

/**
 * Provides a generator by its name. The name is what the generators getName() method returns, not its class name!
 */
class GeneratorProvider {

    /**
     * Provides a generator by its name or null if not found.
     *
     * @param name The name of the generator to get.
     *
     * @return A Generator or null if not found.
     */
    public static Generator getGeneratorByName(String name) {
        Generator generator = null

        ServiceLoader<Generator> generators = ServiceLoader.load(Generator.class)
        for (Generator loadedGenerator : generators) {
            if (loadedGenerator.getName().equals(name)) {
                generator = loadedGenerator
                break
            }
        }

        return generator
    }
}
