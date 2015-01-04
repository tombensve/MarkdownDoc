/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *
 *     Code Version
 *         1.3.5
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
package se.natusoft.doc.markdowndoc.editor

import groovy.transform.CompileStatic
import net.iharder.dnd.FileDrop
import se.natusoft.doc.markdowndoc.editor.adapters.WindowListenerAdapter
import se.natusoft.doc.markdowndoc.editor.api.*
import se.natusoft.doc.markdowndoc.editor.config.*
import se.natusoft.doc.markdowndoc.editor.functions.utils.FileWindowProps
import se.natusoft.doc.markdowndoc.editor.tools.ServiceDefLoader

import javax.swing.*
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.BadLocationException
import javax.swing.text.Caret
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseMotionListener
import java.awt.event.WindowEvent
import java.lang.reflect.Method
import java.util.List

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_EDITING
import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_TOOL

/**
 * This is an editorPane for editing markdown documents.
 */
@CompileStatic
public class MarkdownEditor extends JFrame implements Editor, GUI, KeyListener, Configurable, MouseMotionProvider {

    //
    // Static members
    //

    // Holds all configurations.
    private static ConfigProvider configs = new ConfigProviderHolder()

    // Manages persistent properties.
    private static PersistentProps persistentPropertiesProvider = new PersistentPropertiesProvider()

    //
    // Private Members
    //

    protected ToolBar toolBar

    // These are special panels on each side of the editorPane panel, but inside of the
    // toolbar. They are not created and added until some EditorComponent asks for
    // one of them.
    private JPanel editorTopPanel
    private JPanel editorBottomPanel
    private JPanel editorLeftPanel
    private JPanel editorRightPanel

    // The scrollPane sits in the center of this and the above editorPane*Panel resides
    // NORTH, SOUTH, EAST, and WEST of the editorPane.
    private JPanel editorPanel

    // This sits around the editorPane.
    private JScrollPane scrollPane

    // The actual editorPane component.
    protected JTextPane editorPane

    // Styles an JTextPane.
    private JTextComponentStyler editorStyler

    // When a file has been opened, or saved this will point to that file.
    // On save a file chooser will be opened if this is null otherwise this
    // file will be used for saving to.
    private File currentFile

    // Saved on key "pressed" and used later to get the current caret position.
    private int keyPressedCaretPos = 0

    // All other than the basic JEditorPane functionality are provided by EditorComponent:s of
    // which there are 2 sub-variants: EditorFunction (provides toolbar button, trigger key, and
    // functionality), and EditorInputFilter (receives keyboard events and can manipulate the
    // editorPane for automatic list bullets, etc).
    //
    // These EditorComponent:s are loaded using ServiceLoader returning EditorComponent
    // instances.

    protected ServiceLoader<EditorComponent> componentLoader = ServiceLoader.load(EditorComponent.class)

    // This is for styling the editorPane while editing.
    protected ServiceLoader<JTextComponentStyler> stylerLoader = ServiceLoader.load(JTextComponentStyler.class)

    // Holds EditorComponents that are EditorFunction subclasses.
    protected List<EditorFunction> functions = new LinkedList<>()

    // Holds EditorComponents that are EditorInputFilter subclasses.
    protected List<EditorInputFilter> filters = new LinkedList<>()

    // The components that delivers mouse motion events.
    protected List<MouseMotionProvider> mouseMotionProviders = new LinkedList<>()

    // All Configurable instances of components or filters are stored in this.
    protected List<Configurable> configurables = new LinkedList<>()

    //
    // Editor Configs
    //

