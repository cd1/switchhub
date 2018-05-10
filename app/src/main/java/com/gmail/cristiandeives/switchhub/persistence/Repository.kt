package com.gmail.cristiandeives.switchhub.persistence

import android.arch.persistence.room.Room
import android.content.Context
import android.os.Handler
import android.support.annotation.UiThread
import android.util.Log
import com.gmail.cristiandeives.switchhub.http.GameCategoriesAdapter
import com.gmail.cristiandeives.switchhub.http.NintendoEshopService
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.Executors

@UiThread
internal class Repository private constructor(ctx: Context) {
    private val roomDb = Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, DATABASE_NAME)
        .addMigrations(AppDatabase.MigrationV1V2)
        .build()
    private val eshopRetrofitService = Retrofit.Builder()
        .baseUrl("https://www.nintendo.com")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(GameCategoriesAdapter()).build()))
        .build()
        .create(NintendoEshopService::class.java)
    private val executor = Executors.newCachedThreadPool()
    private val uiHandler = Handler()

    inline fun refreshGames(crossinline onSuccess: () -> Unit = {}, crossinline onError: (e: Exception) -> Unit = {}) {
        Log.d(TAG, "loading games...")

        executor.execute {
            val newGames = mutableListOf<Game>()

            try {
                // I need this "pageLoop" label to break out of the .forEach below...
                run pageLoop@ {
                    generateSequence(0) { it + 1 }.forEach { page ->
                        Log.d(TAG, "page #$page")
                        val response = eshopRetrofitService.getGames(PAGE_SIZE, page * PAGE_SIZE).execute()
                        if (response.isSuccessful) {
                            Log.d(TAG, "received successful response")

                            val responseGames = response.body()?.games?.toGameData() ?: emptyList()
                            Log.d(TAG, "new games: ${responseGames.size}")

                            newGames += responseGames

                            if (responseGames.size < PAGE_SIZE) {
                                return@pageLoop
                            }
                        } else {
                            Log.e(TAG, "server responded with unexpected status: ${response.code()}")
                            response.errorBody().use { body ->
                                uiHandler.post {
                                    onError(RuntimeException(body?.string()))
                                }
                                return@execute
                            }
                        }
                    }
                }

                roomDb.gameDao().mergeGames(newGames)

                uiHandler.post {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(TAG, "failed to execute GET request", e)
                uiHandler.post {
                    onError(e)
                }
            }
        }
    }

    fun getAllGames() = roomDb.gameDao().selectAll()

    fun getGame(id: String) = roomDb.gameDao().select(id)

    inline fun getGameTitle(id: String, crossinline onSuccess: (String) -> Unit = {}, crossinline onError: (e: Exception) -> Unit = {}) {
        executor.execute {
            try {
                val title = roomDb.gameDao().selectTitle(id)
                uiHandler.post {
                    onSuccess(title)
                }
            } catch (e: Exception) {
                Log.e(TAG, "failed to SELECT the database", e)
                uiHandler.post {
                    onError(e)
                }
            }
        }
    }

    inline fun updateGameUserList(id: String, userList: Game.UserList, crossinline onSuccess: () -> Unit = {}, crossinline onError: (e: Exception) -> Unit = {}) {
        executor.execute {
            try {
                roomDb.gameDao().updateUserList(id, userList)
                uiHandler.post {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(TAG, "failed to UPDATE the database", e)
                uiHandler.post {
                    onError(e)
                }
            }
        }
    }

    companion object {
        private const val DATABASE_NAME = "switchhub.db"
        private const val PAGE_SIZE = 200

        private val TAG = Repository::class.java.simpleName
        private var instance: Repository?  = null

        fun getInstance(ctx: Context): Repository {
            val nonNullInstance = instance ?: Repository(ctx)
            if (instance !== nonNullInstance) {
                instance = nonNullInstance
            }

            return nonNullInstance
        }
    }
}