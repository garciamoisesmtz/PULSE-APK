package com.pulse.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/** Ejercicios de máquina con registro de peso, en el orden en que aparecen en las rutinas. */
val trackedExercises: List<Exercise> = listOf(
    Ex.pressPecho, Ex.jalon, Ex.extension, Ex.curlFemoral,
    Ex.remo, Ex.aperturas, Ex.laterales, Ex.curlBiceps, Ex.triceps,
)

@Composable
fun MeasurementsScreen(store: Store, openFormSignal: Int) {
    val sprint = store.data.sprint ?: return
    var tab by remember { mutableIntStateOf(0) }
    var editStart by remember { mutableStateOf(false) }
    LaunchedEffect(openFormSignal) { if (openFormSignal > 0) tab = 0 }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        ScreenTitle("Progreso")
        SegmentedTabs(listOf("Peso corporal", "Fuerza"), tab) { tab = it }
        Gap(24)
        if (tab == 0) {
            BodySection(store, sprint, openFormSignal, onEditStart = { editStart = true })
        } else {
            StrengthSection(store.data, sprint)
        }
        Gap(24)
    }

    if (editStart) {
        EditStartDialog(store, sprint, onDismiss = { editStart = false })
    }
}

@Composable
fun SegmentedTabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val outer = RoundedCornerShape(10.dp)
    val inner = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(outer)
            .background(Pc.Surface)
            .border(1.dp, Pc.Border, outer)
            .padding(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val sel = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(inner)
                    .background(if (sel) Pc.SurfaceElevated else Pc.Surface)
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = Tx.Button.copy(fontSize = 14.sp, color = if (sel) Pc.TextPrimary else Pc.TextSecondary))
            }
        }
    }
}

