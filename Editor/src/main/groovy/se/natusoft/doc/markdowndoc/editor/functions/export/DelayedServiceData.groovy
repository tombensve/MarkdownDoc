package se.natusoft.doc.markdowndoc.editor.functions.export

import se.natusoft.doc.markdowndoc.editor.api.EnvServices

/**
 * Locally provided information.
 */
public interface DelayedServiceData extends EnvServices{

    /**
     * Returns the default property key.
     */
    String getDefaultsPropKey()

    /**
     *  Returns the file to export to.
     */
    File getExportFile()

}