/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *
 *     Description
 *         An editor that supports editing markdown with formatting preview.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-10-12: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.Services

import java.lang.reflect.Field

@CompileStatic
/**
 * Base class for exportable data.
 */
class ExportData {

    //
    // Private Members
    //

    /** Environment services. */
    private DelayedServiceData delayedServicesData

    //
    // Properties
    //

    /** After call to loadPDFData(File) this contains all below fields of ExportDataValue type. */
    List<ExportDataValue> exportDataValues = null

    //
    // Constructors
    //

    /**
     * Creates a new ExportData instance.
     *
     * @param localServiceData provides data like export file and other things.
     */
    ExportData(@NotNull final DelayedServiceData localServiceData) {
        this.delayedServicesData = localServiceData
    }

    //
    // Methods
    //

    /**
     * Initializes the exportDataValues list with all the fields
     * for easier dynamic access.
     */
    void loadDataValues() {
        exportDataValues = new LinkedList<ExportDataValue>()

        this.class.declaredFields.findAll { final Field field ->
            field.type == ExportDataValue.class
        }.each { final Field field ->
            field.accessible = true
            try {
                exportDataValues.add((ExportDataValue)field.get(this))
            }
            catch (final Exception e) {
                System.err.println("ERROR: " + e.getMessage())
            }
        }
    }

    /**
     * Loads the values of the fields in this class from the specified properties file.
     *
     * @param file The properties file to load from.
     */
    void loadExportData(@NotNull final File file) {
        final Properties props = Services.persistentPropertiesProvider.load(fileToPropertiesName(file))
        if (props != null) {
            props.stringPropertyNames().each { final String propName ->
                exportDataValues.findAll { final ExportDataValue exportDataValue ->
                    exportDataValue.getKey().equals(propName)
                }.each { final ExportDataValue exportDataValue ->
                    exportDataValue.setValue(props.getProperty(propName))
                }
            }
        }
    }

    /**
     * Saves the values of the fields in this class to the specified properties file.
     *
     * @param file The properties file to selectNewFile to.
     */
    void saveExportData(@NotNull final File file) {
        final Properties props = new Properties()
        exportDataValues.each { final ExportDataValue exportDataValue ->
            props.setProperty(exportDataValue.getKey(), exportDataValue.getValue())
        }
        props.setProperty(delayedServicesData.defaultsPropKey,
                delayedServicesData.exportFile.getAbsolutePath())
        Services.persistentPropertiesProvider.save(fileToPropertiesName(file), props)
    }

    /**
     * Converts a File to a properties name. This is used for saving generation meta data for last PDF generation
     * using the File representing the text being edited.
     *
     * @param file The file to convert to properties name.
     */
    private static @NotNull String fileToPropertiesName(@NotNull final File file) {
        file.getName().replace(".", "_")
    }

}
