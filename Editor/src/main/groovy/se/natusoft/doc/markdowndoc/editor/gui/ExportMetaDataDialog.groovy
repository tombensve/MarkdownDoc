package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.GUI
import se.natusoft.doc.markdowndoc.editor.api.MouseMotionProvider
import se.natusoft.doc.markdowndoc.editor.functions.export.ExportData
import se.natusoft.doc.markdowndoc.editor.functions.export.ExportDataValue

import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

/**
 * A popup dialog that allow user to enter meta data for generating.
 */
@CompileStatic
@TypeChecked
class ExportMetaDataDialog extends PopupWindow implements MouseMotionProvider {
    //
    // Properties
    //

    /** The export meta data to handle. */
    @NotNull ExportData exportData

    /** Callback for when generate is clicked. */
    @NotNull Closure<Void> generate = null

    //
    // Methods
    //

    void init() {
        safeOpacity = popupOpacity

        setLayout(new BorderLayout())

        final JPanel borderPanel = new JPanel(new BorderLayout())
        borderPanel.setBorder(new EmptyBorder(5, 5, 5, 5))
        add(borderPanel, BorderLayout.CENTER)
        updateColors(borderPanel)

        this.exportData.loadDataValues()

        final JPanel dataLabelPanel = new JPanel(new GridLayout(this.exportData.exportDataValues.size(),1))
        borderPanel.add(dataLabelPanel, BorderLayout.WEST)
        updateColors(dataLabelPanel)

        final JPanel dataValuePanel = new JPanel(new GridLayout(this.exportData.exportDataValues.size(),1))
        borderPanel.add(dataValuePanel, BorderLayout.CENTER)
        updateColors(dataValuePanel)

        borderPanel.add(Box.createRigidArea(new Dimension(12, 12)), BorderLayout.EAST)

        this.exportData.exportDataValues.each { final ExportDataValue exportDataValue ->

            // Force the value comp to initialize.
            exportDataValue.value

            updateColors(exportDataValue.labelComp)
            dataLabelPanel.add(exportDataValue.labelComp)

            updateColors(exportDataValue.valueComp)
            dataValuePanel.add(exportDataValue.valueComp)
        }

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        updateColors(buttonPanel)

        final JButton generateButton = new JButton("Generate")
        generateButton.addActionListener({ final ActionEvent actionEvent ->
            close()
            doGenerate()
        } as ActionListener)
        buttonPanel.add(generateButton)
        final JButton cancelButton = new JButton("Cancel")
        cancelButton.addActionListener({ final ActionEvent actionEvent -> close() } as ActionListener)
        buttonPanel.add(cancelButton)

        borderPanel.add(buttonPanel, BorderLayout.SOUTH)

    }

    void setExportData(@NotNull final ExportData ed) {
        this.exportData = ed
        init()
    }

    // Closure can access these, and these can access outer class, but closure cannot access outer class
    // directly. Thereby these bounces. Closures also seem to have problems calling private methods of
    // owner class!

    void open(@NotNull final GUI gui) {
        visible = true
        alwaysOnTop = true
        size = preferredSize

        final Rectangle mainBounds = gui.getWindowFrame().getBounds()
        final int x = (int)mainBounds.x + (int)(mainBounds.width / 2) - (int)(getWidth() / 2)
        final int y = (int)mainBounds.y + 70
        setLocation(x, y)
    }

    void close() {
        visible = false
    }

    void doGenerate() {
        this.generate?.call()
    }
}
