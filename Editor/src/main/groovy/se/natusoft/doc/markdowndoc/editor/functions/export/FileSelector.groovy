package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic

import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

@CompileStatic
public class FileSelector extends JPanel implements ActionListener {
    private JTextField fileName = new JTextField(30)
    private JButton selectButton = new JButton("Select")
    private String what
    private DelayedServiceData dsd

    public FileSelector(String what, DelayedServiceData dsd) {
        this.what = what
        this.dsd = dsd
        setLayout(new BorderLayout())
        add(this.fileName, BorderLayout.CENTER)
        add(this.selectButton, BorderLayout.EAST)
        this.selectButton.addActionListener(this)
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JFileChooser fileChooser = new JFileChooser()
        fileChooser.setDialogTitle("Specify " + what + " file")
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG)
        if (this.fileName.getText() != null && this.fileName.getText().trim().length() > 0) {
            fileChooser.setSelectedFile(new File(this.fileName.getText()))
        }
        FileNameExtensionFilter filter = new FileNameExtensionFilter(this.what, this.what)
        int returnVal = fileChooser.showSaveDialog(dsd.GUI.windowFrame)
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            setFile(fileChooser.getSelectedFile().getAbsolutePath())
        }
    }

    public void setFile(String file) {
        this.fileName.setText(file)
    }

    public String getFile() {
        return this.fileName.getText()
    }

    @Override
    public void setBackground(Color bgColor) {
        super.setBackground(bgColor)
        if (this.fileName != null) {
            this.fileName.setBackground(bgColor)
        }
    }
}
