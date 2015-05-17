/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.9
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
import net.iharder.dnd.FileDrop
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.exception.ParseException
import se.natusoft.doc.markdown.generator.HTMLGenerator
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.parser.MarkdownParser
import se.natusoft.doc.markdowndoc.editor.MDECaret
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.*
import se.natusoft.doc.markdowndoc.editor.config.*
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException

import javax.swing.*
import javax.swing.text.html.HTMLEditorKit
import java.awt.*
import java.awt.event.*

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_EDITING
import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD
import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_PREVIEW

/**
 * This provides formatted markdown preview function.
 */
@CompileStatic
public class JEditorPanePreviewFunction implements EditorFunction, KeyListener, MouseMotionProvider, Configurable {

    //
    // Private Members
    //

    private Editor editor
    private JToggleButton previewButton
    private JEditorPane preview
    private boolean enabled = false

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
                        public ValidSelectionConfigEntry.Value[] validValues() {
                            GraphicsEnvironment gEnv = GraphicsEnvironment
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


    private Closure keyboardShortcutConfigChanged = { ConfigEntry ce ->
        updateTooltipText()
    }

    private Closure fontConfigChanged = { ConfigEntry ce ->
        ((HTMLEditorKit) preview.getEditorKit()).getStyleSheet().addRule(
                "body {font-family: " + ce.getValue() + " font-size: " +
                        fontSizeConfig.getValue() + " }")
        SwingUtilities.updateComponentTreeUI(preview)
    }

    private Closure fontSizeConfigChanged = { ConfigEntry ce ->
        ((HTMLEditorKit) preview.getEditorKit()).getStyleSheet().addRule(
                "body {font-family: " + fontConfig.getValue() + " }")
        SwingUtilities.updateComponentTreeUI(preview)
    }

    private Closure topMarginConfigChanged = { ConfigEntry ce ->
        this.preview.margin.top = ((IntegerConfigEntry)ce).intValue
        this.preview.revalidate()
    }

    private Closure bottomMarginConfigChanged = { ConfigEntry ce ->
        this.preview.margin.bottom = ((IntegerConfigEntry)ce).intValue
        this.preview.revalidate()
    }

    private Closure leftMarginConfigChanged = { ConfigEntry ce ->
        this.preview.margin.left = ((IntegerConfigEntry)ce).intValue
        this.preview.revalidate()
    }

    private Closure rightMarginConfigChanged = { ConfigEntry ce ->
        this.preview.margin.right = ((IntegerConfigEntry)ce).intValue
        this.preview.revalidate()
    }


    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    public void registerConfigs(ConfigProvider configProvider) {
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
    public void unregisterConfigs(ConfigProvider configProvider) {
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

    public JEditorPanePreviewFunction() {
        Icon previewIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddpreview.png"))
        this.previewButton = new JToggleButton(previewIcon)
        this.previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*JEditorPanePreviewFunction.this.*/enabled = !/*JEditorPanePreviewFunction.this.*/previewButton.isSelected()
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

        new FileDrop(this.preview, new FileDrop.Listener() {
            public void filesDropped(java.io.File[] files) {
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
    private void showFile(File file) {
        if (file.getName().endsWith("md") || file.getName().endsWith("markdown")) {
            try {
                StringBuilder markdownText = new StringBuilder()
                BufferedReader mdFileReader = new BufferedReader(new FileReader(file))
                String line = mdFileReader.readLine()
                while (line != null) {
                    markdownText.append(line)
                    markdownText.append("\n")
                    line = mdFileReader.readLine()
                }
                mdFileReader.close()

                String html = markdownToHTML(markdownText.toString())
                this.preview.setText(html)
                this.preview.setLocation(0,0)
            }
            catch (ParseException pe) {
                pe.printStackTrace(System.err)
            }
            catch (GenerateException ge) {
                ge.printStackTrace(System.err)
            }
            catch (IOException ioe) {
                ioe.printStackTrace(System.err)
            }
        }
    }

    @Override
    public void setEditor(Editor editor) {
        this.editor = editor
    }

    @Override
    public void close() {
    }

    @Override
    public String getGroup() {
        return ToolBarGroups.PREVIEW.name()
    }

    @Override
    public String getName() {
        return "Preview"
    }

    @Override
    public JComponent getToolBarButton() {
        return this.previewButton
    }

    /**
     * Returns the keyboard shortcut for the function.
     */
    @Override
    public KeyboardKey getKeyboardShortcut() {
        return keyboardShortcutConfig.getKeyboardKey()
    }

    @Override
    public void perform() throws FunctionException {
        if (!this.enabled) {
            this.enabled = true
            this.previewButton.setSelected(this.enabled)
            previewOn()
        } else {
            this.enabled = false
            this.previewButton.setSelected(this.enabled)
            previewOff()
            this.editor.requestEditorFocus()
        }
    }

    private void previewOff() {
        this.editor.showEditorComponent()

        this.editor.enableToolBarGroup(ToolBarGroups.FORMAT.name())
    }

    private void previewOn() {
        try {
            String html = markdownToHTML(this.editor.getEditorContent())
            this.preview.setText(html)

            // Set a relatively correct position in the HTML view based on the
            // edit view.
            this.preview.setLocation(this.editor.getCaretLocation())

            this.editor.showOtherComponent(this.preview)

            this.editor.disableToolBarGroup(ToolBarGroups.FORMAT.name())

            this.preview.requestFocus()
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(), e.getMessage(), "Preview error!", JOptionPane.ERROR_MESSAGE)
        }
    }

    private String markdownToHTML(String markdownText) throws IOException, ParseException, GenerateException {
        ByteArrayInputStream markDownStream = new ByteArrayInputStream(markdownText.getBytes())

        Parser parser = new MarkdownParser()
        Doc document = new Doc()
        Properties parserOptions = new Properties()
        parser.parse(document, markDownStream, parserOptions)
        markDownStream.close()

        ByteArrayOutputStream htmlStream = new ByteArrayOutputStream()
        Generator generator = new HTMLGenerator()
        HTMLGeneratorOptions htmlOpts = new HTMLGeneratorOptions()
        htmlOpts.setInlineCSS(true)
        htmlOpts.setCss(null)
        htmlOpts.setPrimitiveHTML(true)
        htmlOpts.setResultFile(null)
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
    public void keyTyped(KeyEvent e) {
        ((KeyListener)this.editor).keyTyped(e)
    }

    @Override
    public void keyPressed(KeyEvent e) {
        ((KeyListener)this.editor).keyPressed(e)
    }

    @Override
    public void keyReleased(KeyEvent e) {
        ((KeyListener)this.editor).keyReleased(e)
    }

    /**
     * Adds a mouse motion listener to receive mouse motion events.
     *
     * @param listener The listener to add.
     */
    @Override
    public void addMouseMotionListener(MouseMotionListener listener) {
        this.preview.addMouseMotionListener(listener)
    }

    /**
     * Removes a mouse motion listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeMouseMotionListener(MouseMotionListener listener) {
        this.preview.removeMouseMotionListener(listener)
    }
}
