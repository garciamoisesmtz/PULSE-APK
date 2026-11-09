package com.pulse.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

@Composable
fun DashboardScreen(store: Store, onTrain: () -> Unit, onMeasure: () -> Unit, onSettings: () -> Unit) {
    val data = store.data
    val sprint = data.sprint ?: return
    val today = LocalDate.now()
    val idx = sprint.dayIndex(today)
    val finished = idx >= sprint.lengthDays
    val dayNum = (idx + 1).coerceIn(1L, sprint.lengthDays.toLong())
    val type = sessionFor(today, sprint)
    val doneToday = data.done.contains(today.toEpochDay())
    val latest = data.measurements.filter { it.day >= sprint.startDay }.maxByOrNull { it.day }
    val str = streak(data, today)
    val comp = compliance(data, today)
    var confirmNew by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Gap(8)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulseLogo(18.sp)
            Spacer(Modifier.weight(1f))
            Text(fmtDateShort(today), style = Tx.Label)
            IconButton(onClick = onSettings) {
                Icon(Lucide.Settings, contentDescription = "Ajustes", tint = Pc.TextSecondary, modifier = Modifier.size(22.dp))
            }
        }
        Gap(24)
        Text(if (finished) "Sprint completado" else "Sprint de ${sprint.lengthDays} días", style = Tx.Label)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(if (finished) "${sprint.lengthDays}" else "Día $dayNum", style = Tx.Display)
            HGap(8)
            Text(if (finished) "días" else "de ${sprint.lengthDays}", style = Tx.H3.copy(color = Pc.TextSecondary), modifier = Modifier.padding(bottom = 10.dp))
        }
        Gap(14)
        ProgressLine(if (finished) 1f else dayNum.toFloat() / sprint.lengthDays)
        Gap(28)

        if (finished) {
            val sum = sprintSummary(data, today)
            PCard {
                Text("Resultado del sprint", style = Tx.H2)
                Gap(16)
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("Inicio", style = Tx.Small)
                        Text("${fmtNum(sprint.startWeight)} lb", style = Tx.Num)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Final", style = Tx.Small)
                        Text(sum?.endWeight?.let { "${fmtNum(it)} lb" } ?: "Pendiente", style = Tx.Num)
                    }
                }
                Gap(12)
                val delta = sum?.endWeight?.let { it - sprint.startWeight }
                Text(
                    buildString {
                        if (delta != null) append("Cambio: ${fmtDelta(delta)} lb. ")
                        if (sum?.compliance != null) append("Cumpliste el ${sum.compliance}% de los entrenamientos.")
                    }.ifBlank { "Registra tu peso final en Medidas para cerrar el sprint." },
                    style = Tx.BodySecondary,
                )
                Gap(16)
                PrimaryButton("Iniciar un nuevo sprint", onClick = { confirmNew = true })
            }
            Gap(16)
        }

        if (!finished && type != null) {
            PCard {
                Text("Hoy", style = Tx.Label)
                Gap(6)
                Text(type.title, style = Tx.H1)
                Gap(4)
                val count = buildSession(type, sprint.week(today)).sumOf { it.items.size }
                Text(
                    when {
                        type == SessionType.DESCANSO -> "Descansar también es parte del plan. Si quieres, sal a caminar con tu familia."
                        doneToday -> "Completado. Buen trabajo."
                        else -> "$count ejercicios, unos ${type.minutes} minutos."
                    },
                    style = Tx.BodySecondary,
                )
                if (type != SessionType.DESCANSO) {
                    Gap(16)
                    if (doneToday) SecondaryButton("Ver rutina", onClick = onTrain, icon = Lucide.Check)
                    else PrimaryButton("Empezar entrenamiento", onClick = onTrain, icon = Lucide.Play)
                }
            }
            Gap(16)
        }

        if (sprint.isMeasureDay(today) && data.measurements.none { it.day == today.toEpochDay() }) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(Pc.Surface)
                    .border(1.dp, Pc.Accent, CardShape)
                    .clickable(onClick = onMeasure)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Lucide.Scale, contentDescription = null, tint = Pc.Accent, modifier = Modifier.size(22.dp))
                HGap(14)
                Column(Modifier.weight(1f)) {
                    Text("Hoy es día de pesarte", style = Tx.H3)
                    Text("En ayunas, después de ir al baño.", style = Tx.BodySecondary)
                }
                Icon(Lucide.ChevronRight, contentDescription = null, tint = Pc.TextSecondary, modifier = Modifier.size(20.dp))
            }
            Gap(16)
        }

        Row(Modifier.fillMaxWidth()) {
            StatTile("$str", "Racha", Modifier.weight(1f), unit = if (str == 1) "día" else "días")
            HGap(10)
            StatTile(comp?.toString() ?: "–", "Cumplimiento", Modifier.weight(1f), unit = if (comp != null) "%" else null)
        }
        Gap(16)

        PCard {
            Text("Peso", style = Tx.Label)
            Gap(10)
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("${fmtNum(sprint.startWeight)}", style = Tx.Num)
                    Text("Inicial, lb", style = Tx.Small)
                }
                Column(Modifier.weight(1f)) {
                    Text(latest?.let { fmtNum(it.weight) } ?: "–", style = Tx.Num)
                    Text("Actual, lb", style = Tx.Small)
                }
                Column(Modifier.weight(1f)) {
                    val d = latest?.let { it.weight - sprint.startWeight }
                    Text(d?.let { fmtDelta(it) } ?: "–", style = Tx.Num.copy(color = if (d != null && d < -0.05) Pc.Accent else Pc.TextPrimary))
                    Text("Cambio, lb", style = Tx.Small)
                }
            }
            val next = sprint.measureDates().firstOrNull { it.isAfter(today) }
            if (next != null) {
                Gap(14)
                Text("Próxima medición: ${fmtDateLong(next)}", style = Tx.BodySecondary)
            }
        }
        Gap(16)

        PCard {
            Text("Esta semana", style = Tx.Label)
            Gap(12)
            WeekStrip(data, sprint, today)
        }
        Gap(24)
    }

    if (confirmNew) {
        ConfirmDialog(
            "Nuevo sprint",
            "Se guardará el resumen de este sprint y empezarás uno nuevo con tu peso de hoy.",
            "Empezar de nuevo",
            onDismiss = { confirmNew = false },
            onConfirm = {
                confirmNew = false
                val sum = sprintSummary(data, today)
                store.update { d -> d.copy(sprint = null, history = d.history + listOfNotNull(sum)) }
            },
        )
    }
}

