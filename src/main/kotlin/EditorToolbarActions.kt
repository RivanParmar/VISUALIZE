import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon

open class EditorToolbarActions internal constructor(val name: String,
                                                     val icon: Icon,
                                                     val delegate: ToggleAction): ToggleAction(name, name, icon), DumbAware {

    override fun isSelected(e: AnActionEvent): Boolean = delegate.isSelected(e)

    override fun setSelected(e: AnActionEvent, state: Boolean) = delegate.setSelected(e, state)

    override fun displayTextInToolbar(): Boolean = true
}