package se.natusoft.doc.markdowndoc.editor.functions;

import se.natusoft.doc.markdowndoc.editor.ToolBarGroups;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Provides an open function.
 */
public class OpenFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton openButton;

    //
    // Constructors
    //

    public OpenFunction() {
        Icon openIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddopen.png"));
        this.openButton = new JButton(openIcon);
        this.openButton.setToolTipText("Open (Meta-O)");
        this.openButton.addActionListener(new ActionListener() {
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
     * Sets the editor for the function to use.
     *
     * @param editor The editor to set.
     */
    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    @Override
    public String getGroup() {
        return ToolBarGroups.file.name();
    }

    @Override
    public String getName() {
        return "Open file";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.openButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_O;
    }

    @Override
    public void perform() throws FunctionException {
        try {
            open();
        }
        catch (IOException ioe) {
            throw new FunctionException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Opens a new file using a file chooser.
     */
    private void open() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Markdown", "md", "markdown");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(this.editor.getGUI().getWindowFrame());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            this.editor.loadFile(fileChooser.getSelectedFile());
        }
    }


}
