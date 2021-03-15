package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.exception.DocException

import java.util.List as JUList

/**
 * This represents a table.
 *
 * A table looks like this:
 * <pre>
 *
 * | C1  | C2  | C3   |
 * | :-- | --- | ---: |
 * | One | Two | Tree |
 *
 * </pre>
 *
 * Note that cell content is represented as a Paragraph! This because i want to support things like
 * italics and bold in columns, things will go bad if images larger than a row is included, and also
 * if the total of all columns are larger than a line, since there is no line wrap in columns!
 *
 * NOTE: This will fail if not all rows have the same number of columns!!!
 */
@CompileStatic
class Table extends DocFormatItem {

    //
    // Private Members
    //

    /** The header if any */
    private TableRow header = null

    /** Represents the table contents */
    private JUList<TableRow> rows = [ ]

    /** The current row. */
    private TableRow currentRow

    /** Column alignment. */
    private JUList<Align> colAlign = []

    //
    // Methods
    //

    /**
     * Returns the format this model represents.
     */
    @Override
    @NotNull
    DocFormat getFormat() {
        DocFormat.Table
    }

    /**
     * Validates this Paragraph.
     */
    @Override
    boolean validate() {
        super.items.size() > 0
    }

    void addColumnAlign(Align align) {
        this.colAlign << align
    }

    /**
     * @return The alignment for each column.
     */
    JUList<Align> getColumnAlign() {
        this.colAlign
    }

    /**
     * Adds a table row.
     */
    void addRow() {
        this.currentRow = new TableRow()
        this.rows << this.currentRow
    }

    /**
     * @return the number of columns.
     */
    int getNumberOfColumns() {
        int columns = 0

        rows.forEach { TableRow row ->
            if (row.size() > columns) columns = row.size()
        }

        columns
    }

    /**
     * @return The number of rows.
     */
    int getNumberOfRows() {
        this.rows.size()
    }

    /**
     * This is probably the best way to access content!
     *
     * @return All table rows.
     */
    @NotNull
    JUList<TableRow> getRows() {
        this.rows
    }

    /**
     * Returns a specific cell.
     *
     * @param row The row of the cell to get.
     * @param column The column of the cell to get.
     *
     * @return The cell content.
     */
    @Nullable
    Paragraph getCell(int row, int column) {

        if (row > this.rows.size() || column > this.rows[column].size() || row < 0 || column < 0) {
            throw new DocException(message: "Bad table coordinate: ${row},${column}!")
        }
        this.rows[row][column]
    }

    /**
     * Adds empty columns to rows that have less columns than largest row.
     */
    void ensureSquare() {
        int columns = getNumberOfColumns()

        rows.forEach { TableRow row ->
            while (row.size() < columns) {
                row << new Paragraph()
            }
        }
    }

    /**
     * Adds a column to the table.
     *
     * @param columnParagraph The column paragraph to add.
     */
    void addColumn(Paragraph columnParagraph) {
        if (rows.size() == 0) {
            addRow()
        }

        this.currentRow.add(columnParagraph)
    }

    /**
     * Adds a header entry.
     *
     * @param header
     */
    void addHeader(String headerEntry) {
        if (this.header == null) {
            this.header = new TableRow()
        }

        Paragraph paragraph = new Paragraph()
        paragraph.addItem(headerEntry)
        this.header.add(paragraph)
    }

    /**
     * @return The max number of characters of each column.
     */
    @NotNull
    JUList<Integer> getColumnWidths() {
        JUList<Integer> widths = []

        ensureSquare()

        int columns = this.rows[0].size()

        for (int i = 0; i < columns; i++) {

            int charWidth = 0


            // Damn it! I accidentally did: 'this.rows.forEach { TableRow row -> ...'
            // Got a very weird error :-)
            this.rows.each {TableRow row ->

                int columnWidth = row[i].toString().size()
                if (columnWidth > charWidth) charWidth = columnWidth
            }

            widths << new Integer(charWidth)
        }

        widths
    }

    @NotNull
    String toString() {
        super.toString() + "\n"
    }

    //
    // Inner Classes
    //

    /**
     * Represents a table row. Each column is represented as a Paragraph, even though content
     * must fit
     */
    class TableRow extends LinkedList<Paragraph> {}

    /**
     * Specifies text alignment in columns.
     */
    enum Align {
        LEFT,
        CENTER,
        RIGHT
    }
}
