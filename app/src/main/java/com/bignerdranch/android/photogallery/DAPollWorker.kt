package com.bignerdranch.android.photogallery

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

private const val TAG = "PollWorker"

class DAPollWorker (val daContext: Context, workerParams: WorkerParameters)
    : Worker(daContext, workerParams) {

    override fun doWork(): Result {
        val query = DAQueryPreferences.daGetStoredQuery(daContext)
        val daLastResultId = DAQueryPreferences.daGetLastResultId(daContext)
        val daItems: List<DAGalleryItem> = if (query.isEmpty()) {
            DAFlickrFetchr().daFetchPhotosRequest()
                .execute()
                .body()
                ?.photos
                ?.daGalleryItems
        } else {
            DAFlickrFetchr().daSearchPhotosRequest(query)
                .execute()
                .body()
                ?.photos
                ?.daGalleryItems
        } ?: emptyList()

        if (daItems.isEmpty()) {
            return Result.success()
        }

        val daResultId = daItems.first().id
        if (daResultId == daLastResultId) {
            Log.i(TAG, "Got an old result: $daResultId")
        } else {
            Log.i(TAG, "Got a new result: $daResultId")
            DAQueryPreferences.daSetLastResultId(daContext, daResultId)

            val daIntent = DAPhotoGalleryActivity.daNewIntent(daContext)
            val daPendingIntent = PendingIntent.getActivity(daContext, 0, daIntent, 0)

            val daResources = daContext.resources
            val daNotification = NotificationCompat
                .Builder(daContext, NOTIFICATION_CHANNEL_ID)
                .setTicker(daResources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(daResources.getString(R.string.new_pictures_title))
                .setContentText(daResources.getString(R.string.new_pictures_text))
                .setContentIntent(daPendingIntent)
                .setAutoCancel(true)
                .build()

            showBackgroundNotification(0, daNotification)
        }

        return Result.success()
    }

    private fun showBackgroundNotification(
        daRequestCode: Int,
        daNotification: Notification
    ) {
        val daIntent = Intent(DA_ACTION_SHOW_NOTIFICATION).apply {
            putExtra(DA_REQUEST_CODE, daRequestCode)
            putExtra(DA_NOTIFICATION, daNotification)
        }
        daContext.sendOrderedBroadcast(daIntent, DA_PERM_PRIVATE)
    }

    companion object {
        const val DA_ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION"
        const val DA_PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE"
        const val DA_REQUEST_CODE = "REQUEST_CODE"
        const val DA_NOTIFICATION = "NOTIFICATION"
    }
}