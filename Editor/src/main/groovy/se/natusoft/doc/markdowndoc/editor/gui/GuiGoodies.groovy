package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
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

        final String osName = System.getProperty("os.name").toUpperCase()
    }

    static Rectangle getDefaultScreenBounds() {
        return screenBounds
    }

    void setSafeOpacity(final float _opacity) {
        if (supportsTranslucency) {
            this.window.opacity = _opacity
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
        for (final GraphicsDevice device: gs) {
            GraphicsConfiguration[] configurations = device.configurations
            for (final GraphicsConfiguration config: configurations) {
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

                    return
                }
            }
        }

        // Couldn't move to the point, it may be off screen.
    }

}
