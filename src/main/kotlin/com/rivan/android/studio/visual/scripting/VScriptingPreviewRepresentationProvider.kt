package com.rivan.android.studio.visual.scripting

import com.android.tools.idea.projectsystem.getModuleSystem
import com.android.tools.idea.uibuilder.editor.multirepresentation.PreferredVisibility
import com.android.tools.idea.uibuilder.editor.multirepresentation.PreviewRepresentation
import com.android.tools.idea.uibuilder.editor.multirepresentation.PreviewRepresentationProvider
import com.android.tools.idea.uibuilder.editor.multirepresentation.RepresentationName
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.KotlinLanguage
import javax.swing.JPanel

class VScriptingPreviewRepresentationProvider() : PreviewRepresentationProvider {

    override val displayName: RepresentationName
        get() = "Visual Scripting"

    override fun accept(project: Project, psiFile: PsiFile): Boolean {
        if (psiFile.getModuleSystem()?.usesCompose == true) {
            return false
        }

        return psiFile.language == JavaLanguage.INSTANCE || psiFile.language == KotlinLanguage.INSTANCE
    }

    override fun createRepresentation(psiFile: PsiFile): PreviewRepresentation {
        return VScriptingPreviewRepresentation(JPanel(), PreferredVisibility.SPLIT)
    }
}