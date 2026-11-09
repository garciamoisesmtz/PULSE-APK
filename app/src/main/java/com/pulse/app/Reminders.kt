package com.pulse.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object Reminders {
    const val CHANNEL = "pulse_reminders"
    const val WORKOUT = 1
    const val WEIGH = 2
    const val WALK = 3
    private const val EXTRA = "type"

    fun createChannel(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        val ch = NotificationChannel(CHANNEL, "Recordatorios", NotificationManager.IMPORTANCE_HIGH)
        ch.description = "Entrenamiento, pesaje y caminata"
        nm.createNotificationChannel(ch)
    }

    fun canNotify(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(ctx).areNotificationsEnabled()
        }

    fun scheduleAll(ctx: Context, data: AppData) {
        val now = LocalDateTime.now()
        set(ctx, WORKOUT, nextWorkout(data, now))
        set(ctx, WEIGH, nextWeigh(data, now))
        set(ctx, WALK, nextWalk(data, now))
    }

    private fun pending(ctx: Context, type: Int): PendingIntent {
        val i = Intent(ctx, ReminderReceiver::class.java).putExtra(EXTRA, type)
        return PendingIntent.getBroadcast(ctx, type, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun set(ctx: Context, type: Int, at: LocalDateTime?) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        val pi = pending(ctx, type)
        am.cancel(pi)
        if (at == null) return
        val ms = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        try {
            val exact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
            if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, pi)
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, pi)
        }
    }

    fun nextWorkout(data: AppData, now: LocalDateTime): LocalDateTime? {
        val r = data.settings.workout
        val s = data.sprint ?: return null
        if (!r.enabled) return null
        for (i in 0..14) {
            val d = now.toLocalDate().plusDays(i.toLong())
            val t = d.atTime(r.hour, r.minute)
            if (!t.isAfter(now)) continue
            if (d.dayOfWeek.value !in data.settings.workoutDays) continue
            val st = sessionFor(d, s) ?: continue
            if (st == SessionType.DESCANSO) continue
            return t
        }
        return null
    }

    fun nextWeigh(data: AppData, now: LocalDateTime): LocalDateTime? {
        val r = data.settings.weigh
        val s = data.sprint ?: return null
        if (!r.enabled) return null
        return s.measureDates().map { it.atTime(r.hour, r.minute) }.firstOrNull { it.isAfter(now) }
    }

    fun nextWalk(data: AppData, now: LocalDateTime): LocalDateTime? {
        val r = data.settings.walk
        if (!r.enabled || data.sprint == null) return null
        val today = now.toLocalDate().atTime(r.hour, r.minute)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    @SuppressLint("MissingPermission")
    fun show(ctx: Context, type: Int, data: AppData, test: Boolean = false) {
        if (!canNotify(ctx)) return
        val today = LocalDate.now()
        if (!test && type == WORKOUT && data.done.contains(today.toEpochDay())) return
        val (title, text) = when (type) {
            WORKOUT -> {
                val st = sessionFor(today, data.sprint)
                val name = st?.title ?: "tu rutina"
                val mins = st?.minutes ?: 30
                val str = streak(data, today)
                val tail = if (str > 0) " Racha actual: $str días." else ""
                "Hora de entrenar" to "Hoy toca $name, unos $mins minutos.$tail"
            }
            WEIGH -> "Hoy es día de pesarte" to "Pésate en ayunas, después de ir al baño, y registra peso y cintura en PULSE."
            else -> "Camina 10 a 15 minutos" to "Una caminata después de cenar ayuda a bajar el pico de azúcar e insulina."
        }
        val open = PendingIntent.getActivity(
            ctx, 100 + type,
            Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF75CDB0.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(ctx).notify(type, n)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getIntExtra("type", 0)
        val data = loadData(context)
        Reminders.createChannel(context)
        Reminders.show(context, type, data)
        Reminders.scheduleAll(context, data)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminders.createChannel(context)
        Reminders.scheduleAll(context, loadData(context))
    }
}
