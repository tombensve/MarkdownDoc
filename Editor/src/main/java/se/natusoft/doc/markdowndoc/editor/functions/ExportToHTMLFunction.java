package se.natusoft.doc.markdowndoc.editor.functions;

import se.natusoft.doc.markdowndoc.editor.ToolBarGroups;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Provides a function that exports to HTML.
 */
public class ExportToHTMLFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton htmlButton;

    //
    // Constructors
    //

    public ExportToHTMLFunction() {
        Icon htmlIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddhtml.png"));
        this.htmlButton = new JButton(htmlIcon);
        htmlButton.setToolTipText("Export as HTML (Alt-H)");
        htmlButton.addActionListener(new ActionListener() {
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
    public String getToolBarGroup() {
        return ToolBarGroups.export.name();
    }

    @Override
    public JComponent getToolBarButton() {
        return this.htmlButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.CTRL_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_H;
    }

    @Override
    public void perform() throws FunctionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
