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
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
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
        ((HTMLEditorKit)this.preview.getEditorKit()).getStyleSheet().addRule(
                "body {margin-left: 15; margin-right:15; margin-top:15; margin-bottom:15}");
        this.preview.addKeyListener(this);
    }

    //
    // Methods
    //

    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    @Override
    public String getToolBarGroup() {
        return ToolBarGroups.preview.name();
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
        }
        else {
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
            this.preview.setText(convertEditorContentToHTML());

            // Set a relatively correct position in the HTML view based on the
            // edit view.
            this.preview.setLocation(this.editor.getCaretLocation());

            this.editor.showOtherComponent(this.preview);

            this.editor.disableToolBarGroup(ToolBarGroups.format.name());

            this.preview.requestFocus();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this.editor.getWindowFrame(), e.getMessage(), "Preview error!", JOptionPane.ERROR_MESSAGE);
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
