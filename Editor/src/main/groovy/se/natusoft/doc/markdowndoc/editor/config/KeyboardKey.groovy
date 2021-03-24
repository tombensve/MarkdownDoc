/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
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
package se.natusoft.doc.markdowndoc.editor.config

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

import java.awt.event.KeyEvent

/**
 * Represents a keyboard key, possibly with modifiers in a format that can be saved to a properties file.
 */
@CompileStatic
class KeyboardKey {

    //
    // Private Members
    //

    /** Our internal representation. */
    private String key

    //
    // Constructors
    //

    /**
     * Creates a new KeyboardKey instance.
     *
     * @param keyEvent The key event to get key from.
     */
    KeyboardKey(@NotNull final KeyEvent keyEvent) {
        if (keyEvent != null) { // For Linux/Ubutnu!
            this.key = KeyEvent.getKeyModifiersText(keyEvent.getModifiers()) + "+" + KeyEvent.getKeyText(keyEvent.getKeyCode())
        }
    }

    /**
     * Creates a new KeyboardKey instance.
     *
     * @param key The key in "internal" key format.
     */
    KeyboardKey(@NotNull final String key) {
        this.key = key
    }

    //
    // Methods
    //

    /**
     * Compares for equality.
     *
     * @param key The object to compare to.
     */
    boolean equals(@NotNull final Object key) {
        key instanceof KeyboardKey && ((KeyboardKey) key).key.equals(this.key)
    }

    /**
     * Return as string.
     */
    @NotNull String toString() {
        this.key
    }
}
