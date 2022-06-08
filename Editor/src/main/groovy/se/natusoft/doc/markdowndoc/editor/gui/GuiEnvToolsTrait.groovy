/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
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
 *     tommy ()
 *         Changes:
 *         2015-08-03: Created!
 *
 */
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
 * This is a trait with Gui related tools.
 * <p/>
 * About effects: Opacity works on Mac OS X and Windows, occasionally & badly on Linux/KDE.
 * Rounded corner windows is apparently faked on Mac OS X since it fills in the parts that
 * should be 100% transparent with white. The fading effect I'm trying to achieve by looping
 * over changed opacity works very well on Windows, on Mac OS X and Linux/KDE not so much.
 * <p/>
 * So I have to say that the graphical effects in general does work best on Windows, and
 * I'm not, nor have ever been, a Windows fan!
 */
@CompileStatic
trait GuiEnvToolsTrait {

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

    /**
     * This needs to be called before any other method can be called since it provides the window to work with.
     *
     * @param window The window to work with.
     */
    void initGuiEnvTools(final Window window) {
        this.window = window
    }

    Window getManagedWindow() {
        this.window
    }

    static Rectangle getDefaultScreen_Bounds() {
        return screenBounds
    }

    static boolean isFullScreenWindow(final Window w) {
        // Take consideration for inner window sizes which seems to be the case in Windows 10.
        w.height >= (screenBounds.height - 50) && w.width >= (screenBounds.width - 20)
    }

    static Rectangle getDefaultScreen_Bounds(final int topMargin, final int bottomMargin) {
        return new Rectangle(
                screenBounds.x as int,
                (screenBounds.y + topMargin) as int,
                screenBounds.width as int,
                (screenBounds.height - topMargin - bottomMargin) as int
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
        // This looks too bad in most cases, so I'm commenting this out before I finally decide.
//        if (supportsPerPixelTranslucency) {
//            if (this.window instanceof JFrame) {
//                (this.window as JFrame).undecorated = true
//            }
//
//            this.window.addComponentListener(new ComponentSquareShaper(window: this.window))
//        }
    }

    @CompileStatic
    private static class ComponentSquareShaper extends ComponentAdapter {
        Window window

        @Override
        void componentResized(final ComponentEvent e) {
            // Groovy uses BigDecimal for decimal constants! Maybe they want banks to use Groovy :-).
            window.setShape(new RoundRectangle2D.Double(0.0 as double, 0.0 as double, this.window.width as double,
                    this.window.height as double, 10.0 as double, 10.0 as double))
        }
    }

    void fadeInWindow(final float maxOpacity) {
        new Thread(new FadeInRunnable(guiGoodies: this, maxOpacity: maxOpacity)).start()
    }

    @CompileStatic
    private static class FadeInRunnable implements  Runnable {
        GuiEnvToolsTrait guiGoodies
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
    private static class FadeOutRunnable implements Runnable {
        GuiEnvToolsTrait guiGoodies
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
            final GraphicsConfiguration[] configurations = device.configurations
            configurations.each { final GraphicsConfiguration config ->
                final Rectangle bounds = config.bounds
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
