package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic

import javax.swing.JComponent
import javax.swing.JLabel
import java.awt.Color

/**
 * Base of all field values in PDFData
 */
@CompileStatic
public abstract class ExportDataValue {
    JLabel labelComp
    JComponent valueComp

    public ExportDataValue(String labelText) {
        this.labelComp = new JLabel("    " + labelText + " ")
    }

    public String getKey() {
        return this.labelComp.getText().trim().toLowerCase().replaceAll(" ", "-").replaceAll(":", "")
    }

    public void setBackgroundColor(Color bgColor) {
        this.valueComp.setBackground(bgColor)
    }

    public abstract String getValue()
    public abstract void setValue(String value)
}
