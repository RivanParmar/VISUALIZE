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
