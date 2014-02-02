/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3
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
 *         2013-05-27: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor;

import net.iharder.dnd.FileDrop;
import se.natusoft.doc.markdowndoc.editor.adapters.WindowListenerAdapter;
import se.natusoft.doc.markdowndoc.editor.api.*;
import se.natusoft.doc.markdowndoc.editor.api.providers.JELine;
import se.natusoft.doc.markdowndoc.editor.api.providers.PersistentPropertiesProvider;
import se.natusoft.doc.markdowndoc.editor.config.*;
import se.natusoft.doc.markdowndoc.editor.functions.utils.FileWindowProps;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_EDITING;
import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_TOOL;

/**
 * This is an editor for editing markdown documents.
 */
public class MarkdownEditor extends JFrame implements Editor, GUI, KeyListener, Configurable {
    //
    // Static members
    //

    // Holds all configurations.
    private static ConfigProvider configs = new ConfigProviderHolder();

    // Manages persistent properties.
    private static PersistentProps persistentPropertiesProvider = new PersistentPropertiesProvider();

    //
    // Private Members
    //

    private MDEToolBar toolBar;

    // These are special panels on each side of the editor panel, but inside of the
    // toolbar. They are not created and added until some EditorComponent asks for
    // one of them.
    private JPanel editorTopPanel;
    private JPanel editorBottomPanel;
    private JPanel editorLeftPanel;
    private JPanel editorRightPanel;

    // The scrollPane sits in the center of this and the above editor*Panel resides
    // NORTH, SOUTH, EAST, and WEST of the editor.
    private JPanel editorPanel;

    // This sits around the editor.
    private JScrollPane scrollPane;

    // The actual editor component.
    private JTextPane editor;

    // Styles an JTextPane.
    private JTextComponentStyler editorStyler;

    // When a file has been opened, or saved this will point to that file.
    // On save a file chooser will be opened if this is null otherwise this
    // file will be used for saving to.
    private File currentFile;

    // This is incremented for each editor window opened and decreased for each closed.
    // When 0 is reached again the JVM will exit.
    private static int instanceCount = 0;

    // Saved on key "pressed" and used later to get the current caret position.
    private int keyPressedCaretPos = 0;

    // All other than the basic JEditorPane functionality are provided by EditorComponent:s of
    // which there are 2 sub-variants: EditorFunction (provides toolbar button, trigger key, and
    // functionality), and EditorInputFilter (receives keyboard events and can manipulate the
    // editor for automatic list bullets, etc).
    //
    // These EditorComponent:s are loaded using ServiceLoader returning EditorComponent
    // instances.

    private ServiceLoader<EditorComponent> componentLoader = ServiceLoader.load(EditorComponent.class);

    // This is for styling the editor while editing.
    private ServiceLoader<JTextComponentStyler> stylerLoader = ServiceLoader.load(JTextComponentStyler.class);

    // Holds EditorComponents that are EditorFunction subclasses.
    private List<EditorFunction> functions = new LinkedList<>();

    // Holds EditorComponents that are EditorInputFilter subclasses.
    private List<EditorInputFilter> filters = new LinkedList<>();

    //
    // Configs
    //

    private List<Configurable> configurables = new LinkedList<>();

