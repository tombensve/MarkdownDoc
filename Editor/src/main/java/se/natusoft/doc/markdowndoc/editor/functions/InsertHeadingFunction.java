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
 * Provides an insert heading function.
 */
public class InsertHeadingFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton headerButton;


    //
    // Constructors
    //

    public InsertHeadingFunction() {
        Icon headingIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddheading.png"));
        this.headerButton = new JButton(headingIcon);
        headerButton.setToolTipText("Heading (Meta-T)");
        headerButton.addActionListener(new ActionListener() {
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
        return ToolBarGroups.format.name();
    }

    @Override
    public JComponent getToolBarButton() {
        return this.headerButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_T;
    }

    @Override
    public void perform() throws FunctionException {
        this.editor.insertText("#");
        this.editor.requestEditorFocus();
    }
}
