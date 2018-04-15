package com.gmail.cristiandeives.switchhub

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log

@MainThread
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(savedInstanceState=$savedInstanceState)")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        ViewModelProviders.of(this)[GamesViewModel::class.java].apply {
            if (nintendoGames.value == null) {
                Log.d(TAG, "no game info found")
                changeFragmentTo(LoadingGamesFragment::class.java)

                loadingState.observe(this@MainActivity, Observer { state ->
                    Log.v(TAG, "> loadingState#onChanged(t=$state)")

                    if (state == LoadingState.LOADED || state == LoadingState.LOADED_ALL) {
                        loadingState.removeObservers(this@MainActivity)
                        changeFragmentTo(GamesFragment::class.java)
                    }

                    Log.v(TAG, "< loadingState#onChanged(t=$state)")
                })
            } else {
                Log.d(TAG, "previous game info found")
                changeFragmentTo(GamesFragment::class.java)
            }
        }

        Log.v(TAG, "< onCreate(savedInstanceState=$savedInstanceState)")
    }

    private fun <T> changeFragmentTo(fragClass: Class<out T>)
            where T : Fragment {
        Log.d(TAG, "changing fragment to $fragClass")

        val fragTag = fragClass.simpleName
        if (supportFragmentManager.findFragmentByTag(fragTag) == null) {
            val frag = fragClass.newInstance()

            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, frag, fragTag)
                    .commit()
        } else {
            Log.d(TAG, "fragment already exists; don't create a new one")
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
