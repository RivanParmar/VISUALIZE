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

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import org.jetbrains.annotations.NotNull;

/**
 *  The main class extending {@link VScriptingSplitTextEditorProvider} and creating an editor with {@link VScriptingTextEditorProvider}
 *  and {@link VScriptingPreviewFileEditorProvider}.
 *  This class has to be registered in the plugin.xml file as a "fileEditorProvider".
 */

public class VScriptingSplitEditorProvider extends VScriptingSplitTextEditorProvider{

    /**
     * Create the {@link VScriptingSplitEditorProvider} with {@link VScriptingTextEditorProvider} as the TextEditor and
     * {@link VScriptingPreviewFileEditorProvider} as the preview editor.
     */
    public VScriptingSplitEditorProvider() {
        super(new VScriptingTextEditorProvider(), new VScriptingPreviewFileEditorProvider());
    }

    // Create the split editor and return a new VScriptingEditorWithPreview
    @Override
    protected FileEditor createSplitEditor(@NotNull FileEditor firstEditor, @NotNull FileEditor secondEditor) {
        if (!(firstEditor instanceof TextEditor) || !(secondEditor instanceof VScriptingPreviewFileEditor)) {
            throw new IllegalArgumentException("Main editor should be text editor!");
        }
        return new VScriptingEditorWithPreview(((TextEditor)firstEditor), ((VScriptingPreviewFileEditor)secondEditor));
    }
}
