package com.pulse.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        Reminders.createChannel(this)
        val store = Store(applicationContext)
        Reminders.scheduleAll(applicationContext, store.data)
        setContent { PulseTheme { PulseApp(store) } }
    }
}

enum class Tab(val label: String) { HOME("Inicio"), CAL("Calendario"), TRAIN("Entrenar"), MEAS("Progreso"), SET("Ajustes") }

private fun Tab.icon(): ImageVector = when (this) {
    Tab.HOME -> Lucide.House
    Tab.CAL -> Lucide.CalendarDays
    Tab.TRAIN -> Lucide.Dumbbell
    Tab.MEAS -> Lucide.TrendingDown
    Tab.SET -> Lucide.Settings
}

@Composable
fun PulseApp(store: Store) {
    var splash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1400)
        splash = false
    }
    val screen = when {
        splash -> 0
        store.data.sprint == null -> 1
        else -> 2
    }
    Box(Modifier.fillMaxSize().background(Pc.Background)) {
        Crossfade(targetState = screen, animationSpec = tween(350), label = "root") { s ->
            when (s) {
                0 -> SplashScreen()
                1 -> OnboardingScreen(store)
                else -> MainScaffold(store)
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Column(
        Modifier.fillMaxSize().background(Pc.Background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PulseLogo(40.sp)
        Gap(16)
        Text("PRECISION. DISCIPLINE. PROGRESS.", style = Tx.Small.copy(letterSpacing = 2.sp))
    }
}

@Composable
fun MainScaffold(store: Store) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var openMeasure by remember { mutableIntStateOf(0) }
    Scaffold(
        containerColor = Pc.Background,
        bottomBar = { BottomBar(tab) { tab = it } },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                Tab.HOME -> DashboardScreen(store, onTrain = { tab = Tab.TRAIN }, onMeasure = { openMeasure++; tab = Tab.MEAS })
                Tab.CAL -> CalendarScreen(store)
                Tab.TRAIN -> WorkoutScreen(store, onExit = { tab = Tab.HOME })
                Tab.MEAS -> MeasurementsScreen(store, openFormSignal = openMeasure)
                Tab.SET -> SettingsScreen(store)
            }
        }
    }
}

@Composable
fun BottomBar(current: Tab, onSelect: (Tab) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Pc.Background)) {
        HorizontalDivider(color = Pc.Border, thickness = 1.dp)
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp)) {
            Tab.entries.forEach { t ->
                val sel = t == current
                val color = if (sel) Pc.Accent else Pc.TextSecondary
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(t) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(t.icon(), contentDescription = t.label, tint = color, modifier = Modifier.size(22.dp))
                    Gap(4)
                    Text(t.label, style = Tx.Nav.copy(color = color))
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(store: Store) {
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(19) }
    var minute by remember { mutableIntStateOf(30) }
    var picker by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val w = weight.toNum()
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Gap(32)
        PulseLogo(22.sp)
        Gap(48)
        Text("Tu sprint de 90 días empieza hoy.", style = Tx.H1.copy(fontSize = 34.sp, lineHeight = 38.sp))
        Gap(12)
        Text(
            "Registra tu punto de partida. PULSE te recordará cada entrenamiento y te avisará los días de pesarte: hoy, al día 30, al 60 y al 90.",
            style = Tx.BodySecondary,
        )
        Gap(32)
        NumberField(weight, { weight = it }, "Peso inicial", "lb")
        Gap(12)
        NumberField(waist, { waist = it }, "Cintura a la altura del ombligo (opcional)", "cm")
        Gap(24)
        Text("¿A qué hora vas a entrenar?", style = Tx.H3)
        Gap(10)
        TimeButton(hour, minute, onClick = { picker = true })
        Gap(40)
        PrimaryButton(
            "Comenzar sprint",
            enabled = w != null && w > 50,
            onClick = {
                val today = LocalDate.now().toEpochDay()
                val wst = waist.toNum()
                store.update { d ->
                    d.copy(
                        sprint = Sprint(today, 90, w ?: 0.0, wst),
                        measurements = (d.measurements.filter { it.day != today } + Measurement(today, w ?: 0.0, wst)).sortedBy { it.day },
                        settings = d.settings.copy(workout = Reminder(true, hour, minute)),
                    )
                }
                if (Build.VERSION.SDK_INT >= 33) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
        )
        Gap(32)
    }
    if (picker) {
        TimeDialog(hour, minute, onDismiss = { picker = false }) { h, m ->
            hour = h
            minute = m
            picker = false
        }
    }
}
