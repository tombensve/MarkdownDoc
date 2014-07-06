package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic

import java.awt.*
import java.util.List

@CompileStatic
public class ExportData {

    private DelayedServiceData delayedServicesData

    /** After call to loadPDFData(File) this contains all below fields of ExportDataValue type. */
    List<ExportDataValue> exportDataValues = null

    public ExportData(DelayedServiceData localServiceData) {
        this.delayedServicesData = localServiceData
    }

    protected DelayedServiceData getLocalServicesData() {
        return this.delayedServicesData
    }

    /**
     * Loads the values of the fields in this class from the specified properties file.
     *
     * @param file The properties file to load from.
     */
    protected void loadExportData(File file) {
        Properties props = this.delayedServicesData.getPersistentProps().load(fileToPropertiesName(file))
        if (props != null) {
            for (String propName : props.stringPropertyNames()) {
                for (ExportDataValue exportDataValue : exportDataValues) {
                    if (exportDataValue.getKey().equals(propName)) {
                        exportDataValue.setValue(props.getProperty(propName))
                    }
                }
            }
        }
    }

    /**
     * Saves the values of the fields in this class to the specified properties file.
     *
     * @param file The properties file to save to.
     */
    protected void saveExportData(File file) {
        Properties props = new Properties()
        for (ExportDataValue exportDataValue : exportDataValues) {
            props.setProperty(exportDataValue.getKey(), exportDataValue.getValue())
        }
        props.setProperty(delayedServicesData.defaultsPropKey,
                delayedServicesData.exportFile.getAbsolutePath())
        delayedServicesData.getPersistentProps().save(fileToPropertiesName(file), props)
    }

    public void setBackgroundColor(Color bgColor) {
        for (ExportDataValue edv : this.exportDataValues) {
            edv.setBackgroundColor(bgColor)
        }
    }

    /**
     * Converts a File to a properties name. This is used for saving generation meta data for last PDF generation
     * using the File representing the text being edited.
     *
     * @param file The file to convert to properties name.
     */
    protected String fileToPropertiesName(File file) {
        return file.getName().replace(".", "_")
    }

}
