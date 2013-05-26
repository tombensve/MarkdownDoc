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
 * This provides a function that inserts list markdown format.
 */
public class InsertListFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton listButton;

    //
    // Constructors
    //

    public InsertListFunction() {
        Icon listIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddlist.png"));
        this.listButton = new JButton(listIcon);
        listButton.setToolTipText("List (Meta-L)");
        listButton.addActionListener(new ActionListener() {
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
        return "Insert list format";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.listButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_L;
    }

    @Override
    public void perform() throws FunctionException {
        this.editor.insertText("* ");
        this.editor.requestEditorFocus();
    }
}
