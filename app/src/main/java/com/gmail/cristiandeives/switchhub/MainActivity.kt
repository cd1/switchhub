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
            // TODO: remove the "title" check when version 6 isn't relevant anymore
            // this handles the DB merge from LocalGame + data from the Internet and it assumes that
            // the data isn't valid and the app needs to refresh the game data from the Internet.
            if (games.value?.isNotEmpty() == true && games.value?.all { it.title.isNotEmpty() } == true) {
                Log.d(TAG, "existing game info found")
                changeFragmentTo(GamesFragment::class.java)
            } else {
                Log.d(TAG, "no game info found")
                changeFragmentTo(LoadingGamesFragment::class.java)

                // we need to observe this LiveData so GamesViewModel.unsortedGames can emit
                // any value (which will then emit a value in GamesViewModel.loadingState...)
                games.observe(this@MainActivity, Observer { gs ->
                    Log.v(TAG, "games#onChanged(t[]=${gs?.size})")
                })

                loadingState.observe(this@MainActivity, Observer { state ->
                    Log.v(TAG, "> loadingState#onChanged(state=$state)")

                    // TODO: remove the "title" check when version 6 isn't relevant anymore
                    if (state == LoadingState.LOADED && games.value?.all { it.title.isNotEmpty() } == true) {
                        games.removeObservers(this@MainActivity)
                        changeFragmentTo(GamesFragment::class.java)
                    }

                    Log.v(TAG, "< loadingState#onChanged(state=$state)")
                })
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
