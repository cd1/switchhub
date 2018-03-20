package com.gmail.cristiandeives.switchhub

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.MainThread
import android.util.Log
import com.gmail.cristiandeives.switchhub.http.GameCategoriesAdapter
import com.gmail.cristiandeives.switchhub.http.GameResponse
import com.gmail.cristiandeives.switchhub.http.NintendoEshopService
import com.squareup.moshi.Moshi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@MainThread
internal class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val sharedPrefs = getApplication<Application>().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    val loadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.NOT_LOADED }
    val pageSize = 30
    var games: List<Game>? = null
    var page = 0
        private set
    var sortCriteria: SortCriteria
        get() = sharedPrefs.getInt(PREF_KEY_SORT, -1).let { sortPrefValue ->
            SortCriteria.values().find { it.ordinal == sortPrefValue } ?: SortCriteria.FEATURED
        }

        set(value) {
            if (value === sortCriteria) {
                Log.d(TAG, "user selected same sort criteria as current value [$value]; ignoring change")
                return
            }

            sharedPrefs.edit()
                .putInt(PREF_KEY_SORT, value.ordinal)
                .apply()
            loadInitialGames()
        }

    private val service = Retrofit.Builder()
        .baseUrl("https://www.nintendo.com")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(GameCategoriesAdapter()).build()))
        .build()
        .create(NintendoEshopService::class.java)

    fun loadInitialGames() = loadGamesInternal()

    fun loadMoreGames() = loadGamesInternal(page + 1)

    private fun loadGamesInternal(nextPage: Int = 0) {
        if (loadingState.value != LoadingState.LOADING) {
            val startFrom = nextPage * pageSize

            loadingState.value = LoadingState.LOADING

            Log.d(TAG, "loading game page #$nextPage; offset = $startFrom, limit = $pageSize")
            service.getGames(
                offset = startFrom,
                limit = pageSize,
                sort = sortCriteria.sortBy,
                sortDirection = sortCriteria.sortDirection
            ).enqueue(GetGamesCallback(nextPage))
        } else {
            Log.d(TAG, "some game page is already loading; don't try to load something else")
        }
    }

    inner class GetGamesCallback(private val nextPage: Int) : Callback<GameResponse> {
        private val logTag = GetGamesCallback::class.java.simpleName

        override fun onResponse(call: Call<GameResponse>, response: Response<GameResponse>) {
            if (response.isSuccessful) {
                Log.d(logTag, "received successful response")

                page = nextPage
                val newGames = response.body()?.games?.toGameData() ?: emptyList()
                Log.d(TAG, "new games: ${newGames.size} out of $pageSize")

                games = if (page == 0) {
                    newGames
                } else {
                    games?.let { it + newGames } ?: newGames
                }

                loadingState.value = if (newGames.size == pageSize) {
                    LoadingState.LOADED
                } else {
                    LoadingState.LOADED_ALL
                }
            } else {
                Log.e(logTag, "server responded with unexpected status: ${response.code()} [${response.errorBody()?.string()}]")

                loadingState.value = LoadingState.FAILED
            }
        }

        override fun onFailure(call: Call<GameResponse>, t: Throwable) {
            Log.e(logTag, "failed to execute GET request", t)

            loadingState.value = LoadingState.FAILED
        }
    }

    companion object {
        private const val PREF_NAME = "main_preferences"
        private const val PREF_KEY_SORT = "sort"
        private val TAG = MainViewModel::class.java.simpleName
    }
}
