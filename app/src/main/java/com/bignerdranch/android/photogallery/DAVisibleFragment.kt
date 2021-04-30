package com.bignerdranch.android.photogallery

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment

private const val TAG = "VisibleFragment"

abstract class DAVisibleFragment : Fragment() {
    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "canceling notification")
            resultCode = Activity.RESULT_CANCELED
        }
    }

    override fun onStart() { super.onStart()
        val filter = IntentFilter(DAPollWorker.DA_ACTION_SHOW_NOTIFICATION)
        requireActivity().registerReceiver(
            onShowNotification,
            filter,
            DAPollWorker.DA_PERM_PRIVATE,
            null
        )
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }
}