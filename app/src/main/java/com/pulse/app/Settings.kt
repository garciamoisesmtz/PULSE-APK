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
fun SettingsScreen(store: Store, onBack: (() -> Unit)? = null) {
    val ctx = LocalContext.current
    val data = store.data
    val sprint = data.sprint ?: return
    var granted by remember { mutableStateOf(Reminders.canNotify(ctx)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    var confirmReset by remember { mutableStateOf(false) }
    var editStart by remember { mutableStateOf(false) }
    val s = data.settings

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        if (onBack != null) {
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.padding(start = 0.dp)) {
                    Icon(Lucide.ChevronLeft, contentDescription = "Volver", tint = Pc.TextPrimary)
                }
                Text("Volver", style = Tx.Label)
            }
        }
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

        Text("Entrenamiento", style = Tx.H3)
        Gap(10)
        PCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pitidos del temporizador", style = Tx.H3)
                    Gap(2)
                    Text("Tres pitidos cortos en los últimos 3 segundos y uno largo al terminar. Suenan aunque el teléfono esté en vibración, con el volumen de alarma.", style = Tx.BodySecondary)
                }
                HGap(12)
                Switch(
                    checked = s.sound,
                    onCheckedChange = { on -> store.update { d -> d.copy(settings = d.settings.copy(sound = on)) } },
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
        }
        Gap(28)

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
            SecondaryButton("Editar peso inicial", icon = Lucide.Pencil, onClick = { editStart = true })
            Gap(8)
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

    if (editStart) {
        EditStartDialog(store, sprint, onDismiss = { editStart = false })
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