@Composable
private fun BodySection(store: Store, sprint: Sprint, openFormSignal: Int, onEditStart: () -> Unit) {
    val data = store.data
    val today = LocalDate.now()
    var showForm by remember { mutableStateOf(false) }
    LaunchedEffect(openFormSignal) { if (openFormSignal > 0) showForm = true }
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    val list = data.measurements.filter { it.day >= sprint.startDay }.sortedBy { it.day }
    val latest = list.lastOrNull()
    val w = weight.toNum()
    val bigNum = Tx.Display.copy(fontSize = 44.sp, lineHeight = 48.sp)

    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Inicial, lb", style = Tx.Label)
                Icon(
                    Lucide.Pencil,
                    contentDescription = "Editar peso inicial",
                    tint = Pc.TextSecondary,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onEditStart)
                        .padding(5.dp),
                )
            }
            Text(fmtNum(sprint.startWeight), style = bigNum)
        }
        Column(Modifier.weight(1f)) {
            Box(Modifier.height(26.dp), contentAlignment = Alignment.CenterStart) {
                Text("Actual, lb", style = Tx.Label)
            }
            Text(latest?.let { fmtNum(it.weight) } ?: "–", style = bigNum)
        }
    }
    if (latest != null) {
        val d = latest.weight - sprint.startWeight
        val waistText = if (latest.waist != null && sprint.startWaist != null) ", cintura ${fmtDelta(latest.waist - sprint.startWaist)} cm" else ""
        Gap(6)
        Text("${fmtDelta(d)} lb desde el inicio$waistText", style = Tx.Body.copy(color = if (d < -0.05) Pc.Accent else Pc.TextSecondary))
    }
    Gap(20)

    if (showForm) {
        PCard {
            Text("Nueva medición", style = Tx.H2)
            Gap(4)
            Text("En ayunas, después de ir al baño y con poca ropa.", style = Tx.BodySecondary)
            Gap(14)
            NumberField(weight, { weight = it }, "Peso", "lb")
            Gap(10)
            NumberField(waist, { waist = it }, "Cintura (opcional)", "cm")
            Gap(16)
            Row {
                SecondaryButton("Cancelar", modifier = Modifier.weight(1f), onClick = { showForm = false })
                HGap(8)
                PrimaryButton("Guardar", enabled = w != null && w > 50, modifier = Modifier.weight(1f), onClick = {
                    val day = today.toEpochDay()
                    val m = Measurement(day, w ?: 0.0, waist.toNum())
                    store.update { d -> d.copy(measurements = (d.measurements.filter { it.day != day } + m).sortedBy { it.day }) }
                    weight = ""
                    waist = ""
                    showForm = false
                })
            }
        }
    } else {
        PrimaryButton("Registrar medición", icon = Lucide.Plus, onClick = { showForm = true })
    }
    Gap(20)

    PCard {
        Text("Evolución del peso", style = Tx.H3)
        Gap(12)
        if (list.size >= 2) {
            LineChart(list.map { it.day to it.weight }, Modifier.fillMaxWidth().height(180.dp))
        } else {
            Text("La gráfica aparece desde tu segunda medición.", style = Tx.BodySecondary)
        }
    }
    val waistPoints = list.mapNotNull { m -> m.waist?.let { m.day to it } }
    if (waistPoints.size >= 2) {
        Gap(16)
        PCard {
            Text("Evolución de la cintura", style = Tx.H3)
            Gap(12)
            LineChart(waistPoints, Modifier.fillMaxWidth().height(150.dp), minRange = 2.0)
        }
    }
    Gap(20)

    Text("Fechas de medición", style = Tx.H3)
    Gap(10)
    PCard {
        val dates = sprint.measureDates()
        dates.forEachIndexed { i, d ->
            val m = list.firstOrNull { it.day == d.toEpochDay() }
            val past = !d.isAfter(today)
            Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (m != null) Pc.Accent else Pc.Surface)
                        .border(1.dp, if (m != null) Pc.Accent else Pc.TextSecondary, CircleShape),
                )
                HGap(12)
                Column(Modifier.weight(1f)) {
                    Text("Día ${d.toEpochDay() - sprint.startDay}", style = Tx.Body)
                    Text(fmtDateMed(d), style = Tx.Small)
                }
                Text(
                    m?.let { "${fmtNum(it.weight)} lb" } ?: if (past) "Sin registro" else "Pendiente",
                    style = Tx.NumSmall.copy(fontSize = 15.sp, color = if (m != null) Pc.TextPrimary else Pc.TextSecondary),
                )
            }
            if (i < dates.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Pc.Border))
        }
    }
    Gap(20)

    if (list.isNotEmpty()) {
        Text("Historial", style = Tx.H3)
        Gap(10)
        PCard {
            list.reversed().forEachIndexed { i, m ->
                val prev = list.lastOrNull { it.day < m.day }
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(fmtDateMed(LocalDate.ofEpochDay(m.day)), style = Tx.Body)
                        Text(
                            (prev?.let { "${fmtDelta(m.weight - it.weight)} lb vs. anterior" } ?: "Punto de partida") +
                                (m.waist?.let { ", cintura ${fmtNum(it)} cm" } ?: ""),
                            style = Tx.Small,
                        )
                    }
                    Text(fmtNum(m.weight), style = Tx.NumSmall)
                    if (m.day != sprint.startDay) {
                        IconButton(onClick = { store.update { d -> d.copy(measurements = d.measurements.filter { it.day != m.day }) } }) {
                            Icon(Lucide.Trash, contentDescription = "Borrar", tint = Pc.TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        HGap(48)
                    }
                }
                if (i < list.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Pc.Border))
            }
        }
    }
}

@Composable
private fun StrengthSection(data: AppData, sprint: Sprint) {
    val items = trackedExercises.mapNotNull { ex ->
        val list = data.loads[ex.id].orEmpty().filter { it.day >= sprint.startDay }.sortedBy { it.day }
        if (list.isEmpty()) null else ex to list
    }
    if (items.isEmpty()) {
        PCard {
            Text("Todavía no hay registros", style = Tx.H3)
            Gap(6)
            Text(
                "Durante el entrenamiento, anota el peso que usas en cada ejercicio de máquina. Aquí vas a ver cómo sube a lo largo del sprint.",
                style = Tx.BodySecondary,
            )
        }
        return
    }
    val improved = items.count { (_, l) -> l.last().value > l.first().value }
    Text("$improved de ${items.size}", style = Tx.Display.copy(fontSize = 44.sp, lineHeight = 48.sp))
    Text("ejercicios con más peso que al inicio", style = Tx.BodySecondary)
    Gap(20)
    items.forEach { (ex, l) ->
        val first = l.first().value
        val last = l.last().value
        val delta = last - first
        PCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ex.name, style = Tx.H3, modifier = Modifier.weight(1f))
                Text(
                    "${fmtDelta(delta)} lb",
                    style = Tx.NumSmall.copy(color = if (delta > 0.05) Pc.Accent else Pc.TextSecondary),
                )
            }
            Gap(10)
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Inicio", style = Tx.Small)
                    Text("${fmtNum(first)} lb", style = Tx.NumSmall)
                }
                Column(Modifier.weight(1f)) {
                    Text("Actual", style = Tx.Small)
                    Text("${fmtNum(last)} lb", style = Tx.NumSmall)
                }
                Column(Modifier.weight(1f)) {
                    Text("Registros", style = Tx.Small)
                    Text("${l.size}", style = Tx.NumSmall)
                }
            }
            Gap(12)
            if (l.size >= 2) {
                LineChart(l.map { it.day to it.value }, Modifier.fillMaxWidth().height(120.dp), minRange = 4.0)
            } else {
                Text("La gráfica aparece desde tu segundo registro.", style = Tx.Small)
            }
        }
        Gap(12)
    }
}

