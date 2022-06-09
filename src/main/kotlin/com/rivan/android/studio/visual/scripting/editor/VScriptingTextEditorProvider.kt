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

package com.rivan.android.studio.visual.scripting.editor

import com.intellij.facet.ProjectFacetManager
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.idea.KotlinLanguage

/**
 * A PsiAwareTextEditor to be shown along with the preview editor.
 * The PsiAwareTextEditor is for now only. If it won't be required in the future, then it would be replaced with a normal TextEditor.
 */

class VScriptingTextEditorProvider : PsiAwareTextEditorProvider() {

    // Only the accept function is to be overridden, otherwise leave all other functions as they are
    // The accept function will accept only Java and Kotlin files and that too only in Android projects and so the editor
    // will be shown only when Java and Kotlin files are opened.
    override fun accept(project: Project, file: VirtualFile): Boolean {
        if (!super.accept(project, file)) {
            return false
        }
        // First find the file that has been opened in the editor
        val checkedFile = PsiManager.getInstance(project).findFile(file)
        // Return true if the opened project is of Android, i.e., it has AndroidFacet and the file is an instance of
        // JavaLanguage or KotlinLanguage
        return ProjectFacetManager.getInstance(project).hasFacets(AndroidFacet.ID)
                && (checkedFile!!.language === JavaLanguage.INSTANCE || checkedFile!!.language == KotlinLanguage.INSTANCE)
    }
}