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

package com.rivan.android.studio.visual.scripting.editor;

import com.intellij.facet.ProjectFacetManager;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.KotlinLanguage;

/**
 *  File Editor Provider that provides the {@link VScriptingPreviewFileEditor} as the editor.
 */

public class VScriptingPreviewFileEditorProvider implements FileEditorProvider {

    // Override this to accept only Java and Kotlin files
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        // Find the currently opened file
        PsiFile checkedFile = PsiManager.getInstance(project).findFile(file);
        // Return true if the opened project is of Android, i.e., it has AndroidFacet and the file is an instance of
        // JavaLanguage or KotlinLanguage
        return (checkedFile.getLanguage() == JavaLanguage.INSTANCE || checkedFile.getLanguage() == KotlinLanguage.INSTANCE)
                && ProjectFacetManager.getInstance(project).hasFacets(AndroidFacet.ID);
    }

    // Create VScriptingPreviewFileEditor
    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new VScriptingPreviewFileEditor(project, file);
    }

    // Return editorTypeId
    @NotNull
    @Override
    public String getEditorTypeId() {
        return "vscripting-editor";
    }

    // Place the Visual Editor (preview editor) after the TextEditor, i.e., the default editor
    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}
