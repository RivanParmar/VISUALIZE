package com.rivan.android.studio.visual.scripting;

import com.android.tools.adtui.Pannable;
import com.android.tools.adtui.Zoomable;
import com.android.tools.idea.common.surface.SurfaceScreenScalingFactor;
import com.android.tools.idea.ui.designer.EditorDesignSurface;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.ZoomableViewport;
import com.intellij.util.ui.AsyncProcessIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public abstract class VisualEditorSurface<T extends SceneManager> extends EditorDesignSurface implements Disposable,
        DataProvider, Zoomable, Pannable, ZoomableViewport {

    private final Project project;

    public VisualEditorSurface(@NotNull Project project, @NotNull Disposable parentDisposable) {
        super(new BorderLayout());

        Disposer.register(parentDisposable, this);
        this.project = project;

        setOpaque(true);
        setFocusable(false);
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

    @Override
    public boolean isPannable() {
        return true;
    }

    @Override
    public abstract boolean canZoomToFit();

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
         * The "small" icon mode isn't just for the icon size; it's for the layout position too; see {@link #doLayout}
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
