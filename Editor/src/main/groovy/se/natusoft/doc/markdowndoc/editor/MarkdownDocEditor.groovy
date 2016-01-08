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
import se.natusoft.doc.markdowndoc.editor.gui.GuiEnvToolsTrait
import se.natusoft.doc.markdowndoc.editor.gui.MouseListenersTrait
import se.natusoft.doc.markdowndoc.editor.gui.MultiPopupToolbar
import se.natusoft.doc.markdowndoc.editor.tools.ServiceDefLoader

import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.BadLocationException
import javax.swing.text.Caret
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
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
@Singleton
class MarkdownDocEditor extends JFrame implements Editor, GUI, KeyListener, MouseListenersTrait, Configurable,
        MouseMotionProvider, GuiEnvToolsTrait {

    //
    // Constants
    //

    private static final String WINDOW_TITLE = "MarkdownDoc Editor 1.4.2"

    //
    // Static members
    //

    // This is for styling the editorPane while editing.
    static final ServiceLoader<JTextComponentStylerFactory> stylerFactoryLoader =
            ServiceLoader.load(JTextComponentStylerFactory.class)

    static final JTextComponentStylerFactory editorStylerFactory = stylerFactoryLoader?.iterator()?.next()

    //
    // Private Members
    //

    /** The currently edited editable. */
    private Editable editable

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

    // Saved on key "pressed" and used later to get the current caret position.
    private int keyPressedCaretPos = 0

    // All other than the basic JEditorPane functionality are provided by EditorComponent:s of
    // which there are 2 sub-variants: EditorFunction (provides toolbar button, trigger key, and
    // functionality), and EditorInputFilter (receives keyboard events and can manipulate the
    // editorPane for automatic list bullets, etc).
    //
    // These EditorComponent:s are loaded using ServiceLoader returning EditorComponent
    // instances.

    /** Dynamically loads different editor components found and registered on classpath. */
    protected ServiceLoader<EditorComponent> componentLoader = ServiceLoader.load(EditorComponent.class)

    /** Holds EditorComponents that are EditorFunction subclasses. */
    protected List<EditorFunction> functions = new LinkedList<>()

    /**  Holds EditorComponents that are EditorInputFilter subclasses.*/
    protected List<EditorInputFilter> filters = new LinkedList<>()


    /** Providers of mouse motion events. */
    protected List<MouseMotionProvider> mouseMotionProviders = new LinkedList<>()

    /** listeners of mouse motion events. */
    protected List<MouseMotionListener> mouseMotionListeners = new LinkedList<>()

    /** Any closure in this list will be called on triggering of cancel. */
    protected List<Closure<Void>> cancelCallbacks = new LinkedList<>()

    //
    // Editor Configs
    //

    private static ValidSelectionConfigEntry fontConfig =
            new ValidSelectionConfigEntry("editor.pane.font", "The font to use.", "Helvetica",
                    new ValidSelectionConfigEntry.ValidValues() {
                        @Override
                        ValidSelectionConfigEntry.Value[] validValues() {
                            final GraphicsEnvironment gEnv = GraphicsEnvironment
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
                            UIManager.getInstalledLookAndFeels().each { final UIManager.LookAndFeelInfo lfi ->
                                final String className = lfi.getClassName()
                                final String simpleName = className.substring(className.lastIndexOf('.') + 1)
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
                            final List<String> toolbarProviders = new LinkedList<>()
                            ServiceDefLoader.load(ToolBar.class).each { final Class toolbarClass ->
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

//    private static DoubleConfigEntry editorOpacity = new DoubleConfigEntry("editor.window.opacity",
//            "The editor opacity.", 1.0d, 0.0d,1.0d, CONFIG_GROUP_EDITING)

    //
    // Config callbacks
    //

//    private Closure opacityChanged = { @NotNull ConfigEntry ce ->
//        float opacity = Float.valueOf(ce.value)
//        safeOpacity = opacity
//    }

    private Closure fontConfigChanged = { @NotNull final ConfigEntry ce ->
        this.editable?.editorPane?.setFont(Font.decode(ce.getValue()).
                deriveFont(Float.valueOf(fontSizeConfig.getValue())))
    }

    private Closure fontSizeConfigChanged = { @NotNull final ConfigEntry ce ->
        this.editable?.editorPane?.setFont(Font.decode(fontConfig.getValue()).deriveFont(Float.valueOf(ce.getValue())))
    }

    private Closure backgroundColorConfigChanged = { @NotNull final ConfigEntry ce ->
        this.editable?.editorPane?.setBackground(new ConfigColor(ce))
    }

    private Closure foregroundColorConfigChanged = { @NotNull final ConfigEntry ce ->
        this.editable?.editorPane?.setForeground(new ConfigColor(ce))
    }

    private Closure caretColorConfigChanged = { @NotNull final ConfigEntry ce ->
        this.editable?.editorPane?.setCaretColor(new ConfigColor(ce))
    }

    private Closure lookAndFeelConfigChanged = { @NotNull final ConfigEntry ce ->
        try {
            UIManager.setLookAndFeel(ce.getValue())
            SwingUtilities.updateComponentTreeUI(this)  // update awt
            validate()
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            e.printStackTrace()
        }
    }

    private Closure toolbarConfigChanged = { @NotNull final ConfigEntry ce ->
        if (this.toolBar != null) {
            this.toolBar.detach(this)
            if (this.toolBar instanceof Configurable) {
                (this.toolBar as Configurable).unregisterConfigs(Services.configs)
                Services.configurables.remove(this.toolBar as Configurable)
            }
        }
        try {
            String cval = ce.getValue()
            // translate old config to new since some ServiceLoader loaded components have moved package.
            if (cval.startsWith("se.natusoft.doc.markdowndoc.editor") &&
                    !cval.startsWith("se.natusoft.doc.markdowndoc.editor.gui")) {
                cval = cval.replace("se.natusoft.doc.markdowndoc.editor","se.natusoft.doc.markdowndoc.editor.gui")
            }
            this.toolBar = (ToolBar)Class.forName(cval).newInstance()
            if (this.toolBar instanceof Configurable) {
                (this.toolBar as Configurable).registerConfigs(Services.configs)
                Services.configurables.add(this.toolBar as Configurable)
            }
            this.functions.each { final EditorFunction function ->
                // It is OK to not have a tool bar button!
                if (function.getGroup() != null && function.getToolBarButton() != null) {
                    this.toolBar.addFunction(function)
                }

            }

            this.toolBar.createToolBarContent()
            this.toolBar.attach(this)
            validate()
        }
        catch (final ReflectiveOperationException e) {
            e.printStackTrace()
        }
    }

    private Closure topMarginConfigChanged = { @NotNull final ConfigEntry ce ->
        if (this.editable != null) {
            final Insets margins = this.editable.editorPane.getMargin()
            margins.top = ((IntegerConfigEntry) ce).getIntValue()
            this.editable.editorPane.setMargin(margins)
            this.editable.editorPane.revalidate()
        }
    }

    private Closure bottomMarginConfigChanged = { @NotNull final ConfigEntry ce ->
        if (this.editable != null) {
            final Insets margins = this.editable.editorPane.margin
            margins.bottom = ((IntegerConfigEntry) ce).intValue
            this.editable.editorPane.setMargin(margins)
            this.editable.editorPane.revalidate()
        }
    }

    private Closure leftMarginConfigChanged = { @NotNull final ConfigEntry ce ->
        if (this.editable != null) {
            final Insets margins = this.editable.editorPane.getMargin()
            margins.left = ((IntegerConfigEntry) ce).getIntValue()
            this.editable.editorPane.setMargin(margins)
            this.editable.editorPane.revalidate()
        }
    }

    private Closure rightMarginConfigChanged = { @NotNull final ConfigEntry ce ->
        if (this.editable != null) {
            final Insets margins = this.editable.editorPane.getMargin()
            margins.right = ((IntegerConfigEntry) ce).getIntValue()
            this.editable.editorPane.setMargin(margins)
            this.editable.editorPane.revalidate()
        }
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
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
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
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

    protected void closeWindow() {
        final ConfigProvider cp = Services.configs
        Services.configurables.each {final Configurable configurable ->
            configurable.unregisterConfigs(cp)
        }
        setVisible(false)
        editorClosed()
    }

    /**
     * Sets up the gui, etc.
     */
    void initGUI() {
        initGuiEnvTools(this)
        addWindowListener(new WindowListenerAdapter() {
            @Override
            void windowClosing(final WindowEvent ignored) {
                closeWindow()
            }
        })

        // Register configs
        registerConfigs(Services.configs)
        Services.configurables.add(this)

        // Set Look and Feel

        if (lookAndFeelConfig.getValue().length() > 0) {
            try {
                UIManager.setLookAndFeel(lookAndFeelConfig.getValue())
            } catch (final
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

        if (this.editable != null) {
            this.mouseMotionProviders.add(this.editable)
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
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

        this.scrollPane.setAutoscrolls(true)
        //this.scrollPane.getViewport().setAlignmentY(0.0f)

        this.editorPanel.add(scrollPane, BorderLayout.CENTER)
        add(this.editorPanel, BorderLayout.CENTER)

        // Load editorPane functions.

        final List<DelayedInitializer> delayedInitializers = new LinkedList<>()

        componentLoader.each { final EditorComponent component ->

            if (component instanceof Configurable) {
                (component as Configurable).registerConfigs(Services.configs)
                Services.configurables.add(component as Configurable)
            }

            component.setEditor(this)

            if (component instanceof EditorFunction) {
                this.functions.add(component as EditorFunction)
            }

            if (component instanceof EditorInputFilter) {
                this.filters.add(component as EditorInputFilter)
            }

            if (component instanceof MouseMotionProvider) {
                this.mouseMotionProviders.add(component as MouseMotionProvider)
            }

            if (component instanceof DelayedInitializer) {
                delayedInitializers.add(component as DelayedInitializer)
            }

        }



        delayedInitializers.each { final DelayedInitializer delayedInitializer -> delayedInitializer.init() }

        // Additional setup now that a component have possibly loaded config.


        // Toolbar

        toolbarConfigChanged(toolbarConfig)

    }

    //
    // Methods
    //

    /**
     * Adds a callback for cancel.
     *
     * @param callback The callback to add.
     */
    void addCancelCallback(final Closure<Void> callback) {
        this.cancelCallbacks += callback
    }

    /**
     * Removes a callback for cancel.
     *
     * @param callback The callback to remove.
     */
    void removeCancelCallback(final Closure<Void> callback) {
        this.cancelCallbacks -= callback
    }

    /**
     * Calls all cancel callbacks.
     */
    private callCancelCallbacks() {
        this.cancelCallbacks.each { final Closure<Void> callback ->
            callback.call()
        }
    }

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
        return this.editable?.editorPane
    }

    /**
     * Adds a mouse motion listener.
     *
     * @param listener The listener to add.
     */
    @Override
    synchronized void addMouseMotionListener(@NotNull final MouseMotionListener listener) {
        this.mouseMotionListeners.add(listener)
        this.mouseMotionProviders.each {
            final MouseMotionProvider mmp -> mmp?.addMouseMotionListener(listener)
        }
    }

    /**
     * Removes a mouse motion listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    synchronized void removeMouseMotionListener(@NotNull final MouseMotionListener listener) {
        this.mouseMotionListeners.remove(listener)
        this.mouseMotionProviders.each { final MouseMotionProvider mmp -> mmp?.removeMouseMotionListener(listener) }
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
        this.scrollPane.viewport.viewRect.y
    }

    /**
     * Returns the styler for the editorPane.
     */
    @Override
    @NotNull JTextComponentStyler getStyler() {
        this.editable.styler
    }

    // KeyListener Implementation.

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     */
    @Override
    void keyTyped(final KeyEvent e) {
        this.editable.saved = false
    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key pressed event.
     */
    @Override
    void keyPressed(@NotNull final KeyEvent e) {
        final int keyCode = e.getKeyCode()
        if (keyCode == KeyEvent.VK_ESCAPE) {
            callCancelCallbacks()
        }
        else if (
                keyCode != KeyEvent.VK_META &&
                keyCode != KeyEvent.VK_ALT &&
                keyCode != KeyEvent.VK_CONTROL &&
                keyCode != KeyEvent.VK_SHIFT
        ) {
            this.keyPressedCaretPos = this.editable?.editorPane?.getCaretPosition()

            final KeyboardKey keyboardKey = new KeyboardKey(e)

            this.functions.find { final EditorFunction function ->
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
    void keyReleased(@NotNull final KeyEvent e) {
        final int keyCode = e.getKeyCode()
        if (
                keyCode != KeyEvent.VK_META &&
                        keyCode != KeyEvent.VK_ALT &&
                        keyCode != KeyEvent.VK_CONTROL &&
                        keyCode != KeyEvent.VK_SHIFT
                ) {
            this.filters.each { final EditorInputFilter filter ->
                filter.keyPressed(e)
            }
        }
    }

    /**
     * Handles files being dropped on the editorPane.
     *
     * @param file The file dropped.
     */
    private void dropFile(@NotNull final File file)  {
        try {
            if (!this.editable?.saved) {
                if (this.editable.file == null) {
                    selectNewFile()
                }
                else {
                    this.editable.save()
                }
            }
            else {
                if (this.editorPane.getText().trim().length() > 0) {
                    selectNewFile()
                }
            }
            Editable droppedEditable = new EditableProvider(file, editorStylerFactory)
            Editables.inst.addEditable(droppedEditable)
            setEditable(Editables.inst.getEditable(file))
        }
        catch (final IOException ioe) {
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
    void editorClosed() {
        System.exit(0)
    }

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
        catch (final BadLocationException ignored) {
            i = 0
        }
        // If i was < 0 to start with it will still be that here!
        if (i < 0) {
            i = 0
        }

        new JELine(this.editable.editorPane, i)
    }

    /**
     * Set/replace the entire content of the editorPane.
     *
     * @param content The new content to set.
     */
    @Override
    void setEditorContent(@NotNull final String content) {
        this.editorPane.setText(content)
    }

    /**
     * Inserts new text into the editorPane or replaces current selection.
     *
     * @param text The text to insert.
     */
    @Override
    void insertText(@NotNull final String text) {
        this.editable.styler.disable()
        this.editable.editorPane.replaceSelection(text)
        this.editable.styler.enable()
        this.editable.styler.styleCurrentParagraph()
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
    void moveCaretBack(final int noChars) {
        final Caret caret = this.editorPane.getCaret()
        caret.setDot(caret.getDot() - noChars)
    }

    /**
     * Moves the caret forward.
     *
     * @param noChars The number of characters to move caret.
     */
    @Override
    void moveCaretForward(final int noChars) {
        final Caret caret = this.editorPane.caret
        caret.dot = (caret.dot + noChars)
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
    void setCaretLocation(@NotNull final Point location) {
        this.editorPane.location = location
    }

    /**
     * Returns the caret dot location.
     */
    @Override
    int getCaretDot() {
        this.editorPane.caret.dot
    }

    /**
     * Sets the caret dot location.
     *
     * @param dot The new dot location to set.
     */
    @Override
    void setCaretDot(final int dot) {
        this.editorPane.caret.dot = dot
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
        } catch (final IOException ioe) {
            JOptionPane.showMessageDialog(
                    this, ioe.message, "Failed to open new editorPane!", JOptionPane.ERROR_MESSAGE)
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
    void showOtherComponent(@NotNull final JComponent component) {
        this.scrollPane.setViewportView(component)
    }

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to enable.
     */
    @Override
    void enableToolBarGroup(@NotNull final String groupName) {
        this.toolBar.enableGroup(groupName)
    }

    /**
     * Disables all button in the specified tool bar group.
     *
     * @param groupName The name of the tool bar group to disable.
     */
    @Override
    void disableToolBarGroup(@NotNull final String groupName) {
        this.toolBar.disableGroup(groupName)
    }

    /**
     * Refreshes styling and formatting of the document.
     */
    @Override
    void refreshStyling() {
        final String text = getEditorContent()
        final int caretDot = this.caretDot // Save for restore after text update.
        styler.disable()
        editorContent = text
        this.caretDot = caretDot // Restore of previous caret position.
        styler.enable()
        styler.styleDocument()
    }

    /**
     * Selects the editable to edit in the editor view.
     *
     * @param file The file to edit.
     */
    @Override
    void setEditable(@NotNull final Editable editable) {
        if (this.editable != null) {
            FileDrop.remove(this.editable.editorPane)
            this.editable.editorPane.removeKeyListener(this)
            this.mouseMotionProviders.remove(this.editable)
            this.mouseMotionListeners.each { final MouseMotionListener listener ->
                this.editable.removeMouseMotionListener(listener)
            }
            this.editable.editorPane.removeMouseListener(this)
        }

        this.editable = editable
        this.scrollPane.viewportView = this.editable.editorPane

        this.editable.editorPane.addKeyListener(this)
        this.editable.editorPane.addMouseListener(this)

        final Insets margins = new Insets(
                topMargin.intValue,
                leftMargin.intValue,
                bottomMargin.intValue,
                rightMargin.intValue
        )
        this.editable.editorPane.setMargin(margins)

        //noinspection GroovyResultOfObjectAllocationIgnored
        new FileDrop(this.editable.editorPane, new  FileDrop.Listener() {
            void filesDropped(final File[] files) {
                if (files.length >= 1) {
                    dropFile(files[0])
                }
            }
        })

        this.mouseMotionProviders.add(this.editable)
        this.mouseMotionListeners.each { final MouseMotionListener listener ->
            this.editable.addMouseMotionListener(listener)
        }

        Services.configs.refreshConfigs()

        this.editable.styler.styleDocument()

        this.editable.editorPane.requestFocusEnabled = true
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    @Override
    void mouseClicked(final MouseEvent e) {
        if (e.clickCount == 1) {
            callCancelCallbacks()
        }
    }

    /**
     * Returns the current editable.
     */
    Editable getEditable() {
        this.editable
    }

    /**
     * Saves the current editable.
     *
     * @throws IOException on failure to do so.
     */
    @Override
    void save() throws IOException {
        this.editable.save()
    }

    /**
     * Opens a file chooser for specifying file as a new file.
     *
     * @throws IOException
     */
    @Override
    void selectNewFile() throws IOException {
        doSelectNewFile(this.GUI)
    }

    /**
     * Opens a file chooser for specifying file as a new file.
     *
     * @throws IOException
     */
    static Editable doSelectNewFile(final GUI gui) throws IOException {
        Editable firstOpened = null

        final JFileChooser fileChooser = new JFileChooser()
        fileChooser.approveButtonText = "Create/Open"
        fileChooser.dialogTitle = "Create new or open file(s)"
        final FileNameExtensionFilter filter = new FileNameExtensionFilter("Markdown", "md", "markdown", "mdpart")
        fileChooser.setFileFilter(filter)
        fileChooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        fileChooser.multiSelectionEnabled = true
        // NOTE: According to the Javadoc showDialog(...) can take a null for the first argument. However
        //       when null is passed there is an ArrayIndexOutOfBounds displayed on stderr! It is however not
        //       thrown back here, so things still work. (Java 8, haven't tested with earlier.)
        final int returnVal = fileChooser.showDialog(gui?.windowFrame, "Create/Open")
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFiles.each { final File file ->
                firstOpened = handleSelectedFile(file, firstOpened)
            }
            if (fileChooser.selectedFile != null) {
                firstOpened = handleSelectedFile(fileChooser.selectedFile, firstOpened)
            }
        }

        firstOpened
    }

    private static Editable handleSelectedFile(final File file, Editable firstOpened) {
        if (file.isDirectory()) {
            loadDir(file)
            if (firstOpened == null) {
                firstOpened = Editables.inst.firstEditable
            }
        }
        else {
            if (!file.exists()) {
                file.createNewFile()
            }
            final Editable openedEditable = openFile(file)
            if (firstOpened == null) {
                firstOpened = openedEditable
            }
        }

        firstOpened
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
    static void enableOSXFullscreenIfOnOSX(final Window window) {
        if (window == null) return

        try {
            final Class util = Class.forName("com.apple.eawt.FullScreenUtilities")
            final Class[] params = new Class[2]
            params[0] = Window.class
            params[1] = Boolean.TYPE
            final Method method = util.getMethod("setWindowCanFullScreen", params)
            method.invoke(util, window, true)
        } catch (final ClassNotFoundException ignored) {
            /* Not on Mac OS X! */
        } catch (final Exception e) {
            e.printStackTrace(System.err)
        }

    }

    //
    // Startup
    //

    /**
     * Opens one editorPane window. In this version there should only be one. You can now switch between
     * multiple files within the same editor instance.
     *
     * @throws IOException
     */
    static void setupAndOpenEditor() throws IOException {
        Editable defaultEditable = null

        // BUG: The files have to be loaded (at least one) before the GUI is initialized. This is a bug!
        if (Editables.inst.empty) {
            try {
                defaultEditable = doSelectNewFile(null)
            }
            catch (final IOException ioe) {
                System.err.println("Failed to open file dialog!")
                ioe.printStackTrace(System.err)
                System.exit(1)
            }
            if (Editables.inst.empty) {
                // TODO: A popup here maybe ?
                System.err.println("Since you did not specify a file you have to specify the name of " +
                        "a new file, but since you cancelled the file dialog the editor cannot open!")
                System.exit(1)
            }
        }
        else {
            // Since we have multiple files with only one visible at a time, which one do we start with ?
            // I decided to go with the first entry, but note that Editables actually is a Map, so what
            // is meant by first is a bit loose.
            defaultEditable = Editables.inst.firstEditable
        }

        final MarkdownDocEditor mde = MarkdownDocEditor.instance
        mde.initGUI()

        enableOSXFullscreenIfOnOSX(mde)

        if (defaultEditable != null) {
            // Since we have multiple files with only one visible at a time, which one do we start with ?
            // I decided to go with the first entry, but note that Editables actually is a Map, so what
            // is meant by first is a bit loose.
            mde.editable = defaultEditable
        }
        else {
            System.err.println("BUG: defaultEditable not set in setupAndOpenEditor()!")
            System.exit(1)
        }

        mde.visible = true
        Services.configs.refreshConfigs()
        mde.editorPane.requestFocus()

    }

    /**
     * Simply loads a file and adds it to editables. This will not double load a file!
     * If the specified file is already loaded nothing happens and the previously loaded
     * Editable is returned.
     *
     * @param file The file to open.
     *
     * @return An Editable instance representing the file.
     */
    @NotNull static Editable openFile(@NotNull final File file) {
        Editable editable = Editables.inst.getEditable(file)
        if (editable == null) {
            editable = new EditableProvider(file, this.editorStylerFactory)
            Editables.inst.addEditable(editable)
        }
        editable
    }

    /**
     * Recursively loads all markdown files in directory.
     *
     * @param dir The directory to scan.
     */
    private static void loadDir(@NotNull final File dir) {
        dir.eachFileRecurse(FileType.FILES) { final File file ->
            if (file.name.endsWith(".md") || file.name.endsWith(".markdown") || file.name.endsWith(".mdpart")) {
                openFile(file)
            }
        }
    }

    /**
     * The delayed main handling of all files on the commandline. Will open one editorPane window per file.
     *
     * @param args The passed arguments.
     */
    private static void startup(final String... args) {
        try {
            if (editorStylerFactory == null) {
                throw new RuntimeException("No META-INF/services/se.natusoft.doc.markdowndoc.editorPane.api." +
                        "JTextComponentStylerFactory file pointing out an implementation to use have been provided!")
            }

            if (args.length > 0) {
                args.each { final String arg ->
                    final File argFile = new File(arg)
                    if (argFile.exists()) {
                        if (argFile.directory) {
                            loadDir(argFile)
                        } else {
                            openFile(argFile)
                        }
                    }
                    else {
                        throw new IOException("File: '${argFile}' does not exist!")
                    }
                }
            }

            setupAndOpenEditor()


        } catch (final IOException ioe) {
            System.err.println("Failed to open editorPane: " + ioe.message)
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
                    startup(args)
                }
            }
        )

    }

}
