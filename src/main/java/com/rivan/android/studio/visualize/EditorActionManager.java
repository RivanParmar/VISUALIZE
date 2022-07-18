package com.rivan.android.studio.visualize;

import org.jetbrains.annotations.NotNull;

/**
 * Provides and handles actions for a Visual Editor.
 */
public abstract class EditorActionManager<S extends VisualEditorSurface<?>> {

    protected final S surface;

    protected EditorActionManager(@NotNull S surface) {
        this.surface = surface;
    }
}
