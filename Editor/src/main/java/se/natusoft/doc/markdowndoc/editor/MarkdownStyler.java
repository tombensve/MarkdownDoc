/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3
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
package se.natusoft.doc.markdowndoc.editor;

import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider;
import se.natusoft.doc.markdowndoc.editor.api.Configurable;
import se.natusoft.doc.markdowndoc.editor.api.JTextComponentStyler;
import se.natusoft.doc.markdowndoc.editor.config.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_EDITING;

/**
 * This sits on a reference to a JTextPane and provides Markdown styling for it.
 */
public class MarkdownStyler implements Configurable, JTextComponentStyler {

    //
    // Private Members
    //

    /** The JTextPane we are styling. */
    private JTextComponent textComponentToStyle;

    /** When true styling will be done. */
    private boolean enabled = true;

    //
    // Config
    //

    // monospacedFontFamily

    /** The font family to use for monospaced text. */
    private String monospacedFontFamily = "Monospaced";

    /** Config entry used in SettingsWindow to edit config. */
    private static ValidSelectionConfigEntry monospacedFontConfig =
            new ValidSelectionConfigEntry("editor.pane.monospaced.font", "The monospaced font to use.", "Monospaced",
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        public ValidSelectionConfigEntry.Value[] validValues() {
                            GraphicsEnvironment gEnv = GraphicsEnvironment
                                    .getLocalGraphicsEnvironment();
                            return ValidSelectionConfigEntry.convertToValues(gEnv.getAvailableFontFamilyNames());
                        }
                    },
                    CONFIG_GROUP_EDITING
            );

    /**
     * Configuration callback for monospaced font.
     */
    private ConfigChanged monospacedFontConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            MarkdownStyler.this.monospacedFontFamily = Font.decode(ce.getValue()).getFamily();

            StyledDocument doc = (StyledDocument)MarkdownStyler.this.textComponentToStyle.getDocument();
            Style base = StyleContext.
                    getDefaultStyleContext().
                    getStyle(StyleContext.DEFAULT_STYLE);
            doc.removeStyle("code");
            Style code = doc.addStyle("code", base);
            StyleConstants.setFontFamily(code, MarkdownStyler.this.monospacedFontFamily);
            StyleConstants.setFontSize(code, MarkdownStyler.this.monospacedFontSize);
            styleDocument();
        }
    };

    // monospacedFontSize

    /** The font size to use for monospaced text. */
    private int monospacedFontSize = 16;

    /** Config entry used in SettingsWindow to edit config. */
    private static IntegerConfigEntry monospacedFontSizeConfig =
            new IntegerConfigEntry("editor.pane.font.monospaced.size", "The size of the monospaced font.", 16, 8, 50, CONFIG_GROUP_EDITING);

    /**
     * Configuration callback for monospaced font size.
     */
    private ConfigChanged monospacedFontSizeConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            MarkdownStyler.this.monospacedFontSize = Integer.valueOf(ce.getValue());

            MarkdownStyler.this.monospacedFontFamily = Font.decode(ce.getValue()).getFamily();
            StyledDocument doc = (StyledDocument)MarkdownStyler.this.textComponentToStyle.getDocument();
            Style base = StyleContext.
                    getDefaultStyleContext().
                    getStyle(StyleContext.DEFAULT_STYLE);
            doc.removeStyle("code");
            Style code = doc.addStyle("code", base);
            StyleConstants.setFontFamily(code, MarkdownStyler.this.monospacedFontFamily);
            StyleConstants.setFontSize(code, MarkdownStyler.this.monospacedFontSize);
            styleDocument();
        }
    };

    // markdownFormatWhileEditing

    /** True for markdown styling while editing. */
    private boolean markdownFormatWhileEditing = true;

    /** Config entry used in SettingsWindow to edit config. */
    private static BooleanConfigEntry markdownFormatWhileEditingConfig =
            new BooleanConfigEntry("editor.pane.markdown.format", "Format markdown while editing.", true, CONFIG_GROUP_EDITING);

    /**
     * Configuration callback for markdown formatting while editing.
     */
    private ConfigChanged markdownFormatWhileEditingConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            MarkdownStyler.this.markdownFormatWhileEditing = Boolean.valueOf(ce.getValue());

            styleDocument();
        }
    };

    // Make styling chars tiny

    /** True to make styling characters tiny. */
    private boolean makeStylingCharsTiny = false;

    /** Config entry used in SettingsWindow to edit config. */
    private static BooleanConfigEntry makeStylingCharsTinyConfig =
            new BooleanConfigEntry("editor.pane.formatting.chars.tiny", "Make formatting chars tiny", false, CONFIG_GROUP_EDITING);

    /**
     * Configuration callback.
     */
    private ConfigChanged makeStylingCharsTinyConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            MarkdownStyler.this.makeStylingCharsTiny = ce.getBoolValue();

            styleDocument();
        }
    };

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
        this.textComponentToStyle = textComponentToStyle;

        StyledDocument doc = new DefaultStyledDocument() {
            public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);
                if (MarkdownStyler.this.enabled) styleCurrentParagraph();
            }

            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len);
                if (MarkdownStyler.this.enabled) styleCurrentParagraph();
            }
        };
        Style base = StyleContext.
                getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);
        Style emphasis = doc.addStyle("emphasis", base);
        StyleConstants.setItalic(emphasis, true);
        Style bold = doc.addStyle("bold", base);
        StyleConstants.setBold(bold, true);

        Style h1 = doc.addStyle("h1", base);
        StyleConstants.setFontSize(h1, 34);
        StyleConstants.setBold(h1, true);
        Style h2 = doc.addStyle("h2", base);
        StyleConstants.setFontSize(h2, 30);
        StyleConstants.setBold(h2, true);
        Style h3 = doc.addStyle("h3", base);
        StyleConstants.setFontSize(h3, 26);
        StyleConstants.setBold(h3, true);
        Style h4 = doc.addStyle("h4", base);
        StyleConstants.setFontSize(h4, 22);
        StyleConstants.setBold(h4, true);
        Style h5 = doc.addStyle("h5", base);
        StyleConstants.setFontSize(h5, 18);
        StyleConstants.setBold(h5, true);
        Style h6 = doc.addStyle("h6", base);
        StyleConstants.setFontSize(h6, 14);
        StyleConstants.setBold(h6, true);
        Style tiny = doc.addStyle("tiny", base);
        StyleConstants.setBold(tiny, false);
        StyleConstants.setFontSize(tiny, 6);
        Style code = doc.addStyle("code", base);
        StyleConstants.setFontFamily(code, this.monospacedFontFamily);
        StyleConstants.setFontSize(code, this.monospacedFontSize);

        this.textComponentToStyle.setDocument(doc);
    }

    /**
     * Register the configs used by the styler.
     *
     * @param configProvider The config provider to register with.
     */
    public void registerConfigs(ConfigProvider configProvider) {
        configProvider.registerConfig(monospacedFontConfig, this.monospacedFontConfigChanged);
        configProvider.registerConfig(monospacedFontSizeConfig, this.monospacedFontSizeConfigChanged);
        configProvider.registerConfig(markdownFormatWhileEditingConfig, this.markdownFormatWhileEditingConfigChanged);
        configProvider.registerConfig(makeStylingCharsTinyConfig, this.makeStylingCharsTinyConfigChanged);
    }

    /**
     * Unregister the configs used by the styler.
     *
     * @param configProvider The config provider to unregister with.
     */
    public void unregisterConfigs(ConfigProvider configProvider) {
        configProvider.unregisterConfig(monospacedFontConfig, this.monospacedFontConfigChanged);
        configProvider.unregisterConfig(monospacedFontSizeConfig, this.monospacedFontSizeConfigChanged);
        configProvider.unregisterConfig(markdownFormatWhileEditingConfig, this.markdownFormatWhileEditingConfigChanged);
        configProvider.unregisterConfig(makeStylingCharsTinyConfig, this.makeStylingCharsTinyConfigChanged);
    }

    /**
     * Enables styling (on by default)
     */
    @Override
    public void enable() {
        this.enabled = true;
    }

    /**
     * Disables styling (should be done while loading document!)
     */
    @Override
    public void disable() {
        this.enabled = false;
    }

    /**
     * Styles the whole document.
     */
    @Override
    public void styleDocument() {
        try {
            StyledDocument doc = (StyledDocument)this.textComponentToStyle.getDocument();

            String text = doc.getText(0, doc.getLength());

            ParagraphBounds bounds = new ParagraphBounds();
            bounds.start = 0;
            bounds.end = text.length() - 1;
            styleDocument(bounds, text);
        }
        catch (BadLocationException ble) {
            ble.printStackTrace(System.err);
        }
    }

    /**
     * Styles the current paragraph.
     */
    @Override
    public void styleCurrentParagraph() {
        try {
            StyledDocument doc = (StyledDocument)this.textComponentToStyle.getDocument();

            String text = doc.getText(0, doc.getLength());

            ParagraphBounds bounds = findParagraphBounds(this.textComponentToStyle.getCaretPosition() - 1, text);
            styleDocument(bounds, text);

        }
        catch (BadLocationException ble) {
            ble.printStackTrace(System.err);
        }
    }

    /**
     * Styles part of the document specified by the paragraph bounds.
     *
     * @param bounds The bounds for styling.
     * @param text The text of the document to style.
     */
    private void styleDocument(ParagraphBounds bounds, String text) {
        // Speedup by disabling component while styling.
        this.textComponentToStyle.setEnabled(false);

        StyledDocument doc = (StyledDocument)this.textComponentToStyle.getDocument();
        Style base = StyleContext.
                getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);

        try {
            if (text == null) {
                text = doc.getText(0, doc.getLength());
            }
            doc.setCharacterAttributes(bounds.start, bounds.end - bounds.start, base, true);
            if (this.markdownFormatWhileEditing) {
                for (int pos = bounds.start; pos <= bounds.end; pos++) {
                    try {
                        char c = text.charAt(pos);
                        char p = pos == 0 ? text.charAt(pos) : text.charAt(pos - 1);
                        char pp = pos <= 1 ? text.charAt(pos) : text.charAt(pos - 2);

                        // -- Header --------
                        if (c == '#' && p != '\\') {
                            int cnt = 0;
                            int spos = pos;
                            while (pos < text.length() && text.charAt(pos) == '#') {
                                ++cnt;
                                ++pos;
                            }
                            int epos = getPosOfNext(text, pos, '\n');
                            Style header = null;

                            // Only style if it starts at the beginning of a "paragraph".
                            int hsize = 0;
                            if (pos <= 5 || text.charAt(pos - cnt - 1) == '\n' || text.charAt(pos - cnt - 1) == '\r') {
                                switch(cnt) {
                                    case 1:
                                        header = doc.getStyle("h1");
                                        hsize = 2;
                                        break;
                                    case 2:
                                        header = doc.getStyle("h2");
                                        hsize = 3;
                                        break;
                                    case 3:
                                        header = doc.getStyle("h3");
                                        hsize = 4;
                                        break;
                                    case 4:
                                        header = doc.getStyle("h4");
                                        hsize = 5;
                                        break;
                                    case 5:
                                        header = doc.getStyle("h5");
                                        hsize = 6;
                                        break;
                                    default:
                                        hsize = 7;
                                        header = doc.getStyle("h6");
                                }
                            }
                            if (header != null) {
                                if (this.makeStylingCharsTiny) {
                                    doc.setCharacterAttributes(spos + hsize, epos - (spos + hsize), header, true);
                                    Style hstyle = doc.getStyle("tiny");
                                    doc.setCharacterAttributes(spos, hsize, hstyle, true);
                                }
                                else {
                                    doc.setCharacterAttributes(spos, epos - spos, header, true);
                                }
                            }
                            pos = epos;
                        }

                        // -- Monospaced --------
                        else if (pos >= 4 && c == ' ' && text.charAt(pos + 1) == ' ' &&
                                text.charAt(pos + 2) == ' ' && text.charAt(pos + 3) == ' ') {
                            if (text.charAt(pos - 1) == '\n' || pos == 4) {
                                if (!getStartOfParagraphText(pos, bounds, text).trim().startsWith("* ")) {
                                    int epos = getEndOfParagraph(text, pos);
                                    Style codeStyle = doc.getStyle("code");
                                    doc.setCharacterAttributes(pos, epos - pos, codeStyle, true);
                                    pos = epos + 1;
                                }
                            }
                        }

                        // -- Bold italic (when not escaped, but when double escaped) --------
                        else if ((c == '_' || c == '*') && (p != '\\' || pp == '\\')) {
                            boolean bold = false;
                            int spos = pos;
                            if ((pos + 1) < text.length() && text.charAt(pos + 1) == c) {
                                bold = true;
                                ++pos;
                            }
                            else if (c == '*' && (pos + 1) < text.length() && text.charAt(pos + 1) == ' ') {
                                continue; // Skip if there is a spaced after *. This means it is a list entry.
                            }

                            char endChar = c;
                            int epos = getPosOfNext(text, pos + 1, endChar);

                            if (bold) {
                                Style boldStyle = doc.getStyle("bold");
                                doc.setCharacterAttributes(spos + 2, epos - spos - 2, boldStyle, true);
                                if (this.makeStylingCharsTiny) {
                                    Style tiny = doc.getStyle("tiny");
                                    doc.setCharacterAttributes(spos, 2, tiny, true);
                                    doc.setCharacterAttributes(epos, 2, tiny, true);
                                }
                            }
                            else {
                                Style emphasisStyle = doc.getStyle("emphasis");
                                doc.setCharacterAttributes(spos + 1, epos - spos - 1, emphasisStyle, true);
                                if (this.makeStylingCharsTiny) {
                                    Style tiny = doc.getStyle("tiny");
                                    doc.setCharacterAttributes(spos, 1, tiny, true);
                                    doc.setCharacterAttributes(epos, 1, tiny, true);
                                }
                            }
                            pos = epos + 1;
                        }
                    }
                    catch (IndexOutOfBoundsException iobe) {/* we hide these intentionally! */}
                }
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace(System.err);
        }

        this.textComponentToStyle.setEnabled(true);
    }


    /**
     * Returns the position of the next 'n'.
     *
     * @param text The text to search.
     * @param start The position to start at.
     * @param n The character to find.
     */
    private int getPosOfNext(String text, int start, char n) {
        int npos = start;
        try {
            char prev = ' ';
            char prevprev = ' ';
            while (npos < text.length() && !(prev == '\n' && prevprev == '\n') && text.charAt(npos) != n) {
                prevprev = prev;
                prev = text.charAt(npos);
                ++npos;
            }
        }
        catch (IndexOutOfBoundsException iobe) {/*OK*/}

        return npos;
    }

    /**
     * Returns the position of the end of the paragraph.
     *
     * @param text The text to search.
     * @param start The position to start searching at.
     */
    private int getEndOfParagraph(String text, int start) {
        int npos = start;

        try {
            while (npos < text.length() && (safeGetChar(text, npos) != '\n' && safeGetChar(text, npos + 1) != '\n')) {
                ++npos;
            }
        }
        catch (IndexOutOfBoundsException iobe) {/*OK*/}

        if (npos >= text.length()) {
            npos = text.length() - 1;
        }

        return npos;
    }

    /**
     * Gets the char from the specified string at the specified position without the risk of a StringIndexOutOfBoundsException!
     * By default a space is returned if such an exception occur. This makes the beginning and end of the text much less messy
     * to handle.
     *
     * @param text The text to get the character from.
     * @param position The position in the text of the character to get.
     */
    private char safeGetChar(String text, int position) {
        try {
            return text.charAt(position);
        }
        catch (StringIndexOutOfBoundsException siobe) {
            return ' ';
        }
    }

    /**
     * Returns true if the character at the specified position in the specified text contains a "new line" character.
     *
     * @param text The text to the character from.
     * @param position The position of the character to check.
     */
    private boolean checkPgBound(String text, int position) {
        return _intCheckPgBound(text, position) && _intCheckPgBound(text, position + 1);
    }

    private boolean _intCheckPgBound(String text, int position) {
        //System.out.println("\"\\n\\r\".indexOf(safeGetChar(text, " + position + ")) = '"+ safeGetChar(text, position) + "'");
        return "\n\r".indexOf(safeGetChar(text, position)) >= 0;
    }

    /**
     * Find the beginning of the current paragraph and then the end of it, and returns those found bounds.
     *
     * @param currentPos The current position in the text to scan.
     * @param text The text to scan.
     */
    private ParagraphBounds findParagraphBounds(int currentPos, String text) {
        //System.out.println("CurrentPos: " + currentPos);
        if (currentPos < 0) currentPos = 0;
        ParagraphBounds bounds = new ParagraphBounds();

        // Find beginning
        while (currentPos > 0 && !checkPgBound(text, currentPos)) {
            --currentPos;
        }
        bounds.start = text.length() > currentPos ? (text.charAt(currentPos) == ' ' ? currentPos + 1 : currentPos) : currentPos;

        // Find end
        currentPos = bounds.start + 1;
        while (currentPos < text.length() && !checkPgBound(text, currentPos)) {
            ++currentPos;
        }
        bounds.end = text.length() > currentPos ? (text.charAt(currentPos) == ' ' ? currentPos - 1 : currentPos) : currentPos;

        //System.out.println("" + bounds);

        return bounds;
    }

    /**
     * Returns the paragraph as a String.
     *
     * @param bounds The paragraph bounds.
     * @param text The whole document text.
     */
    private String getParagraphText(ParagraphBounds bounds, String text) {
        return text.substring(bounds.start, bounds.end);
    }

    /**
     * Returns the first 10 characters of the paragraph as a String.
     *
     * @param currentPos The current position.
     * @param bounds The paragraph bounds.
     * @param text The whole document text.
     */
    private String getStartOfParagraphText(int currentPos, ParagraphBounds bounds, String text) {
        if (bounds.start == 0) { // Possibly the whole document!
            bounds = findParagraphBounds(currentPos, text);
        }
        return text.substring(bounds.start, bounds.start + 60);
    }
}
