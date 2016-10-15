/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.0
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
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.HTMLGenerator
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.api.MouseMotionProvider
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.functions.export.*
import se.natusoft.doc.markdowndoc.editor.gui.ExportMetaDataDialog

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseMotionListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * Provides a function that exports to HTML.
 */
@CompileStatic
@TypeChecked
class ExportToHTMLFunction extends AbstractExportFunction implements EditorFunction, Configurable, MouseMotionProvider {
    //
    // Constants
    //

    /** The property key for the last generated HTML file. */
    private static final String GENERATED_HTML_FILE = "generated.html.file"

    //
    // Private Members
    //

    /** The toolbar button. */
    private JButton htmlButton

    /** Holds all input values for the generate PDF meta data dialog. */
    private final HTMLData htmlData = new HTMLData(getLocalServiceData())

    /** The PDF meta data / options dialog. */
    @SuppressWarnings("GroovyMissingReturnStatement")
    private ExportMetaDataDialog htmlMetaDataDialog =
            new ExportMetaDataDialog(exportData: this.htmlData, generate: { generateHTML() })

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.export.html.keyboard.shortcut", "Export HTML keyboard shortcut",
                    new KeyboardKey("Ctrl+H"), CONFIG_GROUP_KEYBOARD)

    private Closure keyboardShortcutConfigChanged = { final ConfigEntry ce ->
        updateTooltipText()
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
        this.htmlMetaDataDialog.registerConfigs(configProvider)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
        this.htmlMetaDataDialog.unregisterConfigs(configProvider)
    }

    /**
     * Adds a mouse motion listener to receive mouse motion events.
     *
     * @param listener The listener to add.
     */
    @Override
    void addMouseMotionListener(@NotNull final MouseMotionListener listener) {
        this.htmlMetaDataDialog.addMouseMotionListener(listener)
    }

    /**
     * Removes a mouse motion listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    void removeMouseMotionListener(@NotNull final MouseMotionListener listener) {
        this.htmlMetaDataDialog.removeMouseMotionListener(listener)
    }

    //
    // Inner Classes
    //

    /**
     * This holds components that will be added to the htmlMetaDataDialog in perform().
     */
    @CompileStatic
    private class HTMLData extends ExportData {

        private ExportDataValue inlineCSS = new ExportDataSelectValue("inline CSS:")
        private ExportDataValue css = new ExportFileValue(
                "CSS file:",
                "CSS"
        )
        private ExportDataValue fileLinks = new ExportFileValue(
                "'file:' links relative to:",
                "File links"
        )
        private ExportDataValue openResult = new ExportDataSelectValue("Open result:")

        HTMLData(@NotNull final DelayedServiceData delayedServiceData) {
            super(delayedServiceData)
            ((ExportFileValue)css).delayedServiceData = delayedServiceData
            ((ExportFileValue)fileLinks).delayedServiceData = delayedServiceData
        }
    }

    //
    // Constructors
    //

    ExportToHTMLFunction() {
        super(GENERATED_HTML_FILE)
        final Icon htmlIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdd2html.png"))
        this.htmlButton = new JButton(htmlIcon)
        htmlButton.addActionListener({ final ActionEvent actionEvent -> perform() } as ActionListener)
        updateTooltipText()
    }

    //
    // Methods
    //

    private void updateTooltipText() {
        htmlButton.setToolTipText("Export as HTML (" + keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    @Override
    @NotNull String getGroup() {
        ToolBarGroups.EXPORT.name()
    }

    @Override
    @NotNull String getName() {
        "Export to HTML"
    }

    @Override
    @NotNull KeyboardKey getKeyboardShortcut() {
        keyboardShortcutConfig.getKeyboardKey()
    }

    @Override
    @NotNull JComponent getToolBarButton() {
        this.htmlButton
    }

    /**
     * Executes this function.
     *
     * @throws FunctionException on failure to perform function.
     */
    @Override
    void perform() throws FunctionException {
        this.exportFile = getExportOutputFile("HTML", "html", "html", "htm")

        if (this.exportFile != null) {

            // Set initial values to last saved values for the specified file.
            if (editor.editable.file != null) {
                this.htmlData.loadExportData(editor.editable.file)
            }

            this.htmlMetaDataDialog.open(this.editor.GUI)
        }
    }

    /**
     * Initiates HTML generation and handles any failures with an error dialog.
     */
    private void generateHTML() {
        try {
            _generateHTML()
        }
        catch (final RuntimeException re) {
            re.printStackTrace(System.err)
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(), re.getMessage(), "Failed to save HTML!",
                    JOptionPane.ERROR_MESSAGE)
        }
    }

    /**
     * Actually performs the PDF generation using MarkdownDocs PDF generator.
     */
    private void _generateHTML() {
        final Generator generator = new HTMLGenerator()
        final HTMLGeneratorOptions htmlOpts = new HTMLGeneratorOptions()
        htmlOpts.setInlineCSS(Boolean.valueOf(this.htmlData.inlineCSS.getValue()))
        htmlOpts.setCss(this.htmlData.css.getValue())
        if (this.htmlData.fileLinks.getValue().trim().length() > 0) {
            htmlOpts.setMakeFileLinksRelativeTo(this.htmlData.fileLinks.getValue())
        }

        BufferedOutputStream htmlStream = null
        try {
            htmlStream = new BufferedOutputStream(new FileOutputStream(this.exportFile))
            generator.generate(getMarkdownDocument(), htmlOpts, null, htmlStream)
            htmlStream.flush()
        }
        catch (final IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe)
        }
        catch (final GenerateException ge) {
            throw new RuntimeException(ge.getMessage(), ge)
        }
        finally {
            try {if (htmlStream!= null) htmlStream.close()} catch (final IOException ignored) {}
        }

        if (this.editor?.editable?.file != null) {
            this.htmlData.saveExportData(this.editor.editable.file)
        }

        if (this.htmlData.openResult.getValue().equals("true")) {
            final Desktop desktop = Desktop.getDesktop()
            try {
                desktop.open(this.exportFile)
            }
            catch (final IOException ioe2) {
                JOptionPane.showMessageDialog(
                        this.editor.getGUI().getWindowFrame(), ioe2.getMessage(), "Failed to open HTML!",
                        JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    /**
     * Cleanup.
     */
    void close() {}
}
