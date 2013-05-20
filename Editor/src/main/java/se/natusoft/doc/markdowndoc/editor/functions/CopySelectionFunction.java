package se.natusoft.doc.markdowndoc.editor.functions;

import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.JComponent;
import java.awt.event.KeyEvent;

/**
 * This provides a function that copies the currently selected text.
 */
public class CopySelectionFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;

    //
    // Methods
    //

    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    @Override
    public String getToolBarGroup() {
        return null;
    }

    @Override
    public JComponent getToolBarButton() {
        return null;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_C;
    }

    @Override
    public void perform() throws FunctionException {
        this.editor.copy();
    }
}
