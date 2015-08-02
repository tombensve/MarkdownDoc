package se.natusoft.doc.markdowndoc.editor

/**
 * A trait that determines OS run on.
 */
trait OSTrait {
    private static final String osName = System.getProperty("os.name").toLowerCase()

    static boolean isWindowsOS() {
        osName.contains("windows")
    }

    static boolean isLinuxOS() {
        osName.contains("linux")
    }

    static boolean isMacOSXOS() {
        osName.contains("os x")
    }
}
