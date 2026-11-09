package com.pulse.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun MeasurementsScreen(store: Store, openFormSignal: Int) {
    val data = store.data
    val sprint = data.sprint ?: return
    val today = LocalDate.now()
    var showForm by remember { mutableStateOf(false) }
    LaunchedEffect(openFormSignal) { if (openFormSignal > 0) showForm = true }
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    val list = data.measurements.filter { it.day >= sprint.startDay }.sortedBy { it.day }
    val latest = list.lastOrNull()
    val w = weight.toNum()

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        ScreenTitle("Mediciones", "Peso en libras y cintura en centímetros")

        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Inicial", style = Tx.Label)
                Text(fmtNum(sprint.startWeight), style = Tx.Display.copy(fontSize = 44.sp, lineHeight = 48.sp))
            }
            Column(Modifier.weight(1f)) {
                Text("Actual", style = Tx.Label)
                Text(latest?.let { fmtNum(it.weight) } ?: "–", style = Tx.Display.copy(fontSize = 44.sp, lineHeight = 48.sp))
            }
        }
        if (latest != null) {
            val d = latest.weight - sprint.startWeight
            Gap(6)
            Text(
                "${fmtDelta(d)} lb desde el inicio" + (if (latest.waist != null && sprint.startWaist != null) ", cintura ${fmtDelta(latest.waist - sprint.startWaist)} cm" else ""),
                style = Tx.Body.copy(color = if (d < -0.05) Pc.Accent else Pc.TextSecondary),
            )
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
                WeightChart(list, Modifier.fillMaxWidth().height(180.dp))
            } else {
                Text("La gráfica aparece desde tu segunda medición.", style = Tx.BodySecondary)
            }
        }
        Gap(20)

        Text("Fechas de medición", style = Tx.H3)
        Gap(10)
        PCard {
            sprint.measureDates().forEachIndexed { i, d ->
                val m = list.firstOrNull { it.day == d.toEpochDay() }
                val past = !d.isAfter(today)
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (m != null) Pc.Accent else Pc.Surface)
                            .border(1.dp, if (m != null) Pc.Accent else Pc.TextSecondary, androidx.compose.foundation.shape.CircleShape),
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
                if (i < sprint.measureDates().lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Pc.Border))
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
                        Text("${fmtNum(m.weight)}", style = Tx.NumSmall)
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
        Gap(24)
    }
}

