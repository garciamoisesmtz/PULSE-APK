package com.pulse.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class Sprint(val startDay: Long, val lengthDays: Int = 90, val startWeight: Double, val startWaist: Double?)
data class Measurement(val day: Long, val weight: Double, val waist: Double?)
data class LoadEntry(val day: Long, val value: Double)
data class Reminder(val enabled: Boolean, val hour: Int, val minute: Int)
data class SprintSummary(val startDay: Long, val endDay: Long, val startWeight: Double, val endWeight: Double?, val compliance: Int?)

data class Settings(
    val workout: Reminder = Reminder(true, 19, 30),
    val workoutDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6),
    val weigh: Reminder = Reminder(true, 5, 15),
    val walk: Reminder = Reminder(false, 19, 0),
)

data class AppData(
    val sprint: Sprint? = null,
    val measurements: List<Measurement> = emptyList(),
    val done: Set<Long> = emptySet(),
    val loads: Map<String, List<LoadEntry>> = emptyMap(),
    val settings: Settings = Settings(),
    val history: List<SprintSummary> = emptyList(),
)

private const val PREFS = "pulse"
private const val KEY = "data"

fun loadData(ctx: Context): AppData = try {
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)?.let { parseAppData(it) } ?: AppData()
} catch (e: Exception) {
    AppData()
}

class Store(private val ctx: Context) {
    var data by mutableStateOf(loadData(ctx))
        private set

    fun update(f: (AppData) -> AppData) {
        data = f(data)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, data.toJson()).apply()
        Reminders.scheduleAll(ctx, data)
    }
}

private fun Reminder.json(): JSONObject = JSONObject().put("enabled", enabled).put("hour", hour).put("minute", minute)
private fun reminderFrom(j: JSONObject?, def: Reminder): Reminder =
    if (j == null) def else Reminder(j.optBoolean("enabled", def.enabled), j.optInt("hour", def.hour), j.optInt("minute", def.minute))

fun AppData.toJson(): String {
    val o = JSONObject()
    sprint?.let { s ->
        val j = JSONObject().put("startDay", s.startDay).put("lengthDays", s.lengthDays).put("startWeight", s.startWeight)
        s.startWaist?.let { j.put("startWaist", it) }
        o.put("sprint", j)
    }
    val ms = JSONArray()
    measurements.forEach { m ->
        val j = JSONObject().put("day", m.day).put("weight", m.weight)
        m.waist?.let { j.put("waist", it) }
        ms.put(j)
    }
    o.put("measurements", ms)
    val d = JSONArray()
    done.forEach { d.put(it) }
    o.put("done", d)
    val lj = JSONObject()
    loads.forEach { (k, list) ->
        val a = JSONArray()
        list.forEach { e -> a.put(JSONObject().put("day", e.day).put("value", e.value)) }
        lj.put(k, a)
    }
    o.put("loads", lj)
    val days = JSONArray()
    settings.workoutDays.forEach { days.put(it) }
    o.put("settings", JSONObject()
        .put("workout", settings.workout.json())
        .put("workoutDays", days)
        .put("weigh", settings.weigh.json())
        .put("walk", settings.walk.json()))
    val h = JSONArray()
    history.forEach { s ->
        val j = JSONObject().put("startDay", s.startDay).put("endDay", s.endDay).put("startWeight", s.startWeight)
        s.endWeight?.let { j.put("endWeight", it) }
        s.compliance?.let { j.put("compliance", it) }
        h.put(j)
    }
    o.put("history", h)
    return o.toString()
}

fun parseAppData(text: String): AppData {
    val o = JSONObject(text)
    val sprint = o.optJSONObject("sprint")?.let { j ->
        Sprint(
            startDay = j.getLong("startDay"),
            lengthDays = j.optInt("lengthDays", 90),
            startWeight = j.getDouble("startWeight"),
            startWaist = if (j.has("startWaist")) j.getDouble("startWaist") else null,
        )
    }
    val ms = mutableListOf<Measurement>()
    o.optJSONArray("measurements")?.let { a ->
        for (i in 0 until a.length()) {
            val j = a.getJSONObject(i)
            ms.add(Measurement(j.getLong("day"), j.getDouble("weight"), if (j.has("waist")) j.getDouble("waist") else null))
        }
    }
    val done = mutableSetOf<Long>()
    o.optJSONArray("done")?.let { a -> for (i in 0 until a.length()) done.add(a.getLong(i)) }
    val loads = mutableMapOf<String, List<LoadEntry>>()
    o.optJSONObject("loads")?.let { lj ->
        val keys = lj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val a = lj.getJSONArray(k)
            loads[k] = (0 until a.length()).map { i ->
                val e = a.getJSONObject(i)
                LoadEntry(e.getLong("day"), e.getDouble("value"))
            }
        }
    }
    val def = Settings()
    val st = o.optJSONObject("settings")
    val settings = if (st == null) def else Settings(
        workout = reminderFrom(st.optJSONObject("workout"), def.workout),
        workoutDays = st.optJSONArray("workoutDays")?.let { a -> (0 until a.length()).map { a.getInt(it) }.toSet() } ?: def.workoutDays,
        weigh = reminderFrom(st.optJSONObject("weigh"), def.weigh),
        walk = reminderFrom(st.optJSONObject("walk"), def.walk),
    )
    val hist = mutableListOf<SprintSummary>()
    o.optJSONArray("history")?.let { a ->
        for (i in 0 until a.length()) {
            val j = a.getJSONObject(i)
            hist.add(SprintSummary(
                j.getLong("startDay"), j.getLong("endDay"), j.getDouble("startWeight"),
                if (j.has("endWeight")) j.getDouble("endWeight") else null,
                if (j.has("compliance")) j.getInt("compliance") else null,
            ))
        }
    }
    return AppData(sprint, ms.sortedBy { it.day }, done, loads, settings, hist)
}
