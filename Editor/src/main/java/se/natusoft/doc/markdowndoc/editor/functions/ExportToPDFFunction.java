package se.natusoft.doc.markdowndoc.editor.functions;

import se.natusoft.doc.markdowndoc.editor.ToolBarGroups;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Provides a function that exports to PDF.
 */
public class ExportToPDFFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton pdfButton;

    //
    // Constructors
    //

    public ExportToPDFFunction() {
        Icon pdfIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddpdf.png"));
        this.pdfButton = new JButton(pdfIcon);
        this.pdfButton.setToolTipText("Export as PDF (Alt-P)");
        this.pdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                perform();
            }
        });
    }

    //
    // Methods
    //

    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    @Override
    public String getGroup() {
        return ToolBarGroups.export.name();
    }

    @Override
    public String getName() {
        return "Export to PDF";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.pdfButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.CTRL_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_P;
    }

    @Override
    public void perform() throws FunctionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
