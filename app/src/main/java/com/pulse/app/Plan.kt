package com.pulse.app

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

enum class SessionType(val title: String, val short: String, val minutes: Int) {
    FUERZA_A("Fuerza A", "A", 40),
    FUERZA_B("Fuerza B", "B", 40),
    CARDIO("Cardio y abdomen", "C", 35),
    ACTIVO("Día activo", "+", 30),
    DESCANSO("Descanso", "", 0);

    val isTraining: Boolean get() = this == FUERZA_A || this == FUERZA_B || this == CARDIO
}

const val MEASURE_INTERVAL = 30

fun Sprint.startDate(): LocalDate = LocalDate.ofEpochDay(startDay)
fun Sprint.endDate(): LocalDate = startDate().plusDays(lengthDays.toLong())
fun Sprint.dayIndex(date: LocalDate): Long = date.toEpochDay() - startDay
fun Sprint.isMeasureDay(date: LocalDate): Boolean {
    val i = dayIndex(date)
    return i in 0..lengthDays.toLong() && i % MEASURE_INTERVAL == 0L
}
fun Sprint.measureDates(): List<LocalDate> = (0..lengthDays step MEASURE_INTERVAL).map { startDate().plusDays(it.toLong()) }
fun Sprint.week(date: LocalDate): Int = (dayIndex(date) / 7).toInt() + 1

fun sessionFor(date: LocalDate, sprint: Sprint?): SessionType? {
    if (sprint == null) return null
    val i = sprint.dayIndex(date)
    if (i < 0 || i >= sprint.lengthDays) return null
    val startMon = sprint.startDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val thisMon = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val even = ChronoUnit.WEEKS.between(startMon, thisMon) % 2 == 0L
    return when (date.dayOfWeek) {
        DayOfWeek.MONDAY, DayOfWeek.FRIDAY -> if (even) SessionType.FUERZA_A else SessionType.FUERZA_B
        DayOfWeek.WEDNESDAY -> if (even) SessionType.FUERZA_B else SessionType.FUERZA_A
        DayOfWeek.TUESDAY, DayOfWeek.THURSDAY -> SessionType.CARDIO
        DayOfWeek.SATURDAY -> SessionType.ACTIVO
        else -> SessionType.DESCANSO
    }
}

/** Semanas 1–2: aprendizaje. Semanas 7 y 13: descarga. Resto: 3 series. */
fun setsForWeek(week: Int): Int = if (week <= 2 || week == 7 || week == 13) 2 else 3

fun weekNote(week: Int): String = when {
    week <= 2 -> "Semana $week de adaptación: 2 series con peso cómodo para aprender la técnica."
    week == 7 || week == 13 -> "Semana $week de descarga: 2 series para que el cuerpo se recupere."
    else -> "Semana $week: 3 series. Sube el peso cuando completes todas las repeticiones con 2 de sobra."
}

fun streak(data: AppData, today: LocalDate): Int {
    val s = data.sprint ?: return 0
    var d = if (data.done.contains(today.toEpochDay())) today else today.minusDays(1)
    var count = 0
    while (s.dayIndex(d) >= 0) {
        val t = sessionFor(d, s)
        if (t != null && t.isTraining) {
            if (data.done.contains(d.toEpochDay())) count++ else break
        }
        d = d.minusDays(1)
    }
    return count
}

fun compliance(data: AppData, today: LocalDate): Int? {
    val s = data.sprint ?: return null
    var planned = 0
    var completed = 0
    var d = s.startDate()
    val last = minOf(today, s.endDate().minusDays(1))
    while (!d.isAfter(last)) {
        val t = sessionFor(d, s)
        if (t != null && t.isTraining) {
            val isDone = data.done.contains(d.toEpochDay())
            if (d.isBefore(today) || isDone) {
                planned++
                if (isDone) completed++
            }
        }
        d = d.plusDays(1)
    }
    return if (planned == 0) null else completed * 100 / planned
}

fun sprintSummary(data: AppData, today: LocalDate): SprintSummary? {
    val s = data.sprint ?: return null
    val endM = data.measurements.filter { it.day >= s.endDate().toEpochDay() }.minByOrNull { it.day }
        ?: data.measurements.filter { it.day >= s.startDay }.maxByOrNull { it.day }
    return SprintSummary(s.startDay, s.endDate().toEpochDay(), s.startWeight, endM?.weight, compliance(data, today))
}

enum class Kind { TIMED, REPS }

data class Exercise(
    val id: String,
    val name: String,
    val anim: Anim,
    val kind: Kind,
    val seconds: Int = 0,
    val reps: String = "",
    val rest: Int = 60,
    val tracksLoad: Boolean = false,
    val steps: List<String>,
    val cue: String,
)

data class Item(val ex: Exercise, val sets: Int)
data class Block(val title: String, val items: List<Item>)

fun buildSession(type: SessionType, week: Int): List<Block> {
    val s = setsForWeek(week)
    val warm = Block("Calentamiento", listOf(Item(Ex.marcha, 1), Item(Ex.circulos, 1), Item(Ex.sentadillaLibre, 1), Item(Ex.cadera, 1)))
    return when (type) {
        SessionType.FUERZA_A -> listOf(
            warm,
            Block("Fuerza", listOf(
                Item(Ex.goblet, s), Item(Ex.pressPecho, s), Item(Ex.jalon, s),
                Item(Ex.extension, s), Item(Ex.curlFemoral, s), Item(Ex.plancha, s),
            )),
            Block("Cardio final", listOf(Item(Ex.marchaAlta, 1))),
        )
        SessionType.FUERZA_B -> listOf(
            warm,
            Block("Fuerza", listOf(
                Item(Ex.zancadas, s), Item(Ex.remo, s), Item(Ex.aperturas, s), Item(Ex.laterales, s),
                Item(Ex.curlBiceps, 2), Item(Ex.triceps, 2), Item(Ex.puente, s),
            )),
            Block("Cardio final", listOf(Item(Ex.marchaAlta, 1))),
        )
        SessionType.CARDIO -> listOf(
            warm,
            Block("Cardio", listOf(Item(Ex.caminata, 1))),
            Block("Abdomen", listOf(Item(Ex.plancha, 3), Item(Ex.planchaLateral, 2), Item(Ex.bichoMuerto, 2))),
        )
        SessionType.ACTIVO -> listOf(
            Block("Actividad", listOf(Item(Ex.caminataLarga, 1))),
            Block("Movilidad", listOf(Item(Ex.circulos, 1), Item(Ex.cadera, 1))),
        )
        SessionType.DESCANSO -> emptyList()
    }
}
