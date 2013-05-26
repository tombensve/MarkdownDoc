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
 * This provides a function that inserts bold formatting.
 */
public class InsertBoldFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton boldButton;

    //
    // Constructors
    //

    public InsertBoldFunction() {
        Icon boldIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddbold.png"));
        this.boldButton = new JButton(boldIcon);
        boldButton.setToolTipText("Bold (Meta-B)");
        boldButton.addActionListener(new ActionListener() {
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
        return "Insert bold format";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.boldButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_B;
    }

    @Override
    public void perform() throws FunctionException {
        this.editor.insertText("****");
        this.editor.moveCaretBack(2);
        this.editor.requestEditorFocus();
    }
}
