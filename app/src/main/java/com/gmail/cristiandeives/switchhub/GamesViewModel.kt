package com.gmail.cristiandeives.switchhub

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.MainThread
import android.util.Log
import com.gmail.cristiandeives.switchhub.persistence.Repository
import java.util.Collections
import java.util.concurrent.Executors

@MainThread
internal class GamesViewModel(app: Application) : AndroidViewModel(app) {
    private val sharedPrefs = app.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val repo = Repository.getInstance(app)
    private val executor = Executors.newCachedThreadPool()

    val localGames = repo.getAllLocalGames()
    val nintendoGames = repo.nintendoGames
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
            sortGames()
        }

    fun loadNintendoGames() {
        if (loadingState.value != LoadingState.LOADING) {
            loadingState.value = LoadingState.LOADING

            repo.refreshNintendoGames(onSuccess = {
                sortGames()
                loadingState.value = LoadingState.LOADED
            }, onError = {
                loadingState.value = LoadingState.FAILED
            })
        } else {
            Log.d(TAG, "games are already loading; won't try to load them again")
        }
    }

    private fun sortGames() {
        loadingState.value = LoadingState.LOADING

        executor.execute {
            val comparator: Comparator<NintendoGame> = when (sortCriteria.sortBy) {
                SortCriteria.By.FEATURED -> compareBy { it.featuredIndex }
                SortCriteria.By.TITLE -> compareBy { it.title }
                SortCriteria.By.PRICE -> compareBy { it.price }
                SortCriteria.By.RELEASE_DATE -> compareBy { it.releaseDate }
            }

            val comparatorWithDirection = when (sortCriteria.direction) {
                SortCriteria.Direction.ASCENDING -> comparator
                SortCriteria.Direction.DESCENDING -> Collections.reverseOrder(comparator)
            }

            val sortedGames = nintendoGames.value?.sortedWith(comparatorWithDirection)
            nintendoGames.postValue(sortedGames)
            loadingState.postValue(LoadingState.LOADED)
        }
    }

    companion object {
        private const val PREF_NAME = "main_preferences"
        private const val PREF_KEY_SORT = "sort"
        private val TAG = GamesViewModel::class.java.simpleName
    }
}
