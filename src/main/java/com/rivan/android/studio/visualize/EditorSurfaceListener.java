package com.rivan.android.studio.visualize;

import com.android.annotations.concurrency.UiThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface implemented by listeners for {@link VisualEditorSurface} events
 */
public interface EditorSurfaceListener {

    /** The current model changed */
    @UiThread
    default void modelChanged(@NotNull VisualEditorSurface<?> surface, @Nullable VisualEditorModel model) {}

    /** Change the visibility of related accessory panel */
    default void showAccessoryPanel(@NotNull VisualEditorSurface<?> surface, boolean show) {}
}
