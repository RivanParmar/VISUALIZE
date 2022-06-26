package com.rivan.android.studio.visual.scripting;

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

import com.android.annotations.concurrency.GuardedBy;
import com.android.annotations.concurrency.UiThread;
import com.android.tools.adtui.Pannable;
import com.android.tools.adtui.Zoomable;
import com.android.tools.adtui.common.SwingCoordinate;
import com.android.tools.editor.PanZoomListener;
import com.android.tools.idea.common.model.DefaultSelectionModel;
import com.android.tools.idea.common.model.ItemTransferable;
import com.android.tools.idea.common.model.SecondarySelectionModel;
import com.android.tools.idea.common.model.SelectionModel;
import com.android.tools.idea.common.surface.MouseClickDisplayPanel;
import com.android.tools.idea.common.surface.SurfaceScale;
import com.android.tools.idea.common.surface.SurfaceScreenScalingFactor;
import com.android.tools.idea.common.surface.layout.MatchParentLayoutManager;
import com.android.tools.idea.ui.designer.EditorDesignSurface;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
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

    private static final Integer LAYER_PROGRESS = JLayeredPane.POPUP_LAYER + 10;
    private static final Integer LAYER_MOUSE_CLICK = LAYER_PROGRESS + 10;

    private final Project project;

    @NotNull private final JLayeredPane layeredPane;
    @NotNull private final MouseClickDisplayPanel mouseClickDisplayPanel;

    private final Object listenersLock = new Object();

    @GuardedBy("listenersLock")
    @NotNull private ArrayList<PanZoomListener> zoomListeners = new ArrayList<>();

    private final SelectionModel selectionModel;

    /**
     * {@link JScrollPane} contained in this surface when zooming is enabled.
     */
    @Nullable
    private final JScrollPane scrollPane;

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
                               @NotNull Function<VisualEditorSurface<T>, SurfaceActionHandler> editorSurfaceActionHandlerProvider,
                               @NotNull ZoomControlsPolicy zoomControlsPolicy) {
        this(project, parentDisposable, editorSurfaceActionHandlerProvider, new DefaultSelectionModel(), zoomControlsPolicy, Double.MAX_VALUE);
    }

    public VisualEditorSurface(@NotNull Project project, @NotNull Disposable parentDisposable,
                               @NotNull Function<VisualEditorSurface<T>, SurfaceActionHandler> actionHandlerProvider,
                               @NotNull SelectionModel selectionModel,
                               @NotNull ZoomControlsPolicy zoomControlsPolicy,
                               double maxFitIntoZoomLevel) {
        super(new BorderLayout());

        Disposer.register(parentDisposable, this);
        this.project = project;
        this.selectionModel = selectionModel;
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
        } else {
            layeredPane.setLayout(new OverlayLayout(layeredPane));
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

    @NotNull
    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    @NotNull
    public SecondarySelectionModel getSecondarySelectionModel() {
        return selectionModel;
    }

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

    @SwingCoordinate
    protected abstract Dimension getDefaultOffset();

    @SwingCoordinate
    @NotNull
    protected abstract Dimension getPreferredContentSize(int availableWidth, int availableHeight);

    @Override
    public boolean isPannable() {
        return true;
    }

    @Override
    public abstract boolean canZoomToFit();

    @Override
    public boolean canZoomToActual() {
        return false;
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

    private void notifyPanningChanged(AdjustmentEvent adjustmentEvent) {
        for (PanZoomListener myZoomListener : getZoomListeners()) {
            myZoomListener.panningChanged(adjustmentEvent);
        }
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
