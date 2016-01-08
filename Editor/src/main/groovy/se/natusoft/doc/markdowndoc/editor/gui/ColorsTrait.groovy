/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.4.2
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
 *         2015-08-03: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.gui

import javax.swing.*
import java.awt.*

/**
 * A trait for color setting support.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
trait ColorsTrait {

    static Color getDefaultForegroundColor() {
        Color.WHITE
    }

    static Color getDefaultBackgroundColor() {
        Color.BLACK
    }

    static void updateColors(final JComponent component) {
        component.background = defaultBackgroundColor
        component.foreground = defaultForegroundColor
    }

    static void updateColors(final JWindow window) {
        window.background = defaultBackgroundColor
        window.foreground = defaultForegroundColor
    }

    static void updateColors(final JFrame frame) {
        frame.background = defaultBackgroundColor
        frame.foreground = defaultForegroundColor
    }

}
