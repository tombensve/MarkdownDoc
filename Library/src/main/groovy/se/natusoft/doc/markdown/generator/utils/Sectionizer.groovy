/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.1.1
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
 *     tommy ()
 *         Changes:
 *         2018-06-21: Created!
 *         
 */
package se.natusoft.doc.markdown.generator.utils

import se.natusoft.doc.markdown.generator.styles.MSS

/**
 * Stores a "current" section in a ThreadLocal during the execution of the closure.
 *
 * Not having to pass the current section to every method needing it makes the code a
 * little bit cleaner and less messy.
 */
class Sectionizer {

    private static ThreadLocal<MSS.Section> currentSection = null

    private static Deque<MSS.Section> previousSections = null

    static final setup() {
        currentSection = new ThreadLocal<>()
        previousSections = new LinkedList<>()
    }

    static final cleanup() {
        currentSection = null
        previousSections = null
    }

    static final withSection( MSS.Section section, Closure sectonizedCode ) {
        if ( currentSection.get() != null ) {
            previousSections.push( currentSection.get() )
        }

        currentSection.set( section )

        sectonizedCode.call()

        if ( previousSections.isEmpty() ) {
            currentSection.remove()
        }
        else {
            currentSection.set( previousSections.pop() )
        }
    }

    static final MSS.Section getSection() {
        MSS.Section section = currentSection.get()
        if ( section == null ) section = MSS.MSS_Pages.standard

        section
    }
}
