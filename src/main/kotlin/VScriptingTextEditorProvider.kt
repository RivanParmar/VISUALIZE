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

import com.intellij.facet.ProjectFacetManager
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.idea.KotlinLanguage

class VScriptingTextEditorProvider : PsiAwareTextEditorProvider() {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        if (!super.accept(project, file)) {
            return false
        }
        val checkedFile = PsiManager.getInstance(project).findFile(file)
        return ProjectFacetManager.getInstance(project).hasFacets(AndroidFacet.ID) && (checkedFile!!.language === JavaLanguage.INSTANCE || checkedFile!!.language == KotlinLanguage.INSTANCE)
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val actualEditor = super.createEditor(project, file)
        if (actualEditor is TextEditor) {
            val toolbar = FloatingActionsToolbar(actualEditor.editor, "Visual.Scripting.Toolbar.Actions")
            Disposer.register(actualEditor, toolbar)
        }
        return actualEditor
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR
    }
}