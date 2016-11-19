/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.1
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
 *         2016-01-08: Created!
 *         
 */
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
