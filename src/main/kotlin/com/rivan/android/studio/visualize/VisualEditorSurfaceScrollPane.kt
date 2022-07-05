package com.rivan.android.studio.visualize

import com.intellij.openapi.wm.IdeGlassPane
import com.intellij.ui.components.JBScrollBar
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import org.gradle.internal.impldep.org.intellij.lang.annotations.JdkConstants
import java.awt.Adjustable
import java.awt.Color
import java.awt.event.AdjustmentListener
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JScrollBar
import javax.swing.JScrollPane
import javax.swing.plaf.ScrollBarUI

/**
 * Custom scroll pane for the visual editor.
 */

class VisualEditorSurfaceScrollPane private constructor() : JBScrollPane(0) {

    private class MyScrollBar(@JdkConstants.AdjustableOrientation orientation: Int) : JBScrollBar(orientation),
    IdeGlassPane.TopComponent{

        private var persistentUI: ScrollBarUI? = null

        override fun canBePreprocessed(event: MouseEvent): Boolean {
            return canBePreprocessed(event, this)
        }

        override fun setUI(ui: ScrollBarUI?) {
            if (persistentUI == null) persistentUI = ui
            super.setUI(persistentUI)
            isOpaque = false
        }

        override fun getUnitIncrement(direction: Int): Int = 20
        override fun getBlockIncrement(direction: Int): Int = 1

        init {
            isOpaque = false
        }
    }

    override fun createVerticalScrollBar(): JScrollBar = MyScrollBar(Adjustable.VERTICAL)
    override fun createHorizontalScrollBar(): JScrollBar = MyScrollBar(Adjustable.HORIZONTAL)

    init {
        setupCorners()
    }

    companion object {
        /**
         * Returns a [JScrollPane] containing the given content and with the given background color.
         *
         * @param content the scrollable content.
         * @param background the scroll surface background.
         * @param onPanningChanged callback when the scrollable area changes size.
         */
        @JvmStatic
        fun createDefaultScrollPane(content: JComponent,
                                    background: Color,
                                    onPanningChanged: AdjustmentListener
        ): JScrollPane =
            VisualEditorSurfaceScrollPane().apply {
                setViewportView(content)
                border = JBUI.Borders.empty()
                viewport.background = background
                verticalScrollBarPolicy = VERTICAL_SCROLLBAR_ALWAYS
                horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_ALWAYS
                horizontalScrollBar.addAdjustmentListener(onPanningChanged)
                verticalScrollBar.addAdjustmentListener(onPanningChanged)
            }
    }

}