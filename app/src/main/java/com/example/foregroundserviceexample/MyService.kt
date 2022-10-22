package com.example.foregroundserviceexample

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.squareup.picasso.Picasso


class MyService : Service() {

    companion object {
        const val ACTION_PAUSE = 9
        const val ACTION_RESUME = 10
        const val ACTION_CLEAR = 11
    }

    private var mediaPlayer: MediaPlayer? = null
    private var mRemoteView: RemoteViews? = null
    private var mNotification: Notification? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.e("MyService", "Service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val bundle = intent?.getBundleExtra("bundle")
        val song = bundle?.getSerializable("song") as Song?
        val actionName = intent?.getIntExtra("action_music_service", 0)

        song?.let {
            startMusic(song)
            sendNotification(song)
        }

        actionName?.let {
            handleActionMusic(actionName)
        }

        return START_NOT_STICKY
    }

    private fun startMusic(song: Song) {

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
        }

        mediaPlayer?.run {
            if (!isPlaying) {
                setDataSource(song.songUrl)
                prepare()
                start()
            }
        }
    }

    private fun pauseMusic(){
        mediaPlayer?.run{
            if(isPlaying){
                pause()
                mRemoteView?.setOnClickPendingIntent(R.id.iv_toggle, getPendingIntent(this@MyService, ACTION_RESUME))
                mRemoteView?.setImageViewResource(R.id.iv_toggle, R.drawable.ic_play)
                with(NotificationManagerCompat.from(this@MyService)) {
                    mNotification?.let {
                        notify(1, it)
                    }
                }
            }
        }
    }

    private fun resumeMusic(){
        mediaPlayer?.run {
            if(!isPlaying){
                start()
                mRemoteView?.setOnClickPendingIntent(R.id.iv_toggle, getPendingIntent(this@MyService, ACTION_PAUSE))
                mRemoteView?.setImageViewResource(R.id.iv_toggle, R.drawable.ic_pause)
                with(NotificationManagerCompat.from(this@MyService)) {
                    mNotification?.let {
                        notify(1, it)
                    }
                }
            }
        }
    }

    private fun handleActionMusic(action: Int){
        when(action){
            ACTION_PAUSE -> pauseMusic()
            ACTION_RESUME -> resumeMusic()
            ACTION_CLEAR -> stopSelf()
        }
    }

    private fun sendNotification(song: Song?) {

//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE)

        mRemoteView = RemoteViews(packageName, R.layout.layout_custom_notification)
        mRemoteView?.run {
            setTextViewText(R.id.tv_name, song?.name)
            setTextViewText(R.id.tv_artist, song?.artist)

            mRemoteView?.setOnClickPendingIntent(R.id.iv_cancel, getPendingIntent(this@MyService, ACTION_CLEAR))

            mNotification = mNotification ?: NotificationCompat.Builder(this@MyService, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setSound(null)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCustomContentView(mRemoteView)
                .build()

            mediaPlayer?.run {
                if(isPlaying){
                    mRemoteView?.setOnClickPendingIntent(R.id.iv_toggle, getPendingIntent(this@MyService, ACTION_PAUSE))
                    mRemoteView?.setImageViewResource(R.id.iv_toggle, R.drawable.ic_pause)
                }else{
                    mRemoteView?.setOnClickPendingIntent(R.id.iv_toggle, getPendingIntent(this@MyService, ACTION_RESUME))
                    mRemoteView?.setImageViewResource(R.id.iv_toggle, R.drawable.ic_play)
                }
            }

            Picasso.get()
                .load(song?.thumbnail)
                .into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        mRemoteView?.setImageViewBitmap(R.id.iv_thumnail, bitmap)
                        with(NotificationManagerCompat.from(this@MyService)) {
                            mNotification?.let {
                                notify(1, it)
                            }
                        }
                    }

                    override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    }

                })

            startForeground(1, mNotification)
        }

    }

    private fun getPendingIntent(context: Context, action: Int) : PendingIntent{
        val intent = Intent(this, MyReceiver::class.java)
        intent.putExtra("action_name", action)

        return PendingIntent.getBroadcast(context.applicationContext, action, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer?.let {
            mediaPlayer?.release()
            mediaPlayer = null
        }

        Log.e("MyService", "Service onDestroy")

    }
}
