package com.example.alarmko.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.alarmko.R
import com.example.alarmko.ui.AlarmRingActivity

class AlarmService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val CHANNEL_ID = "alarmko_alarm_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val hourOverride = intent?.getIntExtra("ALARM_HOUR_OVERRIDE", -1) ?: -1
        val minuteOverride = intent?.getIntExtra("ALARM_MINUTE_OVERRIDE", -1) ?: -1

        if (alarmId == -1) {
            stopSelf()
            return START_NOT_STICKY
        }

        acquireWakeLock()
        val notification = buildNotification(alarmId, hourOverride, minuteOverride)
        startForeground(NOTIFICATION_ID, notification)
        startRingtone()
        showAlarmScreen(alarmId, hourOverride, minuteOverride)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        releaseWakeLock()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(alarmId: Int, hour: Int = -1, minute: Int = -1): Notification {
        val fullScreenIntent = Intent(this, AlarmRingActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            if (hour != -1) putExtra("ALARM_HOUR_OVERRIDE", hour)
            if (minute != -1) putExtra("ALARM_MINUTE_OVERRIDE", minute)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(getString(R.string.alarm_label))
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun showAlarmScreen(alarmId: Int, hour: Int = -1, minute: Int = -1) {
        val intent = Intent(this, AlarmRingActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            if (hour != -1) putExtra("ALARM_HOUR_OVERRIDE", hour)
            if (minute != -1) putExtra("ALARM_MINUTE_OVERRIDE", minute)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun startRingtone() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        @Suppress("DEPRECATION")
        val lockLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            PowerManager.PARTIAL_WAKE_LOCK
        } else {
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE
        }

        wakeLock = powerManager.newWakeLock(
            lockLevel,
            "Alarmko::AlarmWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}