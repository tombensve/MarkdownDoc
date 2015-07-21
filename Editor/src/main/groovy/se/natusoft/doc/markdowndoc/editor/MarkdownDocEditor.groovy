/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *
 *     Code Version
 *         1.4
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

import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import net.iharder.dnd.FileDrop
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.adapters.WindowListenerAdapter
import se.natusoft.doc.markdowndoc.editor.api.*
import se.natusoft.doc.markdowndoc.editor.config.*
import se.natusoft.doc.markdowndoc.editor.file.EditableProvider
import se.natusoft.doc.markdowndoc.editor.file.Editables
import se.natusoft.doc.markdowndoc.editor.gui.MultiPopupToolbar
import se.natusoft.doc.markdowndoc.editor.tools.ServiceDefLoader

import javax.swing.*
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
@TypeChecked
class MarkdownDocEditor extends JFrame implements Editor, GUI, KeyListener, Configurable, MouseMotionProvider {

    //
    // Constants
    //

    private static final String WINDOW_TITLE = "MarkdownDoc Editor 1.4"

    //
    // Static members
    //

    // Holds all configurations.
    private static ConfigProvider configs = new ConfigProviderHolder()

    // Manages persistent properties.
    private static PersistentProps persistentPropertiesProvider = new PersistentPropertiesProvider()

    // This is for styling the editorPane while editing.
    protected static ServiceLoader<JTextComponentStyler> stylerLoader = ServiceLoader.load(JTextComponentStyler.class)

    // All Configurable instances of components or filters are stored in this.
    protected static List<Configurable> configurables = new LinkedList<>()

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

    private Editable currentEditable

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

    // Holds EditorComponents that are EditorFunction subclasses.
    protected List<EditorFunction> functions = new LinkedList<>()

    // Holds EditorComponents that are EditorInputFilter subclasses.
    protected List<EditorInputFilter> filters = new LinkedList<>()

    // The components that delivers mouse motion events.
    protected List<MouseMotionProvider> mouseMotionProviders = new LinkedList<>()

    protected List<MouseMotionListener> mouseMotionListeners = new LinkedList<>()

    //
    // Properties
    //

    // Styles an JTextPane.
    JTextComponentStyler editorStyler

    //
    // Editor Configs
    //

