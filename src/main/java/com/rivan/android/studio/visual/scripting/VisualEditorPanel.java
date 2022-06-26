package com.rivan.android.studio.visual.scripting;

import com.android.tools.adtui.common.AdtPrimaryPanel;
import com.android.tools.adtui.workbench.ToolWindowDefinition;
import com.android.tools.adtui.workbench.WorkBench;
import com.android.tools.idea.AndroidPsiUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class VisualEditorPanel extends JPanel implements Disposable {

    private static final String EDITOR_UNAVAILABLE_MESSAGE = "Visual Editor is unavailable until after a successful project sync";

    @NotNull private final Project project;
    @NotNull private final VirtualFile file;
    @NotNull private final VisualEditorSurface<?> surface;
    @NotNull private final MyContentPanel myContentPanel;
    @NotNull private final WorkBench<VisualEditorSurface<?>> workBench;

    /**
     * Which {@link ToolWindowDefinition} should be added to {@link #workBench}.
     */
    @NotNull private final Function<AndroidFacet, List<ToolWindowDefinition<VisualEditorSurface<?>>>> toolWindowDefinitions;

    /**
     * Current {@link State} of the panel.
     */
    @NotNull private State state;

    /**
     * Creates a new {@link VisualEditorPanel}.
     *
     * @param project the project associated with the file being edited.
     * @param file the file being edited.
     * @param toolWindowDefinitions list of tool windows to be added to the workbench.
     * @param defaultEditorPanelState default {@link State} to initialize the panel to.
     */
    public VisualEditorPanel(@NotNull Project project, @NotNull VirtualFile file,
                             @NotNull WorkBench<VisualEditorSurface<?>> workBench,
                             @NotNull Function<VisualEditorPanel, VisualEditorSurface<?>> surface,
                             @NotNull Function<AndroidFacet, List<ToolWindowDefinition<VisualEditorSurface<?>>>> toolWindowDefinitions,
                             @NotNull State defaultEditorPanelState) {
        super(new BorderLayout());
        this.project = project;
        this.file = file;
        this.workBench = workBench;

        myContentPanel = new MyContentPanel();
        this.surface = surface.apply(this);
        Disposer.register(this, this.surface);

        JPanel toolbarAndNotification = new JPanel();
        toolbarAndNotification.setLayout(new BorderLayout());

        workBench.setLoadingText("Loading...");

        state = defaultEditorPanelState;

        onStateChange();

        this.toolWindowDefinitions = toolWindowDefinitions;
    }

    /**
     * Sets the {@link State} of the {@link VisualEditorSurface}.
     */
    public void setState(@NotNull State state) {

        if (this.state != state) {
            this.state = state;
            onStateChange();
        }
    }

    private void onStateChange() {
        State currentState = state;

        // Update the workbench context on state change, so we can have different contexts for each mode.
        workBench.setContext(currentState.name());
        workBench.setDefaultPropertiesForContext(currentState == State.SPLIT);
    }

    @NotNull
    public State getState() {
        return state;
    }

    // Build was either cancelled or there was an error.
    private void buildError() {
        workBench.loadingStopped(EDITOR_UNAVAILABLE_MESSAGE);
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    public void activate() {
        surface.activate();
    }

    public void deactivate() {
        surface.deactivate();
    }

    @NotNull
    public VisualEditorSurface<?> getSurface() {
        return surface;
    }

    @NotNull
    private PsiJavaFile getJavaFile() {
        PsiJavaFile javaFile = (PsiJavaFile) AndroidPsiUtils.getPsiFileSafely(project, file);
        assert javaFile != null;
        return javaFile;
    }

    @Override
    public void dispose() {

    }

    @NotNull
    public WorkBench<VisualEditorSurface<?>> getWorkBench() {
        return workBench;
    }

    /**
     * Represents the {@link VisualEditorPanel} state within the split editor. The state can be changed by the user or
     * automatically based on saved preferences.
     */
    public enum State {
        /** Surface is taking total space of the editor. **/
        FULL,
        /** Surface is sharing the editor horizontal space with a text editor. **/
        SPLIT,
        /** Surface is deactivated and not being displayed. **/
        DEACTIVATED
    }

    private class MyContentPanel extends AdtPrimaryPanel implements DataProvider {

        private MyContentPanel() {
            super(new BorderLayout());
        }

        @Override
        public @Nullable Object getData(@NotNull @NonNls String dataId) {
            return null;
        }
    }
}
