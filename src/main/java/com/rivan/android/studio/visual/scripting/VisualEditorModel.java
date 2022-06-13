package com.rivan.android.studio.visual.scripting;

import com.android.annotations.concurrency.Slow;
import com.android.resources.ResourceUrl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class VisualEditorModel implements Disposable, ModificationTracker {

    @NotNull private final AndroidFacet facet;
    @NotNull private final VirtualFile file;

    /** Model name. This can be used when multiple models are displayed at the same time */
    @Nullable private String modelDisplayName;
    /** Text to display when displaying a tooltip related to this model */
    @Nullable private String modelTooltip;

    @VisibleForTesting
    protected VisualEditorModel(@Nullable Disposable parent,
                                @Nullable String modelDisplayName,
                                @Nullable String modelTooltip,
                                @NotNull AndroidFacet facet,
                                @NotNull VirtualFile file) {
        this.facet = facet;
        this.modelDisplayName = modelDisplayName;
        this.modelTooltip = modelTooltip;
        this.file = file;

        if (parent != null) {
            Disposer.register(parent, this);
        }
    }

    public boolean activate(@NotNull Object source) {
        if (getFacet().isDisposed()) {
            return false;
        }

        boolean wasActive;

        return false;
    }

    public void updateTheme() {

    }

    @Slow
    private void updateTheme(@NotNull ResourceUrl themeUrl, @NotNull Disposable computationToken) {

    }

    private void deactivate() {

    }

    public boolean deactivate(@NotNull Object source) {
        return false;
    }

    @NotNull
    public VirtualFile getFile() {
        return file;
    }

    @NotNull
    public AndroidFacet getFacet() {
        return facet;
    }

    @NotNull
    public Module getModule() {
        return facet.getModule();
    }

    @NotNull
    public Project getProject() {
        return getModule().getProject();
    }

    @Override
    public void dispose() {

    }

    @Override
    public long getModificationCount() {
        return 0;
    }
}
