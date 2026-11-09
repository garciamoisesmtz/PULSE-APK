package com.pulse.app

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

/** Pose en un lienzo de 100 x 100. Piso en y = 90. B = extremidad de atrás, F = de adelante. */
data class Pose(
    val head: Offset, val sh: Offset, val hip: Offset,
    val eB: Offset, val hB: Offset, val eF: Offset, val hF: Offset,
    val kB: Offset, val fB: Offset, val kF: Offset, val fF: Offset,
)

enum class Joint { HAND_F, HAND_B, FOOT_F, FOOT_B, HIP }

sealed class Prop {
    data class Bar(val a: Offset, val b: Offset) : Prop()
    data class Pad(val topLeft: Offset, val size: Size) : Prop()
    data class Cable(val anchor: Offset, val joint: Joint) : Prop()
}

data class Anim(
    val a: Pose,
    val b: Pose,
    val props: List<Prop> = emptyList(),
    val focus: List<Joint> = emptyList(),
    val ms: Int = 1400,
    val front: Boolean = false,
)

fun o(x: Int, y: Int) = Offset(x.toFloat(), y.toFloat())
private fun mix(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

private fun lerpPose(a: Pose, b: Pose, t: Float) = Pose(
    mix(a.head, b.head, t), mix(a.sh, b.sh, t), mix(a.hip, b.hip, t),
    mix(a.eB, b.eB, t), mix(a.hB, b.hB, t), mix(a.eF, b.eF, t), mix(a.hF, b.hF, t),
    mix(a.kB, b.kB, t), mix(a.fB, b.fB, t), mix(a.kF, b.kF, t), mix(a.fF, b.fF, t),
)

private fun Pose.joint(j: Joint): Offset = when (j) {
    Joint.HAND_F -> hF
    Joint.HAND_B -> hB
    Joint.FOOT_F -> fF
    Joint.FOOT_B -> fB
    Joint.HIP -> hip
}

@Composable
fun ExerciseFigure(anim: Anim, modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "figure")
    val t by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(anim.ms, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "t",
    )
    Canvas(modifier) {
        val s = minOf(size.width, size.height) / 100f
        val ox = (size.width - 100f * s) / 2f
        val oy = (size.height - 100f * s) / 2f
        fun m(p: Offset) = Offset(ox + p.x * s, oy + p.y * s)
        val p = lerpPose(anim.a, anim.b, t)
        val propColor = Pc.TextSecondary.copy(alpha = 0.28f)

        drawLine(Pc.Border, m(o(4, 90)), m(o(96, 90)), strokeWidth = s * 0.8f)

        anim.props.forEach { pr ->
            when (pr) {
                is Prop.Bar -> drawLine(propColor, m(pr.a), m(pr.b), strokeWidth = s * 2.2f, cap = StrokeCap.Round)
                is Prop.Pad -> drawRoundRect(propColor, topLeft = m(pr.topLeft), size = Size(pr.size.width * s, pr.size.height * s), cornerRadius = CornerRadius(s * 1.4f))
                is Prop.Cable -> {
                    drawLine(Pc.TextSecondary.copy(alpha = 0.55f), m(pr.anchor), m(p.joint(pr.joint)), strokeWidth = s * 0.7f)
                    drawCircle(propColor, radius = s * 2f, center = m(pr.anchor))
                }
            }
        }

        val w = s * 3.4f
        val back = if (anim.front) Pc.TextPrimary else Pc.TextSecondary
        fun limb(a: Offset, b: Offset, c: Offset, color: Color) {
            drawLine(color, m(a), m(b), strokeWidth = w, cap = StrokeCap.Round)
            drawLine(color, m(b), m(c), strokeWidth = w, cap = StrokeCap.Round)
        }
        limb(p.sh, p.eB, p.hB, back)
        limb(p.hip, p.kB, p.fB, back)
        drawLine(Pc.TextPrimary, m(p.sh), m(p.hip), strokeWidth = w * 1.15f, cap = StrokeCap.Round)
        drawLine(Pc.TextPrimary, m(p.sh), m(p.head), strokeWidth = w * 0.8f, cap = StrokeCap.Round)
        drawCircle(Pc.TextPrimary, radius = s * 5.2f, center = m(p.head))
        limb(p.hip, p.kF, p.fF, Pc.TextPrimary)
        limb(p.sh, p.eF, p.hF, Pc.TextPrimary)

        anim.focus.forEach { j ->
            val c = m(p.joint(j))
            drawCircle(Pc.Accent.copy(alpha = 0.22f), radius = s * 5f, center = c)
            drawCircle(Pc.Accent, radius = s * 2.2f, center = c)
        }
    }
}