    private static ValidSelectionConfigEntry fontConfig =
            new ValidSelectionConfigEntry("editor.pane.font", "The font to use.", "Helvetica",
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        public ValidSelectionConfigEntry.Value[] validValues() {
                            GraphicsEnvironment gEnv = GraphicsEnvironment
                                    .getLocalGraphicsEnvironment()
                            return ValidSelectionConfigEntry.convertToValues(gEnv.getAvailableFontFamilyNames())
                        }
                    },
                    CONFIG_GROUP_EDITING
            )

    private static DoubleConfigEntry fontSizeConfig =
            new DoubleConfigEntry("editor.pane.font.size", "The size of the font.", 16.0, 8.0, 50.0, CONFIG_GROUP_EDITING)

    private static ColorConfigEntry backgroundColorConfig =
            new ColorConfigEntry("editor.pane.background.color", "The editorPane background color.", 240, 240, 240, CONFIG_GROUP_EDITING)

    private static ColorConfigEntry foregroundColorConfig =
            new ColorConfigEntry("editor.pane.foreground.color", "The editorPane text color.", 80, 80, 80, CONFIG_GROUP_EDITING)

    private static ColorConfigEntry caretColorConfig =
            new ColorConfigEntry("editor.pane.caret.color", "The caret color", 0, 0, 0, CONFIG_GROUP_EDITING)

    private static ValidSelectionConfigEntry lookAndFeelConfig =
            new ValidSelectionConfigEntry("editorPane.lookandfeel", "The LookAndFeel to use.",
                    UIManager.getSystemLookAndFeelClassName(),
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        public ValidSelectionConfigEntry.Value[] validValues() {
                            ValidSelectionConfigEntry.Value[] vv =
                                    new ValidSelectionConfigEntry.Value[0]
                            UIManager.getInstalledLookAndFeels().each { UIManager.LookAndFeelInfo lfi ->
                                String className = lfi.getClassName()
                                String simpleName = className.substring(className.lastIndexOf('.') + 1)
                                vv += new ValidSelectionConfigEntry.Value(simpleName, className)
                            }
                            return vv
                        }
                    },
                    CONFIG_GROUP_TOOL
            )

    private static ValidSelectionConfigEntry toolbarConfig =
            new ValidSelectionConfigEntry("editorPane.toolbar", "The toolbar to use.",
                    MultiPopupToolbar.class.getName(),
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        public ValidSelectionConfigEntry.Value[] validValues() {
                            List<String> toolbarProviders = new LinkedList<>()
                            ServiceDefLoader.load(ToolBar.class).each { Class toolbarClass ->
                                toolbarProviders.add(toolbarClass.getName())
                            }
                            return ValidSelectionConfigEntry.convertToValues(ValidSelectionConfigEntry.stringListToArray(toolbarProviders),
                                    '.')
                        }
                    },
                    CONFIG_GROUP_TOOL
            )

    private static IntegerConfigEntry topMargin = new IntegerConfigEntry("editor.pane.top.margin",
            "The top margin.", 40, 0, 500, CONFIG_GROUP_EDITING)

    private static IntegerConfigEntry bottomMargin = new IntegerConfigEntry("editor.pane.bottom.margin",
            "The bottom margin.", 40, 0, 500, CONFIG_GROUP_EDITING)

    private static IntegerConfigEntry leftMargin = new IntegerConfigEntry("editor.pane.left.margin",
            "The left margin.", 60, 0, 500, CONFIG_GROUP_EDITING)

    private static IntegerConfigEntry rightMargin = new IntegerConfigEntry("editor.pane.right.margin",
            "The right margin.", 60, 0, 500, CONFIG_GROUP_EDITING)

    //
    // Config callbacks
    //

    private Closure fontConfigChanged = { ConfigEntry ce ->
        editorPane.setFont(Font.decode(ce.getValue()).
                deriveFont(Float.valueOf(fontSizeConfig.getValue())))
    }

    private Closure fontSizeConfigChanged = { ConfigEntry ce ->
        editorPane.setFont(Font.decode(fontConfig.getValue()).deriveFont(Float.valueOf(ce.getValue())))
    }

    private Closure backgroundColorConfigChanged = { ConfigEntry ce ->
        editorPane.setBackground(new ConfigColor(ce))
    }

    private Closure foregroundColorConfigChanged = { ConfigEntry ce ->
        editorPane.setForeground(new ConfigColor(ce))
    }

    private Closure caretColorConfigChanged = { ConfigEntry ce ->
        editorPane.setCaretColor(new ConfigColor(ce))
    }

    private Closure lookAndFeelConfigChanged = { ConfigEntry ce ->
        try {
            UIManager.setLookAndFeel(ce.getValue())
            SwingUtilities.updateComponentTreeUI(this)  // update awt
            validate()
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace()
        }
    }

    private Closure toolbarConfigChanged = { ConfigEntry ce ->
        if (this.toolBar != null) {
            this.toolBar.detach(this)
        }
        try {
            this.toolBar = (ToolBar)Class.forName(ce.getValue()).newInstance()
            functions.each { EditorFunction function ->
                // It is OK to not have a tool bar button!
                if (function.getGroup() != null && function.getToolBarButton() != null) {
                    this.toolBar.addFunction(function)
                }

            }

            this.toolBar.createToolBarContent()
            this.toolBar.attach(this)
            validate()
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace()
        }
    }

    private Closure topMarginConfigChanged = { ConfigEntry ce ->
        Insets margins = this.editorPane.getMargin()
        margins.top = ((IntegerConfigEntry)ce).getIntValue()
        this.editorPane.setMargin(margins)
        this.editorPane.revalidate()
    }

    private Closure bottomMarginConfigChanged = { ConfigEntry ce ->
        Insets margins = this.editorPane.margin
        margins.bottom = ((IntegerConfigEntry)ce).intValue
        this.editorPane.setMargin(margins)
        this.editorPane.revalidate()
    }

    private Closure leftMarginConfigChanged = { ConfigEntry ce ->
        Insets margins = this.editorPane.getMargin()
        margins.left = ((IntegerConfigEntry)ce).getIntValue()
        this.editorPane.setMargin(margins)
        this.editorPane.revalidate()
    }

    private Closure rightMarginConfigChanged = { ConfigEntry ce ->
        Insets margins = this.editorPane.getMargin()
        margins.right = ((IntegerConfigEntry)ce).getIntValue()
        this.editorPane.setMargin(margins)
        this.editorPane.revalidate()
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    public void registerConfigs(ConfigProvider configProvider) {
        configProvider.registerConfig(fontConfig, this.fontConfigChanged)
        configProvider.registerConfig(fontSizeConfig, this.fontSizeConfigChanged)
        configProvider.registerConfig(backgroundColorConfig, this.backgroundColorConfigChanged)
        configProvider.registerConfig(foregroundColorConfig, this.foregroundColorConfigChanged)
        configProvider.registerConfig(lookAndFeelConfig, this.lookAndFeelConfigChanged)
        configProvider.registerConfig(topMargin, this.topMarginConfigChanged)
        configProvider.registerConfig(bottomMargin, this.bottomMarginConfigChanged)
        configProvider.registerConfig(leftMargin, this.leftMarginConfigChanged)
        configProvider.registerConfig(rightMargin, this.rightMarginConfigChanged)
        configProvider.registerConfig(caretColorConfig, this.caretColorConfigChanged)
        configProvider.registerConfig(toolbarConfig, this.toolbarConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    public void unregisterConfigs(ConfigProvider configProvider) {
        configProvider.unregisterConfig(fontConfig, this.fontConfigChanged)
        configProvider.unregisterConfig(fontSizeConfig, this.fontSizeConfigChanged)
        configProvider.unregisterConfig(backgroundColorConfig, this.backgroundColorConfigChanged)
        configProvider.unregisterConfig(foregroundColorConfig, this.foregroundColorConfigChanged)
        configProvider.unregisterConfig(lookAndFeelConfig, this.lookAndFeelConfigChanged)
        configProvider.unregisterConfig(topMargin, this.topMarginConfigChanged)
        configProvider.unregisterConfig(bottomMargin, this.bottomMarginConfigChanged)
        configProvider.unregisterConfig(leftMargin, this.leftMarginConfigChanged)
        configProvider.unregisterConfig(rightMargin, this.rightMarginConfigChanged)
        configProvider.unregisterConfig(caretColorConfig, this.caretColorConfigChanged)
        configProvider.unregisterConfig(toolbarConfig, this.toolbarConfigChanged)
    }

    //
    // Constructors
    //

    /**
     * Creates a new MarkdownEditor instance.
     */
    public MarkdownEditor() {}

    protected void closeWindow() {
        ConfigProvider cp = getConfigProvider()
        configurables.each {Configurable configurable ->
            configurable.unregisterConfigs(cp)
        }
        setVisible(false)
        editorClosed()
    }

    /**
     * Sets up the gui, etc.
     */
    public void initGUI() {
        addWindowListener(new WindowListenerAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow()
            }
        })

        // Register configs
        registerConfigs(getConfigProvider())
        this.configurables.add(this)

        // Set Look and Feel

        if (lookAndFeelConfig.getValue().length() > 0) {
            try {
                UIManager.setLookAndFeel(lookAndFeelConfig.getValue())
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace()
            }
        }

        // Main Window

        this.setLayout(new BorderLayout())
        this.setSize(new Dimension(800, 800))
        this.setTitle("MarkdownDoc Editor 1.3.6");

        // Editor

        this.editorPanel = new JPanel()
        this.editorPanel.setLayout(new BorderLayout())
        this.editorPanel.setAutoscrolls(true)


        this.editorPane = new JTextPane() {
            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize()
                dim.setSize(getWidth(), dim.getHeight())

                return dim
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize()
            }

        }

        this.mouseMotionProviders.add(new MouseMotionProvider() {
            @Override
            public void addMouseMotionListener(MouseMotionListener listener) {
                editorPane.addMouseMotionListener(listener)
            }

            @Override
            public void removeMouseMotionListener(MouseMotionListener listener) {
                editorPane.removeMouseMotionListener(listener)
            }

        })

        this.editorStyler = this.stylerLoader.iterator().next()
        if (this.editorStyler == null) {
            throw new RuntimeException("No META-INF/services/se.natusoft.doc.markdowndoc.editorPane.api.JTextComponentStyler " +
                    "file pointing out an implementation to use have been provided!")
        }

        this.editorStyler.init(this.editorPane)
        if (this.editorStyler instanceof Configurable) {
            ((Configurable)this.editorStyler).registerConfigs(getConfigProvider())
            this.configurables.add((Configurable)this.editorStyler)
        }

        this.editorPane.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                updateScrollbar()
            }
        })

        //noinspection GroovyResultOfObjectAllocationIgnored,UnnecessaryQualifiedReference
        new FileDrop(this.editorPane, new FileDrop.Listener() {
            public void filesDropped(java.io.File[] files) {
                if (files.length >= 1) {
                    dropFile(files[0])
                }
            }
        })

        // Setup active view

        this.scrollPane = new JScrollPane(this.editorPane)
        //scrollPane.setAutoscrolls(true)
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

        this.scrollPane.setAutoscrolls(true)
        //this.scrollPane.getViewport().setAlignmentY(0.0f)

        this.editorPanel.add(scrollPane, BorderLayout.CENTER)
        add(this.editorPanel, BorderLayout.CENTER)

        // Load editorPane functions.

        List<DelayedInitializer> delayedInitializers = new LinkedList<>()

        componentLoader.each { EditorComponent component ->
            if (component instanceof Configurable) {
                ((Configurable)component).registerConfigs(getConfigProvider())
                this.configurables.add((Configurable)component)
            }

            component.setEditor(this)

            if (component instanceof EditorFunction) {
                this.functions.add((EditorFunction) component)
            } else if (component instanceof EditorInputFilter) {
                this.filters.add((EditorInputFilter) component)
            }

            if (component instanceof MouseMotionProvider) {
                this.mouseMotionProviders.add((MouseMotionProvider)component)
            }

            if (component instanceof DelayedInitializer) {
                delayedInitializers.add((DelayedInitializer)component)
            }

        }

        delayedInitializers.each { DelayedInitializer delayedInitializer -> delayedInitializer.init() }

        // Additional setup now that a component have possibly loaded config.

        Insets margins = new Insets(
                topMargin.getIntValue(),
                leftMargin.getIntValue(),
                bottomMargin.getIntValue(),
                rightMargin.getIntValue()
        )
        this.editorPane.setMargin(margins)
        this.editorPane.addKeyListener(this)

        this.editorPane.setRequestFocusEnabled(true)

        // Toolbar

        toolbarConfigChanged(toolbarConfig)

    }

    //
    // Methods
    //

    /**
     * Returns the editorPane GUI API.
     */
    @Override
    public GUI getGUI() {
        return this
    }

    /**
     * Returns the top margin.
     */
    @Override
    public int getTopMargin() {
        return topMargin.getIntValue()
    }

    /**
     * Adds a mouse motion listener.
     *
     * @param listener The listener to add.
     */
    @Override
    public synchronized void addMouseMotionListener(MouseMotionListener listener) {
        this.mouseMotionProviders.each { MouseMotionProvider mmp -> mmp.addMouseMotionListener(listener) }
    }

    /**
     * Removes a mouse motion listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public synchronized void removeMouseMotionListener(MouseMotionListener listener) {
        this.mouseMotionProviders.each { MouseMotionProvider mmp -> mmp.removeMouseMotionListener(listener) }
    }

    /**
     * Returns the persistent properties provider.
     */
    @Override
    public PersistentProps getPersistentProps() {
        return persistentPropertiesProvider
    }

    /**
     * Returns the panel above the editorPane and toolbar.
     */
    @Override
    public JPanel getTopPanel() {
        if (this.editorTopPanel == null) {
            this.editorTopPanel = new JPanel()
            this.editorPanel.add(this.editorTopPanel, BorderLayout.NORTH)
        }
        return this.editorTopPanel
    }

    /**
     * Returns the panel below the editorPane and toolbar.
     */
    @Override
    public JPanel getBottomPanel() {
        if (this.editorBottomPanel == null) {
            this.editorBottomPanel = new JPanel()
            this.editorPanel.add(this.editorBottomPanel, BorderLayout.SOUTH)
        }
        return this.editorBottomPanel
    }

    /**
     * Returns the panel to the left of the editorPane and toolbar.
     */
    @Override
    public JPanel getLeftPanel() {
        if (this.editorLeftPanel == null) {
            this.editorLeftPanel = new JPanel()
            this.editorPanel.add(this.editorLeftPanel, BorderLayout.WEST)
        }
        return this.editorLeftPanel
    }

    /**
     * Returns the panel to the right of the editorPane and toolbar.
     */
    @Override
    public JPanel getRightPanel() {
        if (this.editorRightPanel == null) {
            this.editorRightPanel = new JPanel()
            this.editorPanel.add(this.editorRightPanel, BorderLayout.EAST)
        }
        return this.editorRightPanel
    }

    /**
     * Returns the editorPane panel. A toolbar can for example be added here!
     */
    @Override
    public JPanel getEditorPanel() {
        return this.editorPanel
    }

    /**
     * Returns the y coordinate of the top of the scrollable editorPane view.
     */
    @Override
    public int getEditorVisibleY() {
        return this.scrollPane.getViewport().getViewRect().y
    }

    /**
     * Returns the styler for the editorPane.
     */
    @Override
    public JTextComponentStyler getStyler() {
        return this.editorStyler
    }

    /**
     * Returns the config API.
     */
    @SuppressWarnings("UnnecessaryQualifiedReference")
    @Override
    public ConfigProvider getConfigProvider() {
        return MarkdownEditor.configs
    }

    // KeyListener Implementation.

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     */
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key pressed event.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode()
        if (
                keyCode != KeyEvent.VK_META &&
                keyCode != KeyEvent.VK_ALT &&
                keyCode != KeyEvent.VK_CONTROL &&
                keyCode != KeyEvent.VK_SHIFT
        ) {
            this.keyPressedCaretPos = this.editorPane.getCaretPosition()

            KeyboardKey keyboardKey = new KeyboardKey(e)

            this.functions.find { EditorFunction function ->
                function.getKeyboardShortcut() != null && function.getKeyboardShortcut().equals(keyboardKey)
            }?.perform()
        }
        updateScrollbar()

    }

    /**
     * Updates the vertical scrollbar according to caret position.
     */
    protected void updateScrollbar() {
        // This will center the cursor vertically in the window. I found that it got confusing
        // so I decided to leave this out, but keep it commented out for a while. Maybe I enable
        // it with a setting later.
//        try {
//            int scrollValue = (int)this.editorPane.modelToView(this.editorPane.getCaret().getDot()).getY()
//            scrollValue = scrollValue - (this.scrollPane.getHeight() / 2)
//            if (scrollValue < 0) {
//                scrollValue = 0
//            }
//            if (scrollValue > this.scrollPane.getVerticalScrollBar().getMaximum()) {
//                scrollValue = this.scrollPane.getVerticalScrollBar().getMaximum()
//            }
//            this.scrollPane.getVerticalScrollBar().setValue(scrollValue)
//        }
//        catch (BadLocationException ble) {
//            //ble.printStackTrace(System.err)
//        }
    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key released event.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode()
        if (
                keyCode != KeyEvent.VK_META &&
                        keyCode != KeyEvent.VK_ALT &&
                        keyCode != KeyEvent.VK_CONTROL &&
                        keyCode != KeyEvent.VK_SHIFT
                ) {
            this.filters.each { EditorInputFilter filter ->
                filter.keyPressed(e)
            }
        }
    }

    // --- Editor implementation.

    /**
     * This gets called when the window is closed. This can be overriden to
     * handle more more actions like exiting the JVM for example.
     */
    @Override
    public void editorClosed() {
    }

    /**
     * Returns the current file or null if none.
     */
    @Override
    public File getCurrentFile() {
        return this.currentFile
    }

    /**
     * Sets the current file.
     *
     * @param file The file to set.
     */
    @Override
    public void setCurrentFile(File file) {
        this.currentFile = file
    }

    /**
     * Returns the contents of the editorPane.
     */
    @Override
    public String getEditorContent() {
        return this.editorPane.getText()
    }

    /**
     * Returns the current selection or null if none.
     */
    @Override
    public String getEditorSelection() {
        return this.editorPane.getSelectedText()
    }

    /**
     * Returns the current line.
     */
    @Override
    public Line getCurrentLine() {
        int i = keyPressedCaretPos - 1
        try {
            while (i >= 0) {
                String check = this.editorPane.getText(i, 1)
                if (check.startsWith("\n")) {
                    ++i
                    break
                }
                --i
            }
        }
        catch (BadLocationException ignored) {
            i = 0
        }
        // If i was < 0 to start with it will still be that here!
        if (i < 0) {
            i = 0
        }

        return new JELine(this.editorPane, i)
    }

    /**
     * Set/replace the entire content of the editorPane.
     *
     * @param content The new content to set.
     */
    @Override
    public void setEditorContent(String content) {
        this.editorPane.setText(content)
    }

    /**
     * Inserts new text into the editorPane or replaces current selection.
     *
     * @param text The text to insert.
     */
    @Override
    public void insertText(String text) {
        this.editorStyler.disable()
        this.editorPane.replaceSelection(text)
        this.editorStyler.enable()
        this.editorStyler.styleCurrentParagraph()
    }

    /**
     * Adds a blank line.
     */
    public void addBlankLine() {
        this.editorPane.replaceSelection("\n")
    }

    /**
     * Moves the cared backwards.
     *
     * @param noChars The number of characters to move caret.
     */
    @Override
    public void moveCaretBack(int noChars) {
        Caret caret = this.editorPane.getCaret()
        caret.setDot(caret.getDot() - noChars)
    }

    /**
     * Moves the caret forward.
     *
     * @param noChars The number of characters to move caret.
     */
    @Override
    public void moveCaretForward(int noChars) {
        Caret caret = this.editorPane.getCaret()
        caret.setDot(caret.getDot() + noChars)
    }

    /**
     * Returns the current caret location.
     */
    @Override
    public Point getCaretLocation() {
        return this.editorPane.getLocation(new Point())
    }

    /**
     * Moves the current caret location.
     *
     * @param location The new location.
     */
    @Override
    public void setCaretLocation(Point location) {
        this.editorPane.setLocation(location)
    }

    /**
     * Returns the caret dot location.
     */
    @Override
    public int getCaretDot() {
        return this.editorPane.getCaret().getDot()
    }

    /**
     * Sets the caret dot location.
     *
     * @param dot The new dot location to set.
     */
    @Override
    public void setCaretDot(int dot) {
        this.editorPane.getCaret().setDot(dot)
    }


    /**
     * Requests focus for the editorPane.
     */
    @Override
    public void requestEditorFocus() {
        this.editorPane.requestFocusInWindow()
    }

    /**
     * Needed for popping upp dialogs.
     */
    @Override
    public JFrame getWindowFrame() {
        return this
    }

    /**
     * Opens a new editorPane window.
     */
    @Override
    public void openNewEditorWindow() {
        try {
            openEditor(null)
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    this, ioe.getMessage(), "Failed to open new editorPane!", JOptionPane.ERROR_MESSAGE)
        }
    }

    /**
     * Copies the currently selected text.
     */
    @Override
    public void copy() {
        this.editorPane.copy()
    }

    /**
     * Cuts the currently selected text.
     */
    @Override
    public void cut() {
        this.editorPane.cut()
    }

    /**
     * Pastes the currently copied/cut text.
     */
    @Override
    public void paste() {
        this.editorPane.paste()
    }

    /**
     * Makes the editorPane view visible in the main scrollable view.
     */
    @Override
    public void showEditorComponent() {
        this.scrollPane.setViewportView(this.editorPane)
    }

    /**
     * Makes the specified component visible in the main scrollable view.
     */
    @Override
    public void showOtherComponent(JComponent component) {
        this.scrollPane.setViewportView(component)
    }

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to enable.
     */
    @Override
    public void enableToolBarGroup(String groupName) {
        this.toolBar.enableGroup(groupName)
    }

    /**
     * Disables all button in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to disable.
     */
    @Override
    public void disableToolBarGroup(String groupName) {
        this.toolBar.disableGroup(groupName)
    }

    /**
     * Refreshes styling and formatting of the document.
     */
    @Override
    public void refreshStyling() {
        String text = getEditorContent()
        int caretDot = getCaretDot()
        getGUI().getStyler().disable()
        setEditorContent(text)
        setCaretDot(caretDot)
        getGUI().getStyler().enable()
        getStyler().styleDocument()
    }

    /**
     * Opens the specified file in the editorPane.
     *
     * @param file The file to open.
     * @throws java.io.IOException
     */
    @Override
    public void loadFile(File file) throws IOException {
        setCurrentFile(file)

        StringBuilder sb = new StringBuilder()
        file.withReader('UTF-8') { BufferedReader reader ->
            reader.eachLine { String line ->
                line = line.replace("‚Äù", "\"")
                sb.append(line)
                sb.append("\n")
            }
        }

        this.editorStyler.disable()
            setEditorContent(sb.toString())
            this.editorPane.setCaretPosition(0)

            getConfigProvider().refreshConfigs()
        this.editorStyler.enable()

        FileWindowProps fileWindowProps = new FileWindowProps()
        fileWindowProps.load(this)
        if (fileWindowProps.hasProperties()) {
            getGUI().getWindowFrame().setBounds(fileWindowProps.getBounds())
        }

        this.editorStyler.styleDocument()
    }

    /**
     * Saves the currently edited file with the specified path.
     *
     * @param file The file path to save to.
     *
     * @throws IOException
     */
    @Override
    public void saveFileAs(File file) throws IOException {
        file.withWriter('UTF-8') { BufferedWriter writer ->
            writer.write(getEditorContent())
        }
        FileWindowProps fileWindowProps = new FileWindowProps()
        fileWindowProps.setBounds(getGUI().getWindowFrame().getBounds())
        fileWindowProps.saveBounds(this)
    }

    /**
     * Opens a file chooser for specifying file to save to.
     *
     * @throws IOException
     */
    @Override
    public void save() throws IOException {
        JFileChooser fileChooser = new JFileChooser()
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG)
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Markdown", "md", "markdown")
        fileChooser.setFileFilter(filter)
        int returnVal = fileChooser.showSaveDialog(getGUI().getWindowFrame())
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            saveFileAs(fileChooser.getSelectedFile())
        }
    }

    /**
     * Handles files being dropped on the editorPane.
     *
     * @param file The file dropped.
     */
    private void dropFile(File file)  {
        try {
            if (this.currentFile != null) {
                saveFileAs(this.currentFile)
            }
            else {
                if (this.editorPane.getText().trim().length() > 0) {
                    save()
                }
            }
            loadFile(file)
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    this, ioe.getMessage(),
                    "Failed to open dropped file!", JOptionPane.ERROR_MESSAGE)
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
    @SuppressWarnings(["unchecked", "rawtypes", "unused"])
    public static void enableOSXFullscreenIfOnOSX(Window window) {
        if (window == null) return

        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities")
            Class[] params = new Class[2]
            params[0] = Window.class
            params[1] = Boolean.TYPE
            Method method = util.getMethod("setWindowCanFullScreen", params)
            method.invoke(util, window, true)
        } catch (ClassNotFoundException ignored) {
            /* Not on Mac OS X! */
        } catch (Exception e) {
            e.printStackTrace(System.err)
        }

    }

    //
    // Startup
    //

    // This is incremented for each editorPane window opened and decreased for each closed.
    // When 0 is reached again the JVM will exit.
    private static int instanceCount = 0

    /**
     * Opens one editorPane window with the specified file loaded.
     *
     * @param file The file to load.
     *
     * @throws IOException
     */
    @SuppressWarnings("UnnecessaryQualifiedReference")
    public static void openEditor(final File file) throws IOException {
        ++MarkdownEditor.instanceCount

        MarkdownEditor me = new MarkdownEditor() {
            @Override
            public void editorClosed() {
                setVisible(false)
                --MarkdownEditor.instanceCount
                if (MarkdownEditor.instanceCount == 0) {
                    System.exit(0)
                }

            }

        }
        me.initGUI()

        enableOSXFullscreenIfOnOSX(me)

        if (file != null) {
            me.loadFile(file)
        }

        me.setVisible(true)
        me.editorPane.requestFocus()
    }

    /**
     * The delayed main handling of all files on the commandline. Will open one editorPane window per file.
     *
     * @param args The passed arguments.
     */
    private static void startup(String... args) {
        try {
            if (args.length > 0) {
                args.each { String arg ->
                    File argFile = new File(arg)
                    openEditor(argFile)
                }

            } else {
                openEditor(null)
            }
        } catch (IOException ioe) {
            System.err.println("Failed to open editorPane: " + ioe.getMessage())
        }
    }

    /**
     * Real main which calls startup(args) via SwingUtilities.invokeLater(...).
     *
     * @param args The arguments to the invocation.
     */
    public static void main(final String... args) {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run()
                {
                    startup(args)
                }
            }
        )

    }

}
