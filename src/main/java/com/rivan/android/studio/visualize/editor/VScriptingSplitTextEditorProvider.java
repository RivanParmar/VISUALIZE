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

package com.rivan.android.studio.visualize.editor;

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 *  Abstract class to create a Split Text Editor.
 */

public abstract class VScriptingSplitTextEditorProvider implements AsyncFileEditorProvider, DumbAware {

    @NotNull
    protected final FileEditorProvider myFirstProvider;
    @NotNull
    protected final FileEditorProvider mySecondProvider;

    @NotNull
    private final String myEditorTypeId;

    /**
     * Constructor that takes two {@link FileEditorProvider}s.
     */
    public VScriptingSplitTextEditorProvider(@NotNull FileEditorProvider firstProvider, @NotNull FileEditorProvider secondProvider) {
        myFirstProvider = firstProvider;
        mySecondProvider = secondProvider;

        myEditorTypeId = "vscripting-split-provider[" + myFirstProvider.getEditorTypeId() + ";" + mySecondProvider.getEditorTypeId() + "]";
    }

    // Call both the provider's accept functions
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return myFirstProvider.accept(project, file) && mySecondProvider.accept(project, file);
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return createEditorAsync(project, file).build();
    }

    @NotNull
    @Override
    public Builder createEditorAsync(@NotNull final Project project, @NotNull final VirtualFile file) {
        final Builder firstBuilder = getBuilderFromEditorProvider(myFirstProvider, project, file);
        final Builder secondBuilder = getBuilderFromEditorProvider(mySecondProvider, project, file);

        return new Builder() {
            @Override
            public FileEditor build() {
                return createSplitEditor(firstBuilder.build(), secondBuilder.build());
            }
        };
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return myEditorTypeId;
    }

    protected abstract FileEditor createSplitEditor(@NotNull FileEditor firstEditor, @NotNull FileEditor secondEditor);

    // Hide the default editor
    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    @NotNull
    public static Builder getBuilderFromEditorProvider(@NotNull final FileEditorProvider provider,
                                                       @NotNull final Project project,
                                                       @NotNull final VirtualFile file) {
        if (provider instanceof AsyncFileEditorProvider) {
            return ((AsyncFileEditorProvider)provider).createEditorAsync(project, file);
        }
        else {
            return new Builder() {
                @Override
                public FileEditor build() {
                    return provider.createEditor(project, file);
                }
            };
        }
    }
}
