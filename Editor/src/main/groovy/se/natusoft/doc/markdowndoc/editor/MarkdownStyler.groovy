/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.8
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
 *         2014-02-01: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor

import groovy.transform.CompileStatic
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.JTextComponentStyler
import se.natusoft.doc.markdowndoc.editor.api.JTextComponentStyler.ParagraphBounds
import se.natusoft.doc.markdowndoc.editor.config.*

import javax.swing.*
import javax.swing.text.*
import java.awt.*

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_EDITING

/**
 * This sits on a reference to a JTextPane and provides Markdown styling for it.
 */
@CompileStatic
public class MarkdownStyler implements Configurable, JTextComponentStyler {

    //
    // Private Members
    //

    /** The JTextPane we are styling. */
    private JTextComponent textComponentToStyle

    /** When true styling will be done. */
    private boolean enabled = true

    //
    // Config
    //

    // monospacedFontFamily

    /** The font family to use for monospaced text. */
    private String monospacedFontFamily = "Monospaced"

    /** Config entry used in SettingsWindow to edit config. */
    private static ValidSelectionConfigEntry monospacedFontConfig =
            new ValidSelectionConfigEntry("editorPane.pane.monospaced.font", "The monospaced font to use.", "Monospaced",
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        public ValidSelectionConfigEntry.Value[] validValues() {
                            GraphicsEnvironment gEnv = GraphicsEnvironment
                                    .getLocalGraphicsEnvironment()
                            return ValidSelectionConfigEntry.convertToValues(gEnv.getAvailableFontFamilyNames())
                        }
                    },
                    CONFIG_GROUP_EDITING
            )

    /**
     * Configuration callback for monospaced font.
     */
    private Closure monospacedFontConfigChanged = { ConfigEntry ce ->
        this.monospacedFontFamily = Font.decode(ce.getValue()).getFamily()

        StyledDocument doc = (StyledDocument)this.textComponentToStyle.getDocument()
        Style base = StyleContext.
                getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE)
        doc.removeStyle("code")
        this.codeStyle = doc.addStyle("code", base)
        StyleConstants.setFontFamily(this.codeStyle, this.monospacedFontFamily)
        StyleConstants.setFontSize(this.codeStyle, this.monospacedFontSize)
        styleDocument()
    }

    // monospacedFontSize

    /** The font size to use for monospaced text. */
    private int monospacedFontSize = 16

    /** Config entry used in SettingsWindow to edit config. */
    private static IntegerConfigEntry monospacedFontSizeConfig =
            new IntegerConfigEntry("editorPane.pane.font.monospaced.size", "The size of the monospaced font.", 14, 8, 50, CONFIG_GROUP_EDITING)

    /**
     * Configuration callback for monospaced font size.
     */
    private Closure monospacedFontSizeConfigChanged = { ConfigEntry ce ->
        this.monospacedFontSize = Integer.valueOf(ce.getValue())

        StyledDocument doc = (StyledDocument)this.textComponentToStyle.getDocument()
        Style base = StyleContext.
                getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE)
        doc.removeStyle("code")
        this.codeStyle = doc.addStyle("code", base)
        StyleConstants.setFontFamily(this.codeStyle, this.monospacedFontFamily)
        StyleConstants.setFontSize(this.codeStyle, this.monospacedFontSize)
        styleDocument()
    }

    // markdownFormatWhileEditing

    /** True for markdown styling while editing. */
    private boolean markdownFormatWhileEditing = true

    /** Config entry used in SettingsWindow to edit config. */
    private static BooleanConfigEntry markdownFormatWhileEditingConfig =
            new BooleanConfigEntry("editorPane.pane.markdown.format", "Format markdown while editing.", true, CONFIG_GROUP_EDITING)

    /**
     * Configuration callback for markdown formatting while editing.
     */
    private Closure markdownFormatWhileEditingConfigChanged = { ConfigEntry ce ->
        this.markdownFormatWhileEditing = Boolean.valueOf(ce.getValue())

        styleDocument()
    }

    // Make styling chars tiny

    /** True to make styling characters tiny. */
    private boolean makeStylingCharsTiny = false

    /** Config entry used in SettingsWindow to edit config. */
    private static BooleanConfigEntry makeStylingCharsTinyConfig =
            new BooleanConfigEntry("editorPane.pane.formatting.chars.tiny", "Make formatting chars tiny", false, CONFIG_GROUP_EDITING)

    /**
     * Configuration callback.
     */
    private Closure makeStylingCharsTinyConfigChanged = { ConfigEntry ce ->
        this.makeStylingCharsTiny = ce.getBoolValue()

        styleDocument()
    }

    //
    // Styles
    //

    @SuppressWarnings("FieldCanBeLocal")
    private Style baseStyle
    private Style emphasisStyle
    private Style boldStyle
    private Style h1Style
    private Style h2Style
    private Style h3Style
    private Style h4Style
    private Style h5Syle
    private Style h6Style
    private Style tinyStyle
    private Style codeStyle

    //
    // Constructors
    //

    /**
     * Creates a new MarkdownStyler instance.
     */
    public MarkdownStyler() {}

    //
    // Methods
    //

    /**
     * Initializes the Styler with a component to style.
     *
     * @param textComponentToStyle The component to style.
     */
    @Override
    public void init(JTextPane textComponentToStyle) {
        this.textComponentToStyle = textComponentToStyle

        StyledDocument doc = new DefaultStyledDocument() {
            public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a)
                if (/*MarkdownStyler.this.*/enabled) styleCurrentParagraph()
            }

            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len)
                if (/*MarkdownStyler.this.*/enabled) styleCurrentParagraph()
            }
        }


        baseStyle = StyleContext.
                getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE)

        emphasisStyle = doc.addStyle("emphasis", baseStyle)
        StyleConstants.setItalic(emphasisStyle, true)

        boldStyle = doc.addStyle("bold", baseStyle)
        StyleConstants.setBold(boldStyle, true)

        h1Style = doc.addStyle("h1", baseStyle)
        StyleConstants.setFontSize(h1Style, 34)
        StyleConstants.setBold(h1Style, true)

        h2Style = doc.addStyle("h2", baseStyle)
        StyleConstants.setFontSize(h2Style, 30)
        StyleConstants.setBold(h2Style, true)

        h3Style = doc.addStyle("h3", baseStyle)
        StyleConstants.setFontSize(h3Style, 26)
        StyleConstants.setBold(h3Style, true)

        h4Style = doc.addStyle("h4", baseStyle)
        StyleConstants.setFontSize(h4Style, 22)
        StyleConstants.setBold(h4Style, true)

        h5Syle = doc.addStyle("h5", baseStyle)
        StyleConstants.setFontSize(h5Syle, 18)
        StyleConstants.setBold(h5Syle, true)

        h6Style = doc.addStyle("h6", baseStyle)
        StyleConstants.setFontSize(h6Style, 14)
        StyleConstants.setBold(h6Style, true)

        tinyStyle = doc.addStyle("tiny", baseStyle)
        StyleConstants.setBold(tinyStyle, false)
        StyleConstants.setFontSize(tinyStyle, 6)

        codeStyle = doc.addStyle("code", baseStyle)
        StyleConstants.setFontFamily(codeStyle, this.monospacedFontFamily)
        StyleConstants.setFontSize(codeStyle, this.monospacedFontSize)

        this.textComponentToStyle.setDocument(doc)
    }

    /**
     * Register the configs used by the styler.
     *
     * @param configProvider The config provider to register with.
     */
    public void registerConfigs(ConfigProvider configProvider) {
        configProvider.registerConfig(monospacedFontConfig, this.monospacedFontConfigChanged)
        configProvider.registerConfig(monospacedFontSizeConfig, this.monospacedFontSizeConfigChanged)
        configProvider.registerConfig(markdownFormatWhileEditingConfig, this.markdownFormatWhileEditingConfigChanged)
        configProvider.registerConfig(makeStylingCharsTinyConfig, this.makeStylingCharsTinyConfigChanged)
    }

    /**
     * Unregister the configs used by the styler.
     *
     * @param configProvider The config provider to unregister with.
     */
    public void unregisterConfigs(ConfigProvider configProvider) {
        configProvider.unregisterConfig(monospacedFontConfig, this.monospacedFontConfigChanged)
        configProvider.unregisterConfig(monospacedFontSizeConfig, this.monospacedFontSizeConfigChanged)
        configProvider.unregisterConfig(markdownFormatWhileEditingConfig, this.markdownFormatWhileEditingConfigChanged)
        configProvider.unregisterConfig(makeStylingCharsTinyConfig, this.makeStylingCharsTinyConfigChanged)
    }

    /**
     * Enables styling (on by default)
     */
    @Override
    public void enable() {
        this.enabled = true
    }

    /**
     * Disables styling (should be done while loading document!)
     */
    @Override
    public void disable() {
        this.enabled = false
    }

    /**
     * Returns true if styling is enabled.
     */
    @Override
    public boolean isEnabled() {
        return this.enabled
    }

    /**
     * Styles the whole document.
     */
    @Override
    public void styleDocument() {
        if (isEnabled()) {
            try {
                StyledDocument doc = (StyledDocument) this.textComponentToStyle.getDocument()

                String text = doc.getText(0, doc.getLength())

                ParagraphBounds bounds = new ParagraphBounds()
                bounds.start = 0
                bounds.end = text.length() - 1
                intStyleDocument(bounds, text)
            } catch (BadLocationException ble) {
                ble.printStackTrace(System.err)
            }
        }
    }

    /**
     * Styles the current paragraph.
     */
    @Override
    public void styleCurrentParagraph() {
        if (isEnabled()) {
            try {
                StyledDocument doc = (StyledDocument) this.textComponentToStyle.getDocument()

                String text = doc.getText(0, doc.getLength())

                ParagraphBounds bounds = findParagraphBounds(this.textComponentToStyle.getCaretPosition() - 1, text)
                intStyleDocument(bounds, text)

            } catch (BadLocationException ble) {
                ble.printStackTrace(System.err)
            }
        }
    }

    /**
     * Styles part of the document specified by the paragraph bounds.
     *
     * @param bounds The bounds for styling.
     * @param text The text of the document to style.
     */
    private void intStyleDocument(ParagraphBounds bounds, String text) {
        // Speedup by disabling component while styling.
        this.textComponentToStyle.setEnabled(false)

        StyledDocument doc = (StyledDocument)this.textComponentToStyle.getDocument()
        Style base = StyleContext.
                getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE)