@Composable
fun WeekStrip(data: AppData, sprint: Sprint, today: LocalDate) {
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        for (i in 0..6) {
            val d = monday.plusDays(i.toLong())
            val t = sessionFor(d, sprint)
            val done = data.done.contains(d.toEpochDay())
            val isToday = d == today
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DAY_LETTERS[i], style = Tx.Small.copy(color = if (isToday) Pc.TextPrimary else Pc.TextSecondary))
                Gap(6)
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (done) Pc.Accent else Pc.SurfaceElevated)
                        .border(if (isToday && !done) 1.5.dp else 1.dp, if (isToday) Pc.Accent else Pc.Border, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) Icon(Lucide.Check, contentDescription = "Hecho", tint = Pc.OnAccent, modifier = Modifier.size(16.dp))
                    else Text(t?.short ?: "", style = Tx.Small.copy(color = Pc.TextSecondary, fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(store: Store) {
    val data = store.data
    val sprint = data.sprint
    val today = LocalDate.now()
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf(today) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        ScreenTitle("Calendario")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${MONTHS[month.monthValue - 1].replaceFirstChar { it.uppercase() }} ${month.year}", style = Tx.H2, modifier = Modifier.weight(1f))
            IconButton(onClick = { month = month.minusMonths(1) }) {
                Icon(Lucide.ChevronLeft, contentDescription = "Mes anterior", tint = Pc.TextPrimary)
            }
            IconButton(onClick = { month = month.plusMonths(1) }) {
                Icon(Lucide.ChevronRight, contentDescription = "Mes siguiente", tint = Pc.TextPrimary)
            }
        }
        Gap(12)
        Row(Modifier.fillMaxWidth()) {
            DAY_LETTERS.forEach { l ->
                Text(l, style = Tx.Small, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Gap(8)
        val first = month.atDay(1)
        val offset = first.dayOfWeek.value - 1
        val days = month.lengthOfMonth()
        val rows = (offset + days + 6) / 7
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (c in 0..6) {
                    val n = r * 7 + c - offset + 1
                    if (n in 1..days) {
                        val d = month.atDay(n)
                        DayCell(d, data, today, d == selected, Modifier.weight(1f)) { selected = d }
                    } else {
                        Spacer(Modifier.weight(1f).aspectRatio(0.9f))
                    }
                }
            }
        }
        Gap(14)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Pc.Accent))
            HGap(6); Text("Hecho", style = Tx.Small); HGap(16)
            Box(Modifier.size(7.dp).clip(CircleShape).border(1.dp, Pc.TextSecondary, CircleShape))
            HGap(6); Text("Pendiente", style = Tx.Small); HGap(16)
            Box(Modifier.width(12.dp).size(width = 12.dp, height = 2.dp).background(Pc.Accent))
            HGap(6); Text("Medición", style = Tx.Small)
        }
        Gap(20)

        val t = sessionFor(selected, sprint)
        val done = data.done.contains(selected.toEpochDay())
        val m = data.measurements.firstOrNull { it.day == selected.toEpochDay() }
        PCard {
            Text(fmtDateLong(selected), style = Tx.H2)
            Gap(6)
            Text(
                when {
                    t == null -> "Fuera del sprint."
                    sprint != null -> "Día ${sprint.dayIndex(selected) + 1}: ${t.title}"
                    else -> t.title
                },
                style = Tx.BodySecondary,
            )
            if (sprint != null && sprint.isMeasureDay(selected)) {
                Gap(6)
                Text(
                    if (m != null) "Medición: ${fmtNum(m.weight)} lb" + (m.waist?.let { ", cintura ${fmtNum(it)} cm" } ?: "")
                    else "Día de medición",
                    style = Tx.Body.copy(color = Pc.Accent),
                )
            }
            if (t != null && t != SessionType.DESCANSO && !selected.isAfter(today)) {
                Gap(16)
                if (done) {
                    SecondaryButton("Marcar como no hecho", onClick = {
                        store.update { d -> d.copy(done = d.done - selected.toEpochDay()) }
                    })
                } else {
                    PrimaryButton("Marcar como hecho", icon = Lucide.Check, onClick = {
                        store.update { d -> d.copy(done = d.done + selected.toEpochDay()) }
                    })
                }
            }
        }
        Gap(24)
    }
}

