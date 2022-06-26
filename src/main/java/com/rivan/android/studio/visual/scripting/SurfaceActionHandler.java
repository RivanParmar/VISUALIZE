package com.rivan.android.studio.visual.scripting;

import com.intellij.ide.CopyProvider;
import com.intellij.ide.CutProvider;
import com.intellij.ide.DeleteProvider;
import com.intellij.ide.PasteProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

public class SurfaceActionHandler implements DeleteProvider, CutProvider, CopyProvider, PasteProvider {

    protected final VisualEditorSurface<?> surface;
    private CopyPasteManager copyPasteManager;

    public SurfaceActionHandler(@NotNull VisualEditorSurface<?> surface) {
        this(surface, CopyPasteManager.getInstance());
    }

    protected SurfaceActionHandler(@NotNull VisualEditorSurface<?> surface, @NotNull CopyPasteManager copyPasteManager) {
        this.surface = surface;
        this.copyPasteManager = copyPasteManager;
    }

    @Override
    public void performCopy(@NotNull DataContext dataContext) {
        if (!surface.getSelectionModel().isEmpty()) {
            copyPasteManager.setContents(surface.getSelectionAsTransferable());
        }
    }

    @Override
    public boolean isCopyEnabled(@NotNull DataContext dataContext) {
        return hasNonEmptySelection();
    }

    @Override
    public boolean isCopyVisible(@NotNull DataContext dataContext) {
        return true;
    }

    @Override
    public void performCut(@NotNull DataContext dataContext) {
        if (!surface.getSelectionModel().isEmpty()) {

        }
    }

    @Override
    public boolean isCutEnabled(@NotNull DataContext dataContext) {
        return hasNonEmptySelection();
    }

    @Override
    public boolean isCutVisible(@NotNull DataContext dataContext) {
        return true;
    }

    @Override
    public void deleteElement(@NotNull DataContext dataContext) {

    }

    @Override
    public boolean canDeleteElement(@NotNull DataContext dataContext) {
        return hasNonEmptySelection();
    }

    @Override
    public void performPaste(@NotNull DataContext dataContext) {

    }

    @Override
    public boolean isPastePossible(@NotNull DataContext dataContext) {
        return false;
    }

    @Override
    public boolean isPasteEnabled(@NotNull DataContext dataContext) {
        return false;
    }

    private boolean hasNonEmptySelection() {
        return !surface.getSelectionModel().isEmpty();
    }
}
