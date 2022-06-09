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

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon

/**
 * Class defining how the buttons in the SplitEditorToolbar will look. They will have an icon and text defining what the button does.
 *
 * @param text The text to be displayed along with the icon.
 * @param icon The icon to be displayed before the text.
 * @param delegate The ToggleAction to be performed on click.
 */

open class EditorToolbarActions internal constructor(val text: String,
                                                     val icon: Icon,
                                                     val delegate: ToggleAction): ToggleAction(text, text, icon), DumbAware {

    override fun isSelected(e: AnActionEvent): Boolean = delegate.isSelected(e)

    override fun setSelected(e: AnActionEvent, state: Boolean) = delegate.setSelected(e, state)

    // This needs to be true to show text along with an icon, otherwise only the icon will be shown
    override fun displayTextInToolbar(): Boolean = true
}