@Composable
fun LineChart(points: List<Pair<Long, Double>>, modifier: Modifier, minRange: Double = 4.0) {
    val tm = rememberTextMeasurer()
    val labelStyle = TextStyle(color = Pc.TextSecondary, fontSize = 11.sp, fontFamily = Inter)
    Canvas(modifier) {
        val vs = points.map { it.second }
        var min = vs.min()
        var max = vs.max()
        if (max - min < minRange) {
            val c = (max + min) / 2
            min = c - minRange / 2
            max = c + minRange / 2
        }
        val padL = 36.dp.toPx()
        val padT = 8.dp.toPx()
        val padB = 8.dp.toPx()
        val w = size.width - padL - 6.dp.toPx()
        val h = size.height - padT - padB
        val d0 = points.first().first
        val span = maxOf(1L, points.last().first - d0)
        for (i in 0..2) {
            val y = padT + h * i / 2f
            drawLine(Pc.Border, Offset(padL, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            val v = max - (max - min) * i / 2.0
            drawText(tm, fmtNum(v), topLeft = Offset(0f, y - 8.dp.toPx()), style = labelStyle)
        }
        val pts = points.map { (day, v) ->
            Offset(
                padL + w * ((day - d0).toFloat() / span),
                padT + h * ((max - v) / (max - min)).toFloat(),
            )
        }
        val path = Path()
        pts.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
        drawPath(path, Pc.Accent, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        pts.forEach {
            drawCircle(Pc.Surface, radius = 4.dp.toPx(), center = it)
            drawCircle(Pc.Accent, radius = 4.dp.toPx(), center = it, style = Stroke(width = 2.dp.toPx()))
        }
    }
}

@Composable
fun EditStartDialog(store: Store, sprint: Sprint, onDismiss: () -> Unit) {
    var weight by remember { mutableStateOf(fmtNum(sprint.startWeight)) }
    var waist by remember { mutableStateOf(sprint.startWaist?.let { fmtNum(it) } ?: "") }
    val w = weight.toNum()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pc.Surface,
        title = { Text("Peso inicial", style = Tx.H2) },
        text = {
            Column {
                Text("Corrige tu punto de partida. Se actualiza también la medición del día 0.", style = Tx.BodySecondary)
                Gap(14)
                NumberField(weight, { weight = it }, "Peso inicial", "lb")
                Gap(10)
                NumberField(waist, { waist = it }, "Cintura inicial (opcional)", "cm")
            }
        },
        confirmButton = {
            TextButton(
                enabled = w != null && w > 50,
                onClick = {
                    val nw = w ?: sprint.startWeight
                    val nWaist = waist.toNum()
                    store.update { d ->
                        val s = d.sprint ?: return@update d
                        val others = d.measurements.filter { it.day != s.startDay }
                        d.copy(
                            sprint = s.copy(startWeight = nw, startWaist = nWaist),
                            measurements = (others + Measurement(s.startDay, nw, nWaist)).sortedBy { it.day },
                        )
                    }
                    onDismiss()
                },
            ) { Text("Guardar", style = Tx.Button.copy(color = if (w != null && w > 50) Pc.Accent else Pc.TextSecondary)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", style = Tx.Button.copy(color = Pc.TextSecondary)) } },
    )
}