@Composable
fun WeightChart(points: List<Measurement>, modifier: Modifier) {
    val tm = rememberTextMeasurer()
    val labelStyle = TextStyle(color = Pc.TextSecondary, fontSize = 11.sp, fontFamily = Inter)
    Canvas(modifier) {
        val ws = points.map { it.weight }
        var min = ws.min()
        var max = ws.max()
        if (max - min < 4) {
            val c = (max + min) / 2
            min = c - 2
            max = c + 2
        }
        val padL = 36.dp.toPx()
        val padT = 8.dp.toPx()
        val padB = 8.dp.toPx()
        val w = size.width - padL - 6.dp.toPx()
        val h = size.height - padT - padB
        val d0 = points.first().day
        val span = maxOf(1L, points.last().day - d0)
        for (i in 0..2) {
            val y = padT + h * i / 2f
            drawLine(Pc.Border, Offset(padL, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            val v = max - (max - min) * i / 2.0
            drawText(tm, fmtNum(v), topLeft = Offset(0f, y - 8.dp.toPx()), style = labelStyle)
        }
        val pts = points.map {
            Offset(
                padL + w * ((it.day - d0).toFloat() / span),
                padT + h * ((max - it.weight) / (max - min)).toFloat(),
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
fun SettingsScreen(store: Store) {
    val ctx = LocalContext.current
    val data = store.data
    val sprint = data.sprint ?: return
    var granted by remember { mutableStateOf(Reminders.canNotify(ctx)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    var confirmReset by remember { mutableStateOf(false) }
    val s = data.settings

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        ScreenTitle("Ajustes")

        if (!granted) {
            PCard {
                Text("Las notificaciones están apagadas", style = Tx.H3.copy(color = Pc.Warning))
                Gap(4)
                Text("Sin ellas PULSE no puede recordarte entrenar ni pesarte.", style = Tx.BodySecondary)
                Gap(14)
                PrimaryButton("Permitir notificaciones", icon = Lucide.Bell, onClick = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        val i = Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(AndroidSettings.EXTRA_APP_PACKAGE, ctx.packageName)
                        ctx.startActivity(i)
                    }
                })
            }
            Gap(20)
        }

        Text("Recordatorios", style = Tx.H3)
        Gap(10)
        ReminderCard("Entrenamiento", "Te avisa los días que te toca entrenar.", s.workout,
            onChange = { r -> store.update { d -> d.copy(settings = d.settings.copy(workout = r)) } }) {
            Gap(14)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 1..7) {
                    val on = i in s.workoutDays
                    Chip(DAY_LETTERS[i - 1], selected = on) {
                        store.update { d ->
                            val days = if (on) d.settings.workoutDays - i else d.settings.workoutDays + i
                            d.copy(settings = d.settings.copy(workoutDays = days))
                        }
                    }
                }
            }
        }
        Gap(12)
        ReminderCard("Día de pesarte", "Al inicio y cada 30 días del sprint.", s.weigh,
            onChange = { r -> store.update { d -> d.copy(settings = d.settings.copy(weigh = r)) } })
        Gap(12)
        ReminderCard("Caminar después de cenar", "Un recordatorio diario de 10 a 15 minutos.", s.walk,
            onChange = { r -> store.update { d -> d.copy(settings = d.settings.copy(walk = r)) } })
        Gap(12)
        SecondaryButton("Enviar notificación de prueba", icon = Lucide.Bell, onClick = {
            Reminders.show(ctx, Reminders.WORKOUT, store.data, test = true)
        })
        Gap(28)

        Text("Sprint actual", style = Tx.H3)
        Gap(10)
        PCard {
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Inicio", style = Tx.Small)
                    Text(fmtDateMed(sprint.startDate()), style = Tx.NumSmall)
                }
                Column(Modifier.weight(1f)) {
                    Text("Fin", style = Tx.Small)
                    Text(fmtDateMed(sprint.endDate()), style = Tx.NumSmall)
                }
            }
            Gap(16)
            SecondaryButton("Reiniciar sprint", icon = Lucide.RotateCcw, onClick = { confirmReset = true })
        }
        Gap(28)

        Text("Si el teléfono tiene ahorro de batería agresivo, quita a PULSE de la optimización de batería para que los recordatorios lleguen a tiempo.", style = Tx.Small)
        Gap(10)
        SecondaryButton("Abrir ajustes de la app", onClick = {
            val i = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ctx.packageName))
            ctx.startActivity(i)
        })
        Gap(28)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            PulseLogo(16.sp)
            Gap(8)
            Text("PRECISION. DISCIPLINE. PROGRESS.", style = Tx.Small.copy(letterSpacing = 2.sp))
        }
        Gap(28)
    }

    if (confirmReset) {
        ConfirmDialog(
            "Reiniciar sprint",
            "Se guardará el resumen del sprint actual y volverás a la pantalla de inicio para registrar tu peso de partida.",
            "Reiniciar",
            onDismiss = { confirmReset = false },
            onConfirm = {
                confirmReset = false
                val sum = sprintSummary(store.data, LocalDate.now())
                store.update { d -> d.copy(sprint = null, history = d.history + listOfNotNull(sum)) }
            },
        )
    }
}

@Composable
private fun ReminderCard(
    title: String,
    desc: String,
    r: Reminder,
    onChange: (Reminder) -> Unit,
    extra: @Composable () -> Unit = {},
) {
    var picker by remember { mutableStateOf(false) }
    PCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = Tx.H3)
                Gap(2)
                Text(desc, style = Tx.BodySecondary)
            }
            HGap(12)
            Switch(
                checked = r.enabled,
                onCheckedChange = { onChange(r.copy(enabled = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Pc.OnAccent,
                    checkedTrackColor = Pc.Accent,
                    checkedBorderColor = Pc.Accent,
                    uncheckedThumbColor = Pc.TextSecondary,
                    uncheckedTrackColor = Pc.SurfaceElevated,
                    uncheckedBorderColor = Pc.Border,
                ),
            )
        }
        if (r.enabled) {
            Gap(12)
            TimeButton(r.hour, r.minute, onClick = { picker = true })
            extra()
        }
    }
    if (picker) {
        TimeDialog(r.hour, r.minute, onDismiss = { picker = false }) { h, m ->
            onChange(r.copy(hour = h, minute = m))
            picker = false
        }
    }
}
