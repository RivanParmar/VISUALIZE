package com.rivan.android.studio.visualize

import com.android.tools.editor.EditorActionsFloatingToolbarProvider
import com.android.tools.editor.EditorActionsToolbarActionGroups
import com.android.tools.idea.uibuilder.editor.EditableDesignSurfaceActionGroups
import com.intellij.openapi.Disposable
import javax.swing.JComponent

/** Creates the floating actions toolbar used on the [VisualEditorSurface] */
class EditorSurfaceFloatingActionsToolbarProvider(
    private val visualEditorSurface: VisualEditorSurface<*>,
    component: JComponent,
    parentDisposable: Disposable
) : EditorActionsFloatingToolbarProvider(component, parentDisposable), EditorSurfaceListener {

    init {
        visualEditorSurface.addListener(this)
        visualEditorSurface.addPanZoomListener(this)
        updateToolbar()
    }

    override fun dispose() {
        super.dispose()
        visualEditorSurface.removeListener(this)
        visualEditorSurface.removePanZoomListener(this)
    }

    override fun modelChanged(surface: VisualEditorSurface<*>, model: VisualEditorModel?) {
        updateToolbar()
    }

    override fun getActionGroups(): EditorActionsToolbarActionGroups {
        return EditableDesignSurfaceActionGroups()
    }
}