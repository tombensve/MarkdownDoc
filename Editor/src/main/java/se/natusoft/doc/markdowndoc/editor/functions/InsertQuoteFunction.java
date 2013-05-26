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
 * This provides a function for inserting quote format.
 */
public class InsertQuoteFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton quoteButton;

    //
    // Constructors
    //

    public InsertQuoteFunction() {
        Icon quoteIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddquote.png"));
        this.quoteButton = new JButton(quoteIcon);
        quoteButton.setToolTipText("Quote (Meta-K)");
        quoteButton.addActionListener(new ActionListener() {
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
        return ToolBarGroups.format.name();
    }

    @Override
    public String getName() {
        return "Insert quote format";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.quoteButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_K;
    }

    @Override
    public void perform() throws FunctionException {
        this.editor.insertText("> ");
        this.editor.requestEditorFocus();
    }
}
