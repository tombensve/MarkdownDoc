package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic

import javax.swing.JCheckBox

/**
 * PDFData boolean valueComp fields.
 */
@CompileStatic
public class ExportDataSelectValue extends ExportDataValue {
    public ExportDataSelectValue(String labelText) {
        super(labelText)
        super.valueComp = new JCheckBox()
    }

    public ExportDataSelectValue(String labelText, boolean defaultValue) {
        this(labelText)
        ((JCheckBox)super.valueComp).setSelected(defaultValue)
    }

    public String getValue() {
        return "" + ((JCheckBox)super.valueComp).isSelected()
    }

    public void setValue(String value) {
        boolean selected = Boolean.valueOf(value)
        ((JCheckBox)super.valueComp).setSelected(selected)
    }
}
