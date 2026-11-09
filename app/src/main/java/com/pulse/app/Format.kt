package com.pulse.app

import java.time.LocalDate

val MONTHS = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
val MONTHS_SHORT = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
val DAYS_FULL = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
val DAYS_SHORT = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
val DAY_LETTERS = listOf("L", "M", "M", "J", "V", "S", "D")

fun fmtDateShort(d: LocalDate) = "${DAYS_SHORT[d.dayOfWeek.value - 1]} ${d.dayOfMonth} ${MONTHS_SHORT[d.monthValue - 1]}"
fun fmtDateLong(d: LocalDate) = "${DAYS_FULL[d.dayOfWeek.value - 1]} ${d.dayOfMonth} de ${MONTHS[d.monthValue - 1]}"
fun fmtDateMed(d: LocalDate) = "${d.dayOfMonth} ${MONTHS_SHORT[d.monthValue - 1]} ${d.year}"

fun fmtTime(h: Int, m: Int): String {
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "$h12:${m.toString().padStart(2, '0')} ${if (h < 12) "a. m." else "p. m."}"
}

fun fmtSecs(s: Int): String = "${s / 60}:${(s % 60).toString().padStart(2, '0')}"

fun fmtNum(d: Double): String {
    val r = Math.round(d * 10) / 10.0
    return if (r % 1.0 == 0.0) r.toLong().toString() else r.toString()
}

fun fmtDelta(d: Double): String = when {
    d > 0.05 -> "+${fmtNum(d)}"
    d < -0.05 -> "−${fmtNum(-d)}"
    else -> "0"
}

fun String.toNum(): Double? = replace(',', '.').trim().toDoubleOrNull()
