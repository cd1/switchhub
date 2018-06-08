package com.gmail.cristiandeives.switchhub

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(savedInstanceState=$savedInstanceState)")
        super.onCreate(savedInstanceState)

        val settingsFrag = SettingsFragment()
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, settingsFrag)
            .commit()

        Log.v(TAG, "< onCreate(savedInstanceState=$savedInstanceState)")
    }

    companion object {
        private val TAG = SettingsActivity::class.java.simpleName
    }
}