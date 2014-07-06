package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic

@CompileStatic
public class ExportFileValue extends ExportDataValue {

    private String whatFile

    // This is provided later in constructor of subclass since it is provided to the constructor.
    DelayedServiceData delayedServiceData

    public ExportFileValue(String labelText, String whatFile) {
        super(labelText)
        this.whatFile = whatFile
    }

    public String getValue() {
        if (whatFile == null || delayedServiceData == null) {
            throw new IllegalStateException("'whatFile' and 'gui' properties must have been provided before this call" +
                    " can be made!")
        }
        if (valueComp == null) {
            valueComp = new FileSelector(this.whatFile, delayedServiceData)
        }
        return ((FileSelector)valueComp).getFile()
    }

    public void setValue(String value) {
        ((FileSelector)valueComp).setFile(value)
    }
}
