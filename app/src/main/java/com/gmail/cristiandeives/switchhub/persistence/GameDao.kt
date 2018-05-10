package com.gmail.cristiandeives.switchhub.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import android.arch.persistence.room.Update
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread

@Dao
internal abstract class GameDao {
    @WorkerThread
    @Insert
    protected abstract fun insert(game: Game)

    @UiThread
    @Query("SELECT * FROM Game")
    abstract fun selectAll(): LiveData<List<Game>>

    @UiThread
    @Query("SELECT * FROM Game WHERE id = :id")
    abstract fun select(id: String): LiveData<Game>

    @WorkerThread
    @Query("SELECT * FROM Game WHERE id = :id")
    abstract fun selectSync(id: String): Game?

    @WorkerThread
    @Query("SELECT title FROM Game WHERE id = :id")
    abstract fun selectTitle(id: String): String

    @WorkerThread
    @Update
    protected abstract fun update(game: Game)

    @WorkerThread
    @Query("UPDATE Game SET userList = :userList WHERE id = :id")
    abstract fun updateUserList(id: String, userList: Game.UserList)

    @WorkerThread
    @Query("DELETE FROM Game WHERE id NOT IN (:ids)")
    protected abstract fun deleteAllGamesExcept(ids: List<String>)

    @WorkerThread
    @Transaction
    open fun mergeGames(games: List<Game>) {
        // INSERT new games, or UPDATE games which already exist
        // we need to make sure we keep the local information (e.g. userlist)
        for (game in games) {
            val existingGame = selectSync(game.id)
            if (existingGame == null) {
                insert(game)
            } else {
                update(game.copy(userList = existingGame.userList))
            }
        }

        // DELETE games which already exist but didn't come from Nintendo's servers now
        deleteAllGamesExcept(games.map { it.id })
    }
}