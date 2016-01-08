/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *
 *     Code Version
 *         1.4
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
package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import net.iharder.dnd.FileDrop
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.exception.ParseException
import se.natusoft.doc.markdown.generator.HTMLGenerator
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.parser.MarkdownParser
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.*
import se.natusoft.doc.markdowndoc.editor.config.*
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.gui.MDECaret

import javax.swing.*
import javax.swing.text.html.HTMLEditorKit
import java.awt.*
import java.awt.event.*

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD
import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_PREVIEW

/**
 * This provides formatted markdown preview function.
 */
@CompileStatic
@TypeChecked
class JEditorPanePreviewFunction implements EditorFunction, KeyListener, MouseMotionProvider, Configurable {

    //
    // Private Members
    //

    private JToggleButton previewButton
    private JEditorPane preview
    private boolean enabled = false

    //
    // Properties
    //

    /** The editor this function is bound to. */
    Editor editor

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.preview.keyboard.shortcut", "Preview keyboard shortcut",
                    new KeyboardKey("Ctrl+F"), CONFIG_GROUP_KEYBOARD)

    private static ValidSelectionConfigEntry fontConfig =
            new ValidSelectionConfigEntry("preview.pane.font", "The preview font to use.", "Helvetica",
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        ValidSelectionConfigEntry.Value[] validValues() {
                            final GraphicsEnvironment gEnv = GraphicsEnvironment
                                    .getLocalGraphicsEnvironment()
                            return ValidSelectionConfigEntry.convertToValues(gEnv.getAvailableFontFamilyNames())
                        }
                    },
                    CONFIG_GROUP_PREVIEW
            )


    private static DoubleConfigEntry fontSizeConfig =
            new DoubleConfigEntry("preview.pane.font.size", "The size of the preview font.", 16.0, 8.0, 50.0, CONFIG_GROUP_PREVIEW)

    private static IntegerConfigEntry topMarginConfig = new IntegerConfigEntry("preview.pane.top.margin",
            "The top margin.", 40, 0, 500, CONFIG_GROUP_PREVIEW)

    private static IntegerConfigEntry bottomMarginConfig = new IntegerConfigEntry("preview.pane.bottom.margin",
            "The bottom margin.", 40, 0, 500, CONFIG_GROUP_PREVIEW)

    private static IntegerConfigEntry leftMarginConfig = new IntegerConfigEntry("preview.pane.left.margin",
            "The left margin.", 60, 0, 500, CONFIG_GROUP_PREVIEW)

    private static IntegerConfigEntry rightMarginConfig = new IntegerConfigEntry("preview.pane.right.margin",
            "The right margin.", 60, 0, 500, CONFIG_GROUP_PREVIEW)


    private Closure keyboardShortcutConfigChanged = { final ConfigEntry ce ->
        updateTooltipText()
    }

    private Closure fontConfigChanged = { final ConfigEntry ce ->
        ((HTMLEditorKit) preview.getEditorKit()).getStyleSheet().addRule(
                "body {font-family: " + ce.getValue() + " font-size: " +
                        fontSizeConfig.getValue() + " }")
        SwingUtilities.updateComponentTreeUI(preview)
    }

    private Closure fontSizeConfigChanged = { final ConfigEntry ce ->
        ((HTMLEditorKit) preview.getEditorKit()).getStyleSheet().addRule(
                "body {font-family: " + fontConfig.getValue() + " }")
        SwingUtilities.updateComponentTreeUI(preview)
    }

    private Closure topMarginConfigChanged = { final ConfigEntry ce ->
        this.preview.margin.top = ((IntegerConfigEntry)ce).intValue
        this.preview.revalidate()
    }

    private Closure bottomMarginConfigChanged = { final ConfigEntry ce ->
        this.preview.margin.bottom = ((IntegerConfigEntry)ce).intValue
        this.preview.revalidate()
    }

    private Closure leftMarginConfigChanged = { final ConfigEntry ce ->
        this.preview.margin.left = ((IntegerConfigEntry)ce).intValue
        this.preview.revalidate()
    }

    private Closure rightMarginConfigChanged = { final ConfigEntry ce ->
        this.preview.margin.right = ((IntegerConfigEntry)ce).intValue
        this.preview.revalidate()
    }


    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, this.keyboardShortcutConfigChanged)
        configProvider.registerConfig(fontConfig, this.fontConfigChanged)
        configProvider.registerConfig(fontSizeConfig, this.fontSizeConfigChanged)
        configProvider.registerConfig(topMarginConfig, topMarginConfigChanged)
        configProvider.registerConfig(bottomMarginConfig, bottomMarginConfigChanged)
        configProvider.registerConfig(leftMarginConfig, leftMarginConfigChanged)
        configProvider.registerConfig(rightMarginConfig, rightMarginConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, this.keyboardShortcutConfigChanged)
        configProvider.unregisterConfig(fontConfig, this.fontConfigChanged)
        configProvider.unregisterConfig(fontSizeConfig, this.fontSizeConfigChanged)
        configProvider.unregisterConfig(topMarginConfig, topMarginConfigChanged)
        configProvider.unregisterConfig(bottomMarginConfig, bottomMarginConfigChanged)
        configProvider.unregisterConfig(leftMarginConfig, leftMarginConfigChanged)
        configProvider.unregisterConfig(rightMarginConfig, rightMarginConfigChanged)
    }

    //
    // Constructors
    //

    JEditorPanePreviewFunction() {
        final Icon previewIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdd2preview.png"))
        this.previewButton = new JToggleButton(previewIcon)
        this.previewButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(final ActionEvent ignored) {
                JEditorPanePreviewFunction.this.enabled = !JEditorPanePreviewFunction.this.previewButton.isSelected()
                perform()
            }
        })

        this.preview = new JEditorPane()
        this.preview.editable = false
        this.preview.caret = new MDECaret()
        this.preview.contentType = "text/html"
        this.preview.margin = new Insets(
                                  topMarginConfig.intValue,
                                  leftMarginConfig.intValue,
                                  bottomMarginConfig.intValue,
                                  rightMarginConfig.intValue
                              )

        this.preview.addKeyListener(this)

        // We don't need to save the FileDrop instance since it installs itself into the first argument.
        // Do also note that FileDrop is an external library residing in the Editor/lib catalog since
        // it is not available on maven central.

        //noinspection GroovyResultOfObjectAllocationIgnored
        new FileDrop(this.preview, new FileDrop.Listener() {
            void filesDropped(final File[] files) {
                if (files.length >= 1) {
                    showFile(files[0])
                }
            }
        })

        updateTooltipText()
    }

    //
    // Methods
    //

    private void updateTooltipText() {
        this.previewButton.setToolTipText("Preview (" + keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    /**
     *  This will format and show the dropped file assuming it is a markdown file.
     *  This will not affect the content of the editorPane. Exiting the preview and entering
     *  it again will again preview the editorPane content. This is just a convenience for
     *  reading markdown files formatted by dropping them on preview mode.
     *
     * @param file
     */
    private void showFile(@NotNull final File file) {
        if (file.name.endsWith("md") || file.name.endsWith("markdown")) {
            try {
                final StringBuilder markdownText = new StringBuilder()
                final BufferedReader mdFileReader = new BufferedReader(new FileReader(file))
                String line = mdFileReader.readLine()
                while (line != null) {
                    markdownText.append(line)
                    markdownText.append("\n")
                    line = mdFileReader.readLine()
                }
                mdFileReader.close()

                final String html = markdownToHTML(markdownText.toString())
                this.preview.text = html
                this.preview.setLocation(0,0)
            }
            catch (final ParseException pe) {
                pe.printStackTrace(System.err)
            }
            catch (final GenerateException ge) {
                ge.printStackTrace(System.err)
            }
            catch (final IOException ioe) {
                ioe.printStackTrace(System.err)
            }
        }
    }

    @Override
    void close() {}

    @Override
    @NotNull String getGroup() {
        ToolBarGroups.PREVIEW.name()
    }

    @Override
    @NotNull String getName() {
        "Preview"
    }

    @Override
    @NotNull JComponent getToolBarButton() {
        this.previewButton
    }

    /**
     * Returns the keyboard shortcut for the function.
     */
    @Override
    @NotNull KeyboardKey getKeyboardShortcut() {
        keyboardShortcutConfig.getKeyboardKey()
    }

    @Override
    void perform() throws FunctionException {
        if (!this.enabled) {
            this.enabled = true
            this.previewButton.selected = this.enabled
            previewOn()
        } else {
            this.enabled = false
            this.previewButton.selected = this.enabled
            previewOff()
            this.editor.requestEditorFocus()
        }
    }

    /**
     * Restores the edit view.
     */
    private void previewOff() {
        this.editor.showEditorComponent()

        this.editor.enableToolBarGroup(ToolBarGroups.FORMAT.name())
    }

    /**
     * Replaces the editor component with a JEditorPane in HTML mode holding a HTML formatted
     * version of the markdown in the standard edit JEditorPane.
     */
    private void previewOn() {
        try {
            final String html = markdownToHTML(this.editor.getEditorContent())
            this.preview.setText(html)

            // Set a relatively correct position in the HTML view based on the
            // edit view.
            this.preview.location = this.editor.caretLocation

            this.editor.showOtherComponent(this.preview)

            this.editor.disableToolBarGroup(ToolBarGroups.FORMAT.name())

            this.preview.requestFocus()
        } catch (final Exception e) {
            e.printStackTrace(System.err)
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(), e.getMessage(), "Preview error!", JOptionPane.ERROR_MESSAGE)
        }
    }

    /**
     * Takes a string of markdown and returns a string for formatted HTML.
     *
     * @param markdownText The markdown to convert to HTML.
     *
     * @return The HTML converted markdown.
     *
     * @throws IOException
     * @throws ParseException
     * @throws GenerateException
     */
    private static @NotNull String markdownToHTML(@NotNull final String markdownText)
            throws IOException, ParseException, GenerateException {

        final ByteArrayInputStream markDownStream = new ByteArrayInputStream(markdownText.getBytes())

        final Parser parser = new MarkdownParser()
        final Doc document = new Doc()
        final Properties parserOptions = new Properties()
        parser.parse(document, markDownStream, parserOptions)
        markDownStream.close()

        final ByteArrayOutputStream htmlStream = new ByteArrayOutputStream()
        final Generator generator = new HTMLGenerator()
        final HTMLGeneratorOptions htmlOpts = new HTMLGeneratorOptions()
        htmlOpts.inlineCSS = true
        htmlOpts.css = null
        htmlOpts.primitiveHTML = true
        htmlOpts.resultFile = null
        generator.generate(document, htmlOpts, null, htmlStream)
        htmlStream.close()

        String html = new String(htmlStream.toByteArray())

        // This is a workaround for JEditorPane not supporting a CSS rule for making
        // <code> sensible! It insists on indenting the first row with a tab! With
        // this workaround it will be indenting a space. The drawback is that there
        // will be one more empty line, but this still looks better.
        html = html.replaceAll("<code>", "<code>\n&nbsp")

        return html
    }

    // KeyListener methods

    @Override
    void keyTyped(final KeyEvent e) {
        ((KeyListener)this.editor).keyTyped(e)
    }

    @Override
    void keyPressed(final KeyEvent e) {
        ((KeyListener)this.editor).keyPressed(e)
    }

    @Override
    void keyReleased(final KeyEvent e) {
        ((KeyListener)this.editor).keyReleased(e)
    }

    /**
     * Adds a mouse motion listener to receive mouse motion events.
     *
     * @param listener The listener to add.
     */
    @Override
    void addMouseMotionListener(@NotNull final MouseMotionListener listener) {
        this.preview.addMouseMotionListener(listener)
    }

    /**
     * Removes a mouse motion listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    void removeMouseMotionListener(@NotNull final MouseMotionListener listener) {
        this.preview.removeMouseMotionListener(listener)
    }
}
