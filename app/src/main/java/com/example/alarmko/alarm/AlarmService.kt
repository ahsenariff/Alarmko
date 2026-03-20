package com.example.alarmko.alarm
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.alarmko.R

class AlarmService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: android.media.MediaPlayer? = null

    companion object {
        const val CHANNEL_ID = "alarmko_alarm_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        if (alarmId == -1) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification(alarmId)
        startForeground(NOTIFICATION_ID, notification)
        startRingtone()
        showAlarmScreen(alarmId)

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
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(alarmId: Int): Notification {
        val openIntent = Intent(this, AlarmService::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, alarmId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(getString(R.string.alarm_label))
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(pendingIntent, true)
            .build()
    }

    private fun showAlarmScreen(alarmId: Int) {
        val intent = Intent(this, Class.forName(
            "com.example.alarmko.ui.AlarmRingActivity"
        )).apply {
            putExtra("ALARM_ID", alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        startActivity(intent)
    }

    private fun startRingtone() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = android.media.MediaPlayer().apply {
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
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Alarmko::AlarmWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // максимум 10 минути
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}