package com.pulse.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun WorkoutScreen(store: Store, onExit: () -> Unit) {
    val sprint = store.data.sprint ?: return
    val today = LocalDate.now()
    val scheduled = sessionFor(today, sprint) ?: SessionType.DESCANSO
    var type by remember { mutableStateOf(scheduled) }
    var mode by remember { mutableIntStateOf(0) }
    val week = sprint.week(today).coerceIn(1, 13)
    val blocks = remember(type, week) { buildSession(type, week) }
    val flat = remember(blocks) { blocks.flatMap { b -> b.items.map { b.title to it } } }

    when (mode) {
        0 -> WorkoutOverview(store, type, scheduled, week, blocks, onType = { type = it }, onStart = { mode = 1 })
        1 -> WorkoutRunner(
            flat = flat,
            store = store,
            onFinish = {
                store.update { d -> d.copy(done = d.done + today.toEpochDay()) }
                mode = 2
            },
            onClose = { mode = 0 },
        )
        else -> WorkoutDone(store, onExit)
    }
}

@Composable
private fun WorkoutOverview(
    store: Store,
    type: SessionType,
    scheduled: SessionType,
    week: Int,
    blocks: List<Block>,
    onType: (SessionType) -> Unit,
    onStart: () -> Unit,
) {
    val doneToday = store.data.done.contains(LocalDate.now().toEpochDay())
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            ScreenTitle(type.title, if (type == scheduled) "La rutina de hoy" else "Elegiste otra rutina para hoy")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(SessionType.FUERZA_A, SessionType.FUERZA_B, SessionType.CARDIO, SessionType.ACTIVO).forEach { t ->
                    Chip(if (t == SessionType.CARDIO) "Cardio" else if (t == SessionType.ACTIVO) "Activo" else t.title, selected = t == type) { onType(t) }
                }
            }
            Gap(16)
            if (type == SessionType.DESCANSO) {
                PCard {
                    Text("Hoy toca descansar", style = Tx.H2)
                    Gap(6)
                    Text("Los músculos crecen mientras descansas. Si te sientes con energía, elige una rutina arriba o sal a caminar.", style = Tx.BodySecondary)
                }
            } else {
                Text(weekNote(week), style = Tx.BodySecondary)
                Gap(20)
                blocks.forEach { b ->
                    Text(b.title, style = Tx.Label)
                    Gap(8)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(CardShape)
                            .background(Pc.Surface)
                            .border(1.dp, Pc.Border, CardShape),
                    ) {
                        b.items.forEachIndexed { i, entry ->
                            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Pc.Border))
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.ex.name, style = Tx.Body, modifier = Modifier.weight(1f))
                                Text(prescription(entry), style = Tx.NumSmall.copy(fontSize = 15.sp, color = Pc.TextSecondary))
                            }
                        }
                    }
                    Gap(20)
                }
            }
        }
        if (type != SessionType.DESCANSO) {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                PrimaryButton(if (doneToday) "Repetir entrenamiento" else "Iniciar entrenamiento", onClick = onStart, icon = Lucide.Play)
            }
        }
    }
}

private fun prescription(item: Item): String =
    if (item.ex.kind == Kind.TIMED) {
        if (item.sets > 1) "${item.sets} × ${fmtSecs(item.ex.seconds)}" else fmtSecs(item.ex.seconds)
    } else "${item.sets} × ${item.ex.reps}"

