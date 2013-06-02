/* 
 * 
 * PROJECT
 *     Name
 *         Editor
 *     
 *     Code Version
 *         1.2.6
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
package se.natusoft.doc.markdowndoc.editor.functions;

import se.natusoft.doc.markdown.api.Generator;
import se.natusoft.doc.markdown.api.Parser;
import se.natusoft.doc.markdown.exception.GenerateException;
import se.natusoft.doc.markdown.exception.ParseException;
import se.natusoft.doc.markdown.generator.HTMLGenerator;
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions;
import se.natusoft.doc.markdown.model.Doc;
import se.natusoft.doc.markdown.parser.MarkdownParser;
import se.natusoft.doc.markdowndoc.editor.MDECaret;
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.config.ConfigChanged;
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry;
import se.natusoft.doc.markdowndoc.editor.config.DoubleConfigEntry;
import se.natusoft.doc.markdowndoc.editor.config.ValidSelectionConfigEntry;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This provides formatted markdown preview function.
 */
public class PreviewFunction implements EditorFunction, KeyListener {
    //
    // Private Members
    //

    private Editor editor;
    private JToggleButton previewButton;
    private JEditorPane preview;
    private boolean enabled = false;

    //
    // Configs
    //

    private static ValidSelectionConfigEntry fontConfig =
            new ValidSelectionConfigEntry("preview.pane.font", "The preview font to use.", "Helvetica",
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        public String[] validValues() {
                            GraphicsEnvironment gEnv = GraphicsEnvironment
                                    .getLocalGraphicsEnvironment();
                            return gEnv.getAvailableFontFamilyNames();
                        }
                    }
            );


    private static DoubleConfigEntry fontSizeConfig =
            new DoubleConfigEntry("preview.pane.font.size", "The size of the preview font.", 16.0, 8.0, 50.0);

    //
    // Config callbacks
    //

    private ConfigChanged fontConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            ((HTMLEditorKit) preview.getEditorKit()).getStyleSheet().addRule(
                    "body {font-family: " + ce.getValue() + "; font-size: " +
                            PreviewFunction.this.fontSizeConfig.getValue() +
                            "; margin-left: 50; margin-right:50; margin-top:50; margin-bottom:50; }");
            SwingUtilities.updateComponentTreeUI(preview);
        }
    };

    private ConfigChanged fontSizeConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            ((HTMLEditorKit) preview.getEditorKit()).getStyleSheet().addRule(
                    "body {font-family: " + PreviewFunction.this.fontConfig.getValue() + "; font-size: " +
                            ce.getValue() + "; margin-left: 50; margin-right:50; margin-top:50; margin-bottom:50; }");
            SwingUtilities.updateComponentTreeUI(preview);
        }
    };

    //
    // Constructors
    //

    public PreviewFunction() {
        Icon previewIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddpreview.png"));
        this.previewButton = new JToggleButton(previewIcon);
        this.previewButton.setToolTipText("Preview (Meta-P)");
        this.previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                PreviewFunction.this.enabled = !PreviewFunction.this.previewButton.isSelected();
                perform();
            }
        });

        this.preview = new JEditorPane();
        this.preview.setEditable(false);
        this.preview.setCaret(new MDECaret());
        this.preview.setContentType("text/html");
        this.preview.addKeyListener(this);
    }

    //
    // Methods
    //

    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;

        this.editor.getConfigProvider().registerConfig(fontConfig, this.fontConfigChanged);
        this.editor.getConfigProvider().registerConfig(fontSizeConfig, this.fontSizeConfigChanged);
    }

    @Override
    public void close() {
        this.editor.getConfigProvider().unregisterConfigCallback(fontConfig, this.fontConfigChanged);
        this.editor.getConfigProvider().unregisterConfigCallback(fontSizeConfig, this.fontSizeConfigChanged);
    }

    @Override
    public String getGroup() {
        return ToolBarGroups.preview.name();
    }

    @Override
    public String getName() {
        return "Preview";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.previewButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_P;
    }

    @Override
    public void perform() throws FunctionException {
        if (!this.enabled) {
            this.enabled = true;
            this.previewButton.setSelected(this.enabled);
            previewOn();
        } else {
            this.enabled = false;
            this.previewButton.setSelected(this.enabled);
            previewOff();
            this.editor.requestEditorFocus();
        }
    }

    private void previewOff() {
        this.editor.showEditorComponent();

        this.editor.enableToolBarGroup(ToolBarGroups.format.name());
    }

    private void previewOn() {
        try {
            String html = convertEditorContentToHTML();
            // This is a workaround for JEditorPane not supporting a CSS rule for making
            // <code> sensible! It insists on indenting the first row with a tab! With
            // this workaround it will be indenting a space. The drawback is that there
            // will be one more empty line, but this still looks better.
            html = html.replaceAll("<code>", "<code>\n&nbsp;");
            //System.out.println(html);
            this.preview.setText(html);

            // Set a relatively correct position in the HTML view based on the
            // edit view.
            this.preview.setLocation(this.editor.getCaretLocation());

            this.editor.showOtherComponent(this.preview);

            this.editor.disableToolBarGroup(ToolBarGroups.format.name());

            this.preview.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(), e.getMessage(), "Preview error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String convertEditorContentToHTML() throws IOException, ParseException, GenerateException {
        String markdownText = this.editor.getEditorContent();
        ByteArrayInputStream markDownStream = new ByteArrayInputStream(markdownText.getBytes());

        Parser parser = new MarkdownParser();
        Doc document = new Doc();
        Properties parserOptions = new Properties();
        parser.parse(document, markDownStream, parserOptions);
        markDownStream.close();

        ByteArrayOutputStream htmlStream = new ByteArrayOutputStream();
        Generator generator = new HTMLGenerator();
        HTMLGeneratorOptions htmlOpts = new HTMLGeneratorOptions();
        htmlOpts.setInlineCSS(true);
        htmlOpts.setCss(null);
        htmlOpts.setPrimitiveHTML(true);
        htmlOpts.setResultFile(null);
        generator.generate(document, htmlOpts, null, htmlStream);
        htmlStream.close();

        return new String(htmlStream.toByteArray());
    }

    // KeyListener methods

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getModifiers() == KeyEvent.META_MASK && e.getKeyCode() == KeyEvent.VK_P) {
            perform();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
