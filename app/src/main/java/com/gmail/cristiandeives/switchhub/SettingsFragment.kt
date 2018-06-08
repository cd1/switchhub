package com.gmail.cristiandeives.switchhub

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat

internal class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

    companion object {
        const val PREF_KEY_DISPLAY_MY_GAMES = "display_my_games"
    }
}