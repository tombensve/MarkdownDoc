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
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.api.Generator
import se.natusoft.doc.markdown.exception.GenerateException
import se.natusoft.doc.markdown.generator.HTMLGenerator
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigChanged
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.functions.export.*

import javax.swing.*
import javax.swing.border.SoftBevelBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.lang.reflect.Field

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * Provides a function that exports to HTML.
 */
@CompileStatic
@TypeChecked
public class ExportToHTMLFunction extends AbstractExportFunction implements EditorFunction, Configurable {
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
    private HTMLData htmlData = new HTMLData(getLocalServiceData())

    // The following are referenced from GUI callbacks and thus must be part of the instance.

    /** The PDF meta data / options dialog. */
    private JWindow htmlMetaDataDialog = null

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.export.html.keyboard.shortcut", "Export HTML keyboard shortcut",
                    new KeyboardKey("Ctrl+H"), CONFIG_GROUP_KEYBOARD)

    private Closure keyboardShortcutConfigChanged = { ConfigEntry ce ->
        updateTooltipText()
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    public void registerConfigs(ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    public void unregisterConfigs(ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
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

        public HTMLData(DelayedServiceData delayedServiceData) {
            super(delayedServiceData)
            ((ExportFileValue)css).delayedServiceData = delayedServiceData
            ((ExportFileValue)fileLinks).delayedServiceData = delayedServiceData
        }
    }

    //
    // Constructors
    //

    public ExportToHTMLFunction() {
        super(GENERATED_HTML_FILE)
        Icon htmlIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddhtml.png"))
        this.htmlButton = new JButton(htmlIcon)
        htmlButton.addActionListener({ ActionEvent actionEvent -> perform() } as ActionListener)
        updateTooltipText()
    }

    //
    // Methods
    //

    private void updateTooltipText() {
        htmlButton.setToolTipText("Export as HTML (" + keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    @Override
    public String getGroup() {
        return ToolBarGroups.EXPORT.name()
    }

    @Override
    public String getName() {
        return "Export to HTML"
    }

    @Override
    public KeyboardKey getKeyboardShortcut() {
        return keyboardShortcutConfig.getKeyboardKey()
    }

    @Override
    public JComponent getToolBarButton() {
        return this.htmlButton
    }

    /**
     * Executes this function.
     *
     * @throws FunctionException on failure to perform function.
     */
    @Override
    public void perform() throws FunctionException {
        this.exportFile = getExportOutputFile("HTML", "html", "html", "htm")

        if (this.exportFile != null) {
            this.htmlMetaDataDialog = new JWindow(this.editor.getGUI().getWindowFrame())
            this.htmlMetaDataDialog.setLayout(new BorderLayout())

            JPanel borderPanel = new JPanel(new BorderLayout())
            borderPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED))
            this.htmlMetaDataDialog.add(borderPanel, BorderLayout.CENTER)

            this.htmlData.loadDataValues()
            this.htmlData.setBackgroundColor(this.editor.getGUI().getWindowFrame().getBackground())

            JPanel dataLabelPanel = new JPanel(new GridLayout(this.htmlData.exportDataValues.size(),1))
            borderPanel.add(dataLabelPanel, BorderLayout.WEST)

            JPanel dataValuePanel = new JPanel(new GridLayout(this.htmlData.exportDataValues.size(),1))
            borderPanel.add(dataValuePanel, BorderLayout.CENTER)

            borderPanel.add(Box.createRigidArea(new Dimension(12, 12)), BorderLayout.EAST)

            this.htmlData.exportDataValues.each { ExportDataValue exportDataValue ->
                dataLabelPanel.add(exportDataValue.labelComp)
                dataValuePanel.add(exportDataValue.valueComp)
            }

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
            JButton generateButton = new JButton("Generate")
            generateButton.addActionListener({ ActionEvent actionEvent ->
                this.htmlMetaDataDialog.setVisible(false)
                generateHTML()
            } as ActionListener)
            buttonPanel.add(generateButton)
            JButton cancelButton = new JButton("Cancel")
            cancelButton.addActionListener({ ActionEvent actionEvent ->
                this.htmlMetaDataDialog.setVisible(false)
            } as ActionListener)
            buttonPanel.add(cancelButton)

            borderPanel.add(buttonPanel, BorderLayout.SOUTH)

            // Set initial values to last saved values for the specified file.
            if (this.editor.getCurrentFile() != null) {
                this.htmlData.loadExportData(this.editor.getCurrentFile())
            }

            this.htmlMetaDataDialog.setVisible(true)
            this.htmlMetaDataDialog.setSize(this.htmlMetaDataDialog.getPreferredSize())

            Rectangle mainBounds = this.editor.getGUI().getWindowFrame().getBounds()
            int x = (int)mainBounds.x + (int)(mainBounds.width / 2) - (int)(this.htmlMetaDataDialog.getWidth() / 2)
            int y = (int)mainBounds.y + 70
            this.htmlMetaDataDialog.setLocation(x, y)

        }
    }

    /**
     * Initiates HTML generation and handles any failures with an error dialog.
     */
    private void generateHTML() {
        try {
            _generateHTML()
        }
        catch (RuntimeException re) {
            re.printStackTrace(System.err)
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(), re.getMessage(), "Failed to save HTML!", JOptionPane.ERROR_MESSAGE)
        }
    }

    /**
     * Actually performs the PDF generation using MarkdownDocs PDF generator.
     */
    private void _generateHTML() {
        Generator generator = new HTMLGenerator()
        HTMLGeneratorOptions htmlOpts = new HTMLGeneratorOptions()
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
        catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe)
        }
        catch (GenerateException ge) {
            throw new RuntimeException(ge.getMessage(), ge)
        }
        finally {
            try {if (htmlStream!= null) htmlStream.close()} catch (IOException cioe) {}
        }

        if (this.editor.getCurrentFile() != null) {
            this.htmlData.saveExportData(this.editor.getCurrentFile())
        }

        if (this.htmlData.openResult.getValue().equals("true")) {
            Desktop desktop = Desktop.getDesktop()
            try {
                desktop.open(this.exportFile)
            }
            catch (IOException ioe) {
                JOptionPane.showMessageDialog(
                        this.editor.getGUI().getWindowFrame(), ioe.getMessage(), "Failed to open PDF!", JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