object Anims {
    private val stand = Pose(
        head = o(50, 16), sh = o(50, 27), hip = o(50, 52),
        eB = o(49, 40), hB = o(49, 52), eF = o(51, 40), hF = o(52, 52),
        kB = o(49, 70), fB = o(48, 88), kF = o(51, 70), fF = o(53, 88),
    )
    private val front = Pose(
        head = o(50, 16), sh = o(50, 27), hip = o(50, 52),
        eB = o(43, 40), hB = o(41, 52), eF = o(57, 40), hF = o(59, 52),
        kB = o(46, 70), fB = o(45, 88), kF = o(54, 70), fF = o(55, 88),
    )
    private val seat = Pose(
        head = o(41, 24), sh = o(41, 35), hip = o(40, 62),
        eB = o(40, 48), hB = o(44, 62), eF = o(41, 48), hF = o(45, 62),
        kB = o(57, 62), fB = o(57, 84), kF = o(58, 63), fF = o(59, 85),
    )
    private val bench = listOf(
        Prop.Pad(o(31, 28), Size(5f, 34f)),
        Prop.Pad(o(30, 63), Size(30f, 3f)),
        Prop.Bar(o(27, 6), o(27, 90)),
    )

    val marcha = Anim(
        stand.copy(kF = o(64, 57), fF = o(62, 75), eB = o(56, 38), hB = o(61, 30), eF = o(45, 40), hF = o(44, 51)),
        stand.copy(kB = o(64, 57), fB = o(62, 75), kF = o(51, 70), fF = o(53, 88), eF = o(56, 38), hF = o(61, 30), eB = o(45, 40), hB = o(44, 51)),
        ms = 700,
    )
    val marchaAlta = Anim(
        stand.copy(kF = o(67, 51), fF = o(66, 69), eB = o(57, 37), hB = o(63, 28), eF = o(44, 40), hF = o(42, 50)),
        stand.copy(kB = o(67, 51), fB = o(66, 69), kF = o(51, 70), fF = o(53, 88), eF = o(57, 37), hF = o(63, 28), eB = o(44, 40), hB = o(42, 50)),
        focus = listOf(Joint.FOOT_F), ms = 600,
    )
    val caminata = Anim(
        stand.copy(kF = o(56, 70), fF = o(62, 88), kB = o(46, 70), fB = o(40, 88), eF = o(47, 40), hF = o(44, 51), eB = o(54, 40), hB = o(58, 50)),
        stand.copy(kB = o(56, 70), fB = o(62, 88), kF = o(46, 70), fF = o(40, 88), eB = o(47, 40), hB = o(44, 51), eF = o(54, 40), hF = o(58, 50)),
        ms = 650,
    )
    val circulos = Anim(
        front.copy(eB = o(38, 25), hB = o(26, 22), eF = o(62, 25), hF = o(74, 22)),
        front.copy(eB = o(38, 29), hB = o(26, 33), eF = o(62, 29), hF = o(74, 33)),
        focus = listOf(Joint.HAND_B, Joint.HAND_F), ms = 500, front = true,
    )
    val cadera = Anim(
        front.copy(hip = o(45, 52), eB = o(37, 40), hB = o(39, 51), eF = o(57, 40), hF = o(51, 51), kB = o(44, 70), kF = o(52, 70)),
        front.copy(hip = o(55, 52), eB = o(43, 40), hB = o(49, 51), eF = o(63, 40), hF = o(61, 51), kB = o(48, 70), kF = o(56, 70)),
        focus = listOf(Joint.HIP), ms = 1100, front = true,
    )
    private val squatDown = Pose(
        head = o(54, 34), sh = o(48, 44), hip = o(36, 66),
        eB = o(58, 46), hB = o(68, 46), eF = o(59, 47), hF = o(69, 47),
        kB = o(56, 68), fB = o(51, 88), kF = o(57, 69), fF = o(53, 88),
    )
    val sentadillaLibre = Anim(
        stand.copy(eB = o(58, 28), hB = o(68, 28), eF = o(59, 29), hF = o(69, 29)),
        squatDown,
        focus = listOf(Joint.HIP), ms = 1500,
    )
    val goblet = Anim(
        stand.copy(eB = o(52, 42), hB = o(55, 32), eF = o(53, 42), hF = o(56, 32)),
        squatDown.copy(eB = o(51, 57), hB = o(55, 47), eF = o(52, 57), hF = o(56, 47)),
        focus = listOf(Joint.HIP, Joint.HAND_F), ms = 1800,
    )
    val pressPecho = Anim(
        seat.copy(eB = o(32, 41), hB = o(44, 37), eF = o(33, 42), hF = o(45, 38)),
        seat.copy(eB = o(54, 36), hB = o(66, 36), eF = o(55, 37), hF = o(67, 37)),
        props = bench, focus = listOf(Joint.HAND_F), ms = 1600,
    )
    val jalon = Anim(
        seat.copy(eB = o(46, 22), hB = o(50, 9), eF = o(47, 22), hF = o(51, 9)),
        seat.copy(eB = o(36, 46), hB = o(44, 32), eF = o(37, 46), hF = o(45, 32)),
        props = listOf(Prop.Pad(o(30, 63), Size(30f, 3f)), Prop.Pad(o(53, 56), Size(10f, 4f)), Prop.Bar(o(38, 2), o(64, 2)), Prop.Cable(o(51, 2), Joint.HAND_F)),
        focus = listOf(Joint.HAND_F), ms = 1700,
    )
    val extension = Anim(
        seat.copy(eB = o(37, 50), hB = o(43, 63), eF = o(38, 50), hF = o(44, 63)),
        seat.copy(eB = o(37, 50), hB = o(43, 63), eF = o(38, 50), hF = o(44, 63), fB = o(75, 56), fF = o(76, 57)),
        props = bench, focus = listOf(Joint.FOOT_F), ms = 1600,
    )
    val curlFemoral = Anim(
        stand.copy(head = o(55, 18), sh = o(52, 28), eB = o(58, 36), hB = o(66, 40), eF = o(59, 37), hF = o(67, 41), kB = o(48, 70), fB = o(46, 87)),
        stand.copy(head = o(55, 18), sh = o(52, 28), eB = o(58, 36), hB = o(66, 40), eF = o(59, 37), hF = o(67, 41), kB = o(48, 70), fB = o(34, 60)),
        props = listOf(Prop.Bar(o(70, 28), o(70, 90)), Prop.Cable(o(20, 88), Joint.FOOT_B)),
        focus = listOf(Joint.FOOT_B), ms = 1500,
    )
    val plancha = Anim(
        Pose(o(80, 66), o(71, 72), o(45, 74), o(70, 88), o(81, 88), o(71, 88), o(82, 88), o(31, 81), o(17, 88), o(32, 81), o(18, 88)),
        Pose(o(80, 65), o(71, 72), o(45, 72), o(70, 88), o(81, 88), o(71, 88), o(82, 88), o(31, 80), o(17, 88), o(32, 80), o(18, 88)),
        focus = listOf(Joint.HIP), ms = 2400,
    )
    val zancadas = Anim(
        stand,
        Pose(o(50, 28), o(50, 39), o(50, 64), o(49, 52), o(49, 63), o(51, 52), o(52, 63), o(41, 83), o(29, 86), o(66, 68), o(66, 88)),
        focus = listOf(Joint.HIP), ms = 1600,
    )
    val remo = Anim(
        Pose(o(50, 52), o(44, 60), o(34, 82), o(54, 64), o(64, 68), o(55, 65), o(65, 69), o(50, 72), o(66, 86), o(51, 73), o(67, 87)),
        Pose(o(36, 48), o(34, 58), o(34, 82), o(25, 65), o(37, 70), o(26, 66), o(38, 71), o(50, 72), o(66, 86), o(51, 73), o(67, 87)),
        props = listOf(Prop.Bar(o(70, 78), o(70, 90)), Prop.Cable(o(72, 86), Joint.HAND_F)),
        focus = listOf(Joint.HAND_F), ms = 1600,
    )
    val aperturas = Anim(
        Pose(o(50, 22), o(50, 33), o(50, 60), o(34, 34), o(33, 22), o(66, 34), o(67, 22), o(44, 64), o(44, 86), o(56, 64), o(56, 86)),
        Pose(o(50, 22), o(50, 33), o(50, 60), o(45, 38), o(47, 24), o(55, 38), o(53, 24), o(44, 64), o(44, 86), o(56, 64), o(56, 86)),
        props = listOf(Prop.Pad(o(43, 26), Size(14f, 34f)), Prop.Pad(o(37, 61), Size(26f, 3f))),
        focus = listOf(Joint.HAND_B, Joint.HAND_F), ms = 1600, front = true,
    )
    val laterales = Anim(
        front.copy(eB = o(45, 40), hB = o(44, 52), eF = o(55, 40), hF = o(56, 52)),
        front.copy(eB = o(45, 40), hB = o(44, 52), eF = o(62, 30), hF = o(74, 29)),
        props = listOf(Prop.Bar(o(22, 10), o(22, 90)), Prop.Cable(o(24, 88), Joint.HAND_F)),
        focus = listOf(Joint.HAND_F), ms = 1500, front = true,
    )
    val curlBiceps = Anim(
        stand.copy(eF = o(51, 40), hF = o(53, 53), eB = o(50, 40), hB = o(52, 53)),
        stand.copy(eF = o(51, 40), hF = o(58, 30), eB = o(50, 40), hB = o(57, 30)),
        props = listOf(Prop.Bar(o(66, 10), o(66, 90)), Prop.Cable(o(64, 88), Joint.HAND_F)),
        focus = listOf(Joint.HAND_F), ms = 1400,
    )
    val triceps = Anim(
        stand.copy(eF = o(53, 41), hF = o(61, 31), eB = o(52, 41), hB = o(60, 31)),
        stand.copy(eF = o(53, 41), hF = o(56, 56), eB = o(52, 41), hB = o(55, 56)),
        props = listOf(Prop.Bar(o(66, 2), o(66, 90)), Prop.Cable(o(64, 3), Joint.HAND_F)),
        focus = listOf(Joint.HAND_F), ms = 1400,
    )
    val puente = Anim(
        Pose(o(16, 84), o(26, 85), o(46, 85), o(36, 88), o(46, 88), o(37, 88), o(47, 88), o(58, 70), o(64, 88), o(59, 70), o(65, 88)),
        Pose(o(16, 84), o(26, 85), o(46, 72), o(36, 88), o(46, 88), o(37, 88), o(47, 88), o(60, 66), o(64, 88), o(61, 66), o(65, 88)),
        focus = listOf(Joint.HIP), ms = 1500,
    )
    val bichoMuerto = Anim(
        Pose(o(18, 82), o(28, 83), o(52, 83), o(29, 72), o(30, 60), o(29, 72), o(30, 60), o(54, 66), o(68, 66), o(54, 66), o(68, 66)),
        Pose(o(18, 82), o(28, 83), o(52, 83), o(29, 72), o(30, 60), o(20, 80), o(12, 78), o(54, 66), o(68, 66), o(68, 79), o(84, 82)),
        focus = listOf(Joint.HAND_F, Joint.FOOT_F), ms = 1800,
    )
    val planchaLateral = Anim(
        Pose(o(20, 54), o(28, 62), o(54, 76), o(26, 88), o(36, 88), o(36, 66), o(46, 70), o(70, 82), o(86, 88), o(70, 80), o(86, 85)),
        Pose(o(20, 53), o(28, 62), o(54, 71), o(26, 88), o(36, 88), o(36, 64), o(46, 67), o(70, 79), o(86, 88), o(70, 77), o(86, 85)),
        focus = listOf(Joint.HIP), ms = 2000, front = true,
    )
}
