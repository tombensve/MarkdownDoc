package se.natusoft.doc.markdowndoc.editor.functions;

import se.natusoft.doc.markdowndoc.editor.ToolBarGroups;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * This provides an "insert image" function.
 */
public class InsertImageFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton imageButton;
    private JPanel inputPanel;

    //
    // Constructors
    //

    public InsertImageFunction() {
        Icon imageIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddimg.png"));
        this.imageButton = new JButton(imageIcon);
        imageButton.setToolTipText("Heading (Meta-T)");
        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                perform();
            }
        });

    }

    //
    // Methods
    //

    /**
     * Sets the editor for the component to use.
     *
     * @param editor The editor to set.
     */
    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    @Override
    public String getGroup() {
        return ToolBarGroups.format.name();
    }

    /**
     * Returns the name of the function.
     */
    @Override
    public String getName() {
        return "Insert Image";
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    public JComponent getToolBarButton() {
        return this.imageButton;
    }

    /**
     * Keyboard trigger for the "down" key (shit, ctrl, alt, ...)
     */
    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    /**
     * The keyboard trigger key code.
     */
    @Override
    public int getKeyCode() {
        return KeyEvent.VK_I;
    }

    /**
     * Performs the function.
     *
     * @throws se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
     *
     */
    @Override
    public void perform() throws FunctionException {
        this.inputPanel = new JPanel(new FlowLayout());
        JPanel labelPanel = new JPanel(new GridLayout(2, 1));
        labelPanel.add(new JLabel("Iamge description:"));
        labelPanel.add(new JLabel());

    }

}
