package com.bignerdranch.android.photogallery

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

const val NOTIFICATION_CHANNEL_ID = "flickr_poll"

class DAPhotoGalleryApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val daName = getString(R.string.notification_channel_name)
            val daImportance = NotificationManager.IMPORTANCE_DEFAULT
            val daChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, daName, daImportance)
            val daNotificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            daNotificationManager.createNotificationChannel(daChannel)
        }
    }
}