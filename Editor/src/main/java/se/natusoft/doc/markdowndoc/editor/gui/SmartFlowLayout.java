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
 *         2014-02-01: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.gui;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * A FlowLayout but vertical. Can't believe that there is no such available!!
 */
public class SmartFlowLayout implements LayoutManager2 {
    //
    // Private Members
    //

    /** The components managed by this layout. */
    private List<Component> components = new LinkedList<>();

    /** The calculated minimum size. */
    private Dimension minimumSize = new Dimension(0,0);

    /** The gap to put between each component row. */
    private int vgap = 0;

    /** The gap to put between each component. */
    private int hgap = 0;

    /** If true then all components will have the same width (as the widest component). */
    private boolean commonWidth = true;

    //
    // Constructors
    //

    /**
     * Creates a new VerticalFlowLayout.
     */
    public SmartFlowLayout() {
    }

    /**
     * Creates a new VerticalFlowLayout.
     *
     * @param hgap The gap between each component. Default 0.
     * @param vgap The gap between each component row. Default 0.
     */
    public SmartFlowLayout(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    /**
     * Creates a new VerticalFlowLayout.
     *
     * @param commonWidth If true then all components will have the same width (the width of the widest component). Default true.
     */
    public SmartFlowLayout(boolean commonWidth) {
        this.commonWidth = commonWidth;
    }

    /**
     * Creates a new VerticalFlowLayout.
     *
     * @param hgap The gap between each component. Default 0.
     * @param vgap The gap between each component row. Default 0.
     * @param commonWidth If true then all components will have the same width (the width of the widest component). Default true.
     */
    public SmartFlowLayout(int hgap,int vgap, boolean commonWidth) {
        this.vgap = vgap;
        this.commonWidth = commonWidth;
    }

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
    public void addLayoutComponent(String name, Component comp) {
        this.components.add(comp);
    }

    /**
     * Removes the specified component from the layout.
     *
     * @param comp the component to be removed
     */
    @Override
    public void removeLayoutComponent(Component comp) {
        this.components.remove(comp);
    }

    /**
     * Calculates the preferred size dimensions for the specified
     * container, given the components it contains.
     *
     * @param parent the container to be laid out
     * @see #minimumLayoutSize
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return minimumLayoutSize(parent);
    }

    /**
     * Calculates the minimum size dimensions for the specified
     * container, given the components it contains.
     *
     * @param parent the component to be laid out
     * @see #preferredLayoutSize
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            doLayout(parent, false);
        }
        return this.minimumSize;
    }

    /**
     * Lays out the specified container.
     *
     * @param parent the container to be laid out
     */
    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            doLayout(parent, true);
        }
    }

    /**
     * Does the job of laying out the components.
     *
     * @param parent The parent container we are doing layout for.
     * @param update If true actual layout will be done. If false only minimum size will be calculated.
     */
    private void doLayout(Container parent, boolean update) {
        Insets insets = parent.getInsets();
        int x = insets.left;
        int y = insets.top;

        Container realBoundsContainer = parent;
        if (realBoundsContainer.getParent() != null) {
            realBoundsContainer = realBoundsContainer.getParent();
        }

        int cheight = 0;
        int cwidth = 0;
        for (Component comp : this.components) {
            int compWidth = (int)comp.getPreferredSize().getWidth();
            if (compWidth > cwidth) {
                cwidth = compWidth;
            }
            int compHeight = (int)comp.getPreferredSize().getHeight();
            if (compHeight > cheight) {
                cheight = compHeight;
            }
        }

        for (Component comp : this.components) {
            Dimension compPreferred = comp.getPreferredSize();
            if (this.commonWidth) {
                compPreferred.setSize(cwidth, compPreferred.getHeight());
            }

            if (x + (int)compPreferred.getWidth() <= realBoundsContainer.getWidth()) {
                // Do nothing
            }
            else {
                x = insets.left;
                y = y + cheight + this.vgap;
            }
            if (update) {
                comp.setLocation(x, y);
                comp.setSize(compPreferred);
            }

            x = x + (int)compPreferred.getWidth() + this.hgap;
        }

        int minWidth = x + insets.right;
        int minHeight = y + cheight + insets.bottom;
        this.minimumSize.setSize(minWidth, minHeight);
    }

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     *
     * @param comp        the component to be added
     * @param constraints where/how the component is added to the layout.
     */
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        addLayoutComponent("", comp);
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
    public Dimension maximumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }

    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     *
     * @param target The target container we are doing layout for.
     */
    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0;
    }

    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     *
     * @param target The target container we are doing layout for.
     */
    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     *
     * @param target The target container we are doing layout for.
     */
    @Override
    public void invalidateLayout(Container target) {
        // Nothing to invalidate.
    }
}
