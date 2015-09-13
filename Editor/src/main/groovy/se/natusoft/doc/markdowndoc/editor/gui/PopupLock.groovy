package se.natusoft.doc.markdowndoc.editor.gui

/**
 * This is a singleton utility to lock and release popup windows. That is, a popup window locks on open
 * and releases on close, and when locked other popups are ignored.
 */
class PopupLock {

    /** The singleton instance. */
    public static final PopupLock instance = new PopupLock()

    /** A property indicating locked state or not. */
    synchronized boolean locked = false

    /** Special case going from toolbar popup to other popup where other opens before toolbar closes. */
    synchronized boolean transferLock = false

    public boolean isLocked() {
        return this.locked || this.transferLock
    }
}
