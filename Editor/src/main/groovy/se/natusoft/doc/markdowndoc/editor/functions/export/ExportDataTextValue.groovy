package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic

import javax.swing.JTextField

/**
 * PDFData text valueComp fields.
 */
@CompileStatic
public class ExportDataTextValue extends ExportDataValue {

    public ExportDataTextValue(String labelText) {
        super(labelText)
        setValueComp(new JTextField(25))
    }

    public ExportDataTextValue(String labelText, String defaultValue) {
        this(labelText)
        setValue(defaultValue)
    }

    public String getValue() {
        return ((JTextField)valueComp).getText()
    }

    public void setValue(String value) {
        ((JTextField)valueComp).setText(value)
    }
}
