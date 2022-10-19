package com.example.foregroundserviceexample

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.foregroundserviceexample.databinding.LayoutCustomNotificationBinding

class MyService: Service() {

    override fun onCreate() {
        super.onCreate()
        Log.e("MyService", "Service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            val strDataIntent = it.getStringExtra("key_data_intent");
            val bundle = it.getBundleExtra("bundle")
            val song = bundle?.getSerializable("song") as Song

            Log.e("MyService", song.toString())

            sendNotification(song)
        }

        return START_NOT_STICKY
    }

    private fun sendNotification(song: Song?) {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE)

        val remoteView = RemoteViews(packageName, R.layout.layout_custom_notification)
        remoteView.setTextViewText(R.id.tv_name, song?.name)
        remoteView.setTextViewText(R.id.tv_artist, song?.artist)

        val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCustomContentView(remoteView)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e("MyService", "Service onDestroy")

    }
}