@Composable
private fun DayCell(d: LocalDate, data: AppData, today: LocalDate, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val sprint = data.sprint
    val t = sessionFor(d, sprint)
    val done = data.done.contains(d.toEpochDay())
    val planned = t != null && t != SessionType.DESCANSO
    val measure = sprint?.isMeasureDay(d) == true
    val isToday = d == today
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier
            .aspectRatio(0.9f)
            .padding(2.dp)
            .clip(shape)
            .background(if (selected) Pc.SurfaceElevated else Pc.Background)
            .border(1.dp, if (selected) Pc.Accent else Pc.Background, shape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "${d.dayOfMonth}",
            style = Tx.NumSmall.copy(
                fontSize = 15.sp,
                color = when {
                    isToday -> Pc.Accent
                    t == null -> Pc.TextSecondary.copy(alpha = 0.6f)
                    else -> Pc.TextPrimary
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            ),
        )
        Gap(4)
        Box(Modifier.size(7.dp), contentAlignment = Alignment.Center) {
            when {
                done -> Box(Modifier.size(7.dp).clip(CircleShape).background(Pc.Accent))
                planned -> Box(Modifier.size(7.dp).clip(CircleShape).border(1.dp, Pc.TextSecondary, CircleShape))
                else -> {}
            }
        }
        Gap(3)
        Box(Modifier.size(width = 12.dp, height = 2.dp).background(if (measure) Pc.Accent else Pc.Background.copy(alpha = 0f)))
    }
}
