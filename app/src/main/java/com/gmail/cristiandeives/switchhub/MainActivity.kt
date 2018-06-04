package com.gmail.cristiandeives.switchhub

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

@MainThread
class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(savedInstanceState=$savedInstanceState)")
        super.onCreate(savedInstanceState)

        // restore the default theme (i.e. non-splashscreen) after Activity is loaded
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setTheme(R.style.AppTheme)
        }

        setContentView(R.layout.activity_main)

        ViewModelProviders.of(this)[GamesViewModel::class.java].apply {
            // TODO: remove the "title" check when version 6 isn't relevant anymore
            // this handles the DB merge from LocalGame + data from the Internet and it assumes that
            // the data isn't valid and the app needs to refresh the game data from the Internet.
            if (games.value?.isNotEmpty() == true && games.value?.all { it.title.isNotEmpty() } == true) {
                Log.d(TAG, "existing game info found")
            } else {
                Log.d(TAG, "no game info found")
                bottom_navigation.visibility = View.GONE
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
                        loadingState.removeObservers(this@MainActivity)

                        val homeScreenExtra = intent.getStringExtra(MyListsFragment.EXTRA_HOME_SCREEN)
                        Log.d(TAG, "${MyListsFragment.EXTRA_HOME_SCREEN} = $homeScreenExtra")
                        var args: Bundle? = null
                        val fragClass = when (homeScreenExtra) {
                            MyListsFragment.EXTRA_VALUE_HOME_SCREEN_WISH_LIST, MyListsFragment.EXTRA_VALUE_HOME_SCREEN_MY_GAMES, MyListsFragment.EXTRA_VALUE_HOME_SCREEN_HIDDEN_GAMES -> {
                                args = Bundle().apply { putString(MyListsFragment.EXTRA_HOME_SCREEN, homeScreenExtra) }
                                bottom_navigation.selectedItemId = R.id.my_lists_item
                                MyListsFragment::class.java
                            }

                            else -> {
                                val lastFragTag = savedInstanceState?.getString(EXTRA_LAST_FRAGMENT)
                                Log.d(TAG, "last used fragment: $lastFragTag")
                                when (lastFragTag) {
                                    GamesFragment::class.java.simpleName -> GamesFragment::class.java
                                    MyListsFragment::class.java.simpleName -> MyListsFragment::class.java
                                    else -> GamesFragment::class.java
                                }
                            }
                        }

                        changeFragmentTo(fragClass, args)
                        bottom_navigation.visibility = View.VISIBLE
                    }

                    Log.v(TAG, "< loadingState#onChanged(state=$state)")
                })
            }
        }

        bottom_navigation.setOnNavigationItemSelectedListener(this)

        Log.v(TAG, "< onCreate(savedInstanceState=$savedInstanceState)")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onNavigationItemSelected(item=$item)")

        val fragClass = when (item.itemId) {
            R.id.games_item -> GamesFragment::class.java
            R.id.my_lists_item -> MyListsFragment::class.java
            else -> throw IllegalArgumentException(item.toString())
        }
        changeFragmentTo(fragClass)

        val itemConsumed = true
        Log.v(TAG, "< onNavigationItemSelected(item=$item): $itemConsumed")
        return itemConsumed
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.v(TAG, "> onSaveInstanceState(outState=$outState)")
        super.onSaveInstanceState(outState)

        outState.putString(EXTRA_LAST_FRAGMENT, supportFragmentManager.fragments.firstOrNull()?.tag)

        Log.v(TAG, "< onSaveInstanceState(outState=$outState)")
    }

    private fun <T> changeFragmentTo(fragClass: Class<out T>, args: Bundle? = null)
            where T : Fragment {
        Log.d(TAG, "changing fragment to $fragClass")

        val fragTag = fragClass.simpleName
        if (supportFragmentManager.findFragmentByTag(fragTag) == null) {
            val frag = fragClass.newInstance()
            args?.let { frag.arguments = it }

            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, frag, fragTag)
                    .commitAllowingStateLoss()
        } else {
            Log.d(TAG, "fragment already exists; don't create a new one")
        }
    }

    companion object {
        private const val EXTRA_LAST_FRAGMENT = "last_fragment"
        private val TAG = MainActivity::class.java.simpleName
    }
}
