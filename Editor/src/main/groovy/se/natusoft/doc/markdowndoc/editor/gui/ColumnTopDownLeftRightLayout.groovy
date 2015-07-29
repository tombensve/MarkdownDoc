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
 *         2014-02-01: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.awt.*
import java.util.List

/**
 * Creates a layout that adds components from top to bottom and when the bottom is reached it moves to
 * the top and then right and continues down again.
 */
@CompileStatic
@TypeChecked
class ColumnTopDownLeftRightLayout implements LayoutManager2 {

    //
    // Private Members
    //

    /** The calculated minimum size. */
    private Dimension minimumSize = new Dimension(0,0)

    /** The components managed by this layout. */
    private List<Component> components = new LinkedList<>()

    /** Extra vertical margin for when small content compared to window size. */
    private int extraVMargin = 0

    /** Extra horizontal margin for when small content compared to window size. */
    private int extraHMargin = 0

    /** Extra vertical gap for when small content compared to window size. */
    private int extraVGap = 0

    /** Extra horizontal gap for when small content compared to window size. */
    private int extraHGap = 0

    //
    // Properties
    //

    /** The size of the screen. This must be provided or there will be an NPE! */
    @NotNull Rectangle screenSize

    /**
     * This is the calculated optimal size of a  window that should hold this content. Note that
     * it will never grow larger than the provided screen size!
     * <p/>
     * Since this is a property, you can change its initial value to something larger if wanted.
     * This size will never be shrunk, only grown if needed to fit content.
     */
    Dimension optimalSize = null

    /** The vertical margin to use at edges. */
    int vmargin = 0

    /** The horizontal margin to use at edges. */
    int hmargin = 0

    /** The gap to put between each component row. */
    int vgap = 0

    /** The gap to put between each component. */
    int hgap = 0

    /** If true then the content is spread out over the window area. */
    boolean spreadOut = false

    //
    // Methods
    //

    /**
     * If the layout manager uses a per-component string,
     * adds the component <code>comp</code> to the layout,
     * associating it
     * with the string specified by <code>name</code>.
     *
     * @param name the string to be associated with the component
     * @param comp the component to be added
     */
    @Override
    void addLayoutComponent(@Nullable("Not used") String name, @NotNull Component comp) {
        this.components.add(comp)
    }

    /**
     * Removes the specified component from the layout.
     *
     * @param comp the component to be removed
     */
    @Override
    void removeLayoutComponent(@NotNull Component comp) {
        this.components.remove(comp)
    }

    /**
     * Calculates the preferred size dimensions for the specified
     * container, given the components it contains.
     *
     * @param parent the container to be laid out
     * @see #minimumLayoutSize
     */
    @Override
    @NotNull Dimension preferredLayoutSize(Container parent) {
        minimumLayoutSize(parent)
    }

    /**
     * Calculates the minimum size dimensions for the specified
     * container, given the components it contains.
     *
     * @param parent the component to be laid out
     * @see #preferredLayoutSize
     */
    @Override
    @NotNull Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            doLayout(parent, false)
        }
        this.minimumSize
    }

    /**
     * Lays out the specified container.
     *
     * @param parent the container to be laid out
     */
    @Override
    void layoutContainer(@NotNull Container parent) {
        synchronized (parent.getTreeLock()) {
            doLayout(parent, true)
        }
    }

    /**
     * Does the job of laying out the components.
     *
     * @param parent The parent container we are doing layout for.
     * @param update If true actual layout will be done. If false only minimum size will be calculated.
     */
    private void doLayout(@NotNull Container parent, boolean update) {
        if (this.optimalSize == null) {
            this.optimalSize = new Dimension(400, this.screenSize.height + 1 as int)
        }
        Insets insets = parent.insets
        int x = insets.left + this.hmargin + this.extraHMargin
        int y = 25 + insets.top + this.vmargin + this.extraVMargin
        int highestY = 0

        int commonWidth = 0
        for (Component comp : this.components) {
            int compWidth = (int)comp.preferredSize.width
            if (compWidth > commonWidth) {
                commonWidth = compWidth
            }
        }

        for (Component comp : components) {

            Dimension compPreferred = comp.preferredSize

            compPreferred.setSize(commonWidth, compPreferred.height)

            if (y + 30 + (compPreferred.height as int) + this.vgap + this.extraVGap > optimalSize.height) {
                x = x + commonWidth + this.hgap + this.extraHGap
                y = insets.top + this.vmargin + this.extraVMargin
            }

            if (update) {
                comp.setLocation(x, y)
                comp.setSize(commonWidth, compPreferred.height as int)
            }

            y = y + (compPreferred.height as int) + this.vgap + this.extraVGap
            if (y > highestY) { highestY = y }
        }

        int minWidth = x + insets.right
        int minHeight = insets.top + highestY + insets.bottom
        this.minimumSize.setSize(minWidth, minHeight)

        if ((x + commonWidth + this.hgap + this.extraHGap) > (this.optimalSize.width as int) &&
                (x + commonWidth + this.hgap + this.extraHGap) < this.screenSize.width) {
            this.optimalSize = new Dimension((this.optimalSize.width + 40) as int,
                    this.optimalSize.height as int)
            doLayout(parent, update)
        }
        else if (this.spreadOut && optimalSize.width - (x + commonWidth + this.hgap + this.extraHGap) > 50) {
            this.extraHMargin += 20
            this.extraVMargin += 10
            this.extraHGap += 10
            this.extraVGap += 4
            doLayout(parent, update)
        }
    }

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     *
     * @param comp        the component to be added
     * @param constraints where/how the component is added to the layout.
     */
    @Override
    void addLayoutComponent(@NotNull Component comp, @Nullable("Not used") Object constraints) {

        addLayoutComponent("", comp)
    }

    /**
     * Calculates the maximum size dimensions for the specified container,
     * given the components it contains.
     *
     * @param target The target container we are doing layout for.
     * @see java.awt.Component#getMaximumSize
     * @see java.awt.LayoutManager
     */
    @Override
    Dimension maximumLayoutSize(@NotNull Container target) {
        preferredLayoutSize(target)
    }

    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The valueComp should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     *
     * @param target The target container we are doing layout for.
     */
    @Override
    float getLayoutAlignmentX(@Nullable("Not used") Container target) {
        0.0f
    }

    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The valueComp should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     *
     * @param target The target container we are doing layout for.
     */
    @Override
    float getLayoutAlignmentY(@Nullable("Not used") Container target) {
        0.0f
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     *
     * @param target The target container we are doing layout for.
     */
    @Override
    void invalidateLayout(@Nullable("Not used") Container target) {
        // Nothing to invalidate.
    }
}
