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
 *     tommy ()
 *         Changes:
 *         2015-08-03: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import javax.swing.*
import java.awt.*
import java.awt.font.TextAttribute

/**
 * This is a simple label component that centers the label horizontally and renders in bold.
 */
@CompileStatic
@TypeChecked
class PathLabel extends JLabel {

    PathLabel() {
        // No , this is not "unnecessary" in any way! Groovy refuses to compile without the JLabel. qualification!
        //noinspection UnnecessaryQualifiedReference
        super("", JLabel.CENTER)

        font = font.deriveFont Font.BOLD
//        Map<TextAttribute, ?> attributes = font.getAttributes();
//        attributes.put TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON  // IDEA only error!
//        font = font.deriveFont attributes
        foreground = Color.ORANGE
    }

    @Override
    Dimension minimumSize() {
        return new Dimension(
                getFontMetrics(font).stringWidth(text),
                font.size
        )
    }

    @Override
    Dimension preferredSize() {
        return new Dimension(
                getFontMetrics(font).stringWidth(text),
                font.size
        )
    }
}
