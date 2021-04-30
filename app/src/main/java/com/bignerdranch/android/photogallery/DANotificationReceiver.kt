package com.bignerdranch.android.photogallery

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

private const val TAG = "NotificationReceiver"

class DANotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "received result: $resultCode")
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        val requestCode = intent.getIntExtra(DAPollWorker.DA_REQUEST_CODE, 0)
        val daNotification: Notification =
            intent.getParcelableExtra(DAPollWorker.DA_NOTIFICATION)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(requestCode, daNotification)
    }
}