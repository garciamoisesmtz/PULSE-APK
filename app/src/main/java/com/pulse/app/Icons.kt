package com.pulse.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// Íconos Lucide (ISC License, lucide.dev) convertidos a ImageVector
object Lucide {
    val House: ImageVector by lazy { icon("House", listOf("M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8", "M3 10a2 2 0 0 1 .709-1.528l7-6a2 2 0 0 1 2.582 0l7 6A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z")) }
    val CalendarDays: ImageVector by lazy { icon("CalendarDays", listOf("M8 2v3", "M16 2v3", "M5.0,3.0h14.0a2.0,2.0 0 0 1 2.0,2.0v14.0a2.0,2.0 0 0 1 -2.0,2.0h-14.0a2.0,2.0 0 0 1 -2.0,-2.0v-14.0a2.0,2.0 0 0 1 2.0,-2.0Z", "M3 9h18", "M8 13h.01", "M12 13h.01", "M16 13h.01", "M8 17h.01", "M12 17h.01", "M16 17h.01")) }
    val Dumbbell: ImageVector by lazy { icon("Dumbbell", listOf("M17.596 12.768a2 2 0 1 0 2.829-2.829l-1.768-1.767a2 2 0 0 0 2.828-2.829l-2.828-2.828a2 2 0 0 0-2.829 2.828l-1.767-1.768a2 2 0 1 0-2.829 2.829z", "m2.5 21.5 1.4-1.4", "m20.1 3.9 1.4-1.4", "M5.343 21.485a2 2 0 1 0 2.829-2.828l1.767 1.768a2 2 0 1 0 2.829-2.829l-6.364-6.364a2 2 0 1 0-2.829 2.829l1.768 1.767a2 2 0 0 0-2.828 2.829z", "m9.6 14.4 4.8-4.8")) }
    val Scale: ImageVector by lazy { icon("Scale", listOf("M12 3v18", "m19 8 3 8a5 5 0 0 1-6 0zV7", "M3 7h1a17 17 0 0 0 8-2 17 17 0 0 0 8 2h1", "m5 8 3 8a5 5 0 0 1-6 0zV7", "M7 21h10")) }
    val Settings: ImageVector by lazy { icon("Settings", listOf("M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915", "M9.0,12.0a3.0,3.0 0 1,0 6.0,0a3.0,3.0 0 1,0 -6.0,0")) }
    val Bell: ImageVector by lazy { icon("Bell", listOf("M10.268 21a2 2 0 0 0 3.464 0", "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326")) }
    val Check: ImageVector by lazy { icon("Check", listOf("M20 6 9 17l-5-5")) }
    val Play: ImageVector by lazy { icon("Play", listOf("M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z")) }
    val Pause: ImageVector by lazy { icon("Pause", listOf("M15.0,3.0h3.0a1.0,1.0 0 0 1 1.0,1.0v16.0a1.0,1.0 0 0 1 -1.0,1.0h-3.0a1.0,1.0 0 0 1 -1.0,-1.0v-16.0a1.0,1.0 0 0 1 1.0,-1.0Z", "M6.0,3.0h3.0a1.0,1.0 0 0 1 1.0,1.0v16.0a1.0,1.0 0 0 1 -1.0,1.0h-3.0a1.0,1.0 0 0 1 -1.0,-1.0v-16.0a1.0,1.0 0 0 1 1.0,-1.0Z")) }
    val Timer: ImageVector by lazy { icon("Timer", listOf("M10,2L14,2", "M12,14L15,11", "M4.0,14.0a8.0,8.0 0 1,0 16.0,0a8.0,8.0 0 1,0 -16.0,0")) }
    val Flame: ImageVector by lazy { icon("Flame", listOf("M12 3q1 4 4 6.5t3 5.5a1 1 0 0 1-14 0 5 5 0 0 1 1-3 1 1 0 0 0 5 0c0-2-1.5-3-1.5-5q0-2 2.5-4")) }
    val ChevronLeft: ImageVector by lazy { icon("ChevronLeft", listOf("m15 18-6-6 6-6")) }
    val ChevronRight: ImageVector by lazy { icon("ChevronRight", listOf("m9 18 6-6-6-6")) }
    val Plus: ImageVector by lazy { icon("Plus", listOf("M5 12h14", "M12 5v14")) }
    val X: ImageVector by lazy { icon("X", listOf("M18 6 6 18", "m6 6 12 12")) }
    val Activity: ImageVector by lazy { icon("Activity", listOf("M22 12h-2.48a2 2 0 0 0-1.93 1.46l-2.35 8.36a.25.25 0 0 1-.48 0L9.24 2.18a.25.25 0 0 0-.48 0l-2.35 8.36A2 2 0 0 1 4.49 12H2")) }
    val TrendingDown: ImageVector by lazy { icon("TrendingDown", listOf("M16 17h6v-6", "m22 17-8.5-8.5-5 5L2 7")) }
    val Footprints: ImageVector by lazy { icon("Footprints", listOf("M4 16v-2.38C4 11.5 2.97 10.5 3 8c.03-2.72 1.49-6 4.5-6C9.37 2 10 3.8 10 5.5c0 3.11-2 5.66-2 8.68V16a2 2 0 1 1-4 0Z", "M20 20v-2.38c0-2.12 1.03-3.12 1-5.62-.03-2.72-1.49-6-4.5-6C14.63 6 14 7.8 14 9.5c0 3.11 2 5.66 2 8.68V20a2 2 0 1 0 4 0Z", "M16 17h4", "M4 13h4")) }
    val RotateCcw: ImageVector by lazy { icon("RotateCcw", listOf("M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8", "M3 3v5h5")) }
    val Ruler: ImageVector by lazy { icon("Ruler", listOf("M21.3 15.3a2.4 2.4 0 0 1 0 3.4l-2.6 2.6a2.4 2.4 0 0 1-3.4 0L2.7 8.7a2.41 2.41 0 0 1 0-3.4l2.6-2.6a2.41 2.41 0 0 1 3.4 0Z", "m14.5 12.5 2-2", "m11.5 9.5 2-2", "m8.5 6.5 2-2", "m17.5 15.5 2-2")) }
    val Target: ImageVector by lazy { icon("Target", listOf("M2.0,12.0a10.0,10.0 0 1,0 20.0,0a10.0,10.0 0 1,0 -20.0,0", "M6.0,12.0a6.0,6.0 0 1,0 12.0,0a6.0,6.0 0 1,0 -12.0,0", "M10.0,12.0a2.0,2.0 0 1,0 4.0,0a2.0,2.0 0 1,0 -4.0,0")) }
    val Clock: ImageVector by lazy { icon("Clock", listOf("M2.0,12.0a10.0,10.0 0 1,0 20.0,0a10.0,10.0 0 1,0 -20.0,0", "M12 6v6l4 2")) }
    val Trash: ImageVector by lazy { icon("Trash", listOf("M10 11v6", "M14 11v6", "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6", "M3 6h18", "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2")) }
    val Info: ImageVector by lazy { icon("Info", listOf("M2.0,12.0a10.0,10.0 0 1,0 20.0,0a10.0,10.0 0 1,0 -20.0,0", "M12 16v-4", "M12 8h.01")) }
}

private fun icon(name: String, paths: List<String>): ImageVector {
    val b = ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
    paths.forEach { d ->
        b.addPath(pathData = addPathNodes(d), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
    }
    return b.build()
}