@Composable
private fun WorkoutRunner(flat: List<Pair<String, Item>>, store: Store, onFinish: () -> Unit, onClose: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val progress = remember { mutableStateMapOf<Int, Int>() }
    var rest by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val beeper = remember { Beeper() }
    val sound = store.data.settings.sound
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
            beeper.release()
        }
    }

    val (blockTitle, item) = flat[index]
    val ex = item.ex
    val setsDone = progress[index] ?: 0
    val isLast = index == flat.lastIndex
    val today = LocalDate.now().toEpochDay()

    var timeLeft by remember(index, setsDone) { mutableIntStateOf(ex.seconds) }
    var running by remember(index, setsDone) { mutableStateOf(false) }
    var loadText by remember(index) { mutableStateOf("") }
    val lastLoad = store.data.loads[ex.id]?.lastOrNull()

    fun completeSet() {
        val n = setsDone + 1
        progress[index] = n
        running = false
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (n < item.sets && ex.rest > 0) rest = ex.rest
    }

    fun saveLoad() {
        val v = loadText.toNum() ?: return
        store.update { d ->
            val list = (d.loads[ex.id] ?: emptyList()).filter { it.day != today } + LoadEntry(today, v)
            d.copy(loads = d.loads + (ex.id to list))
        }
    }

    LaunchedEffect(rest) {
        if (rest > 0) {
            delay(1000)
            rest -= 1
            if (sound && rest in 1..3) beeper.short()
            if (rest == 0) {
                if (sound) beeper.long()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }
    LaunchedEffect(running, timeLeft) {
        if (running) {
            if (timeLeft > 0) {
                delay(1000)
                timeLeft -= 1
                if (sound && timeLeft in 1..3) beeper.short()
                if (sound && timeLeft == 0) beeper.long()
            } else {
                completeSet()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().imePadding()) {
            Row(Modifier.padding(start = 8.dp, end = 20.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Lucide.X, contentDescription = "Salir", tint = Pc.TextPrimary)
                }
                Text(blockTitle, style = Tx.Label, modifier = Modifier.weight(1f))
                Text("${index + 1} de ${flat.size}", style = Tx.Label)
            }
            ProgressLine((index + 1f) / flat.size, Modifier.padding(horizontal = 20.dp))
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Gap(16)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Pc.Surface)
                        .border(1.dp, Pc.Border, RoundedCornerShape(14.dp)),
                ) {
                    ExerciseFigure(ex.anim, Modifier.fillMaxSize().padding(12.dp))
                }
                Gap(20)
                Text(ex.name, style = Tx.H1)
                Gap(12)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("${item.sets} ${if (item.sets == 1) "serie" else "series"}")
                    Chip(if (ex.kind == Kind.TIMED) fmtSecs(ex.seconds) else "${ex.reps} reps")
                    if (item.sets > 1 && ex.rest > 0) Chip("${ex.rest} s descanso")
                }
                Gap(20)
                SetDots(item.sets, setsDone)
                if (ex.kind == Kind.TIMED && setsDone < item.sets) {
                    Gap(12)
                    Text(fmtSecs(timeLeft), style = Tx.Timer.copy(color = if (running) Pc.TextPrimary else Pc.TextSecondary))
                }
                if (ex.tracksLoad) {
                    Gap(20)
                    NumberField(loadText, { loadText = it }, "Peso usado en la máquina", "lb")
                    Gap(6)
                    Text(
                        lastLoad?.let { "La última vez usaste ${fmtNum(it.value)} lb." } ?: "Anota el peso para ver tu progreso la próxima vez.",
                        style = Tx.Small,
                    )
                }
                Gap(24)
                Text("Cómo hacerlo", style = Tx.H3)
                Gap(10)
                ex.steps.forEachIndexed { i, s ->
                    Row(Modifier.padding(bottom = 10.dp)) {
                        Text("${i + 1}", style = Tx.NumSmall.copy(fontSize = 15.sp, color = Pc.Accent), modifier = Modifier.width(22.dp))
                        Text(s, style = Tx.Body)
                    }
                }
                Gap(6)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .background(Pc.Surface)
                        .border(1.dp, Pc.Border, CardShape)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Lucide.Target, contentDescription = null, tint = Pc.Accent, modifier = Modifier.size(18.dp))
                    HGap(10)
                    Text(ex.cue, style = Tx.Body)
                }
                Gap(24)
            }

            Column(Modifier.background(Pc.Background).padding(horizontal = 20.dp, vertical = 12.dp)) {
                val allDone = setsDone >= item.sets
                when {
                    allDone -> PrimaryButton(
                        if (isLast) "Terminar entrenamiento" else "Siguiente ejercicio",
                        icon = if (isLast) Lucide.Check else Lucide.ChevronRight,
                        onClick = {
                            saveLoad()
                            rest = 0
                            if (isLast) onFinish() else index++
                        },
                    )
                    ex.kind == Kind.REPS -> PrimaryButton("Serie ${setsDone + 1} completada", icon = Lucide.Check, onClick = { completeSet() })
                    else -> PrimaryButton(
                        when {
                            running -> "Pausar"
                            timeLeft < ex.seconds -> "Continuar"
                            item.sets > 1 -> "Iniciar serie ${setsDone + 1}"
                            else -> "Iniciar"
                        },
                        icon = if (running) Lucide.Pause else Lucide.Play,
                        onClick = { running = !running },
                    )
                }
                Gap(8)
                Row {
                    SecondaryButton("Anterior", enabled = index > 0, modifier = Modifier.weight(1f), onClick = {
                        saveLoad(); index--; rest = 0
                    })
                    HGap(8)
                    SecondaryButton(if (isLast) "Terminar" else "Saltar", modifier = Modifier.weight(1f), onClick = {
                        saveLoad()
                        rest = 0
                        if (isLast) onFinish() else index++
                    })
                }
            }
        }

        if (rest > 0) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Pc.Background.copy(alpha = 0.97f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Descanso", style = Tx.H3.copy(color = Pc.TextSecondary))
                Gap(8)
                Text(fmtSecs(rest), style = Tx.Timer.copy(fontSize = 104.sp, lineHeight = 108.sp))
                Gap(16)
                ProgressLine(rest.toFloat() / ex.rest.coerceAtLeast(1), Modifier.padding(horizontal = 40.dp))
                Gap(16)
                Text("Sigue: serie ${setsDone + 1} de ${item.sets} de ${ex.name.lowercase()}", style = Tx.BodySecondary, textAlign = TextAlign.Center)
                Gap(40)
                Row {
                    SecondaryButton("+15 s", modifier = Modifier.weight(1f), onClick = { rest += 15 })
                    HGap(8)
                    PrimaryButton("Saltar descanso", modifier = Modifier.weight(1.4f), onClick = { rest = 0 })
                }
            }
        }
    }
}

