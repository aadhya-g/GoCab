package com.example.gocab

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREFS_NAME = "GoCabPrefs"
    private const val KEY_DETAILS_FILLED = "details_filled"
    private const val KEY_USER_ROLE = "user_role"   // 👈 new key
    private const val PREF_NAME = "GoCabPrefs"

    fun setBoolean(context: Context, key: String, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(key, defaultValue)
    }


    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Existing Methods ---
    fun isDetailsFilled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DETAILS_FILLED, false)
    }

    fun setDetailsFilled(context: Context, filled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DETAILS_FILLED, filled).apply()
    }

    // --- 🆕 Added Role Handling Methods ---
    fun setUserRole(context: Context, role: String) {
        getPrefs(context).edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun getUserRole(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ROLE, null)
    }

    // --- Clear All ---
    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}




/*
package com.example.gocab

import android.content.Context

object Prefs {

    // ✅ SINGLE SharedPreferences name
    private const val PREF_NAME = "GoCabPrefs"

    // ✅ Keys
    private const val KEY_DETAILS_FILLED = "details_filled"
    private const val KEY_USER_ROLE = "user_role"

    // --- Internal helper ---
    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // --- Details Filled ---
    fun setDetailsFilled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_DETAILS_FILLED, value).apply()
    }

    fun isDetailsFilled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_DETAILS_FILLED, false)
    }

    // --- User Role ---
    fun setUserRole(context: Context, role: String) {
        prefs(context).edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun getUserRole(context: Context): String? {
        return prefs(context).getString(KEY_USER_ROLE, null)
    }

    // --- Clear All ---
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
*/
