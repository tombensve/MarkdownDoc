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
 * This provides a function that inserts formatting for italics.
 */
public class InsertItalicsFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton italicsButton;

    //
    // Constructors
    //

    public InsertItalicsFunction() {
        Icon italicsIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdditalics.png"));
        this.italicsButton = new JButton(italicsIcon);
        italicsButton.setToolTipText("Italics (Meta-I)");
        italicsButton.addActionListener(new ActionListener() {
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
        return "Insert italics format";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.italicsButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_I;
    }

    @Override
    public void perform() throws FunctionException {
        this.editor.insertText("__");
        this.editor.moveCaretBack(1);
        this.editor.requestEditorFocus();
    }
}
