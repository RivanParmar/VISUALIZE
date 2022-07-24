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

import com.android.tools.adtui.stdui.KeyBindingKt;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;

/**
 * Provides and handles actions for a Visual Editor.
 */
public abstract class EditorActionManager<S extends VisualEditorSurface<?>> {

    protected final S surface;

    protected EditorActionManager(@NotNull S surface) {
        this.surface = surface;
    }

    protected static void register(@NotNull AnAction action,
                                   @NonNls String actionId,
                                   @NotNull JComponent component) {
        Arrays.stream(ActionManager.getInstance().getAction(actionId).getShortcutSet().getShortcuts())
                .filter(shortcut -> shortcut instanceof KeyboardShortcut && ((KeyboardShortcut) shortcut).getSecondKeyStroke() == null)
                .forEach(shortcut -> registerAction(action, ((KeyboardShortcut)shortcut).getFirstKeyStroke(), component));
    }

    protected static void registerAction(@NotNull AnAction action,
                                         @NotNull KeyStroke keyStroke,
                                         @NotNull JComponent component) {
        KeyBindingKt.registerAnActionKey(component,
                () -> action, keyStroke,
                action.getClass().getSimpleName(),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /*@NotNull
    public JComponent createToolbar() {
        return new ActionsToolbar();
    }*/

    @NotNull
    public JComponent createVisualEditorSurfaceToolbar() {
        return new EditorSurfaceFloatingActionsToolbarProvider(surface, surface, surface).getFloatingToolbar();
    }

    /**
     * Returns a pre-registered action for the given action name. See {@link IdeActions}
     */
    @Nullable
    protected static AnAction getRegisteredActionByName(@NotNull String actionName) {
        return ActionManager.getInstance().getAction(actionName);
    }

    /**
     * Register keyboard shortcuts onto the provided component.
     *
     * @param component The component onto which shortcut should be registered.
     */
    public abstract void registerActionsShortcuts(@NotNull JComponent component);
}
