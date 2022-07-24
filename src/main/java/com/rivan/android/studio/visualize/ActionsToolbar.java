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

package com.rivan.android.studio.visualize;

import com.android.tools.adtui.common.AdtPrimaryPanel;
import com.android.tools.adtui.common.StudioColorsKt;
import com.android.tools.editor.PanZoomListener;
import com.android.tools.idea.configurations.ConfigurationListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;

/**
 * The actions toolbar updates dynamically based on the component selection, their
 * parents (and if no selection, the root layout)
 */
public final class ActionsToolbar implements EditorSurfaceListener, Disposable, PanZoomListener, ConfigurationListener {

    private final VisualEditorSurface<?> surface;
    private final JComponent toolbarComponent;

    private VisualEditorModel model = null;

    public ActionsToolbar(@NotNull Disposable parent, @NotNull VisualEditorSurface<?> surface) {
        Disposer.register(parent, this);
        this.surface = surface;
        this.surface.addListener(this);
        this.surface.addPanZoomListener(this);

        toolbarComponent = createToolbarComponent();
    }

    @Override
    public void dispose() {
        surface.removePanZoomListener(this);
        surface.removeListener(this);

        if (model != null) {
            //model.removeListener(this);
            model = null;
        }
    }

    @NotNull
    public JComponent getToolbarComponent() {
        return toolbarComponent;
    }

    @NotNull
    private static JComponent createToolbarComponent() {
        JComponent panel = new AdtPrimaryPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, StudioColorsKt.getBorder()));
        return panel;
    }

    @Override
    public void zoomChanged(double previousScale, double newScale) {

    }

    @Override
    public void panningChanged(AdjustmentEvent adjustmentEvent) {

    }

    @Override
    public boolean changed(int flags) {
        return false;
    }
}
