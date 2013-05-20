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
 * Provides a "new" function that opens a new editor window.
 */
public class NewFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton newButton;

    //
    // Constructors
    //

    public NewFunction() {
        Icon newIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddnew.png"));
        this.newButton = new JButton(newIcon);
        newButton.setToolTipText("New document (Meta-N)");
        newButton.addActionListener(new ActionListener() {
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
        return ToolBarGroups.file.name();
    }

    @Override
    public JComponent getToolBarButton() {
        return this.newButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_N;
    }

    @Override
    public void perform() throws FunctionException {
        this.editor.openNewEditorWindow();
    }
}