@Composable
private fun SetDots(total: Int, done: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until total) {
            val isDone = i < done
            val current = i == done
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isDone) Pc.Accent else Pc.Surface)
                    .border(1.dp, if (current) Pc.Accent else Pc.Border, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (isDone) Icon(Lucide.Check, contentDescription = null, tint = Pc.OnAccent, modifier = Modifier.size(16.dp))
                else Text("${i + 1}", style = Tx.Small.copy(color = if (current) Pc.TextPrimary else Pc.TextSecondary))
            }
        }
        HGap(4)
        Text(if (done >= total) "Series completas" else "Serie ${done + 1} de $total", style = Tx.Label)
    }
}

@Composable
private fun WorkoutDone(store: Store, onExit: () -> Unit) {
    val today = LocalDate.now()
    val str = streak(store.data, today)
    val comp = compliance(store.data, today)
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(Pc.Accent), contentAlignment = Alignment.Center) {
            Icon(Lucide.Check, contentDescription = null, tint = Pc.OnAccent, modifier = Modifier.size(34.dp))
        }
        Gap(24)
        Text("Entrenamiento completado", style = Tx.H1, textAlign = TextAlign.Center)
        Gap(8)
        Text("Quedó registrado en tu calendario.", style = Tx.BodySecondary)
        Gap(32)
        Row(Modifier.fillMaxWidth()) {
            StatTile("$str", "Racha", Modifier.weight(1f), unit = if (str == 1) "día" else "días")
            HGap(10)
            StatTile(comp?.toString() ?: "–", "Cumplimiento", Modifier.weight(1f), unit = if (comp != null) "%" else null)
        }
        Gap(32)
        PrimaryButton("Volver al inicio", onClick = onExit)
    }
}