    private static ValidSelectionConfigEntry fontConfig =
            new ValidSelectionConfigEntry("editor.pane.font", "The font to use.", "Helvetica",
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        ValidSelectionConfigEntry.Value[] validValues() {
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
                        ValidSelectionConfigEntry.Value[] validValues() {
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
                        ValidSelectionConfigEntry.Value[] validValues() {
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

    private Closure fontConfigChanged = { @NotNull ConfigEntry ce ->
        this.currentEditable?.editorPane?.setFont(Font.decode(ce.getValue()).
                deriveFont(Float.valueOf(fontSizeConfig.getValue())))
    }

    private Closure fontSizeConfigChanged = { @NotNull ConfigEntry ce ->
        this.currentEditable?.editorPane?.setFont(Font.decode(fontConfig.getValue()).deriveFont(Float.valueOf(ce.getValue())))
    }

    private Closure backgroundColorConfigChanged = { @NotNull ConfigEntry ce ->
        this.currentEditable?.editorPane?.setBackground(new ConfigColor(ce))
    }

    private Closure foregroundColorConfigChanged = { @NotNull ConfigEntry ce ->
        this.currentEditable?.editorPane?.setForeground(new ConfigColor(ce))
    }

    private Closure caretColorConfigChanged = { @NotNull ConfigEntry ce ->
        this.currentEditable?.editorPane?.setCaretColor(new ConfigColor(ce))
    }

    private Closure lookAndFeelConfigChanged = { @NotNull ConfigEntry ce ->
        try {
            UIManager.setLookAndFeel(ce.getValue())
            SwingUtilities.updateComponentTreeUI(this)  // update awt
            validate()
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace()
        }
    }

    private Closure toolbarConfigChanged = { @NotNull ConfigEntry ce ->
        if (this.toolBar != null) {
            this.toolBar.detach(this)
        }
        try {
            this.toolBar = (ToolBar)Class.forName(ce.getValue().
                    replace("se.natusoft.doc.markdowndoc.editor","se.natusoft.doc.markdowndoc.editor.gui")).newInstance()
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
        catch (ReflectiveOperationException e) {
            e.printStackTrace()
        }
    }

    private Closure topMarginConfigChanged = { @NotNull ConfigEntry ce ->
        if (this.currentEditable != null) {
            Insets margins = this.currentEditable.editorPane.getMargin()
            margins.top = ((IntegerConfigEntry) ce).getIntValue()
            this.currentEditable.editorPane.setMargin(margins)
            this.currentEditable.editorPane.revalidate()
        }
    }

    private Closure bottomMarginConfigChanged = { @NotNull ConfigEntry ce ->
        if (this.currentEditable != null) {
            Insets margins = this.currentEditable.editorPane.margin
            margins.bottom = ((IntegerConfigEntry) ce).intValue
            this.currentEditable.editorPane.setMargin(margins)
            this.currentEditable.editorPane.revalidate()
        }
    }

    private Closure leftMarginConfigChanged = { @NotNull ConfigEntry ce ->
        if (this.currentEditable != null) {
            Insets margins = this.currentEditable.editorPane.getMargin()
            margins.left = ((IntegerConfigEntry) ce).getIntValue()
            this.currentEditable.editorPane.setMargin(margins)
            this.currentEditable.editorPane.revalidate()
        }
    }

    private Closure rightMarginConfigChanged = { @NotNull ConfigEntry ce ->
        if (this.currentEditable != null) {
            Insets margins = this.currentEditable.editorPane.getMargin()
            margins.right = ((IntegerConfigEntry) ce).getIntValue()
            this.currentEditable.editorPane.setMargin(margins)
            this.currentEditable.editorPane.revalidate()
        }
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull ConfigProvider configProvider) {
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
    void unregisterConfigs(@NotNull ConfigProvider configProvider) {
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
    // Constructorish
    //

    /**
     * Creates a new MarkdownEditor instance.
     */
    MarkdownDocEditor() {}

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
    void initGUI() {
        addWindowListener(new WindowListenerAdapter() {
            @Override
            void windowClosing(WindowEvent ignored) {
                closeWindow()
            }
        })

        // Register configs
        registerConfigs(getConfigProvider())
        configurables.add(this)

        // Set Look and Feel

        if (lookAndFeelConfig.getValue().length() > 0) {
            try {
                UIManager.setLookAndFeel(lookAndFeelConfig.getValue())
            } catch (
                    ClassNotFoundException |
                    InstantiationException |
                    IllegalAccessException |
                    UnsupportedLookAndFeelException e
                    ) {
                e.printStackTrace()
            }
        }

        // Main Window

        this.setLayout(new BorderLayout())
        this.setSize(new Dimension(800, 800))
        this.setTitle(WINDOW_TITLE);

        // Editor

        this.editorPanel = new JPanel()
        this.editorPanel.setLayout(new BorderLayout())
        this.editorPanel.setAutoscrolls(true)

        if (this.currentEditable != null) {
            this.mouseMotionProviders.add(this.currentEditable)
        }

        // This will center the cursor vertically in the window. I found that it got confusing
        // so I decided to leave this out, but keep it commented out for a while. Maybe I enable
        // it with a setting later.
//        this.editorPane.addCaretListener(new CaretListener() {
//            @Override
//            void caretUpdate(CaretEvent caretEvent) {
//                updateScrollbar()
//            }
//        })

        // Setup active view

        this.scrollPane = new JScrollPane()
        //scrollPane.setAutoscrolls(true)
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

        this.scrollPane.setAutoscrolls(true)
        //this.scrollPane.getViewport().setAlignmentY(0.0f)

        this.editorPanel.add(scrollPane, BorderLayout.CENTER)
        add(this.editorPanel, BorderLayout.CENTER)

        // Load editorPane functions.

        List<DelayedInitializer> delayedInitializers = new LinkedList<>()

        componentLoader.each { EditorComponent component ->
            println "component: ${component}"

            if (component instanceof Configurable) {
                (component as Configurable).registerConfigs(getConfigProvider())
                configurables.add(component as Configurable)
            }

            component.setEditor(this)

            if (component instanceof EditorFunction) {
                this.functions.add(component as EditorFunction)
            } else if (component instanceof EditorInputFilter) {
                this.filters.add(component as EditorInputFilter)
            }

            if (component instanceof MouseMotionProvider) {
                this.mouseMotionProviders.add(component as MouseMotionProvider)
            }

            if (component instanceof DelayedInitializer) {
                delayedInitializers.add(component as DelayedInitializer)
            }

        }

        delayedInitializers.each { DelayedInitializer delayedInitializer -> delayedInitializer.init() }

        // Additional setup now that a component have possibly loaded config.

        setEditedFile(Editables.inst.someEditable)

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
    GUI getGUI() {
        this
    }

    /**
     * Returns the top margin.
     */
    @Override
    int getTopMargin() {
        topMargin.getIntValue()
    }

    /**
     * Convenience method to get the editor pane within the current editable.
     */
    private JTextPane getEditorPane() {
        return this.currentEditable?.editorPane
    }

    /**
     * Adds a mouse motion listener.
     *
     * @param listener The listener to add.
     */
    @Override
    synchronized void addMouseMotionListener(@NotNull MouseMotionListener listener) {
        this.mouseMotionListeners.add(listener)
        this.mouseMotionProviders.each {
            MouseMotionProvider mmp -> mmp?.addMouseMotionListener(listener)
        }
    }

    /**
     * Removes a mouse motion listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    synchronized void removeMouseMotionListener(@NotNull MouseMotionListener listener) {
        this.mouseMotionListeners.remove(listener)
        this.mouseMotionProviders.each { MouseMotionProvider mmp -> mmp?.removeMouseMotionListener(listener) }
    }

    /**
     * Returns the persistent properties provider.
     */
    @Override
    @NotNull PersistentProps getPersistentProps() {
        persistentPropertiesProvider
    }

    /**
     * Returns the panel above the editorPane and toolbar.
     */
    @Override
    @NotNull JPanel getTopPanel() {
        if (this.editorTopPanel == null) {
            this.editorTopPanel = new JPanel()
            this.editorPanel.add(this.editorTopPanel, BorderLayout.NORTH)
        }
        this.editorTopPanel
    }

    /**
     * Returns the panel below the editorPane and toolbar.
     */
    @Override
    @NotNull JPanel getBottomPanel() {
        if (this.editorBottomPanel == null) {
            this.editorBottomPanel = new JPanel()
            this.editorPanel.add(this.editorBottomPanel, BorderLayout.SOUTH)
        }
        this.editorBottomPanel
    }

    /**
     * Returns the panel to the left of the editorPane and toolbar.
     */
    @Override
    @NotNull JPanel getLeftPanel() {
        if (this.editorLeftPanel == null) {
            this.editorLeftPanel = new JPanel()
            this.editorPanel.add(this.editorLeftPanel, BorderLayout.WEST)
        }
        this.editorLeftPanel
    }

    /**
     * Returns the panel to the right of the editorPane and toolbar.
     */
    @Override
    @NotNull JPanel getRightPanel() {
        if (this.editorRightPanel == null) {
            this.editorRightPanel = new JPanel()
            this.editorPanel.add(this.editorRightPanel, BorderLayout.EAST)
        }
        this.editorRightPanel
    }

    /**
     * Returns the editorPane panel. A toolbar can for example be added here!
     */
    @Override
    @NotNull JPanel getEditorPanel() {
        this.editorPanel
    }

    /**
     * Returns the y coordinate of the top of the scrollable editorPane view.
     */
    @Override
    int getEditorVisibleY() {
        this.scrollPane.getViewport().getViewRect().y
    }

    /**
     * Returns the styler for the editorPane.
     */
    @Override
    @NotNull JTextComponentStyler getStyler() {
        this.editorStyler
    }

    /**
     * Returns the config API.
     */
    @SuppressWarnings("UnnecessaryQualifiedReference")
    @NotNull ConfigProvider getConfigProvider() {
        MarkdownDocEditor.configs
    }

    // KeyListener Implementation.

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     */
    @Override
    void keyTyped(KeyEvent ignored) {}

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key pressed event.
     */
    @Override
    void keyPressed(@NotNull KeyEvent e) {
        int keyCode = e.getKeyCode()
        if (
                keyCode != KeyEvent.VK_META &&
                keyCode != KeyEvent.VK_ALT &&
                keyCode != KeyEvent.VK_CONTROL &&
                keyCode != KeyEvent.VK_SHIFT
        ) {
            this.keyPressedCaretPos = this.currentEditable?.editorPane?.getCaretPosition()

            KeyboardKey keyboardKey = new KeyboardKey(e)

            this.functions.find { EditorFunction function ->
                function.getKeyboardShortcut() != null && function.getKeyboardShortcut().equals(keyboardKey)
            }?.perform()
        }
//        updateScrollbar()

    }

    // This will center the cursor vertically in the window. I found that it got confusing
    // so I decided to leave this out, but keep it commented out for a while. Maybe I enable
    // it with a setting later.
//    /**
//     * Updates the vertical scrollbar according to caret position.
//     */
//    protected void updateScrollbar() {
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
//    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key released event.
     */
    @Override
    void keyReleased(@NotNull KeyEvent e) {
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

    /**
     * Handles files being dropped on the editorPane.
     *
     * @param file The file dropped.
     */
    private void dropFile(@NotNull File file)  {
        try {
            if (!this.currentEditable?.saved) {
                if (this.currentEditable.file == null) {
                    selectNewFile()
                }
                else {
                    this.currentEditable.save()
                }
            }
            else {
                if (this.editorPane.getText().trim().length() > 0) {
                    selectNewFile()
                }
            }
            setEditedFile(file)
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    this, ioe.getMessage(),
                    "Failed to open dropped file!", JOptionPane.ERROR_MESSAGE)
        }
    }

    // --- Editor implementation.

    /**
     * This gets called when the window is closed. This can be overriden to
     * handle more more actions like exiting the JVM for example.
     */
    @Override
    void editorClosed() {}

    /**
     * Returns the contents of the editorPane.
     */
    @Override
    @Nullable String getEditorContent() {
        this.editorPane?.text
    }

    /**
     * Returns the current selection or null if none.
     */
    @Override
    @Nullable String getEditorSelection() {
        this.editorPane?.selectedText
    }

    /**
     * Returns the current line.
     */
    @Override
    @NotNull Line getCurrentLine() {
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

        new JELine(this.currentEditable.editorPane, i)
    }

    /**
     * Set/replace the entire content of the editorPane.
     *
     * @param content The new content to set.
     */
    @Override
    void setEditorContent(@NotNull String content) {
        this.editorPane.setText(content)
    }

    /**
     * Inserts new text into the editorPane or replaces current selection.
     *
     * @param text The text to insert.
     */
    @Override
    void insertText(@NotNull String text) {
        this.editorStyler.disable()
        this.editorPane.replaceSelection(text)
        this.editorStyler.enable()
        this.editorStyler.styleCurrentParagraph()
    }

    /**
     * Adds a blank line.
     */
    void addBlankLine() {
        this.editorPane.replaceSelection("\n")
    }

    /**
     * Moves the cared backwards.
     *
     * @param noChars The number of characters to move caret.
     */
    @Override
    void moveCaretBack(int noChars) {
        Caret caret = this.editorPane.getCaret()
        caret.setDot(caret.getDot() - noChars)
    }

    /**
     * Moves the caret forward.
     *
     * @param noChars The number of characters to move caret.
     */
    @Override
    void moveCaretForward(int noChars) {
        Caret caret = this.editorPane.getCaret()
        caret.setDot(caret.getDot() + noChars)
    }

    /**
     * Returns the current caret location.
     */
    @Override
    @NotNull Point getCaretLocation() {
        this.editorPane.getLocation(new Point())
    }

    /**
     * Moves the current caret location.
     *
     * @param location The new location.
     */
    @Override
    void setCaretLocation(@NotNull Point location) {
        this.editorPane.setLocation(location)
    }

    /**
     * Returns the caret dot location.
     */
    @Override
    int getCaretDot() {
        this.editorPane.getCaret().getDot()
    }

    /**
     * Sets the caret dot location.
     *
     * @param dot The new dot location to set.
     */
    @Override
    void setCaretDot(int dot) {
        this.editorPane.getCaret().setDot(dot)
    }


    /**
     * Requests focus for the editorPane.
     */
    @Override
    void requestEditorFocus() {
        this.editorPane.requestFocusInWindow()
    }

    /**
     * Needed for popping upp dialogs.
     */
    @Override
    @NotNull JFrame getWindowFrame() {
        this
    }

    /**
     * Opens a new editorPane window.
     */
    @Override
    void createNewFile() {
        try {
            selectNewFile()
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    this, ioe.getMessage(), "Failed to open new editorPane!", JOptionPane.ERROR_MESSAGE)
        }
    }

    /**
     * Copies the currently selected text.
     */
    @Override
    void copy() {
        this.editorPane.copy()
    }

    /**
     * Cuts the currently selected text.
     */
    @Override
    void cut() {
        this.editorPane.cut()
    }

    /**
     * Pastes the currently copied/cut text.
     */
    @Override
    void paste() {
        this.editorPane.paste()
    }

    /**
     * Makes the editorPane view visible in the main scrollable view.
     */
    @Override
    void showEditorComponent() {
        this.scrollPane.setViewportView(this.editorPane)
    }

    /**
     * Makes the specified component visible in the main scrollable view.
     */
    @Override
    void showOtherComponent(@NotNull JComponent component) {
        this.scrollPane.setViewportView(component)
    }

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to enable.
     */
    @Override
    void enableToolBarGroup(@NotNull String groupName) {
        this.toolBar.enableGroup(groupName)
    }

    /**
     * Disables all button in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to disable.
     */
    @Override
    void disableToolBarGroup(@NotNull String groupName) {
        this.toolBar.disableGroup(groupName)
    }

    /**
     * Refreshes styling and formatting of the document.
     */
    @Override
    void refreshStyling() {
        String text = getEditorContent()
        int caretDot = getCaretDot()
        getGUI().getStyler().disable()
        setEditorContent(text)
        setCaretDot(caretDot)
        getGUI().getStyler().enable()
        getStyler().styleDocument()
    }

    /**
     * Selects the file to edit in the editor view.
     * <p/>
     * If the file is a new file it will be loaded and added to the open editables.
     *
     * @param file The file to edit.
     */
    @Override
    void setEditedFile(@NotNull File file) {
        if (this.currentEditable != null) {
            FileDrop.remove(this.currentEditable.editorPane)
            this.currentEditable.editorPane.removeKeyListener(this)
            this.mouseMotionProviders.remove(this.currentEditable)
            this.mouseMotionListeners.each { MouseMotionListener listener ->
                this.currentEditable.removeMouseMotionListener(listener)
            }
        }

        this.currentEditable = openFile(file, this.editorStyler)
        this.scrollPane.setViewportView(this.currentEditable.editorPane)

        this.currentEditable.editorPane.addKeyListener(this)

        Insets margins = new Insets(
                topMargin.getIntValue(),
                leftMargin.getIntValue(),
                bottomMargin.getIntValue(),
                rightMargin.getIntValue()
        )
        this.currentEditable.editorPane.setMargin(margins)

        //noinspection GroovyResultOfObjectAllocationIgnored
        new FileDrop(this.currentEditable.editorPane, new  FileDrop.Listener() {
            void filesDropped(File[] files) {
                if (files.length >= 1) {
                    dropFile(files[0])
                }
            }
        })

        this.mouseMotionProviders.add(this.currentEditable)
        this.mouseMotionListeners.each { MouseMotionListener listener ->
            this.currentEditable.addMouseMotionListener(listener)
        }

        this.styler.currentStylee = this.currentEditable.editorPane

        this.currentEditable.editorPane.setRequestFocusEnabled(true)
    }

    /**
     * Returns the currently edited file.
     */
    @Override
    File getEditedFile() {
        this.currentEditable.file
    }

    /**
     * Saves the current editable.
     *
     * @throws IOException on failure to do so.
     */
    @Override
    void save() throws IOException {
        this.currentEditable.save()
    }

    /**
     * Opens a file chooser for specifying file as a new file.
     *
     * @throws IOException
     */
    @Override
    void selectNewFile() throws IOException {
        JFileChooser fileChooser = new JFileChooser()
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG)
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Markdown", "md", "markdown")
        fileChooser.setFileFilter(filter)
        int returnVal = fileChooser.showSaveDialog(getGUI().getWindowFrame())
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            setEditedFile(fileChooser.getSelectedFile())
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
    static void enableOSXFullscreenIfOnOSX(Window window) {
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
    static MarkdownDocEditor openEditor(JTextComponentStyler styler) throws IOException {
        ++MarkdownDocEditor.instanceCount

        MarkdownDocEditor me = new MarkdownDocEditor() {
            @Override
            void editorClosed() {
                setVisible(false)
                --MarkdownDocEditor.instanceCount
                if (MarkdownDocEditor.instanceCount == 0) {
                    System.exit(0)
                }

            }

        }
        me.editorStyler = styler
        me.initGUI()

        enableOSXFullscreenIfOnOSX(me)

        me.setVisible(true)
        me.editorPane.requestFocus()

        me
    }

    /**
     * Simply loads a file and adds it to editables. This will not double load a file!
     * If the specified file is already loaded nothing happens and the previously loaded
     * Editable is returned.
     *
     * @param file The file to open.
     * @param styler The styler to initialize the document model with.
     *
     * @return An Editable instance representing the file.
     */
    @NotNull static Editable openFile(@NotNull File file, JTextComponentStyler styler) {
        Editable editable = Editables.inst.getEditable(file)
        if (editable == null) {
            editable = new EditableProvider(file: file)
            styler.initDocumentModel(editable.editorPane)
            Editables.inst.addEditable(editable)
        }
        editable
    }

    /**
     * Recursively loads all markdown files in directory.
     *
     * @param dir The directory to scan.
     * @param styler A styler to initialize the document model with.
     */
    private static void loadDir(@NotNull File dir, JTextComponentStyler styler) {
        dir.eachFileRecurse(FileType.FILES) { File file ->
            if (file.name.endsWith(".md") || file.name.endsWith(".markdown")) {
                openFile(file, styler)
            }
        }
    }

    /**
     * The delayed main handling of all files on the commandline. Will open one editorPane window per file.
     *
     * @param args The passed arguments.
     */
    private static void startup(String... args) {
        try {
            JTextComponentStyler editorStyler = stylerLoader.iterator().next()
            if (editorStyler == null) {
                throw new RuntimeException("No META-INF/services/se.natusoft.doc.markdowndoc.editorPane.api." +
                        "JTextComponentStyler file pointing out an implementation to use have been provided!")
            }

            //this.editorStyler.initDocumentModel(this.editorPane)
            if (editorStyler instanceof Configurable) {
                (editorStyler as Configurable).registerConfigs(MarkdownDocEditor.configs)
                configurables.add(editorStyler as Configurable)
            }

            if (args.length > 0) {
                args.each { String arg ->
                    File argFile = new File(arg)
                    if (argFile.isDirectory()) {
                        loadDir(argFile, editorStyler)
                    }
                    else {
                        openFile(argFile, editorStyler)
                    }
                }
            }

            MarkdownDocEditor me = openEditor(editorStyler)
            me.setEditedFile(Editables.inst.someEditable)

        } catch (IOException ioe) {
            System.err.println("Failed to open editorPane: " + ioe.getMessage())
        }
    }

    /**
     * Real main which calls startup(args) via SwingUtilities.invokeLater(...).
     *
     * @param args The arguments to the invocation.
     */
    static void main(final String... args) {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override void run()
                {
                    startup(".")
                }
            }
        )

    }

}