    private static ValidSelectionConfigEntry fontConfig =
            new ValidSelectionConfigEntry("editor.pane.font", "The font to use.", "Helvetica",
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        public String[] validValues() {
                            GraphicsEnvironment gEnv = GraphicsEnvironment
                                    .getLocalGraphicsEnvironment();
                            return gEnv.getAvailableFontFamilyNames();
                        }
                    },
                    CONFIG_GROUP_EDITING
            );

    private static DoubleConfigEntry fontSizeConfig =
            new DoubleConfigEntry("editor.pane.font.size", "The size of the font.", 16.0, 8.0, 50.0, CONFIG_GROUP_EDITING);

    private static ColorConfigEntry backgroundColorConfig =
            new ColorConfigEntry("editor.pane.background.color", "The editor background color.", 240, 240, 240, CONFIG_GROUP_EDITING);

    private static ColorConfigEntry foregroundColorConfig =
            new ColorConfigEntry("editor.pane.foreground.color", "The editor text color.", 80, 80, 80, CONFIG_GROUP_EDITING);

    private static ColorConfigEntry caretColorConfig =
            new ColorConfigEntry("editor.pane.caret.color", "The caret color", 0, 0, 0, CONFIG_GROUP_EDITING);

    private static ValidSelectionConfigEntry lookAndFeelConfig =
            new ValidSelectionConfigEntry("editor.lookandfeel", "The LookAndFeel to use.",
                    UIManager.getSystemLookAndFeelClassName(),
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        public String[] validValues() {
                            String[] vv = new String[UIManager.getInstalledLookAndFeels().length];
                            int i = 0;
                            for (UIManager.LookAndFeelInfo lfi : UIManager.getInstalledLookAndFeels()) {
                                vv[i++] = lfi.getClassName();
                            }
                            return vv;
                        }
                    },
                    CONFIG_GROUP_TOOL
            );

    private static IntegerConfigEntry topMargin = new IntegerConfigEntry("editor.pane.top.margin",
            "The top margin.", 40, 0, 500, CONFIG_GROUP_EDITING);

    private static IntegerConfigEntry bottomMargin = new IntegerConfigEntry("editor.pane.bottom.margin",
            "The bottom margin.", 40, 0, 500, CONFIG_GROUP_EDITING);

    private static IntegerConfigEntry leftMargin = new IntegerConfigEntry("editor.pane.left.margin",
            "The left margin.", 60, 0, 500, CONFIG_GROUP_EDITING);

    private static IntegerConfigEntry rightMargin = new IntegerConfigEntry("editor.pane.right.margin",
            "The right margin.", 60, 0, 500, CONFIG_GROUP_EDITING);

    //
    // Config callbacks
    //

    private ConfigChanged fontConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            editor.setFont(Font.decode(ce.getValue()).
                    deriveFont(Float.valueOf(fontSizeConfig.getValue())));
        }
    };

    private ConfigChanged fontSizeConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            editor.setFont(Font.decode(fontConfig.getValue()).deriveFont(Float.valueOf(ce.getValue())));
        }
    };

    private ConfigChanged backgroundColorConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            editor.setBackground(new ConfigColor(ce));
        }
    };

    private ConfigChanged foregroundColorConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            editor.setForeground(new ConfigColor(ce));
        }
    };

    private ConfigChanged caretColorConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            editor.setCaretColor(new ConfigColor(ce));
        }
    };

    private ConfigChanged lookAndFeelConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            try {
                Dimension size = MarkdownEditor.this.getSize();
                UIManager.setLookAndFeel(ce.getValue());
                SwingUtilities.updateComponentTreeUI(MarkdownEditor.this);  // update awt
                MarkdownEditor.this.pack();
                MarkdownEditor.this.setSize(size);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }
    };

    private ConfigChanged topMarginConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            Insets margins = MarkdownEditor.this.editor.getMargin();
            margins.top = ((IntegerConfigEntry)ce).getIntValue();
            editor.setMargin(margins);
            editor.revalidate();
        }
    };

    private ConfigChanged bottomMarginConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            Insets margins = MarkdownEditor.this.editor.getMargin();
            margins.bottom = ((IntegerConfigEntry)ce).getIntValue();
            editor.setMargin(margins);
            editor.revalidate();
        }
    };

    private ConfigChanged leftMarginConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            Insets margins = MarkdownEditor.this.editor.getMargin();
            margins.left = ((IntegerConfigEntry)ce).getIntValue();
            editor.setMargin(margins);
            editor.revalidate();
        }
    };

    private ConfigChanged rightMarginConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            Insets margins = MarkdownEditor.this.editor.getMargin();
            margins.right = ((IntegerConfigEntry)ce).getIntValue();
            editor.setMargin(margins);
            editor.revalidate();
        }
    };

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    public void registerConfigs(ConfigProvider configProvider) {
        configProvider.registerConfig(fontConfig, this.fontConfigChanged);
        configProvider.registerConfig(fontSizeConfig, this.fontSizeConfigChanged);
        configProvider.registerConfig(backgroundColorConfig, this.backgroundColorConfigChanged);
        configProvider.registerConfig(foregroundColorConfig, this.foregroundColorConfigChanged);
        configProvider.registerConfig(lookAndFeelConfig, this.lookAndFeelConfigChanged);
        configProvider.registerConfig(topMargin, this.topMarginConfigChanged);
        configProvider.registerConfig(bottomMargin, this.bottomMarginConfigChanged);
        configProvider.registerConfig(leftMargin, this.leftMarginConfigChanged);
        configProvider.registerConfig(rightMargin, this.rightMarginConfigChanged);
        configProvider.registerConfig(caretColorConfig, this.caretColorConfigChanged);

    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    public void unregisterConfigs(ConfigProvider configProvider) {
        configProvider.unregisterConfig(fontConfig, this.fontConfigChanged);
        configProvider.unregisterConfig(fontSizeConfig, this.fontSizeConfigChanged);
        configProvider.unregisterConfig(backgroundColorConfig, this.backgroundColorConfigChanged);
        configProvider.unregisterConfig(foregroundColorConfig, this.foregroundColorConfigChanged);
        configProvider.unregisterConfig(lookAndFeelConfig, this.lookAndFeelConfigChanged);
        configProvider.unregisterConfig(topMargin, this.topMarginConfigChanged);
        configProvider.unregisterConfig(bottomMargin, this.bottomMarginConfigChanged);
        configProvider.unregisterConfig(leftMargin, this.leftMarginConfigChanged);
        configProvider.unregisterConfig(rightMargin, this.rightMarginConfigChanged);
        configProvider.unregisterConfig(caretColorConfig, this.caretColorConfigChanged);
    }

    //
    // Constructors
    //

    /**
     * Creates a new MarkdownEditor instance.
     */
    public MarkdownEditor() {}

    /**
     * Sets up the gui, etc.
     */
    public void initGUI() {
        addWindowListener(new WindowListenerAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ConfigProvider cp = getConfigProvider();
                for (Configurable configurable : MarkdownEditor.this.configurables) {
                    configurable.unregisterConfigs(cp);
                }
                MarkdownEditor.this.setVisible(false);
                MarkdownEditor.this.editorClosed();
            }
        });

        // Register configs
        registerConfigs(getConfigProvider());
        this.configurables.add(this);

        // Set Look and Feel

        if (lookAndFeelConfig.getValue().length() > 0) {
            try {
                UIManager.setLookAndFeel(lookAndFeelConfig.getValue());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }

        // Main Window

        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(800, 800));
        this.setTitle("MarkdownDoc Editor 1.3");

        // Editor

        this.editorPanel = new JPanel();
        this.editorPanel.setLayout(new BorderLayout());
        this.editorPanel.setAutoscrolls(true);


        this.editor = new JTextPane() {
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
        this.editorStyler = this.stylerLoader.iterator().next();
        if (this.editorStyler == null) {
            throw new RuntimeException("No META-INF/services/se.natusoft.doc.markdowndoc.editor.api.JTextComponentStyler " +
                    "file pointing out an implementation to use have been provided!");
        }
        this.editorStyler.init(this.editor);
        if (this.editorStyler instanceof Configurable) {
            ((Configurable)this.editorStyler).registerConfigs(getConfigProvider());
            this.configurables.add((Configurable)this.editorStyler);
        }

        this.editor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                updateScrollbar();
            }
        });


        new FileDrop(this.editor, new FileDrop.Listener() {
            public void filesDropped(java.io.File[] files) {
                if (files.length >= 1) {
                    dropFile(files[0]);
                }
            }
        });

        // Setup active view

        this.scrollPane = new JScrollPane(this.editor);
        //scrollPane.setAutoscrolls(true)
        this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.scrollPane.setAutoscrolls(true);
        //this.scrollPane.getViewport().setAlignmentY(0.0f);

        this.editorPanel.add(scrollPane, BorderLayout.CENTER);
        add(this.editorPanel, BorderLayout.CENTER);

        // Load editor functions.

        for (EditorComponent component : componentLoader) {
            component.setEditor(this);

            if (component instanceof EditorFunction) {
                this.functions.add((EditorFunction) component);
            } else if (component instanceof EditorInputFilter) {
                this.filters.add((EditorInputFilter) component);
            }
        }

        // Additional setup now that a component have possibly loaded config.

        Insets margins = new Insets(
                topMargin.getIntValue(),
                leftMargin.getIntValue(),
                bottomMargin.getIntValue(),
                rightMargin.getIntValue()
        );
        this.editor.setMargin(margins);
        this.editor.addKeyListener(this);

        // Toolbar

        this.toolBar = new MDEToolBar();
        for (EditorFunction function : this.functions) {

            // It is OK to not have a tool bar button!
            if (function.getGroup() != null && function.getToolBarButton() != null) {
                this.toolBar.addFunction(function);
            }

        }

        this.toolBar.createToolBarContent();
        this.editorPanel.add(toolBar, BorderLayout.NORTH);

    }

    //
    // Methods
    //

    /**
     * Returns the editor GUI API.
     */
    public GUI getGUI() {
        return this;
    }

    /**
     * Returns the persistent properties provider.
     */
    public PersistentProps getPersistentProps() {
        return persistentPropertiesProvider;
    }

    /**
     * Returns the panel above the editor and toolbar.
     */
    public JPanel getTopPanel() {
        if (this.editorTopPanel == null) {
            this.editorTopPanel = new JPanel();
            this.editorPanel.add(this.editorTopPanel, BorderLayout.NORTH);
        }
        return this.editorTopPanel;
    }

    /**
     * Returns the panel below the editor and toolbar.
     */
    public JPanel getBottomPanel() {
        if (this.editorBottomPanel == null) {
            this.editorBottomPanel = new JPanel();
            this.editorPanel.add(this.editorBottomPanel, BorderLayout.SOUTH);
        }
        return this.editorBottomPanel;
    }

    /**
     * Returns the panel to the left of the editor and toolbar.
     */
    public JPanel getLeftPanel() {
        if (this.editorLeftPanel == null) {
            this.editorLeftPanel = new JPanel();
            this.editorPanel.add(this.editorLeftPanel, BorderLayout.WEST);
        }
        return this.editorLeftPanel;
    }

    /**
     * Returns the panel to the right of the editor and toolbar.
     */
    public JPanel getRightPanel() {
        if (this.editorRightPanel == null) {
            this.editorRightPanel = new JPanel();
            this.editorPanel.add(this.editorRightPanel, BorderLayout.EAST);
        }
        return this.editorRightPanel;
    }

    /**
     * Returns the styler for the editor.
     */
    @Override
    public JTextComponentStyler getStyler() {
        return this.editorStyler;
    }

    /**
     * Returns the config API.
     */
    public ConfigProvider getConfigProvider() {
        return MarkdownEditor.configs;
    }

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
        int keyCode = e.getKeyCode();
        if (
                keyCode != KeyEvent.VK_META &&
                keyCode != KeyEvent.VK_ALT &&
                keyCode != KeyEvent.VK_CONTROL &&
                keyCode != KeyEvent.VK_SHIFT
        ) {
            this.keyPressedCaretPos = this.editor.getCaretPosition();

            for (EditorFunction function : this.functions) {
                // Please note that I don't OR the downKeyMask with the modifiers since I want it to be
                // exactly the mask and nothing else. If the function returns 4 (Meta) then no other key
                // like Alt or Control is accepted in combination.
                if ((e.getModifiers() == function.getDownKeyMask() || function.getDownKeyMask() == 0) &&
                        e.getKeyCode() == function.getKeyCode()) {
                    function.perform();
                    break;
                }

            }
        }
        updateScrollbar();
    }

    /**
     * Updates the vertical scrollbar according to caret position.
     */
    private void updateScrollbar() {
//        try {
//            int scrollValue = (int)this.editor.modelToView(this.editor.getCaret().getDot()).getY();
//            scrollValue = scrollValue - (this.scrollPane.getHeight() / 2);
//            if (scrollValue < 0) {
//                scrollValue = 0;
//            }
//            if (scrollValue > this.scrollPane.getVerticalScrollBar().getMaximum()) {
//                scrollValue = this.scrollPane.getVerticalScrollBar().getMaximum();
//            }
//            this.scrollPane.getVerticalScrollBar().setValue(scrollValue);
//        }
//        catch (BadLocationException ble) {
//            //ble.printStackTrace(System.err);
//        }
    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key released event.
     */
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        //System.out.println("Keycode: " + keyCode);
        if (
                keyCode != KeyEvent.VK_META &&
                        keyCode != KeyEvent.VK_ALT &&
                        keyCode != KeyEvent.VK_CONTROL &&
                        keyCode != KeyEvent.VK_SHIFT
                ) {
            for (EditorInputFilter filter : this.filters) {
                filter.keyPressed(e);
            }
        }
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
    public Line getCurrentLine() {
        int i = keyPressedCaretPos - 1;
        try {
            while (i >= 0) {
                String check = this.editor.getText(i, 1);
                if (check.startsWith("\n")) {
                    ++i;
                    break;
                }
                --i;
            }
        }
        catch (BadLocationException ble) {
            i = 0;
        }
        // If i was < 0 to start with it will still be that here!
        if (i < 0) {
            i = 0;
        }

        return new JELine(this.editor, i);
    }

    /**
     * Set/replace the entire content of the editor.
     *
     * @param content The new content to set.
     */
    public void setEditorContent(String content) {
        this.editorStyler.disable();
        this.editor.setText(content);
        this.editorStyler.enable();
        this.editorStyler.styleDocument();
    }

    /**
     * Inserts new text into the editor or replaces current selection.
     *
     * @param text The text to insert.
     */
    public void insertText(String text) {
        this.editorStyler.disable();
        this.editor.replaceSelection(text);
        this.editorStyler.enable();
        this.editorStyler.styleCurrentParagraph();
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
     * Returns the caret dot location.
     */
    public int getCaretDot() {
        return this.editor.getCaret().getDot();
    }

    /**
     * Sets the caret dot location.
     *
     * @param dot The new dot location to set.
     */
    public void setCaretDot(int dot) {
        this.editor.getCaret().setDot(dot);
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
        } catch (IOException ioe) {
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
     * @throws java.io.IOException
     */
    public void loadFile(File file) throws IOException {
        setCurrentFile(file);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                line = line.replace("‚Äù", "\"");
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
        }
        setEditorContent(sb.toString());
        this.editor.setCaretPosition(0);
    }

    /**
     * Saves the currently edited file with the specified path.
     *
     * @param file The file path to save to.
     *
     * @throws IOException
     */
    public void saveFileAs(File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(getEditorContent());
        }

        FileWindowProps fileWindowProps = new FileWindowProps();
        fileWindowProps.setBounds(getGUI().getWindowFrame().getBounds());
        fileWindowProps.saveBounds(this);
    }

    /**
     * Opens a file chooser for specifying file to save to.
     *
     * @throws IOException
     */
    public void save() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Markdown", "md", "markdown");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(getGUI().getWindowFrame());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            saveFileAs(fileChooser.getSelectedFile());
        }
    }

    /**
     * Handles files being dropped on the editor.
     *
     * @param file The file dropped.
     */
    private void dropFile(File file)  {
        try {
            if (this.currentFile != null) {
                saveFileAs(this.currentFile);
            }
            else {
                if (this.editor.getText().trim().length() > 0) {
                    save();
                }
            }
            loadFile(file);
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    MarkdownEditor.this, ioe.getMessage(),
                    "Failed to open dropped file!", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----

    //
    // Static methods
    //

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

    //
    // Startup
    //

    /**
     * Opens one editor window with the specified file loaded.
     *
     * @param file The file to load.
     *
     * @throws IOException
     */
    public static void openEditor(File file) throws IOException {
        ++instanceCount;

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
        me.initGUI();

        enableOSXFullscreenIfOnOSX(me);

        if (file != null) {
            me.loadFile(file);
        }

        me.setVisible(true);
        me.editor.requestFocus();

    }

    /**
     * The delayed main handling of all files on the commandline. Will open one editor window per file.
     *
     * @param args The passed arguments.
     */
    private static void startup(String... args) {
        try {
            if (args.length > 0) {
                for (String arg : args) {
                    File argFile = new File(arg);
                    openEditor(argFile);
                }

            } else {
                openEditor(null);
            }
        } catch (IOException ioe) {
            System.err.println("Failed to open editor: " + ioe.getMessage());
        }
    }

    /**
     * Real main which calls startup(args) via SwingUtilities.invokeLater(...).
     *
     * @param args The arguments to the invocation.
     */
    public static void main(final String... args) {

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                startup(args);
            }
        });

    }

}
