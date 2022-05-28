package com.rivan.android.studio.visual.scripting

import com.android.tools.idea.uibuilder.editor.multirepresentation.PreferredVisibility
import com.android.tools.idea.uibuilder.editor.multirepresentation.PreviewRepresentation
import javax.swing.JComponent

class VScriptingPreviewRepresentation(
    override val component: JComponent,
    override val preferredInitialVisibility: PreferredVisibility?
) : PreviewRepresentation {

    override fun dispose() {
        TODO("Not yet implemented")
    }
}