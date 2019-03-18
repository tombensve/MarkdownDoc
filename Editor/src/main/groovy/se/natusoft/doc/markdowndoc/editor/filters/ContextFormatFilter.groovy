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
 *         2013-05-27: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.filters

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.*
import se.natusoft.doc.markdowndoc.editor.config.BooleanConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry

import javax.swing.text.BadLocationException
import java.awt.event.KeyEvent

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_EDITING

/**
 * This filter provides context formatting in the editorPane.
 */
@CompileStatic
@TypeChecked
class ContextFormatFilter implements EditorInputFilter, Configurable {

    //
    // Properties
    //

    Editor editor

    //
    // Config
    //

    /** True for double spaced bullets. */
    private boolean doubleSpacedBullets = false

    /** Config entry used in SettingsWindow to edit config. */
    private static BooleanConfigEntry doubleSpacedBulletsConfig =
            new BooleanConfigEntry("editor.contextformatfilter", "Double space bullets", false, CONFIG_GROUP_EDITING)

    /**
     * Configuration callback for markdown formatting while editing.
     */
    private Closure doubleSpacedBulletsConfigChanged = { @NotNull final ConfigEntry ce ->
        this.doubleSpacedBullets = Boolean.valueOf(ce.getValue())
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.registerConfig(doubleSpacedBulletsConfig, doubleSpacedBulletsConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(doubleSpacedBulletsConfig, doubleSpacedBulletsConfigChanged)
    }

    //
    // Methods
    //

    private static boolean isBulletChar(final char bulletChar) {
        bulletChar == '+' as char || bulletChar == '-' as char || bulletChar == '*' as char
    }

    private static boolean isBulletChar(@NotNull final String bulletChar) {
        bulletChar.length() > 0 && isBulletChar(bulletChar.charAt(0))
    }

    private static boolean isBulletStart(@NotNull final String line) {
        line.length() > 1 && isBulletChar(line.charAt(0)) && line.charAt(1) == ' ' as char
    }

    @Override
    void keyPressed(@NotNull final KeyEvent keyEvent) {
        try {
            // Catch new lines
            if (keyEvent.keyChar == '\n' as char) {
                Line currentLine = this.editor.currentLine

                if (currentLine != null) {
                    final String trimmedLine = currentLine.text.trim()

                    // -- Handle pre-formatted --

                    if ((currentLine.text.length() > 0 && currentLine.text.charAt(0) == '\t' as char) ||
                            currentLine.text.startsWith("    ")) {
                        if (currentLine.text.charAt(0) == '\t' as char) {
                            currentLine.nextLine.text = "\t"
                            this.editor.moveCaretForward(1)
                        }
                        else {
                            currentLine.nextLine.text = "    " + currentLine.nextLine.text
                            this.editor.moveCaretForward(4)
                        }
                    }

                    // -- Handle list bullets --

                    // If the previous line only contains a list bullet and no text, blank the line.
                    else if (trimmedLine.length() == 1 && isBulletChar(trimmedLine)) {
                        currentLine.text = ""
                    }
                    // Otherwise start the new line with a new list bullet.
                    else if (isBulletStart(trimmedLine)) {
                        // Since the user just pressed return whatever was after the cursor will be on
                        // the next line. If return was pressed at the end of the line the next line
                        // will be empty (with the exception of possible whitespace). In this case we
                        // add a new bullet. If it is not empty we don't to anything.
                        if (currentLine.nextLine.text.trim().length() == 0) {
                            int indentPos = currentLine.text.indexOf("*")
                            if (this.doubleSpacedBullets) {
                                this.editor.addBlankLine()
                                currentLine = currentLine.nextLine
                            }
                            final StringBuilder newLine = new StringBuilder()
                            while (indentPos > 0) {
                                newLine.append(' ')
                                --indentPos
                            }
                            newLine.append(trimmedLine.substring(0, 2))
                            final Line nextLine = currentLine.nextLine
                            if (nextLine != null) {
                                nextLine.text = newLine.toString() + nextLine.text
                                this.editor.moveCaretForward(newLine.length())
                            }
                        }
                    }

                    // -- Handle quotes --

                    // If the previous line only contains a quote (>) char and no text, blank the line
                    else if (trimmedLine.equals(">")) {
                        currentLine.text = ""
                    }
                    // Otherwise start the new line with a quote (>) character.
                    else if (trimmedLine.startsWith("> ")) {
                        int indentPos = currentLine.text.indexOf('>')
                        final StringBuilder newLine = new StringBuilder()
                        while (indentPos > 0) {
                            newLine.append(' ')
                            --indentPos
                        }
                        newLine.append("> ")
                        final Line nextLine = currentLine.nextLine
                        if (nextLine != null) {
                            nextLine.text = newLine.toString()
                            this.editor.moveCaretForward(newLine.length())
                        }
                    }
                }
            }
            // Shift-Tab for bullet indents.
            else if (keyEvent.keyChar == '\t' as char) {
                Line line = this.editor.currentLine
                final boolean isLastLine = line.isLastLine()
                if (keyEvent.shiftDown) {
                    // JEditorPane does something weird on shift-tab
                    if (isLastLine) {
                        line = line.previousLine
                    }
                }
                if (line.text.trim().startsWith("*")) {
                    if (keyEvent.shiftDown) {
                        if (line.text.startsWith("   ")) {
                            line.text = line.text.substring(3)
                            if (!isLastLine) {
                                this.editor.moveCaretBack(3)
                            }
                        }
                    }
                    else {
                        final int startPos = line.lineStartPost
                        final int caretLoc = this.editor.caretDot
                        final int tabIx = line.text.indexOf("\t")
                        int moveChars = 3

                        if ((startPos + tabIx) <= caretLoc) {
                            moveChars = 2
                        }

                        line.text = "   " + line.text.replace("\t", "")
                        this.editor.moveCaretForward(moveChars)
                    }
                }
                else {
                    this.editor.currentLine.text = this.editor.currentLine.text.replace("\t", "    ")
                    this.editor.moveCaretForward(3)

                }
            }
        }
        catch (final BadLocationException ble) {
            ble.printStackTrace(System.err)
        }

        this.editor.requestEditorFocus()
    }

    /**
     * Cleanup and unregister any configs.
     */
    void close() {}

}
