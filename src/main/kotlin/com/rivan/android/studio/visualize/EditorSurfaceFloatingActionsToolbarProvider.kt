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