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

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon

/**
 * Class for implementing custom buttons with an icon and text to be shown in the SplitEditorToolbar.
 */

open class EditorToolbarActions internal constructor(val name: String,
                                                     val icon: Icon,
                                                     val delegate: ToggleAction): ToggleAction(name, name, icon), DumbAware {

    override fun isSelected(e: AnActionEvent): Boolean = delegate.isSelected(e)

    override fun setSelected(e: AnActionEvent, state: Boolean) = delegate.setSelected(e, state)

    override fun displayTextInToolbar(): Boolean = true
}