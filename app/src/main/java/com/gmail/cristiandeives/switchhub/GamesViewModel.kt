package com.gmail.cristiandeives.switchhub

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.MainThread
import android.util.Log
import com.gmail.cristiandeives.switchhub.persistence.Repository

@MainThread
internal class GamesViewModel(app: Application) : AndroidViewModel(app) {
    private val sharedPrefs = app.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val repo = Repository.getInstance(app)

    val nintendoGames = repo.nintendoGames
    val localGames = repo.getAllLocalGames()
    val loadingState = MutableLiveData<LoadingState>().apply {
        value = if (nintendoGames.value == null) LoadingState.NOT_LOADED else LoadingState.LOADED
    }
    var sortCriteria: SortCriteria
        get() = sharedPrefs.getInt(PREF_KEY_SORT, -1).let { sortPrefValue ->
            SortCriteria.values().find { it.ordinal == sortPrefValue } ?: SortCriteria.FEATURED
        }

        set(value) {
            if (value == sortCriteria) {
                Log.d(TAG, "user selected same sort criteria as current value [$value]; ignoring change")
                return
            }

            sharedPrefs.edit()
                .putInt(PREF_KEY_SORT, value.ordinal)
                .apply()
            loadNintendoGames(true)
        }

    fun loadNintendoGames(fromStart: Boolean = false) {
        if (loadingState.value != LoadingState.LOADING) {
            loadingState.value = LoadingState.LOADING

            repo.refreshNintendoGames(sortCriteria.sortBy, sortCriteria.sortDirection, fromStart, onSuccess = { moreGamesAvailable ->
                loadingState.value = if (moreGamesAvailable) LoadingState.LOADED else LoadingState.LOADED_ALL
            }, onError = {
                loadingState.value = LoadingState.FAILED
            })
        } else {
            Log.d(TAG, "some game page is already loading; don't try to load something else")
        }
    }

    companion object {
        private const val PREF_NAME = "main_preferences"
        private const val PREF_KEY_SORT = "sort"
        private val TAG = GamesViewModel::class.java.simpleName
    }
}
