package com.rivan.android.studio.visual.scripting;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VisualEditorModel implements Disposable, ModificationTracker {

    protected VisualEditorModel(@Nullable Disposable parentDisposable,
                                @NotNull AndroidFacet facet,
                                @NotNull VirtualFile file) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public long getModificationCount() {
        return 0;
    }
}
