package se.natusoft.doc.markdowndoc.editor;

import se.natusoft.doc.markdowndoc.editor.adapters.WindowListenerAdapter;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.api.EditorInputFilter;
import se.natusoft.doc.markdowndoc.editor.api.Line;
import se.natusoft.doc.markdowndoc.editor.api.providers.JELine;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * This is an editor for editing markdown documents.
 */
public class MarkdownEditor extends JFrame implements Editor, KeyListener {
    //
    // Private Members
    //

    private MDEToolBar toolBar;
    private JPanel editorPanel;
    private JScrollPane scrollPane;
    private JEditorPane editor;
    private File currentFile;
    private static int instanceCount = 0;
    private java.util.List<EditorFunction> functions = new LinkedList<EditorFunction>();
    private static ServiceLoader<EditorFunction> functionLoader = ServiceLoader.load(EditorFunction.class);
    private List<EditorInputFilter> filters = new LinkedList<EditorInputFilter>();
    private static ServiceLoader<EditorInputFilter> filterLoader = ServiceLoader.load(EditorInputFilter.class);
    private KeyEvent currentPressedEvent = null;
    private int keyPressedCaretPos = 0;

    //
    // Constructors
    //

    /**
     * Creates a new MarkdownEditor instance.
     */
    public MarkdownEditor() {
        addWindowListener(new WindowListenerAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MarkdownEditor.this.setVisible(false);
                MarkdownEditor.this.editorClosed();
            }

        });

        // Main Window

        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(800, 800));
        this.setTitle("MarkdownDoc Editor");

        // Editor

        this.editorPanel = new JPanel();
        this.editorPanel.setLayout(new BorderLayout());

        this.editor = new JEditorPane() {
            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                dim.setSize(MarkdownEditor.this.getWidth(), dim.getHeight());

                return dim;
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

        };
        this.editor.setBackground(new Color(200, 200, 200));
        this.editor.setForeground(new Color(100, 100, 100));
        this.editor.setFont(Font.decode("Helvetica").deriveFont(16));
        this.editor.setMargin(new Insets(20, 30, 20, 30));
        this.editor.setCaret(new MDECaret());

        this.editor.addKeyListener(this);

        // Setup active view

        this.scrollPane = new JScrollPane();
        //scrollPane.setAutoscrolls(true)
        this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.scrollPane.setViewportView(this.editor);
        this.scrollPane.getViewport().setAlignmentY(0.0f);

        this.editorPanel.add(scrollPane, BorderLayout.CENTER);
        add(this.editorPanel, BorderLayout.CENTER);

        // Functions

        for (EditorFunction function : functionLoader) {
            function.setEditor(this);
            this.functions.add(function);
        }


        // Filters

        for (EditorInputFilter filter : filterLoader) {
            filter.setEditor(this);
            this.filters.add(filter);
        }


        // Toolbar

        this.toolBar = new MDEToolBar();
        for (EditorFunction function : this.functions) {

            // It is OK to not have a tool bar button!
            if (function.getToolBarGroup() != null && function.getToolBarButton() != null) {
                this.toolBar.addFunction(function);
            }

        }

        this.toolBar.createToolBarContent();
        add(toolBar, BorderLayout.NORTH);

    }

    //
    // Methods
    //

    // KeyListener Implementation.

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key pressed event.
     */
    public void keyPressed(KeyEvent e) {
        this.currentPressedEvent = e;
        this.keyPressedCaretPos = this.editor.getCaretPosition();

//        System.out.println("e.modifiers=" + e.modifiers + ", e,keyCode=" + e.keyCode)
        for (EditorFunction function : this.functions) {
//            System.out.println("f.downKeyMask=" + function.downKeyMask + ", f.keyCode=" + function.keyCode)
            // Please note that we don't or the downKeyMask with the modifiers since we want it to be
            // exactly the mask and nothing else. If the function returns 4 (Meta) then no other key
            // like Alt or Control is accepted in combination.
            if (e.getModifiers() == function.getDownKeyMask() && e.getKeyCode() == function.getKeyCode()) {
                function.perform();
                break;
            }

        }

    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key released event.
     */
    public void keyReleased(KeyEvent e) {
        // The current editor position has not been updated for the key event in
        // keyPressed(...) so we save the pressed event (we get a different event here!)
        // and execute filters on it now when the editor is in a better state.
        for (EditorInputFilter filter : this.filters) {
            filter.keyPressed(this.currentPressedEvent);
        }
    }

    // ----

    public void openSettings() {

    }

    // --- Editor implementation.

    /**
     * This gets called when the window is closed. This can be overriden to
     * handle more more actions like exiting the JVM for example.
     */
    protected void editorClosed() {
    }

    /**
     * Returns the current file or null if none.
     */
    public File getCurrentFile() {
        return this.currentFile;
    }

    /**
     * Sets the current file.
     *
     * @param file The file to set.
     */
    public void setCurrentFile(File file) {
        this.currentFile = file;
    }

    /**
     * Returns the contents of the editor.
     */
    public String getEditorContent() {
        return this.editor.getText();
    }

    /**
     * Returns the current selection or null if none.
     */
    public String getEditorSelection() {
        return this.editor.getSelectedText();
    }

    /**
     * Returns the current line.
     */
    public Line getCurrentLine() throws BadLocationException {
        int i = keyPressedCaretPos;
        while (i >= 0) {
            String check = this.editor.getText(i, 1);
            if (check.startsWith("\n")) {
                ++i;
                break;
            }
            --i;
        }

        return new JELine(this.editor,i);
    }

    /**
     * Set/replace the entire content of the editor.
     *
     * @param content The new content to set.
     */
    public void setEditorContent(String content) {
        this.editor.setText(content);
    }

    /**
     * Inserts new text into the editor or replaces current selection.
     *
     * @param text The text to insert.
     */
    public void insertText(String text) {
        this.editor.replaceSelection(text);
    }

    /**
     * Moves the cared backwards.
     *
     * @param noChars The number of characters to move caret.
     */
    public void moveCaretBack(int noChars) {
        Caret caret = this.editor.getCaret();
        caret.setDot(caret.getDot() - noChars);
    }

    /**
     * Moves the caret forward.
     *
     * @param noChars The number of characters to move caret.
     */
    public void moveCaretForward(int noChars) {
        Caret caret = this.editor.getCaret();
        caret.setDot(caret.getDot() + noChars);
    }

    /**
     * Returns the current caret location.
     */
    public Point getCaretLocation() {
        return this.editor.getLocation(new Point());
    }

    /**
     * Moves the current caret location.
     *
     * @param location The new location.
     */
    public void setCaretLocation(Point location) {
        this.editor.setLocation(location);
    }

    /**
     * Requests focus for the editor.
     */
    public void requestEditorFocus() {
        this.editor.requestFocus();
    }

    /**
     * Needed for popping upp dialogs.
     */
    public JFrame getWindowFrame() {
        return this;
    }

    /**
     * Opens a new editor window.
     */
    public void openNewEditorWindow() {
        try {
            openEditor(null);
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    this, ioe.getMessage(), "Failed to open new editor!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Copies the currently selected text.
     */
    public void copy() {
        this.editor.copy();
    }

    /**
     * Cuts the currently selected text.
     */
    public void cut() {
        this.editor.cut();
    }

    /**
     * Pastes the currently copied/cut text.
     */
    public void paste() {
        this.editor.paste();
    }

    /**
     * Makes the editor view visible in the main scrollable view.
     */
    public void showEditorComponent() {
        this.scrollPane.setViewportView(this.editor);
    }

    /**
     * Makes the specified component visible in the main scrollable view.
     */
    public void showOtherComponent(JComponent component) {
        this.scrollPane.setViewportView(component);
    }

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to enable.
     */
    public void enableToolBarGroup(String groupName) {
        this.toolBar.enableGroup(groupName);
    }

    /**
     * Disables all button in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to disable.
     */
    public void disableToolBarGroup(String groupName) {
        this.toolBar.disableGroup(groupName);
    }

    /**
     * Opens the specified file in the editor.
     *
     * @param file The file to open.
     *
     * @throws java.io.IOException
     */
    public void loadFile(File file) throws IOException {
        setCurrentFile(file);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
        }
        finally {
            br.close();
        }
        setEditorContent(sb.toString());
    }

    // ----

    // Static methods

    public static void openEditor(File file) throws IOException {
        ++instanceCount;

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel")

        MarkdownEditor me = new MarkdownEditor() {
            @Override
            protected void editorClosed() {
                setVisible(false);
                --instanceCount;
                if (instanceCount == 0) {
                    System.exit(0);
                }

            }

        };

        enableOSXFullscreenIfOnOSX(me);

        if (file != null) {
            me.loadFile(file);
        }

        me.setVisible(true);

//        UIManager.LookAndFeelInfo[] lfis = UIManager.getInstalledLookAndFeels()
//        for (UIManager.LookAndFeelInfo lfi : lfis) {
//            System.out.println("" + lfi.toString())
//        }

    }

    public static void main(String... args) {
        try {
            if (args.length > 0) {
                for (String arg : args) {
                    File argFile = new File(arg);
                    openEditor(argFile);
                }

            } else {
                openEditor(null);
            }
        }
        catch (IOException ioe) {
            System.err.println("Failed to open editor: " + ioe.getMessage());
        }
    }

    /**
     * Turns on full screen support in Lion+.
     * <p/>
     * This code from:
     * <a href="http://stackoverflow.com/questions/6873568/fullscreen-feature-for-java-apps-on-osx-lion">
     * http://stackoverflow.com/questions/6873568/fullscreen-feature-for-java-apps-on-osx-lion
     * </a>
     *
     * @param window The window to enable full screen for.
     */
    @SuppressWarnings({"unchecked", "rawtypes", "unused"})
    public static void enableOSXFullscreenIfOnOSX(Window window) {
        if (window == null) return;

        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class[] params = new Class[2];
            params[0] = Window.class;
            params[1] = Boolean.TYPE;
            Method method = util.getMethod("setWindowCanFullScreen", params);
            method.invoke(util, window, true);
        } catch (ClassNotFoundException cnfe) {/* Not on Mac OS X! */} catch (Exception e) {
            e.printStackTrace(System.err);
        }

    }

}
