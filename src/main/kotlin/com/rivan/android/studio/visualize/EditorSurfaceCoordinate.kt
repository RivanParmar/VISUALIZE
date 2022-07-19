package com.rivan.android.studio.visualize

/**
 * The independent scaling system in VisualEditorSurface regardless DPI of display.
 * For example, when zoom level is 50% and ScreenScalingFactor is 2, this value is 50% / 2 = 25%
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.LOCAL_VARIABLE, AnnotationTarget.VALUE_PARAMETER)
internal annotation class SurfaceScale

/**
 * These annotations are used in [VisualEditorSurface] to improve the readability of the relationship between scaling and zoom level.
 */
/**
 * Percentage of scaling a.k.a. zoom level (25%, 33%, 50%, etc). This value consider HDPI of display.
 * This value is same as the zoom level shows in [VisualEditorSurface].
 * SurfaceZoomLevel = Surface Scale * Screen Factor
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.LOCAL_VARIABLE, AnnotationTarget.VALUE_PARAMETER)
internal annotation class SurfaceZoomLevel
