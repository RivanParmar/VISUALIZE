package com.rivan.android.studio.visual.scripting;

import com.android.tools.adtui.workbench.WorkBench;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class VisualEditorPanel extends JPanel implements Disposable {

    private static final String EDITOR_UNAVAILABLE_MESSAGE = "Visual Editor is unavailable until after a successful project sync";

    @NotNull private final Project project;
    @NotNull private final VirtualFile file;
    @NotNull private final WorkBench<VisualEditorSurface<?>> workBench;

    /**
     * Current {@link State} of the panel.
     */
    @NotNull private State state;

    /**
     * Creates a new {@link VisualEditorPanel}.
     *
     * @param project the project associated with the file being edited.
     * @param file the file being edited.
     */
    public VisualEditorPanel(@NotNull Project project, @NotNull VirtualFile file,
                             @NotNull WorkBench<VisualEditorSurface<?>> workBench,
                             @NotNull State defaultEditorPanelState) {
        super(new BorderLayout());
        this.project = project;
        this.file = file;
        this.workBench = workBench;

        workBench.setLoadingText("Loading...");

        state = defaultEditorPanelState;
    }

    @NotNull
    public State getState() {
        return state;
    }

    // Build was either cancelled or there was an error.
    private void buildError() {
        workBench.loadingStopped(EDITOR_UNAVAILABLE_MESSAGE);
    }

    @Override
    public void dispose() {

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
}
