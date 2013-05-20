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
 * Provides editor setting function.
 */
public class SettingsFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton settingsButton;

    //
    // Constructors
    //

    public SettingsFunction() {
        Icon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddsettings.png"));
        this.settingsButton = new JButton(settingsIcon);
        this.settingsButton.setToolTipText("Settings (Alt-S)");
        this.settingsButton.addActionListener(new ActionListener() {
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
        return ToolBarGroups.config.name();
    }

    @Override
    public JComponent getToolBarButton() {
        return this.settingsButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.CTRL_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_S;
    }

    @Override
    public void perform() throws FunctionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
