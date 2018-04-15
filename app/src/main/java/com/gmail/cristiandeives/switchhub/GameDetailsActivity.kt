package com.gmail.cristiandeives.switchhub

import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem

@MainThread
class GameDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(savedInstanceState=$savedInstanceState)")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game_details)

        intent?.getParcelableExtra<NintendoGame>(GameDetailsFragment.EXTRA_GAME)?.let { game ->
            supportActionBar?.title = game.title

            if (supportFragmentManager.findFragmentByTag(FrontBoxArtFragment.FRAGMENT_TAG) == null) {
                val fragment = GameDetailsFragment.newInstance(game)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, FrontBoxArtFragment.FRAGMENT_TAG)
                    .commit()
            }
        } ?: Log.w(TAG, "could not find NintendoGame [key=${GameDetailsFragment.EXTRA_GAME}] inside Activity.intent")

        Log.v(TAG, "< onCreate(savedInstanceState=$savedInstanceState)")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onOptionsItemSelected(item=$item)")

        var itemConsumed = true

        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> {
                itemConsumed = super.onOptionsItemSelected(item)
            }
        }

        Log.v(TAG, "< onOptionsItemSelected(item=$item): $itemConsumed")
        return itemConsumed
    }

    companion object {
        private val TAG = GameDetailsActivity::class.java.simpleName
    }
}