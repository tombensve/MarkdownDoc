package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.TypeChecked

import javax.swing.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.RoundRectangle2D

/**
 * This is a trait with Gui related goodies.
 */
@CompileStatic
@TypeChecked
trait GuiGoodies {

    //
    // Constants
    //

    static final float STANDARD_OPACITY = 0.75f

    //
    // Private Members
    //

    private Window window

    private static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    private static GraphicsDevice gd = ge.getDefaultScreenDevice()
    private static Rectangle screenBounds = gd.defaultConfiguration.bounds
    private static supportsTranslucency =
            gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)
    private static supportsPerPixelTranslucency =
            gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)

    //
    // Methods
    //

    void initGuiGoodies(final Window window) {
        this.window = window
    }

    @PackageScope Window getManagedWindow() {
        this.window
    }

    static Rectangle getDefaultScreen_Bounds() {
        return screenBounds
    }

    static Rectangle getDefaultScreen_Bounds(final int topMargin, final int bottomMargin) {
        return new Rectangle(
                screenBounds.x as int,
                (screenBounds.y + topMargin) as int,
                screenBounds.width as int,
                (screenBounds.height - bottomMargin) as int
        )
    }

    void setSafeOpacity(final float _opacity) {
        if (supportsTranslucency) {
            if (_opacity >= 0.0f && _opacity <= 1.0f && this.window != null) {
                this.window.opacity = _opacity
            }
        }
    }

    void safeMakeRoundedRectangleShape() {
        if (supportsPerPixelTranslucency) {
            if (this.window instanceof JFrame) {
                (this.window as JFrame).undecorated = true
            }

            // This fills out the outside of the shape that is not part of a square with white, so it
            // is not a true shaped window.
            this.window.addComponentListener(new ComponentSquareShaper(window: this.window))
        }
    }

    private static class ComponentSquareShaper extends ComponentAdapter {
        Window window

        @Override
        public void componentResized(final ComponentEvent e) {
            window.setShape(new RoundRectangle2D.Double(0.0, 0.0, this.window.width as double,
                    this.window.height as double, 10.0, 10.0))
        }
    }

    void fadeInWindow(final float maxOpacity) {
        new Thread(new FadeInRunnable(guiGoodies: this, maxOpacity: maxOpacity)).start()
    }

    @CompileStatic
    @TypeChecked
    private static class FadeInRunnable implements  Runnable {
        GuiGoodies guiGoodies
        float maxOpacity

        @Override
        void run() {
            Thread.sleep(500)
            for (float op = 0.0f; op <= maxOpacity; op = op + 0.02f) {
                guiGoodies.safeOpacity = op
                Thread.sleep(5)
            }
        }
    }

    void fadeOutWindow(final Closure<Void> closeWindow) {
        new Thread(new FadeOutRunnable(guiGoodies: this, closeWindow: closeWindow)).start()
    }

    @CompileStatic
    @TypeChecked
    private static class FadeOutRunnable implements Runnable {
        GuiGoodies guiGoodies
        Closure<Void> closeWindow = null

        @Override
        void run() {
            for (float op = this.guiGoodies.getManagedWindow().getOpacity(); op >= 0.0f; op = op - 0.02f) {
                guiGoodies.safeOpacity = op
                Thread.sleep(5)
            }
            closeWindow?.call()
        }
    }

    /**
     * From: http://stackoverflow.com/questions/2941324/how-do-i-set-the-position-of-the-mouse-in-java
     * by Daniel.
     * <p/>
     * Groovyfied by me.
     *
     * @param p The point to move the mouse to.
     */
    static void moveMouse(final Point p) {
        final GraphicsEnvironment ge = GraphicsEnvironment.localGraphicsEnvironment
        final GraphicsDevice[] gs = ge.screenDevices

        // Search the devices for the one that draws the specified point.
        gs.each { final GraphicsDevice device ->
            GraphicsConfiguration[] configurations = device.configurations
            configurations.each { final GraphicsConfiguration config ->
                Rectangle bounds = config.bounds
                if(bounds.contains(p)) {
                    // Set point to screen coordinates.
                    final Point b = bounds.location
                    final Point s = new Point((p.x - b.x) as int, (p.y - b.y) as int)

                    try {
                        final Robot r = new Robot(device)
                        r.mouseMove(s.x as int, s.y as int)
                    } catch (final AWTException e) {
                        e.printStackTrace()
                    }

                    // Me:
                    // IDEA claims this return is "unnecessary", but that is obviously not true
                    // if you look where it is!! It can't apparently figure out that .each {...}
                    // is a loop.
                    //noinspection GroovyUnnecessaryReturn
                    return
                }
            }
        }

        // Couldn't move to the point, it may be off screen.
    }

}
