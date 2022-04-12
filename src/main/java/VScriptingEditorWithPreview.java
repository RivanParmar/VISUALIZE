/* Copyright 2022 Rivan Parmar

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import org.jetbrains.annotations.NotNull;

public class VScriptingEditorWithPreview extends TextEditorWithPreview {

    private final EditorToolbarActions textViewAction = new EditorToolbarActions("Code",
            AllIcons.General.LayoutEditorOnly,
            super.getShowEditorAction());

    private final EditorToolbarActions splitViewAction = new EditorToolbarActions("Split",
            AllIcons.General.LayoutEditorPreview,
            super.getShowEditorAndPreviewAction());

    private final EditorToolbarActions visualViewAction = new EditorToolbarActions("Visual Editor",
            AllIcons.General.LayoutPreviewOnly,
            super.getShowPreviewAction());

    public VScriptingEditorWithPreview(@NotNull TextEditor editor, @NotNull VScriptingPreviewFileEditor preview) {
        super(editor, preview, "Visual Scripting Editor", Layout.SHOW_EDITOR_AND_PREVIEW);
    }


    @Override
    protected @NotNull ToggleAction getShowEditorAction() {
        return textViewAction;
    }

    @Override
    protected @NotNull ToggleAction getShowPreviewAction() {
        return visualViewAction;
    }

    @Override
    protected @NotNull ToggleAction getShowEditorAndPreviewAction() {
        return splitViewAction;
    }
}
