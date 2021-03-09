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
 * Creates a flow layout where each content box is the same size, that is the size of the largest
 * component added.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
class SmartFlowLayout implements LayoutManager2 {
    //
    // Private Members
    //

    /** The components managed by this layout. */
    private List<Component> components = new LinkedList<>()

    /** The calculated minimum size. */
    private Dimension minimumSize = new Dimension(0,0)

    //
    // Properties
    //

    /** The gap to put between each component row. */
    int vgap = 0

    /** The gap to put between each component. */
    int hgap = 0

    /** If true then all components will have the same width (as the widest component). */
    boolean commonWidth = true

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
    void addLayoutComponent(@Nullable("Not used") final String name, @NotNull final Component comp) {
        this.components.add(comp)
    }

    /**
     * Removes the specified component from the layout.
     *
     * @param comp the component to be removed
     */
    @Override
    void removeLayoutComponent(@NotNull final Component comp) {
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
    @NotNull Dimension preferredLayoutSize(final Container parent) {
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
    @NotNull Dimension minimumLayoutSize(final Container parent) {
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
    void layoutContainer(@NotNull final Container parent) {
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
    private void doLayout(@NotNull final Container parent, final boolean update) {
        final Insets insets = parent.getInsets()
        int x = insets.left
        int y = insets.top

        Container realBoundsContainer = parent
        if (realBoundsContainer.getParent() != null) {
            realBoundsContainer = realBoundsContainer.getParent()
        }

        int cheight = 0
        int cwidth = 0
        for (final Component comp : this.components) {
            int compWidth = (int)comp.getPreferredSize().getWidth()
            if (compWidth > cwidth) {
                cwidth = compWidth
            }
            int compHeight = (int)comp.getPreferredSize().getHeight()
            if (compHeight > cheight) {
                cheight = compHeight
            }
        }

        final int noCols = (int)(realBoundsContainer.getWidth() / cwidth)
        final int leftOfWidth = (int)(realBoundsContainer.getWidth() - (cwidth * noCols))
        if (noCols > 1) {
            cwidth = cwidth + (int)(leftOfWidth / noCols)
        }
        else if (noCols == 1) {
            cwidth = realBoundsContainer.getWidth() - 2
        }

        for (final Component comp : this.components) {
            Dimension compPreferred = comp.getPreferredSize()
            if (this.commonWidth) {
                compPreferred.setSize(cwidth, compPreferred.getHeight())
            }

            if ((x + cwidth) <= realBoundsContainer.getWidth()) {
                // Do nothing
            }
            else {
                x = insets.left
                y = y + cheight + this.vgap
            }
            if (update) {
                comp.setLocation(x, y)
                comp.setSize(cwidth, cheight/*(int)compPreferred.getHeight()*/)
            }

            x = x + cwidth + this.hgap
        }

        final int minWidth = x + insets.right
        final int minHeight = y + cheight + insets.bottom
        this.minimumSize.setSize(minWidth, minHeight)
    }

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     *
     * @param comp        the component to be added
     * @param constraints where/how the component is added to the layout.
     */
    @Override
    void addLayoutComponent(@NotNull final Component comp, @Nullable("Not used") final Object constraints) {

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
    Dimension maximumLayoutSize(@NotNull final Container target) {
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
    float getLayoutAlignmentX(@Nullable("Not used") final Container target) {
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
    float getLayoutAlignmentY(@Nullable("Not used") final Container target) {
        0.0f
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     *
     * @param target The target container we are doing layout for.
     */
    @Override
    void invalidateLayout(@Nullable("Not used") final Container target) {
        // Nothing to invalidate.
    }
}
