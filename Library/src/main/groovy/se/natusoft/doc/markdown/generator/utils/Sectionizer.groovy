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
