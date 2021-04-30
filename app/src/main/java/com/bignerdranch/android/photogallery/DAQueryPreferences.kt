package com.bignerdranch.android.photogallery

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_LAST_RESULT_ID = "lastResultId"
private const val PREF_IS_POLLING = "isPolling"

object DAQueryPreferences {

    fun daGetStoredQuery(context: Context): String {
        val daPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return daPrefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    fun daSetStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit{
                putString(PREF_SEARCH_QUERY, query)
            }
    }

    fun daGetLastResultId(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_LAST_RESULT_ID, "")!!
    }

    fun daSetLastResultId(context: Context, lastResultId: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(PREF_LAST_RESULT_ID, lastResultId)
        }
    }

    fun daIsPolling(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_IS_POLLING, false)
    }
    fun daSetPolling(context: Context, isOn: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(PREF_IS_POLLING, isOn)
        }
    }
}