//        System.out.println("Start styling")
        try {
            if (text == null) {
                text = doc.getText(0, doc.getLength())
            }
            doc.setCharacterAttributes(bounds.start, bounds.end - bounds.start, base, true)
            if (this.markdownFormatWhileEditing) {
                for (int pos = bounds.start; pos <= bounds.end; pos++) {
                    try {
                        char c = text.charAt(pos)
                        char p = pos == 0 ? text.charAt(pos) : text.charAt(pos - 1)
                        char pp = pos <= 1 ? text.charAt(pos) : text.charAt(pos - 2)
                        //System.out.print(c);

                        // -- Header --------
                        if (c == '#' && p != '\\') {
                            int cnt = 0
                            int spos = pos
                            while (pos < text.length() && text.charAt(pos) == '#') {
                                ++cnt
                                ++pos
                            }
                            int epos = getPosOfNext(text, pos, (char)'\n')
                            Style header = null

                            // Only style if it starts at the beginning of a "paragraph".
                            int hsize = 0
                            if (pos <= 5 || text.charAt(pos - cnt - 1) == '\n' || text.charAt(pos - cnt - 1) == '\r') {
                                switch(cnt) {
                                    case 1:
                                        header = this.h1Style
                                        hsize = 2
                                        break
                                    case 2:
                                        header = this.h2Style
                                        hsize = 3
                                        break
                                    case 3:
                                        header = this.h3Style
                                        hsize = 4
                                        break
                                    case 4:
                                        header = this.h4Style
                                        hsize = 5
                                        break
                                    case 5:
                                        header = this.h5Syle
                                        hsize = 6
                                        break
                                    default:
                                        hsize = 7
                                        header = this.h6Style
                                }
                            }
                            if (header != null) {
//                                System.out.println("\nHeader identified!")
                                if (this.makeStylingCharsTiny) {
                                    doc.setCharacterAttributes(spos + hsize, epos - (spos + hsize), header, true)
                                    doc.setCharacterAttributes(spos, hsize, this.tinyStyle, true)
                                }
                                else {
                                    doc.setCharacterAttributes(spos, epos - spos, header, true)
                                }
                            }
                            pos = epos
                        }

                        // -- Monospaced --------
                        else if ((pos >= 4 && c == ' ' && text.charAt(pos + 1) == ' ' &&
                                text.charAt(pos + 2) == ' ' && text.charAt(pos + 3) == ' ') ||
                                (pos >= 2 && c == '\t')) {

                            boolean isList = true
                            if (pos >= 5) {
                                int checkPos = pos + 4
                                while (text.charAt(checkPos) == ' ') {
                                    ++checkPos
                                }
                                char p4 = text.charAt(checkPos)
                                if (p4 != '-' && p4 != '+' && p4 != '*') {
                                    isList = false
                                }
                            }

                            if (!isList) {
//                                System.out.println("\nMonospaced identified!")
                                int epos = getEndOfParagraph(text, pos)
                                doc.setCharacterAttributes(pos, (epos - pos), this.codeStyle, true)
                                pos = epos
                            }
//                            else {
//                                System.out.println("\nList bullet identified!")
//                            }
                        }

                        // -- Bold italic (when not escaped, but when double escaped) --------
                        else if ((c == '_' || c == '*') && (p != '\\' || pp == '\\')) {
                            boolean bold = false
                            int spos = pos
                            if ((pos + 1) < text.length() && text.charAt(pos + 1) == c) {
                                bold = true
                                ++pos
                            }
                            else if (c == '*' && (pos + 1) < text.length() && text.charAt(pos + 1) == ' ') {
                                continue // Skip if there is a spaced after *. This means it is a list entry.
                            }

                            @SuppressWarnings("UnnecessaryLocalVariable") // No, this is not unnecessary! It makes it
                                                                          // clear what it is!
                            char endChar = c
                            int epos = getPosOfNext(text, pos + 1, endChar)

                            if (bold) {
//                                System.out.println("\nBold identified!")
                                doc.setCharacterAttributes(spos + 2, epos - spos - 2, this.boldStyle, true)
                                if (this.makeStylingCharsTiny) {
                                    doc.setCharacterAttributes(spos, 2, this.tinyStyle, true)
                                    doc.setCharacterAttributes(epos, 2, this.tinyStyle, true)
                                }
                            }
                            else {
//                                System.out.println("\nEmphasis identified!")
                                doc.setCharacterAttributes(spos + 1, epos - spos - 1, this.emphasisStyle, true)
                                if (this.makeStylingCharsTiny) {
                                    doc.setCharacterAttributes(spos, 1, this.tinyStyle, true)
                                    doc.setCharacterAttributes(epos, 1, this.tinyStyle, true)
                                }
                            }
                            pos = epos + 1
                        }
                    }
                    catch (IndexOutOfBoundsException ignore) {/* we hide these intentionally! */}
                }
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace(System.err)
        }
//        System.out.println("Done styling.")

        this.textComponentToStyle.setEnabled(true)
    }


    /**
     * Returns the position of the next 'n'.
     *
     * @param text The text to search.
     * @param start The position to start at.
     * @param n The character to find.
     */
    private static int getPosOfNext(String text, int start, char n) {
        int npos = start
        try {
            char prev = ' '
            char prevprev = ' '
            while (npos < text.length() && !(prev == '\n' && prevprev == '\n') && text.charAt(npos) != n) {
                prevprev = prev
                prev = text.charAt(npos)
                ++npos
            }
        }
        catch (IndexOutOfBoundsException ignore) {/*OK*/}

        return npos
    }

    /**
     * Returns the position of the end of the paragraph.
     *
     * @param text The text to search.
     * @param start The position to start searching at.
     */
    private static int getEndOfParagraph(String text, int start) {
        int npos = start

        try {
            // I'm leaving this commented out just to remind me of my stupidity!
            //while (npos < text.length() && safeGetChar(text, npos) != '\n' && safeGetChar(text, (npos + 1)) != '\n') {
            //    ++npos
            //}
            while (npos < text.length()) {
//                System.out.print(text.charAt(npos))
                if (safeGetChar(text, npos) == '\n' && safeGetChar(text, npos + 1) == '\n') {
                    break
                }
                ++npos;
            }
        }
        catch (IndexOutOfBoundsException ignore) { /* Intentionally_DoNothing */ }

        if (npos >= text.length()) {
            npos = text.length() - 1
        }

        return npos
    }

    /**
     * Gets the char from the specified string at the specified position without the risk of a StringIndexOutOfBoundsException!
     * By default a space is returned if such an exception occur. This makes the beginning and end of the text much less messy
     * to handle.
     *
     * @param text The text to get the character from.
     * @param position The position in the text of the character to get.
     */
    private static char safeGetChar(String text, int position) {
        try {
            return text.charAt(position)
        }
        catch (StringIndexOutOfBoundsException ignore) {
            return ' ' as char
        }
    }

    /**
     * Returns true if the character at the specified position in the specified text contains a "new line" character.
     *
     * @param text The text to the character from.
     * @param position The position of the character to check.
     */
    private static boolean checkPgBound(String text, int position) {
        return _intCheckPgBound(text, position) && _intCheckPgBound(text, position + 1)
    }

    private static boolean _intCheckPgBound(String text, int position) {
        //System.out.println("\"\\n\\r\".indexOf(safeGetChar(text, " + position + ")) = '"+ safeGetChar(text, position) + "'")
        return position < text.length() && position >= 0 && ( text[position] == '\n' || text[position] == '\r' )
    }

    /**
     * Find the beginning of the current paragraph and then the end of it, and returns those found bounds.
     *
     * @param currentPos The current position in the text to scan.
     * @param text The text to scan.
     */
    private static ParagraphBounds findParagraphBounds(int currentPos, String text) {
        //System.out.println("CurrentPos: " + currentPos)
        if (currentPos < 0) currentPos = 0
        ParagraphBounds bounds = new ParagraphBounds()

        // Find beginning
        while (currentPos > 0 && !checkPgBound(text, currentPos)) {
            --currentPos
        }
        bounds.start = text.length() > currentPos ? (text.charAt(currentPos) == ' ' ? currentPos + 1 : currentPos) : currentPos

        // Find end
        currentPos = bounds.start + 1
        while (currentPos < text.length() && !checkPgBound(text, currentPos)) {
            ++currentPos
        }
        bounds.end = text.length() > currentPos ? (text.charAt(currentPos) == ' ' ? currentPos - 1 : currentPos) : currentPos

        //System.out.println("" + bounds)

        return bounds
    }

    /**
     * Returns the first 10 characters of the paragraph as a String.
     *
     * @param currentPos The current position.
     * @param bounds The paragraph bounds.
     * @param text The whole document text.
     */
    private static String getStartOfParagraphText(int currentPos, ParagraphBounds bounds, String text) {
        if (bounds.start == 0) { // Possibly the whole document!
            bounds = findParagraphBounds(currentPos, text)
        }
        return text.substring(bounds.start, bounds.start + 60)
    }
}
