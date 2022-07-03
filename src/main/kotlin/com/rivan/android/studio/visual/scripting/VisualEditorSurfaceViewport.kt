package com.rivan.android.studio.visual.scripting

import java.awt.Component
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JViewport
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Abstraction over the [VisualEditorSurface] viewport. In scrollable surfaces, this will wrap a [JViewport].
 * For non scrolable surfaces, this will simply wrap a [Component].
 */
interface VisualEditorSurfaceViewport {
    val viewRect: Rectangle
    val viewportComponent: Component

    /**
     * The contained view in this viewport. For non scrollable surfaces, this might be the same
     * as [viewportComponent].
     */
    val viewComponent: Component

    var viewPosition: Point

    val extentSize: Dimension
    val viewSize: Dimension

    fun addChangeListener(changeListener: ChangeListener)
}

/**
 * A [VisualEditorSurfaceViewport] for a scrollable surface. This is a direct abstraction over [JViewport].
 */
class ScrollableEditorSurfaceViewport(val viewport: JViewport): VisualEditorSurfaceViewport {
    override val viewRect: Rectangle
        get() = viewport.viewRect
    override val viewportComponent: Component
        get() = viewport
    override val viewComponent: Component
        get() = viewport.view
    override var viewPosition: Point
        get() = viewport.viewPosition
        set(value) {
            viewport.viewPosition = value
        }
    override val extentSize: Dimension
        get() = viewport.extentSize
    override val viewSize: Dimension
        get() = viewport.viewSize

    override fun addChangeListener(changeListener: ChangeListener) = viewport.addChangeListener(changeListener)
}

/**
 * A [VisualEditorSurfaceViewport] for non scrollable surfaces. These surfaces will usually be embedded in a scrollable panel.
 */
class NonScrollableDesignSurfaceViewport(val view: VisualEditorSurface<*>): VisualEditorSurfaceViewport {
    override val viewRect: Rectangle
        get() = view.bounds
    override val viewportComponent: Component
        get() = view
    override val viewComponent: Component
        get() = view
    override var viewPosition: Point
        get() = Point(0, 0)
        set(_) {}
    override val extentSize: Dimension
        get() = view.visibleRect.size // The extent size in this case is just the visible part of the design surface
    override val viewSize: Dimension
        get() = view.size

    override fun addChangeListener(changeListener: ChangeListener) {
        view.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                changeListener.stateChanged(ChangeEvent(e.source))
            }
        })
    }
}