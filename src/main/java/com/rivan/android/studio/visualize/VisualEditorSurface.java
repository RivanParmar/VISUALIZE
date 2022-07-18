package com.rivan.android.studio.visualize;

/*
 * Copyright 2022 Rivan Parmar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import com.android.annotations.VisibleForTesting;
import com.android.annotations.concurrency.GuardedBy;
import com.android.annotations.concurrency.UiThread;
import com.android.tools.adtui.Pannable;
import com.android.tools.adtui.Zoomable;
import com.android.tools.adtui.actions.ZoomType;
import com.android.tools.adtui.common.SwingCoordinate;
import com.android.tools.editor.PanZoomListener;
import com.android.tools.idea.common.model.AndroidCoordinate;
import com.android.tools.idea.common.model.ItemTransferable;
import com.android.tools.idea.common.surface.MouseClickDisplayPanel;
import com.android.tools.idea.common.surface.SurfaceScreenScalingFactor;
import com.android.tools.idea.common.surface.layout.MatchParentLayoutManager;
import com.android.tools.idea.ui.designer.EditorDesignSurface;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.Magnificator;
import com.intellij.ui.components.ZoomableViewport;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.function.Function;

public abstract class VisualEditorSurface<T extends SceneManager> extends EditorDesignSurface implements Disposable,
        DataProvider, Zoomable, Pannable, ZoomableViewport {

    /**
     * Determines the visibility of the zoom controls in this surface.
     */
    public enum ZoomControlsPolicy {
        /** The zoom controls will always be visible. */
        VISIBLE,
        /** The zoom controls will never be visible. */
        HIDDEN,
        /** The zoom controls will only be visible when the mouse is over the surface. */
        AUTO_HIDE
    }

    /**
     * If the difference between old and new scaling values is less than threshold, the scaling will be ignored.
     */
    @SurfaceZoomLevel
    protected static final double SCALING_THRESHOLD = 0.005;

    private static final Integer LAYER_PROGRESS = JLayeredPane.POPUP_LAYER + 10;
    private static final Integer LAYER_MOUSE_CLICK = LAYER_PROGRESS + 10;

    private final Project project;

    @SurfaceScale private double scale = 1;
    /**
     * The scale level when magnification started. This is used as a standard when the new scale level is evaluated.
     */
    @SurfaceScale private double magnificationStartedScale;

    /**
     * {@link JScrollPane} contained in this surface when zooming is enabled.
     */
    @Nullable private final JScrollPane scrollPane;
    /**
     * Component that wraps the displayed content. If this is a scrollable surface, that will be the Scroll Pane.
     * Otherwise, it will be the ScreenViewPanel container.
     */
    //@NotNull private final JComponent contentContainerPane;
    @NotNull private final VisualEditorSurfaceViewport viewport;
    @NotNull private final JLayeredPane layeredPane;
    @NotNull private final MouseClickDisplayPanel mouseClickDisplayPanel;

    private final Object listenersLock = new Object();

    @GuardedBy("listenersLock")
    @NotNull private ArrayList<PanZoomListener> zoomListeners = new ArrayList<>();

    //private final SelectionModel selectionModel;

    @SurfaceScale private final double maxFitIntoScale;

    @NotNull
    private final Function<VisualEditorSurface<T>, SurfaceActionHandler> actionHandlerProvider;

    /**
     * See {@link ZoomControlsPolicy}.
     */
    @NotNull
    private final ZoomControlsPolicy zoomControlsPolicy;

    @NotNull
    private final AWTEventListener onHoverListener;

    public VisualEditorSurface(@NotNull Project project, @NotNull Disposable parentDisposable,
                               @NotNull Function<VisualEditorSurface<T>, EditorActionManager<? extends VisualEditorSurface<T>>> actionManagerProvider,
                               @NotNull Function<VisualEditorSurface<T>, SurfaceActionHandler> editorSurfaceActionHandlerProvider,
                               @NotNull ZoomControlsPolicy zoomControlsPolicy) {
        this(project, parentDisposable, actionManagerProvider, editorSurfaceActionHandlerProvider, //new DefaultSelectionModel(),
                zoomControlsPolicy, Double.MAX_VALUE);
    }

    public VisualEditorSurface(@NotNull Project project, @NotNull Disposable parentDisposable,
                               @NotNull Function<VisualEditorSurface<T>, EditorActionManager<? extends VisualEditorSurface<T>>> actionManagerProvider,
                               @NotNull Function<VisualEditorSurface<T>, SurfaceActionHandler> actionHandlerProvider,
                               //@NotNull SelectionModel selectionModel,
                               @NotNull ZoomControlsPolicy zoomControlsPolicy,
                               double maxFitIntoZoomLevel) {
        super(new BorderLayout());

        Disposer.register(parentDisposable, this);
        this.project = project;
        //this.selectionModel = selectionModel;
        this.zoomControlsPolicy = zoomControlsPolicy;

        boolean hasZoomControls = this.zoomControlsPolicy != ZoomControlsPolicy.HIDDEN;

        setOpaque(true);
        setFocusable(false);

        this.actionHandlerProvider = actionHandlerProvider;

        progressPanel = new MyProgressPanel();
        progressPanel.setName("Visual Editor Progress Panel");

        if (hasZoomControls) {
            scrollPane = VisualEditorSurfaceScrollPane.createDefaultScrollPane(this, getBackground(), this::notifyPanningChanged);
        } else {
            scrollPane = null;
        }

        mouseClickDisplayPanel = new MouseClickDisplayPanel(this);

        layeredPane = new JLayeredPane();
        layeredPane.setFocusable(true);
        if (scrollPane != null) {
            layeredPane.setLayout(new MatchParentLayoutManager());
            layeredPane.add(scrollPane, JLayeredPane.POPUP_LAYER);
            //contentContainerPane = scrollPane;
            viewport = new ScrollableEditorSurfaceViewport(scrollPane.getViewport());
        } else {
            layeredPane.setLayout(new OverlayLayout(layeredPane));
            viewport = new NonScrollableDesignSurfaceViewport(this);
        }
        layeredPane.add(progressPanel, LAYER_PROGRESS);
        layeredPane.add(mouseClickDisplayPanel, LAYER_MOUSE_CLICK);

        add(layeredPane);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {

                    repaint();
                }
            }
        });

        if (hasZoomControls) {
            JPanel zoomControlsLayerPane = new JPanel();
            zoomControlsLayerPane.setBorder(JBUI.Borders.empty(UIUtil.getScrollBarWidth()));
            zoomControlsLayerPane.setOpaque(false);
            zoomControlsLayerPane.setLayout(new BorderLayout());
            zoomControlsLayerPane.setFocusable(false);

            onHoverListener = event -> {
                if (event.getID() == MouseEvent.MOUSE_ENTERED || event.getID() == MouseEvent.MOUSE_EXITED) {
                    zoomControlsLayerPane.setVisible(
                            SwingUtilities.isDescendingFrom(((MouseEvent)event).getComponent(), VisualEditorSurface.this)
                    );
                }
            };

            layeredPane.add(zoomControlsLayerPane, JLayeredPane.DRAG_LAYER);

            if (this.zoomControlsPolicy == ZoomControlsPolicy.AUTO_HIDE) {
                zoomControlsLayerPane.setVisible(false);
                Toolkit.getDefaultToolkit().addAWTEventListener(onHoverListener, AWTEvent.MOUSE_EVENT_MASK);
            }
        } else {
            onHoverListener = event -> {};
        }

        // Sets the maximum zoom level allowed for ZoomType#FIT.
        maxFitIntoScale = maxFitIntoZoomLevel / getScreenScalingFactor();
    }

    @NotNull
    protected VisualEditorSurfaceViewport getViewport() {
        return viewport;
    }

    /**
     * When true, the surface will autoscroll when the mouse gets near the edges. See {@link JScrollPane#setAutoscrolls(boolean)}
     */
    protected void setSurfaceAutoScrolls(boolean enabled) {
        if (scrollPane != null) {
            scrollPane.setAutoscrolls(enabled);
        }
    }

    @SurfaceScreenScalingFactor
    @Override
    public double getScreenScalingFactor() {
        return 1d;
    }

    /**
     * When not null, returns a {@link JPanel} to be rendered next to the primary panel of the editor.
   */
    public JPanel getAccessoryPanel() {
        return null;
    }

    @NotNull
    public Project getProject() {
        return project;
    }

    @NotNull
    public Function<VisualEditorSurface<T>, SurfaceActionHandler> getActionHandlerProvider() {
        return actionHandlerProvider;
    }

    /*@NotNull
    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    @NotNull
    public SecondarySelectionModel getSecondarySelectionModel() {
        return (SecondarySelectionModel) selectionModel;
    }*/

    @NotNull
    public abstract ItemTransferable getSelectionAsTransferable();

    /**
     * Gets a copy of {@code zoomListeners} under a lock. Use this method instead of accessing the listeners directly.
     */
    @NotNull
    private ImmutableList<PanZoomListener> getZoomListeners() {
        synchronized (listenersLock) {
            return ImmutableList.copyOf(zoomListeners);
        }
    }

    @Override
    public void dispose() {
        synchronized (listenersLock) {
            zoomListeners.clear();
        }

        Toolkit.getDefaultToolkit().removeAWTEventListener(onHoverListener);
    }

    @UiThread
    public void validateScrollArea() {

    }

    @UiThread
    public void revalidateScrollArea() {

    }

    @Nullable
    @Override
    public Magnificator getMagnificator() {
        if (!getSupportPinchAndZoom()) {
            return null;
        }

        return (scale, at) -> null;
    }

    @Override
    public void magnificationStarted(Point at) {
        magnificationStartedScale = getScale();
    }

    @Override
    public void magnificationFinished(double magnification) {

    }

    @Override
    public void magnify(double magnification) {
        if (Double.compare(magnification, 0) == 0) {
            return;
        }

        Point mouse;
        if(!GraphicsEnvironment.isHeadless()) {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo == null) {
                return;
            }
            mouse = pointerInfo.getLocation();
            SwingUtilities.convertPointFromScreen(mouse, getViewport().getViewportComponent());
        } else {
            // In headless mode we assume the scale point is at the center.
            mouse = new Point(getWidth() / 2, getHeight() / 2);
        }
        double sensitivity = 1d;
        @SurfaceScale double newScale = magnificationStartedScale + magnification * sensitivity;
        setScale(newScale, mouse.x, mouse.y);
    }

    /**
     * Execute a zoom on the content. See {@link ZoomType} for the different type of zoom available.
     *
     * @see #zoom(ZoomType, int, int)
     */
    @UiThread
    @Override
    final public boolean zoom(@NotNull ZoomType type) {
        return zoom(type, -1, -1);
    }

    /**
     * <p>
     * Execute a zoom on the content. See {@link ZoomType} for the different types of zoom available.
     * </p><p>
     * If type is {@link ZoomType#IN}, zoom toward the given
     * coordinates (relative to {@link #getLayeredPane()})
     * <p>
     * If x or y are negative, zoom toward the center of the viewport.
     * </p>
     *
     * @param type Type of zoom to be executed
     * @param x    Coordinate where the zoom will be centered
     * @param y    Coordinate where the zoom will be centered
     * @return True if the scaling was changed, false if this was a noop.
     */
    @UiThread
    public boolean zoom(@NotNull ZoomType type, @SwingCoordinate int x, @SwingCoordinate int y) {

        if (type == ZoomType.IN && (x < 0 || y < 0)) {

        }
        boolean scaled;
        switch (type) {
            case IN: {
                @SurfaceZoomLevel double currentScale = scale * getScreenScalingFactor();
                int current = (int) (Math.round(currentScale * 100));
                @SurfaceScale double scale = (ZoomType.zoomIn(current) / 100.0) / getScreenScalingFactor();
                scaled = setScale(scale, x, y);
                break;
            }
            case OUT: {
                @SurfaceZoomLevel double currentScale = scale * getScreenScalingFactor();
                int current = (int) (currentScale * 100);
                @SurfaceScale double scale = (ZoomType.zoomOut(current) / 100.0) / getScreenScalingFactor();
                scaled = setScale(scale, x, y);
                break;
            }
            case ACTUAL:
                scaled = setScale(1d / getScreenScalingFactor());
                break;
            case FIT:
                scaled = setScale(getFitScale(false));
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented: " + type);
        }

        return scaled;
    }

    /**
     * @see #getFitScale(Dimension, boolean)
     */
    @SurfaceScale
    public double getFitScale(boolean fitInto) {
        int availableWidth = getExtentSize().width;
        int availableHeight = getExtentSize().height;
        return getFitScale(getPreferredContentSize(availableWidth, availableHeight), fitInto);
    }

    /**
     * Measure the scale size which can fit the SceneViews into the scrollable area.
     * This function doesn't consider the legal scale range, which can be get by {@link #getMaxScale()} and {@link #getMinScale()}.
     *
     * @param size    dimension to fit into the view
     * @param fitInto If true, don't scale to more than 100%
     * @return The scale to make the content fit the design surface
     * @see #getScreenScalingFactor()
     */
    @SurfaceScale
    protected double getFitScale(@AndroidCoordinate Dimension size, boolean fitInto) {
        // Fit to zoom
        int availableWidth = getExtentSize().width;
        int availableHeight = getExtentSize().height;
        Dimension padding = getDefaultOffset();
        availableWidth -= padding.width;
        availableHeight -= padding.height;

        @SurfaceScale double scaleX = size.width == 0 ? 1 : (double) availableWidth / size.width;
        @SurfaceScale double scaleY = size.height == 0 ? 1 : (double) availableHeight / size.height;
        @SurfaceScale double scale = Math.min(scaleX, scaleY);
        if (fitInto) {
            @SurfaceScale double min = 1d / getScreenScalingFactor();
            scale = Math.min(scale, min);
        }
        scale = Math.min(scale, maxFitIntoScale);
        return scale;
    }

    @SwingCoordinate
    protected abstract Dimension getDefaultOffset();

    @SwingCoordinate
    @NotNull
    protected abstract Dimension getPreferredContentSize(int availableWidth, int availableHeight);

    @UiThread
    final public boolean zoomToFit() {
        return zoom(ZoomType.FIT, -1, -1);
    }

    @Override
    @SurfaceScale
    public double getScale() {
        return scale;
    }

    @Override
    public boolean isPannable() {
        return true;
    }

    @Override
    public boolean canZoomIn() {
        return getScale() < getMaxScale();
    }

    @Override
    public boolean canZoomOut() {
        return getScale() > getMinScale();
    }

    @Override
    public abstract boolean canZoomToFit();

    @Override
    public boolean canZoomToActual() {
        double currentScale = getScale();
        return (currentScale > 1 && canZoomOut()) || (currentScale < 1 && canZoomIn());
    }

    /**
     * Returns the size of the surface scroll viewport.
     */
    @NotNull
    @SwingCoordinate
    public Dimension getExtentSize() {
        return getViewport().getExtentSize();
    }

    /**
     * Set the scale factor used to multiply the content size.
     *
     * @param scale The scale factor. Can be any value but it will be capped between -1 and 10
     *              (value below 0 means zoom to fit)
     * @return True if the scaling was changed, false if this was a noop.
     */
    public boolean setScale(double scale) {
        return setScale(scale, -1, -1);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PROTECTED)
    public boolean setScale(@SurfaceScale double scale, @SwingCoordinate int x, @SwingCoordinate int y) {
        @SurfaceScale final double newScale = Math.min(Math.max(scale, getMinScale()), getMaxScale());
        if (Math.abs(newScale - scale) < SCALING_THRESHOLD / getScreenScalingFactor()) {
            return false;
        }

        double previousScale = scale;
        scale = newScale;

        revalidateScrollArea();
        notifyScaleChanged(previousScale, scale);
        return true;
    }

    protected boolean isKeepingScaleWhenReopen() {
        return true;
    }

    /**
     * Save the current zoom level from the file of the given {@link VisualEditorModel}.
     */
    private void storeCurrentScale(@NotNull VisualEditorModel model) {
        if (!isKeepingScaleWhenReopen()) {
            return;
        }
        // TODO: Create a custom class for saving the zoom level
    }

    /**
     * Load the saved zoom level from the file of the given {@link VisualEditorModel}.
     * Return true if the previous zoom level is restored, false otherwise.
     */
    private boolean restoreScale(@NotNull VisualEditorModel model) {
        if (!isKeepingScaleWhenReopen()) {
            return false;
        }
        // TODO: Restore saved zoom level with the help of custom class
        return false;
    }

    public void setScrollPosition(@SwingCoordinate int x, @SwingCoordinate int y) {
        setScrollPosition(new Point(x, y));
    }

    /**
     * Sets the offset for the scroll viewer to the specified x and y values
     * The offset will never be less than zero, and never greater than the
     * maximum value allowed by the sizes of the underlying view and the extent.
     * If the zoom factor is large enough that the scroll bars aren't visible,
     * the position will be set to zero.
     */
    @Override
    public void setScrollPosition(@SwingCoordinate Point p) {
        p.setLocation(Math.max(0, p.x), Math.max(0, p.y));

        Dimension extent = getExtentSize();
        Dimension view = getViewSize();

        int minX = Math.min(p.x, view.width - extent.width);
        int minY = Math.min(p.y, view.height - extent.height);

        p.setLocation(minX, minY);

        getViewport().setViewPosition(p);
    }

    /**
     * Returns the size of the surface containing the ScreenViews.
     */
    @NotNull
    @SwingCoordinate
    public Dimension getViewSize() {
        return getViewport().getViewSize();
    }

    /**
     * The minimum scale we'll allow.
     */
    @SurfaceScale
    protected double getMinScale() {
        return 0;
    }

    /**
     * The maximum scale we'll allow.
     */
    @SurfaceScale
    protected double getMaxScale() {
        return 1;
    }

    private void notifyScaleChanged(double previousScale, double newScale) {
        for (PanZoomListener myZoomListener : getZoomListeners()) {
            myZoomListener.zoomChanged(previousScale, newScale);
        }
    }

    private void notifyPanningChanged(AdjustmentEvent adjustmentEvent) {
        for (PanZoomListener myZoomListener : getZoomListeners()) {
            myZoomListener.panningChanged(adjustmentEvent);
        }
    }

    protected boolean getSupportPinchAndZoom() {
        return true;
    }

    @NotNull
    public JComponent getLayeredPane() {
        return layeredPane;
    }

    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private final MyProgressPanel progressPanel;

    public void addPanZoomListener(@NotNull PanZoomListener listener) {
        synchronized (listenersLock) {
            zoomListeners.remove(listener);
            zoomListeners.add(listener);
        }
    }

    public void removePanZoomListener(@NotNull PanZoomListener listener) {
        synchronized (listenersLock) {
            zoomListeners.remove(listener);
        }
    }

    public void activate() {
        if (Disposer.isDisposed(this)) {
            return;
        }
    }

    public void deactivate() {

    }

    protected boolean useSmallProgressIcon() {
        return true;
    }

    /**
     * Panel which displays the progress icon. The progress icon can either be a large icon in the
     * center, when there is no rendering showing, or a small icon in the upper right corner when there
     * is a rendering. This is necessary because even though the progress icon looks good on some
     * renderings, depending on the layout theme colors it is invisible in other cases.
     */
    private class MyProgressPanel extends JPanel {
        private AsyncProcessIcon smallProgressIcon;
        private AsyncProcessIcon largeProgressIcon;
        private boolean small;
        private boolean progressVisible;

        public MyProgressPanel() {
            super(new BorderLayout());
            setOpaque(false);
            setVisible(false);
        }

        /**
         * The "small" icon mode isn't just for the icon size; it's for the layout position too; see {@link #doLayout()}
         */
        private void setSmallIcon(boolean small) {
            if (small != this.small) {
                if (progressVisible && getComponentCount() != 0) {
                    AsyncProcessIcon oldIcon = getProgressIcon();
                    oldIcon.suspend();
                }
                this.small = true;
                removeAll();
                AsyncProcessIcon icon = getProgressIcon();
                add(icon, BorderLayout.CENTER);
                if (progressVisible) {
                    icon.setVisible(true);
                    icon.resume();
                }
            }
        }

        public void showProgressIcon() {
            if (!progressVisible) {
                progressVisible = true;
                setVisible(true);
                AsyncProcessIcon icon = getProgressIcon();
                if (getComponentCount() == 0) {
                    add(getProgressIcon(), BorderLayout.CENTER);
                } else {
                    icon.setVisible(true);
                }
                icon.resume();
            }
        }

        public void hideProgressIcon() {
            if (progressVisible) {
                progressVisible = false;
                setVisible(false);
                AsyncProcessIcon icon = getProgressIcon();
                icon.setVisible(false);
                icon.suspend();
            }
        }

        @Override
        public void doLayout() {
            super.doLayout();
            setBackground(JBColor.RED);

            if (!progressVisible) {
                return;
            }

            AsyncProcessIcon icon = getProgressIcon();
            Dimension size = icon.getPreferredSize();
            if (small) {
                icon.setBounds(getWidth() - size.width - 1, 1, size.width, size.height);
            } else {
                icon.setBounds(getWidth() / 2 - size.width / 2, getHeight() / 2 - size.height / 2, size.width, size.height);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return getProgressIcon().getPreferredSize();
        }

        @NotNull
        private AsyncProcessIcon getProgressIcon() {
            return getProgressIcon(small);
        }

        @NotNull
        private AsyncProcessIcon getProgressIcon(boolean small) {
            if (small) {
                if (smallProgressIcon == null) {
                    smallProgressIcon = new AsyncProcessIcon("Visual editor loading");
                    Disposer.register(VisualEditorSurface.this, smallProgressIcon);
                }
                return smallProgressIcon;
            } else {
                if (largeProgressIcon == null) {
                    largeProgressIcon = new AsyncProcessIcon.Big("Visual editor loading");
                    Disposer.register(VisualEditorSurface.this, largeProgressIcon);
                }
                return largeProgressIcon;
            }
        }
    }
}
